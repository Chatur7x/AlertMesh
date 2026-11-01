package com.alertmesh.protocol

import com.alertmesh.protocol.cache.DedupCache
import com.alertmesh.protocol.cache.ReplayGuard
import com.alertmesh.protocol.crypto.TinkCryptoProvider
import com.alertmesh.protocol.model.MessageEnvelope
import com.alertmesh.protocol.model.MessageType
import com.alertmesh.protocol.routing.RateLimiter
import com.alertmesh.protocol.routing.SuppressionTracker
import com.alertmesh.protocol.sync.DigestSync
import com.alertmesh.protocol.sync.SyncStore
import com.alertmesh.protocol.transport.LoopbackTransport
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.concurrent.CopyOnWriteArrayList

private class MemorySyncStore : SyncStore {
    val messages = CopyOnWriteArrayList<MessageEnvelope>()
    override fun getLocalDigest() = messages.map { it.msgId }
    override fun getMessages(msgIds: List<String>) = messages.filter { it.msgId in msgIds }
    override fun saveMessages(messages: List<MessageEnvelope>) { this.messages.addAll(messages) }
}

@OptIn(ExperimentalCoroutinesApi::class)
class ReconnectSyncTest {
    @Test
    fun offlineMessagesArriveAfterPeerReconnect() = runTest {
        val a = LoopbackTransport("sync-a").apply { start() }
        val b = LoopbackTransport("sync-b").apply { start() }
        val cryptoA = TinkCryptoProvider().apply { generateIdentity() }
        val cryptoB = TinkCryptoProvider().apply { generateIdentity() }
        val storeA = MemorySyncStore()
        val storeB = MemorySyncStore()
        val received = CopyOnWriteArrayList<MessageEnvelope>()

        fun engine(transport: LoopbackTransport, crypto: TinkCryptoProvider,
                   store: MemorySyncStore, listener: (MessageEnvelope) -> Unit) = ProtocolEngine(
            transport, crypto, DedupCache(), ReplayGuard(), RateLimiter(maxTokens = 1000), SuppressionTracker(),
            DigestSync(store, crypto) { origin ->
                when (origin) {
                    cryptoA.getDeviceId() -> cryptoA.getPublicKey()
                    cryptoB.getDeviceId() -> cryptoB.getPublicKey()
                    else -> null
                }
            }, listener, engineScope = this
        ).apply {
            registerPublicKey(cryptoA.getDeviceId(), cryptoA.getPublicKey())
            registerPublicKey(cryptoB.getDeviceId(), cryptoB.getPublicKey())
            start()
        }

        val engineA = engine(a, cryptoA, storeA) {}
        val engineB = engine(b, cryptoB, storeB) { received.add(it) }
        listOf(MessageType.TEXT, MessageType.SOS, MessageType.VOICE).forEach { type ->
            storeA.messages.add(engineA.sendMessage(type, byteArrayOf(type.ordinal.toByte())))
        }
        repeat(100) {
            storeA.messages.add(engineA.sendMessage(MessageType.TEXT, ByteArray(16_000) { it.toByte() }))
        }
        assertEquals(0, received.size)

        a.connectTo(b)
        delay(30_000)
        assertEquals(103, received.size)
        assertEquals(listOf(MessageType.TEXT, MessageType.SOS, MessageType.VOICE), received.take(3).map { it.type })
        assertEquals(103, storeB.messages.size)

        engineA.stop(); engineB.stop(); a.stop(); b.stop()
    }
}
