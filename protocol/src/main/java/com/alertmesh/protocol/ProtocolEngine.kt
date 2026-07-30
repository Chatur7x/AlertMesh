package com.alertmesh.protocol

import com.alertmesh.protocol.cache.DedupCache
import com.alertmesh.protocol.cache.ReplayGuard
import com.alertmesh.protocol.crypto.CryptoProvider
import com.alertmesh.protocol.model.MessageEnvelope
import com.alertmesh.protocol.model.MessageType
import com.alertmesh.protocol.model.SosPayload
import com.alertmesh.protocol.routing.RateLimiter
import com.alertmesh.protocol.routing.SuppressionTracker
import com.alertmesh.protocol.sync.DigestSync
import com.alertmesh.protocol.transport.Transport
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.protobuf.ProtoBuf
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random

class ProtocolEngine(
    private val transport: Transport,
    private val cryptoProvider: CryptoProvider,
    private val dedupCache: DedupCache,
    private val replayGuard: ReplayGuard,
    private val rateLimiter: RateLimiter,
    private val suppressionTracker: SuppressionTracker,
    private val digestSync: DigestSync?,
    private val messageListener: (MessageEnvelope) -> Unit,
    private val engineScope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
) {
    private val knownPublicKeys = ConcurrentHashMap<String, ByteArray>()
    private var counter: Long = 0
    private var job: Job? = null
    private var peers = emptySet<String>()

    fun start() {
        job = engineScope.launch {
            launch {
                transport.discoveredPeers().collect { newPeers ->
                    peers = newPeers
                }
            }
            
            transport.incomingFrames().collect { frame ->
                processIncoming(frame.fromPeerId, frame.data)
            }
        }
    }

    fun stop() {
        job?.cancel()
    }

    private suspend fun processIncoming(senderPeerId: String, data: ByteArray) {
        try {
            val envelope = ProtoBuf.decodeFromByteArray(MessageEnvelope.serializer(), data)
            
            if (dedupCache.isDuplicate(envelope.msgId)) return
            
            if (replayGuard.isReplay(envelope.origin, envelope.counter)) return
            
            if (!rateLimiter.tryConsume(senderPeerId)) return

            // Verification
            val originPubKey = knownPublicKeys[envelope.origin] 
                ?: if (envelope.origin == cryptoProvider.getDeviceId()) cryptoProvider.getPublicKey() else return // Missing public key, drop

            val signable = envelope.signable()
            if (!cryptoProvider.verify(originPubKey, signable, envelope.signature)) return

            // Record
            replayGuard.record(envelope.origin, envelope.counter)
            suppressionTracker.recordRebroadcast(envelope.msgId, senderPeerId)
            
            // Deliver
            messageListener(envelope)

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
        } catch (e: Exception) {
            // Drop malformed
        }
    }

    fun registerPublicKey(deviceId: String, publicKey: ByteArray) {
        knownPublicKeys[deviceId] = publicKey
    }

    suspend fun sendMessage(type: MessageType, payload: ByteArray, channel: Int = 0, ttl: Int = 8) {
        val msgCounter = ++counter
        val unsigned = MessageEnvelope(
            msgId = MessageEnvelope.newId(),
            origin = cryptoProvider.getDeviceId(),
            counter = msgCounter,
            type = type,
            channel = channel,
            ttl = ttl,
            payload = payload,
            signature = ByteArray(0)
        )
        val signature = cryptoProvider.sign(unsigned.signable())
        val signed = unsigned.copy(signature = signature)
        
        dedupCache.add(signed.msgId)
        replayGuard.record(signed.origin, signed.counter)
        
        val encoded = ProtoBuf.encodeToByteArray(MessageEnvelope.serializer(), signed)
        peers.forEach { target ->
            transport.send(target, encoded)
        }
    }

    suspend fun sendSos(sosPayload: SosPayload) {
        val payload = ProtoBuf.encodeToByteArray(SosPayload.serializer(), sosPayload)
        sendMessage(MessageType.SOS, payload, channel = 0, ttl = 12)
    }
}
