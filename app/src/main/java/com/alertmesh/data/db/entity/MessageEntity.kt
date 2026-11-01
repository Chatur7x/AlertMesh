package com.alertmesh.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey
    val msgId: String,
    val origin: String,
    val counter: Long,
    val type: String,
    val channel: Int,
    val payload: String, // Base64 encoded
    val signature: String, // Base64 encoded
    val timestamp: Long,
    val hopCount: Int,
    val status: String = "queued"  // queued, sent, delivered, failed, acked
)
