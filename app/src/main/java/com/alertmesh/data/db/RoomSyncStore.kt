package com.alertmesh.data.db

import android.util.Base64
import com.alertmesh.data.db.dao.MessageDao
import com.alertmesh.data.db.entity.MessageEntity
import com.alertmesh.protocol.model.MessageEnvelope
import com.alertmesh.protocol.model.MessageType
import com.alertmesh.protocol.sync.SyncStore
import kotlinx.coroutines.runBlocking

class RoomSyncStore(private val messageDao: MessageDao) : SyncStore {
    
    override fun getLocalDigest(): List<String> = runBlocking {
        messageDao.getSyncMsgIds()
    }

    override fun getMessages(msgIds: List<String>): List<MessageEnvelope> = runBlocking {
        messageDao.getMessagesByIds(msgIds).map { entity ->
            MessageEnvelope(
                msgId = entity.msgId,
                origin = entity.origin,
                counter = entity.counter,
                type = MessageType.valueOf(entity.type),
                channel = entity.channel,
                ttl = 0, // Not stored
                payload = Base64.decode(entity.payload, Base64.NO_WRAP),
                signature = Base64.decode(entity.signature, Base64.NO_WRAP)
            )
        }
    }

    override fun saveMessages(messages: List<MessageEnvelope>) {
        runBlocking {
            messages.forEach { envelope ->
                val entity = MessageEntity(
                    msgId = envelope.msgId,
                    origin = envelope.origin,
                    counter = envelope.counter,
                    type = envelope.type.name,
                    channel = envelope.channel,
                    payload = Base64.encodeToString(envelope.payload, Base64.NO_WRAP),
                    signature = Base64.encodeToString(envelope.signature, Base64.NO_WRAP),
                    timestamp = System.currentTimeMillis(),
                    hopCount = 0, // Unknown hop count from sync
                    status = "sent"
                )
                messageDao.insert(entity)
            }
        }
    }
}
