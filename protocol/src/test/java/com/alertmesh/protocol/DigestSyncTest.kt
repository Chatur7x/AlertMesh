package com.alertmesh.protocol

import com.alertmesh.protocol.crypto.TinkCryptoProvider
import com.alertmesh.protocol.model.MessageEnvelope
import com.alertmesh.protocol.model.MessageType
import com.alertmesh.protocol.sync.DigestSync
import com.alertmesh.protocol.sync.SyncStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

private class FakeSyncStore : SyncStore {
    val saved = mutableListOf<MessageEnvelope>()
    override fun getLocalDigest(): List<String> = saved.map { it.msgId }
    override fun getMessages(msgIds: List<String>): List<MessageEnvelope> = emptyList()
    override fun saveMessages(messages: List<MessageEnvelope>) { saved.addAll(messages) }
}

class DigestSyncTest {

    private fun signedEnvelope(crypto: TinkCryptoProvider, origin: String = "alice"): MessageEnvelope {
        val envelope = MessageEnvelope(
            msgId = "msg-1", origin = origin, counter = 1,
            type = MessageType.TEXT, channel = 0, ttl = 8,
            payload = "Hello".toByteArray(), signature = ByteArray(0)
        )
        return envelope.copy(signature = crypto.sign(envelope.signable()))
    }

    @Test
    fun testValidSignatureSaved() {
        val crypto = TinkCryptoProvider().apply { generateIdentity() }
        val store = FakeSyncStore()
        val sync = DigestSync(store, crypto) { origin -> crypto.getPublicKey() }

        sync.handleSyncResp(listOf(signedEnvelope(crypto)))
        assertEquals(1, store.saved.size)
    }

    @Test
    fun testInvalidSignatureDropped() {
        val crypto = TinkCryptoProvider().apply { generateIdentity() }
        val enemy = TinkCryptoProvider().apply { generateIdentity() }
        val store = FakeSyncStore()
        val sync = DigestSync(store, crypto) { origin -> crypto.getPublicKey() }

        val envelope = signedEnvelope(crypto)
        // Re-sign with the enemy's key, but keep origin = alice. Verification
        // with alice's public key must fail.
        val forged = envelope.copy(signature = enemy.sign(envelope.signable()))

        sync.handleSyncResp(listOf(forged))
        assertEquals(0, store.saved.size)
    }

    @Test
    fun testUnknownOriginDropped() {
        val crypto = TinkCryptoProvider().apply { generateIdentity() }
        val store = FakeSyncStore()
        // No public keys known for any origin.
        val sync = DigestSync(store, crypto) { origin -> null }

        sync.handleSyncResp(listOf(signedEnvelope(crypto)))
        assertEquals(0, store.saved.size)
    }

    @Test
    fun testNoVerifierConfiguredAcceptsAll() {
        val store = FakeSyncStore()
        // No cryptoProvider / lookup: preserves legacy accept-all behavior.
        val sync = DigestSync(store)

        sync.handleSyncResp(listOf(signedEnvelope(TinkCryptoProvider().apply { generateIdentity() })))
        assertEquals(1, store.saved.size)
    }

    @Test
    fun testVerifiedSyncOnlyReturnsNewMessages() {
        val crypto = TinkCryptoProvider().apply { generateIdentity() }
        val store = FakeSyncStore()
        val sync = DigestSync(store, crypto) { crypto.getPublicKey() }
        val message = signedEnvelope(crypto)

        assertEquals(listOf(message), sync.handleSyncResp(listOf(message)))
        assertTrue(sync.handleSyncResp(listOf(message)).isEmpty())
        assertEquals(1, store.saved.size)
    }
}
