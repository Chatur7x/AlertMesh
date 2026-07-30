package com.alertmesh.protocol.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoBuf
import java.util.UUID

@Serializable
data class MessageEnvelope(
    val msgId: String,
    val origin: String,
    val counter: Long,
    val type: MessageType,
    val channel: Int,
    val ttl: Int,
    val payload: ByteArray,
    val signature: ByteArray
) {
    fun signable(): ByteArray {
        val copy = this.copy(ttl = 8, signature = ByteArray(0)) // TTL and signature excluded
        // We need a specific serializable form for signing that excludes TTL
        return ProtoBuf.encodeToByteArray(SignableEnvelope.serializer(), SignableEnvelope(
            msgId = msgId,
            origin = origin,
            counter = counter,
            type = type,
            channel = channel,
            payload = payload
        ))
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MessageEnvelope) return false
        if (msgId != other.msgId) return false
        if (origin != other.origin) return false
        if (counter != other.counter) return false
        if (type != other.type) return false
        if (channel != other.channel) return false
        if (ttl != other.ttl) return false
        if (!payload.contentEquals(other.payload)) return false
        if (!signature.contentEquals(other.signature)) return false
        return true
    }

    override fun hashCode(): Int {
        var result = msgId.hashCode()
        result = 31 * result + origin.hashCode()
        result = 31 * result + counter.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + channel
        result = 31 * result + ttl
        result = 31 * result + payload.contentHashCode()
        result = 31 * result + signature.contentHashCode()
        return result
    }

    companion object {
        fun newId(): String {
            val uuid = UUID.randomUUID()
            return String.format("%016x%016x", uuid.mostSignificantBits, uuid.leastSignificantBits)
        }
    }
}

@Serializable
private data class SignableEnvelope(
    val msgId: String,
    val origin: String,
    val counter: Long,
    val type: MessageType,
    val channel: Int,
    val payload: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SignableEnvelope) return false
        if (msgId != other.msgId) return false
        if (origin != other.origin) return false
        if (counter != other.counter) return false
        if (type != other.type) return false
        if (channel != other.channel) return false
        if (!payload.contentEquals(other.payload)) return false
        return true
    }

    override fun hashCode(): Int {
        var result = msgId.hashCode()
        result = 31 * result + origin.hashCode()
        result = 31 * result + counter.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + channel
        result = 31 * result + payload.contentHashCode()
        return result
    }
}
