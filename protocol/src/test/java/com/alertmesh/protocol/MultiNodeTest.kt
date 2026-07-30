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
import java.util.concurrent.CopyOnWriteArrayList

@OptIn(ExperimentalCoroutinesApi::class)
class MultiNodeTest {
    
    @Test
    fun testLineTopologyRelay() = runTest {
        val tA = LoopbackTransport("A").apply { start() }
        val tB = LoopbackTransport("B").apply { start() }
        val tC = LoopbackTransport("C").apply { start() }

        tA.connectTo(tB)
        tB.connectTo(tC)

        val cryptoA = TinkCryptoProvider().apply { generateIdentity() }
        val cryptoB = TinkCryptoProvider().apply { generateIdentity() }
        val cryptoC = TinkCryptoProvider().apply { generateIdentity() }

        val receivedA = CopyOnWriteArrayList<MessageEnvelope>()
        val receivedB = CopyOnWriteArrayList<MessageEnvelope>()
        val receivedC = CopyOnWriteArrayList<MessageEnvelope>()

        fun createEngine(t: LoopbackTransport, crypto: TinkCryptoProvider, rcv: CopyOnWriteArrayList<MessageEnvelope>) = 
            ProtocolEngine(
                transport = t,
                cryptoProvider = crypto,
                dedupCache = DedupCache(),
                replayGuard = ReplayGuard(),
                rateLimiter = RateLimiter(),
                suppressionTracker = SuppressionTracker(),
                digestSync = null,
                messageListener = { rcv.add(it) },
                engineScope = this
            ).apply {
                registerPublicKey(cryptoA.getDeviceId(), cryptoA.getPublicKey())
                registerPublicKey(cryptoB.getDeviceId(), cryptoB.getPublicKey())
                registerPublicKey(cryptoC.getDeviceId(), cryptoC.getPublicKey())
                start()
            }

        val eA = createEngine(tA, cryptoA, receivedA)
        val eB = createEngine(tB, cryptoB, receivedB)
        val eC = createEngine(tC, cryptoC, receivedC)

        delay(100) // Wait for peer discovery

        eA.sendMessage(MessageType.TEXT, "Hello C from A".toByteArray(), ttl = 3)

        delay(1000)

        // A doesn't get its own msg
        assertEquals(0, receivedA.size)
        // B receives and relays
        assertEquals(1, receivedB.size)
        // C receives
        assertEquals(1, receivedC.size)

        val msgC = receivedC[0]
        assertEquals(2, msgC.ttl) // Because B decremented it before sending

        eA.stop()
        eB.stop()
        eC.stop()
        tA.stop()
        tB.stop()
        tC.stop()
    }
}
