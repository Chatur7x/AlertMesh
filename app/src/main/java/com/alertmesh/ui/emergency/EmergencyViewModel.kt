package com.alertmesh.ui.emergency

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import android.util.Base64
import com.alertmesh.protocol.model.MessageType
import kotlinx.serialization.protobuf.ProtoBuf
import kotlinx.serialization.ExperimentalSerializationApi
import com.alertmesh.di.AppContainer
import com.alertmesh.protocol.model.SosPayload
import com.alertmesh.service.LocationData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class EmergencyUiState(
    val isAlarmActive: Boolean = false,
    val incomingSos: SosPayload? = null,
    val senderFingerprint: String = "",
    val error: String? = null,
    val deliveryMessage: String? = null
)

class EmergencyViewModel(
    private val container: AppContainer
) : ViewModel() {

    private val _uiState = MutableStateFlow(EmergencyUiState())
    val uiState: StateFlow<EmergencyUiState> = _uiState.asStateFlow()
    private var lastIncomingId: String? = null

    init {
        viewModelScope.launch {
            val localId = container.cryptoProvider.getDeviceId()
            container.messageRepository.getAllMessages().collect { messages ->
                val latest = messages.firstOrNull {
                    it.type == MessageType.SOS.name && it.origin != localId && it.status != "seen"
                }
                if (latest != null && latest.msgId != lastIncomingId) {
                    try {
                        @OptIn(ExperimentalSerializationApi::class)
                        val payload = ProtoBuf.decodeFromByteArray(
                            SosPayload.serializer(), Base64.decode(latest.payload, Base64.NO_WRAP)
                        )
                        onSosReceived(payload, latest.origin.take(8))
                        lastIncomingId = latest.msgId
                    } catch (_: Exception) { }
                }
            }
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun triggerSos(payload: SosPayload) {
        viewModelScope.launch {
            val location = container.locationManager.getCurrentLocation()
            val newPayload = payload.copy(
                latitude = location?.latitude ?: payload.latitude,
                longitude = location?.longitude ?: payload.longitude,
                accuracy = location?.accuracy ?: payload.accuracy,
                fixAgeSeconds = location?.let { it.fixAgeMs / 1000 } ?: payload.fixAgeSeconds,
                pressureHpa = container.barometerManager.currentPressureHpa ?: payload.pressureHpa
            )
            try {
                val envelope = container.protocolEngine.sendMessage(
                    MessageType.SOS,
                    ProtoBuf.encodeToByteArray(SosPayload.serializer(), newPayload),
                    channel = 0, ttl = 12
                )
                val peers = container.transport.discoveredPeers().first().size
                container.messageRepository.saveMessage(envelope, 0, if (peers == 0) "queued" else "sent")
                _uiState.update {
                    it.copy(
                        isAlarmActive = true, incomingSos = newPayload, senderFingerprint = "SELF",
                        deliveryMessage = if (peers == 0) "No peers connected. This SOS has not reached another device."
                            else "SOS submitted to the mesh. Receipt is not confirmed."
                    )
                }
            } catch (_: Exception) {
                _uiState.update { it.copy(error = "SOS could not be submitted. Check mesh status and retry.") }
            }
        }
    }

    fun dismissAlarm() {
        lastIncomingId?.let { id ->
            viewModelScope.launch { container.messageRepository.updateMessageStatus(id, "seen") }
        }
        _uiState.update { it.copy(isAlarmActive = false, incomingSos = null, senderFingerprint = "", deliveryMessage = null) }
    }

    fun onSosReceived(payload: SosPayload, senderFingerprint: String) {
        _uiState.update { it.copy(isAlarmActive = true, incomingSos = payload, senderFingerprint = senderFingerprint, deliveryMessage = "Received from another device") }
    }
}

class EmergencyViewModelFactory(private val container: AppContainer) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return EmergencyViewModel(container) as T
    }
}
