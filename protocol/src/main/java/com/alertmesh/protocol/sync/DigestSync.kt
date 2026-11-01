package com.alertmesh.protocol.sync

import com.alertmesh.protocol.crypto.CryptoProvider
import com.alertmesh.protocol.model.MessageEnvelope

interface SyncStore {
    fun getLocalDigest(): List<String>
    fun getMessages(msgIds: List<String>): List<MessageEnvelope>
    fun saveMessages(messages: List<MessageEnvelope>)
}

class DigestSync(
    private val syncStore: SyncStore,
    private val cryptoProvider: CryptoProvider? = null,
    private val publicKeyLookup: ((origin: String) -> ByteArray?)? = null
) {
    fun handleSyncReq(peerDigest: List<String>): List<MessageEnvelope> {
        val localDigest = syncStore.getLocalDigest()
        val missingInPeer = localDigest - peerDigest.toSet()
        return syncStore.getMessages(missingInPeer)
    }

    fun handleSyncResp(messages: List<MessageEnvelope>): List<MessageEnvelope> {
        // Verify each message's signature before saving so a compromised peer
        // cannot inject forged messages via sync responses. When no verifier is
        // configured, accept all messages to preserve existing behavior and keep
        // DigestSync(syncStore) call sites (e.g. AppContainer) compiling unchanged.
        val provider = cryptoProvider
        val lookup = publicKeyLookup
        if (provider != null && lookup != null) {
            val validMessages = messages.filter { envelope ->
                val pubKey = lookup(envelope.origin) ?: return@filter false // unknown origin -> reject
                provider.verify(pubKey, envelope.signable(), envelope.signature)
            }
            val newMessages = validMessages.filter { it.msgId !in syncStore.getLocalDigest() }
            syncStore.saveMessages(newMessages)
            return newMessages
        } else {
            val newMessages = messages.filter { it.msgId !in syncStore.getLocalDigest() }
            syncStore.saveMessages(newMessages)
            return newMessages
        }
    }

    fun createSyncReq(): List<String> {
        return syncStore.getLocalDigest()
    }
}
