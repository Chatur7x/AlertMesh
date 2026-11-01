package com.alertmesh.protocol.transport

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.Socket
import java.util.Base64

class SocketTransport(
    override val localPeerId: String,
    private val host: String,
    private val port: Int
) : Transport {

    private val frames = MutableSharedFlow<TransportFrame>(extraBufferCapacity = 100)
    private val peers = MutableStateFlow<Set<String>>(emptySet())
    private var socket: Socket? = null
    private var outStream: DataOutputStream? = null
    private var inStream: DataInputStream? = null
    private var readJob: Job? = null

    @Serializable
    private data class SocketMessage(
        val type: String = "",
        val from: String = "",
        val to: String = "",
        val data: String = "",
        val peers: List<String> = emptyList()
    )

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun start() {
        withContext(Dispatchers.IO) {
            socket = Socket(host, port)
            outStream = DataOutputStream(socket!!.getOutputStream())
            inStream = DataInputStream(socket!!.getInputStream())
            
            // Send register message
            val registerMsg = SocketMessage(type = "REGISTER", from = localPeerId)
            sendMessage(registerMsg)

            readJob = launch {
                readLoop()
            }
        }
    }

    private fun sendMessage(msg: SocketMessage) {
        val bytes = json.encodeToString(SocketMessage.serializer(), msg).toByteArray(Charsets.UTF_8)
        outStream?.writeInt(bytes.size)
        outStream?.write(bytes)
        outStream?.flush()
    }

    private suspend fun readLoop() {
        withContext(Dispatchers.IO) {
            try {
                val input = inStream ?: return@withContext
                while (isActive) {
                    val length = input.readInt()
                    val bytes = ByteArray(length)
                    input.readFully(bytes)
                    val str = String(bytes, Charsets.UTF_8)
                    val msg = json.decodeFromString(SocketMessage.serializer(), str)
                    
                    when (msg.type) {
                        "FRAME" -> {
                            val data = Base64.getDecoder().decode(msg.data)
                            frames.emit(TransportFrame(msg.from, data))
                        }
                        "PEER_LIST" -> {
                            peers.value = msg.peers.toSet()
                        }
                    }
                }
            } catch (e: Exception) {
                // Ignore or handle disconnect
            }
        }
    }

    override suspend fun stop() {
        withContext(Dispatchers.IO) {
            readJob?.cancel()
            socket?.close()
            socket = null
            outStream = null
            inStream = null
        }
    }

    override suspend fun send(targetPeerId: String, data: ByteArray) {
        withContext(Dispatchers.IO) {
            val msg = SocketMessage(
                type = "FRAME",
                from = localPeerId,
                to = targetPeerId,
                data = Base64.getEncoder().encodeToString(data)
            )
            sendMessage(msg)
        }
    }

    override fun incomingFrames(): Flow<TransportFrame> = frames.asSharedFlow()

    override fun discoveredPeers(): Flow<Set<String>> = peers.asStateFlow()
}
