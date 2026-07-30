package com.alertmesh.protocol.model

import kotlinx.serialization.Serializable

@Serializable
enum class MessageType {
    TEXT, VOICE, SOS, PRESENCE, SYNC_REQ, SYNC_RESP
}
