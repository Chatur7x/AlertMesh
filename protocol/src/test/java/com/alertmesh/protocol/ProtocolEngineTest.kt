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
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProtocolEngineTest {

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
}
