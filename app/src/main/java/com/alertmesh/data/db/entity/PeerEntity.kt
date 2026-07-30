package com.alertmesh.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "peers")
data class PeerEntity(
    @PrimaryKey
    val deviceId: String,
    val fingerprint: String,
    val publicKey: String, // Base64 encoded
    val lastSeen: Long,
    val hopCount: Int,
    val batteryPercent: Int,
    val isOnline: Boolean
)
