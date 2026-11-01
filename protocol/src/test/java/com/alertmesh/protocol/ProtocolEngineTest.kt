package com.alertmesh.protocol

import com.alertmesh.protocol.cache.DedupCache
import com.alertmesh.protocol.cache.ReplayGuard
import com.alertmesh.protocol.crypto.TinkCryptoProvider
import com.alertmesh.protocol.model.MessageEnvelope
import com.alertmesh.protocol.model.MessageType
import com.alertmesh.protocol.routing.RateLimiter
import com.alertmesh.protocol.routing.SuppressionTracker
import com.alertmesh.protocol.transport.LoopbackTransport
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProtocolEngineTest {

    @Test
    fun oversizedVoiceIsRejectedBeforeSend() = runTest {
        val engine = ProtocolEngine(
            transport = LoopbackTransport("sender"),
            cryptoProvider = TinkCryptoProvider().apply { generateIdentity() },
            dedupCache = DedupCache(), replayGuard = ReplayGuard(), rateLimiter = RateLimiter(),
            suppressionTracker = SuppressionTracker(), digestSync = null,
            messageListener = {}, engineScope = this
        )
        try {
            engine.sendVoiceMessage(ByteArray(65_536))
            org.junit.Assert.fail("Oversized voice must be rejected locally")
        } catch (expected: IllegalArgumentException) {
            org.junit.Assert.assertTrue(expected.message!!.contains("64 KB"))
        }
    }

    @Test
    fun targetedTextWithoutRecipientKeyFailsClosed() = runTest {
        val crypto = TinkCryptoProvider().apply { generateIdentity() }
        val engine = ProtocolEngine(
            transport = LoopbackTransport("sender"),
            cryptoProvider = crypto,
            dedupCache = DedupCache(),
            replayGuard = ReplayGuard(),
            rateLimiter = RateLimiter(),
            suppressionTracker = SuppressionTracker(),
            digestSync = null,
            messageListener = {},
            engineScope = this
        )
        try {
            engine.sendMessage(MessageType.TEXT, "private".toByteArray(), targetPeer = "unknown")
            org.junit.Assert.fail("Targeted text must not fall back to plaintext")
        } catch (expected: IllegalStateException) {
            org.junit.Assert.assertTrue(expected.message!!.contains("encryption key unavailable"))
        }
    }

    @Test
    fun testSendAndReceive() = runTest {
        val transportA = LoopbackTransport("A")
        val transportB = LoopbackTransport("B")
        transportA.start()
        transportB.start()
        transportA.connectTo(transportB)

        val cryptoA = TinkCryptoProvider().apply { generateIdentity() }
        val cryptoB = TinkCryptoProvider().apply { generateIdentity() }

        var receivedMessage: MessageEnvelope? = null

        val engineB = ProtocolEngine(
            transport = transportB,
            cryptoProvider = cryptoB,
            dedupCache = DedupCache(),
            replayGuard = ReplayGuard(),
            rateLimiter = RateLimiter(),
            suppressionTracker = SuppressionTracker(),
            digestSync = null,
            messageListener = { msg ->
                receivedMessage = msg
            },
            engineScope = this
        )
        
        engineB.registerPublicKey(cryptoA.getDeviceId(), cryptoA.getPublicKey())
        engineB.registerHybridKey(cryptoA.getDeviceId(), cryptoA.getHybridPublicKey())
        engineB.start()

        val engineA = ProtocolEngine(
            transport = transportA,
            cryptoProvider = cryptoA,
            dedupCache = DedupCache(),
            replayGuard = ReplayGuard(),
            rateLimiter = RateLimiter(),
            suppressionTracker = SuppressionTracker(),
            digestSync = null,
            messageListener = { },
            engineScope = this
        )
        engineA.start()

        delay(100) // Wait for peer discovery

        engineA.sendMessage(MessageType.TEXT, "Hello B".toByteArray())

        delay(500)

        assertEquals("Hello B", String(receivedMessage!!.payload))
        
        engineA.stop()
        engineB.stop()
        transportA.stop()
        transportB.stop()
    }

    @Test
    fun testTtlDecrementAndDrop() = runTest {
        // Will test TTL decrement if B relays to C in MultiNodeTest
    }

    @Test
    fun testSendAndReceiveAck() = runTest {
        val transportA = LoopbackTransport("A")
        val transportB = LoopbackTransport("B")
        transportA.start()
        transportB.start()
        transportA.connectTo(transportB)

        val cryptoA = TinkCryptoProvider().apply { generateIdentity() }
        val cryptoB = TinkCryptoProvider().apply { generateIdentity() }

        var receivedAck: MessageEnvelope? = null

        val engineB = ProtocolEngine(
            transport = transportB,
            cryptoProvider = cryptoB,
            dedupCache = DedupCache(),
            replayGuard = ReplayGuard(),
            rateLimiter = RateLimiter(),
            suppressionTracker = SuppressionTracker(),
            digestSync = null,
            messageListener = { receivedAck = it },
            engineScope = this
        )
        engineB.registerPublicKey(cryptoA.getDeviceId(), cryptoA.getPublicKey())
        engineB.registerHybridKey(cryptoA.getDeviceId(), cryptoA.getHybridPublicKey())
        engineB.start()

        val engineA = ProtocolEngine(
            transport = transportA,
            cryptoProvider = cryptoA,
            dedupCache = DedupCache(),
            replayGuard = ReplayGuard(),
            rateLimiter = RateLimiter(),
            suppressionTracker = SuppressionTracker(),
            digestSync = null,
            messageListener = { },
            engineScope = this
        )
        engineA.start()

        delay(100) // Wait for peer discovery

        val originalMsgId = "ack-original-msg-id"
        engineA.sendAck(originalMsgId, transportB.localPeerId)

        delay(500)

        assertNotNull(receivedAck)
        assertEquals(MessageType.ACK, receivedAck!!.type)
        assertEquals(originalMsgId, String(receivedAck!!.payload))

        engineA.stop()
        engineB.stop()
        transportA.stop()
        transportB.stop()
    }
}
