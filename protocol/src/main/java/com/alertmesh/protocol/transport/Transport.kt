package com.alertmesh.protocol.transport

import kotlinx.coroutines.flow.Flow

data class TransportFrame(
    val fromPeerId: String,
    val data: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TransportFrame) return false
        return fromPeerId == other.fromPeerId && data.contentEquals(other.data)
    }
    override fun hashCode(): Int = 31 * fromPeerId.hashCode() + data.contentHashCode()
}

interface Transport {
    val localPeerId: String
    suspend fun start()
    suspend fun stop()
    suspend fun send(targetPeerId: String, data: ByteArray)
    fun incomingFrames(): Flow<TransportFrame>
    fun discoveredPeers(): Flow<Set<String>>
}
