package com.alertmesh.protocol.sync

import com.alertmesh.protocol.model.MessageEnvelope

interface SyncStore {
    fun getLocalDigest(): List<String>
    fun getMessages(msgIds: List<String>): List<MessageEnvelope>
    fun saveMessages(messages: List<MessageEnvelope>)
}

class DigestSync(private val syncStore: SyncStore) {
    fun handleSyncReq(peerDigest: List<String>): List<MessageEnvelope> {
        val localDigest = syncStore.getLocalDigest()
        val missingInPeer = localDigest - peerDigest.toSet()
        return syncStore.getMessages(missingInPeer)
    }

    fun handleSyncResp(messages: List<MessageEnvelope>) {
        syncStore.saveMessages(messages)
    }

    fun createSyncReq(): List<String> {
        return syncStore.getLocalDigest()
    }
}
