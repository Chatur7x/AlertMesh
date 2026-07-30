package com.alertmesh.data.repository

import android.util.Base64
import com.alertmesh.data.db.dao.MessageDao
import com.alertmesh.data.db.entity.MessageEntity
import com.alertmesh.protocol.model.MessageEnvelope
import kotlinx.coroutines.flow.Flow

class MessageRepository(private val messageDao: MessageDao) {
    fun getAllMessages(): Flow<List<MessageEntity>> {
        return messageDao.getAllMessages()
    }

    suspend fun saveMessage(envelope: MessageEnvelope, hopCount: Int) {
        val entity = MessageEntity(
            msgId = envelope.msgId,
            origin = envelope.origin,
            counter = envelope.counter,
            type = envelope.type.name,
            channel = envelope.channel,
            payload = Base64.encodeToString(envelope.payload, Base64.NO_WRAP),
            signature = Base64.encodeToString(envelope.signature, Base64.NO_WRAP),
            timestamp = System.currentTimeMillis(),
            hopCount = hopCount,
            delivered = true
        )
        messageDao.insert(entity)
    }

    suspend fun getAllMsgIds(): List<String> {
        return messageDao.getAllMsgIds()
    }

    suspend fun getMessageCount(): Int {
        return messageDao.getMessageCount()
    }
}
