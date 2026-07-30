package com.alertmesh.protocol.transport

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class LoopbackTransport(override val localPeerId: String) : Transport {
    private val frames = MutableSharedFlow<TransportFrame>(extraBufferCapacity = 100)
    private val peers = MutableStateFlow<Set<String>>(emptySet())
    
    companion object {
        private val registry = java.util.concurrent.ConcurrentHashMap<String, LoopbackTransport>()
    }

    override suspend fun start() {
        registry[localPeerId] = this
    }

    override suspend fun stop() {
        registry.remove(localPeerId)
        peers.value = emptySet()
        registry.values.forEach { it.removePeer(localPeerId) }
    }

    override suspend fun send(targetPeerId: String, data: ByteArray) {
        registry[targetPeerId]?.receiveFrame(TransportFrame(localPeerId, data))
    }

    override fun incomingFrames(): Flow<TransportFrame> = frames.asSharedFlow()

    override fun discoveredPeers(): Flow<Set<String>> = peers.asStateFlow()
    
    suspend fun receiveFrame(frame: TransportFrame) {
        frames.emit(frame)
    }
    
    fun connectTo(other: LoopbackTransport) {
        addPeer(other.localPeerId)
        other.addPeer(localPeerId)
    }
    
    private fun addPeer(peerId: String) {
        peers.update { it + peerId }
    }

    private fun removePeer(peerId: String) {
        peers.update { it - peerId }
    }
}
