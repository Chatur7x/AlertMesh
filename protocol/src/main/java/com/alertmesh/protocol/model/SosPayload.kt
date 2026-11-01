package com.alertmesh.protocol.model

import kotlinx.serialization.Serializable

@Serializable
data class SosPayload(
    val peopleCount: Int,
    val injuredCount: Int,
    val condition: Condition,
    val note: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val accuracy: Float? = null,
    val fixAgeSeconds: Long? = null,
    val pressureHpa: Float? = null
) {
    init {
        require(note.length <= 100) { "Note must be 100 characters or less" }
    }
}

@Serializable
enum class Condition {
    TRAPPED, INJURED, SAFE, NEED_WATER, NEED_MEDICAL, UNKNOWN
}
