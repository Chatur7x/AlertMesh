package com.alertmesh.protocol.model

import kotlinx.serialization.Serializable

@Serializable
data class PresencePayload(
    val deviceId: String,
    val fingerprint: String,
    val batteryPercent: Int,
    val hopCount: Int,
    val neighborCount: Int,
    val timestamp: Long
)
