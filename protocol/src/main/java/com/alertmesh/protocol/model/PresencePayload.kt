package com.alertmesh.protocol.model

import kotlinx.serialization.Serializable

@Serializable
data class PresencePayload(
    val deviceId: String,
    val fingerprint: String,
    val publicKey: ByteArray = byteArrayOf(),
    val hybridPublicKey: ByteArray = byteArrayOf(),
    val batteryPercent: Int,
    val hopCount: Int,
    val neighborCount: Int,
    val timestamp: Long,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val accuracy: Float? = null,
    val hasSensor: Boolean = false,
    val pressureHpa: Float? = null
)
