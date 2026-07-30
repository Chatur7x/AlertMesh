package com.alertmesh.service

import android.content.Context
import android.util.Base64
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.ConnectionsClient
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import com.google.android.gms.nearby.connection.Strategy
import com.alertmesh.protocol.crypto.CryptoProvider
import com.alertmesh.protocol.transport.Transport
import com.alertmesh.protocol.transport.TransportFrame
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.runBlocking
import kotlin.random.Random

class NearbyTransport(private val context: Context, private val cryptoProvider: CryptoProvider) : Transport {
    private val connectionsClient: ConnectionsClient = Nearby.getConnectionsClient(context)
    private val SERVICE_ID = "com.alertmesh.mesh"
    private val strategy = Strategy.P2P_CLUSTER
    private val connectedEndpoints = mutableSetOf<String>()
    
    private val _incomingFrames = MutableSharedFlow<TransportFrame>(extraBufferCapacity = 64)
    private val _discoveredPeers = MutableStateFlow<Set<String>>(emptySet())
    
    override val localPeerId = cryptoProvider.getDeviceId()

    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            payload.asBytes()?.let { bytes ->
                val frame = TransportFrame(endpointId, bytes)
                _incomingFrames.tryEmit(frame)
            }
        }
        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {}
    }

    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo) {
            runBlocking {
                delay(Random.nextLong(100, 500))
                connectionsClient.acceptConnection(endpointId, payloadCallback)
            }
        }
        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            if (result.status.statusCode == ConnectionsStatusCodes.STATUS_OK) {
                connectedEndpoints.add(endpointId)
                _discoveredPeers.value = connectedEndpoints.toSet()
            }
        }
        override fun onDisconnected(endpointId: String) {
            connectedEndpoints.remove(endpointId)
            _discoveredPeers.value = connectedEndpoints.toSet()
        }
    }

    private val endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
            connectionsClient.requestConnection(localPeerId, endpointId, connectionLifecycleCallback)
        }
        override fun onEndpointLost(endpointId: String) {}
    }

    override suspend fun start() {
        val advertisingOptions = AdvertisingOptions.Builder().setStrategy(strategy).build()
        connectionsClient.startAdvertising(localPeerId, SERVICE_ID, connectionLifecycleCallback, advertisingOptions)
        
        val discoveryOptions = DiscoveryOptions.Builder().setStrategy(strategy).build()
        connectionsClient.startDiscovery(SERVICE_ID, endpointDiscoveryCallback, discoveryOptions)
    }

    override suspend fun stop() {
        connectionsClient.stopAdvertising()
        connectionsClient.stopDiscovery()
        connectionsClient.stopAllEndpoints()
        connectedEndpoints.clear()
        _discoveredPeers.value = emptySet()
    }

    override suspend fun send(targetPeerId: String, data: ByteArray) {
        val payload = Payload.fromBytes(data)
        connectionsClient.sendPayload(targetPeerId, payload)
    }

    override fun incomingFrames(): Flow<TransportFrame> = _incomingFrames.asSharedFlow()

    override fun discoveredPeers(): Flow<Set<String>> = _discoveredPeers.asStateFlow()
}
