package com.alertmesh.server

import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.InputStream
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket
import java.nio.ByteBuffer
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Serializable
data class PeerListMessage(val type: String = "PEER_LIST", val peers: List<String>)

@Serializable
data class TopologyMessage(val type: String = "TOPOLOGY", val edges: List<List<String>>)

class RendezvousServer(
    private val port: Int,
    private val topologyManager: TopologyManager,
    private val lossInjector: PacketLossInjector
) {
    private val clients = ConcurrentHashMap<String, Socket>()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")
    private val broadcastMutex = Mutex()

    fun start() {
        val serverSocket = ServerSocket(port)
        logInfo("Listening on port $port...")

        scope.launch {
            launch {
                topologyManager.topologyChanges.collect {
                    broadcastTopology()
                    broadcastPeerList()
                }
            }

            while (currentCoroutineContext().isActive) {
                try {
                    val clientSocket = serverSocket.accept()
                    launch { handleClient(clientSocket) }
                } catch (e: Exception) {
                    if (currentCoroutineContext().isActive) logWarn("Error accepting connection: ${e.message}")
                }
            }
        }
    }

    private suspend fun handleClient(socket: Socket) {
        var peerId: String? = null
        try {
            val input = socket.getInputStream()
            val output = socket.getOutputStream()

            while (currentCoroutineContext().isActive) {
                val message = readMessage(input) ?: break
                
                val json = Json.parseToJsonElement(message).jsonObject
                val type = json["type"]?.jsonPrimitive?.content
                
                when (type) {
                    "REGISTER" -> {
                        val id = json["from"]?.jsonPrimitive?.content
                        if (id != null) {
                            peerId = id
                            clients[id] = socket
                            logInfo("Node $id registered.")
                            broadcastPeerList()
                            sendTopologyTo(output)
                        }
                    }
                    "FRAME" -> {
                        val from = json["from"]?.jsonPrimitive?.content
                        val to = json["to"]?.jsonPrimitive?.content
                        val dataSize = message.length 
                        
                        if (from != null && to != null) {
                            if (lossInjector.shouldDrop()) {
                                logRed("Node $from → $to: FRAME DROPPED (simulated packet loss)")
                                continue
                            }
                            
                            if (topologyManager.areAdjacent(from, to)) {
                                val targetSocket = clients[to]
                                if (targetSocket != null) {
                                    try {
                                        sendMessage(targetSocket.getOutputStream(), message)
                                        logGreen("Node $from → $to: FRAME relayed ($dataSize bytes)")
                                    } catch (e: Exception) {
                                        logWarn("Failed to relay frame to $to: ${e.message}")
                                    }
                                } else {
                                    logYellow("Node $from → $to: FRAME DROPPED (target offline)")
                                }
                            } else {
                                logRed("Node $from → $to: FRAME DROPPED (not adjacent)")
                            }
                        }
                    }
                    else -> logWarn("Unknown message type: $type")
                }
            }
        } catch (e: Exception) {
            // connection dropped
        } finally {
            peerId?.let {
                clients.remove(it)
                logInfo("Node $it disconnected.")
                broadcastPeerList()
            }
            try { socket.close() } catch (e: Exception) {}
        }
    }
    
    private suspend fun broadcastPeerList() {
        val peers = clients.keys().toList()
        val msg = Json.encodeToString(PeerListMessage(peers = peers))
        broadcastMessage(msg)
    }
    
    private suspend fun broadcastTopology() {
        val edgesList = topologyManager.getEdges().map { listOf(it.first, it.second) }
        val msg = Json.encodeToString(TopologyMessage(edges = edgesList))
        broadcastMessage(msg)
    }

    private suspend fun sendTopologyTo(out: OutputStream) {
        val edgesList = topologyManager.getEdges().map { listOf(it.first, it.second) }
        val msg = Json.encodeToString(TopologyMessage(edges = edgesList))
        broadcastMutex.withLock {
            try {
                sendMessage(out, msg)
            } catch (e: Exception) {}
        }
    }

    private suspend fun broadcastMessage(msg: String) {
        broadcastMutex.withLock {
            val disconnected = mutableListOf<String>()
            for ((id, socket) in clients) {
                try {
                    sendMessage(socket.getOutputStream(), msg)
                } catch (e: Exception) {
                    disconnected.add(id)
                }
            }
            disconnected.forEach {
                clients.remove(it)
                try { clients[it]?.close() } catch (e: Exception) {}
            }
        }
    }

    private fun readMessage(input: InputStream): String? {
        val lenBytes = ByteArray(4)
        var read = 0
        while (read < 4) {
            val count = input.read(lenBytes, read, 4 - read)
            if (count == -1) return null
            read += count
        }
        val length = ByteBuffer.wrap(lenBytes).int
        if (length <= 0 || length > 10 * 1024 * 1024) return null
        
        val payloadBytes = ByteArray(length)
        read = 0
        while (read < length) {
            val count = input.read(payloadBytes, read, length - read)
            if (count == -1) return null
            read += count
        }
        return String(payloadBytes, Charsets.UTF_8)
    }

    private fun sendMessage(output: OutputStream, message: String) {
        val payload = message.toByteArray(Charsets.UTF_8)
        val lenBytes = ByteBuffer.allocate(4).putInt(payload.size).array()
        output.write(lenBytes)
        output.write(payload)
        output.flush()
    }
    
    fun getConnectedNodes(): List<String> = clients.keys().toList()
    
    fun stop() {
        scope.cancel()
        clients.values.forEach { try { it.close() } catch (e: Exception) {} }
        clients.clear()
    }

    private fun time(): String = formatter.format(LocalTime.now())
    
    private fun logInfo(msg: String) = println("\u001B[36m[${time()}] $msg\u001B[0m")
    private fun logGreen(msg: String) = println("\u001B[32m[${time()}] $msg\u001B[0m")
    private fun logRed(msg: String) = println("\u001B[31m[${time()}] $msg\u001B[0m")
    private fun logYellow(msg: String) = println("\u001B[33m[${time()}] $msg\u001B[0m")
    private fun logWarn(msg: String) = println("\u001B[33m[${time()}] $msg\u001B[0m")
}
