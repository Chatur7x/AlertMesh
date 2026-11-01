package com.alertmesh.protocol

import com.alertmesh.protocol.cache.DedupCache
import com.alertmesh.protocol.cache.ReplayGuard
import com.alertmesh.protocol.crypto.CryptoProvider
import com.alertmesh.protocol.model.MessageEnvelope
import com.alertmesh.protocol.model.MessageType
import com.alertmesh.protocol.model.SosPayload
import com.alertmesh.protocol.model.PresencePayload
import com.alertmesh.protocol.model.NodeIdentity
import com.alertmesh.protocol.routing.RateLimiter
import java.nio.charset.Charset
import com.alertmesh.protocol.routing.SuppressionTracker
import com.alertmesh.protocol.sync.DigestSync
import com.alertmesh.protocol.transport.Transport
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.protobuf.ProtoBuf
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class ProtocolEngine(
    private val transport: Transport,
    private val cryptoProvider: CryptoProvider,
    private val dedupCache: DedupCache,
    private val replayGuard: ReplayGuard,
    private val rateLimiter: RateLimiter,
    private val suppressionTracker: SuppressionTracker,
    private val digestSync: DigestSync?,
    private val messageListener: (MessageEnvelope) -> Unit,
    private val peerPresenceListener: ((PresencePayload, ByteArray) -> Unit)? = null,
    private val counterStore: CounterStore? = null,
    private val engineScope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
) {
    private val knownPublicKeys = ConcurrentHashMap<String, ByteArray>()
    private val knownHybridKeys = ConcurrentHashMap<String, ByteArray>()
    private var counter: Long = maxOf(counterStore?.read() ?: 0L, System.currentTimeMillis() * 1_000L)
    private val counterMutex = Mutex()
    private var job: Job? = null
    private var peers = emptySet<String>()
    private var presenceJob: Job? = null
    private var presenceProvider: (() -> PresencePayload)? = null
    var presenceIntervalMs: Long = 10_000
    private val MAX_FRAME_SIZE = 64 * 1024 // 64 KB: reject oversized frames before decoding

    fun start() {
        if (job != null) return
        job = engineScope.launch {
            launch {
                transport.discoveredPeers().collect { newPeers ->
                    val addedPeers = newPeers - peers
                    peers = newPeers
                    
                    addedPeers.forEach { newPeer ->
                        presenceProvider?.let { provider ->
                            try {
                                val payload = provider()
                                val encoded = ProtoBuf.encodeToByteArray(PresencePayload.serializer(), payload)
                                sendMessage(MessageType.PRESENCE, encoded, channel = 0, ttl = 2, targetPeer = newPeer)
                            } catch (_: Exception) {}
                        }
                        digestSync?.let { sync ->
                            try {
                                val digest = sync.createSyncReq()
                                val payload = ProtoBuf.encodeToByteArray(ListSerializer(String.serializer()), digest)
                                sendMessage(MessageType.SYNC_REQ, payload, ttl = 1, targetPeer = newPeer)
                            } catch (_: Exception) {}
                        }
                    }
                }
            }
            
            transport.incomingFrames().collect { frame ->
                processIncoming(frame.fromPeerId, frame.data)
            }
        }
    }

    fun stop() {
        job?.cancel()
        job = null
        peers = emptySet()
        stopPresenceBroadcast()
    }

    fun startPresenceBroadcast(provider: () -> PresencePayload) {
        presenceProvider = provider
        presenceJob?.cancel()
        presenceJob = engineScope.launch {
            while (true) {
                try {
                    val payload = provider()
                    val encoded = ProtoBuf.encodeToByteArray(PresencePayload.serializer(), payload)
                    sendMessage(MessageType.PRESENCE, encoded, channel = 0, ttl = 4)
                } catch (_: Exception) {}
                delay(presenceIntervalMs)
            }
        }
    }

    fun stopPresenceBroadcast() {
        presenceJob?.cancel()
        presenceJob = null
    }

    private suspend fun processIncoming(senderPeerId: String, data: ByteArray) {
        try {
            if (data.size > MAX_FRAME_SIZE) return // Reject oversized frames

            val envelope = ProtoBuf.decodeFromByteArray(MessageEnvelope.serializer(), data)
            
            if (dedupCache.isDuplicate(envelope.msgId)) return
            
            if (replayGuard.isReplay(envelope.origin, envelope.counter)) return
            
            if (!rateLimiter.tryConsume(senderPeerId)) return

            // Verification & Key Bootstrap
            var originPubKey = knownPublicKeys[envelope.origin] 
                ?: if (envelope.origin == cryptoProvider.getDeviceId()) cryptoProvider.getPublicKey() else null

            if (originPubKey == null && envelope.type == MessageType.PRESENCE) {
                try {
                    val presence = ProtoBuf.decodeFromByteArray(PresencePayload.serializer(), envelope.payload)
                    if (presence.deviceId == envelope.origin &&
                        NodeIdentity.deviceIdForPublicKey(presence.publicKey) == envelope.origin) {
                        originPubKey = presence.publicKey
                    }
                } catch (_: Exception) {}
            }

            if (originPubKey == null) return // Missing public key, drop

            val signable = envelope.signable()
            if (!cryptoProvider.verify(originPubKey, signable, envelope.signature)) return

            // Store discovered key
            knownPublicKeys[envelope.origin] = originPubKey

            // Store hybrid encryption key from PRESENCE
            if (envelope.type == MessageType.PRESENCE) {
                try {
                    val presence = ProtoBuf.decodeFromByteArray(PresencePayload.serializer(), envelope.payload)
                    if (presence.hybridPublicKey.isNotEmpty()) {
                        knownHybridKeys[envelope.origin] = presence.hybridPublicKey
                    }
                } catch (_: Exception) {}
            }

            // Record
            replayGuard.record(envelope.origin, envelope.counter)
            suppressionTracker.recordRebroadcast(envelope.msgId, senderPeerId)

            // Try decryption after signature verification (cheap checks first).
            // Not encrypted for us -> catch block keeps payload as-is.
            val finalPayload = try {
                cryptoProvider.decryptFromSender(envelope.payload, originPubKey)
            } catch (_: Exception) {
                envelope.payload
            }
            val deliveredEnvelope = envelope.copy(payload = finalPayload)

            // Deliver or handle Sync / Presence
            when (envelope.type) {
                MessageType.PRESENCE -> {
                    try {
                        val presence = ProtoBuf.decodeFromByteArray(PresencePayload.serializer(), deliveredEnvelope.payload)
                        peerPresenceListener?.invoke(presence, originPubKey)
                    } catch (_: Exception) {}
                    messageListener(deliveredEnvelope)
                }
                MessageType.SYNC_REQ -> {
                    digestSync?.let { sync ->
                        val peerDigest = ProtoBuf.decodeFromByteArray(ListSerializer(String.serializer()), deliveredEnvelope.payload)
                        val missingMessages = sync.handleSyncReq(peerDigest)
                        val batch = mutableListOf<MessageEnvelope>()
                        suspend fun sendBatch() {
                            if (batch.isEmpty()) return
                            val payload = ProtoBuf.encodeToByteArray(ListSerializer(MessageEnvelope.serializer()), batch)
                            sendMessage(MessageType.SYNC_RESP, payload, ttl = 1, targetPeer = senderPeerId)
                            batch.clear()
                            delay(250) // Stay below the receiver's five-frame-per-second refill rate.
                        }
                        for (missing in missingMessages) {
                            val candidate = batch + missing
                            val size = ProtoBuf.encodeToByteArray(ListSerializer(MessageEnvelope.serializer()), candidate).size
                            if (size > MAX_FRAME_SIZE - 2048) sendBatch()
                            val singleSize = ProtoBuf.encodeToByteArray(ListSerializer(MessageEnvelope.serializer()), listOf(missing)).size
                            if (singleSize <= MAX_FRAME_SIZE - 2048) batch.add(missing)
                        }
                        sendBatch()
                    }
                }
                MessageType.SYNC_RESP -> {
                    digestSync?.let { sync ->
                        val messages = ProtoBuf.decodeFromByteArray(ListSerializer(MessageEnvelope.serializer()), deliveredEnvelope.payload)
                        sync.handleSyncResp(messages).forEach(messageListener)
                    }
                }
                MessageType.ACK -> {
                    // ACK received — payload holds the original message's msgId.
                    // Pass through to the app layer so it can update delivery status.
                    messageListener(deliveredEnvelope)
                }
                else -> {
                    messageListener(deliveredEnvelope)
                }
            }

            // Relay
            val newTtl = envelope.ttl - 1
            if (newTtl > 0 && !suppressionTracker.shouldSuppress(envelope.msgId)) {
                delay(Random.nextLong(50, 251))
                val forwardedEnvelope = envelope.copy(ttl = newTtl)
                val encoded = ProtoBuf.encodeToByteArray(MessageEnvelope.serializer(), forwardedEnvelope)
                
                peers.filter { it != senderPeerId }.forEach { target ->
                    transport.send(target, encoded)
                }
            }
        } catch (_: Exception) {
            // Drop malformed
        }
    }

    fun registerPublicKey(deviceId: String, publicKey: ByteArray) {
        require(NodeIdentity.deviceIdForPublicKey(publicKey) == deviceId) { "Public key does not match device ID" }
        knownPublicKeys[deviceId] = publicKey
    }

    fun knownPublicKey(deviceId: String): ByteArray? =
        if (deviceId == cryptoProvider.getDeviceId()) cryptoProvider.getPublicKey() else knownPublicKeys[deviceId]

    fun registerHybridKey(deviceId: String, hybridPublicKey: ByteArray) {
        knownHybridKeys[deviceId] = hybridPublicKey
    }

    suspend fun sendMessage(type: MessageType, payload: ByteArray, channel: Int = 0, ttl: Int = 8, targetPeer: String? = null): MessageEnvelope {
        val msgCounter = counterMutex.withLock {
            val next = ++counter
            check(counterStore?.write(next) != false) { "Could not persist message counter" }
            next
        }
        val finalPayload = if (targetPeer != null && type == MessageType.TEXT) {
            val recipientKey = knownHybridKeys[targetPeer]
                ?: throw IllegalStateException("Recipient encryption key unavailable: $targetPeer")
            cryptoProvider.encryptForRecipient(payload, recipientKey)
        } else payload
        val unsigned = MessageEnvelope(
            msgId = MessageEnvelope.newId(),
            origin = cryptoProvider.getDeviceId(),
            counter = msgCounter,
            type = type,
            channel = channel,
            ttl = ttl,
            payload = finalPayload,
            signature = ByteArray(0)
        )
        val signature = cryptoProvider.sign(unsigned.signable())
        val signed = unsigned.copy(signature = signature)
        
        val encoded = ProtoBuf.encodeToByteArray(MessageEnvelope.serializer(), signed)
        require(encoded.size <= MAX_FRAME_SIZE) { "Message exceeds 64 KB mesh frame limit" }

        dedupCache.add(signed.msgId)
        replayGuard.record(signed.origin, signed.counter)
        
        if (targetPeer != null) {
            transport.send(targetPeer, encoded)
        } else {
            peers.forEach { target ->
                transport.send(target, encoded)
            }
        }
        
        return signed
    }

    suspend fun sendSos(sosPayload: SosPayload) {
        val payload = ProtoBuf.encodeToByteArray(SosPayload.serializer(), sosPayload)
        sendMessage(MessageType.SOS, payload, channel = 0, ttl = 12)
    }

    suspend fun sendVoiceMessage(audioData: ByteArray, channel: Int = 1) {
        sendMessage(MessageType.VOICE, audioData, channel = channel)
    }

    /**
     * Send an acknowledgment for a delivered message. The payload carries the
     * original message's msgId so the sender can mark it as acknowledged.
     */
    suspend fun sendAck(originalMsgId: String, targetPeer: String) {
        val payload = originalMsgId.toByteArray(Charset.defaultCharset())
        sendMessage(MessageType.ACK, payload, ttl = 1, targetPeer = targetPeer)
    }
}
