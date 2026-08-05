package com.alertmesh.ui.emergency

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.alertmesh.di.AppContainer
import com.alertmesh.protocol.model.SosPayload
import com.alertmesh.service.LocationData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EmergencyUiState(
    val isAlarmActive: Boolean = false,
    val incomingSos: SosPayload? = null,
    val senderFingerprint: String = ""
)

class EmergencyViewModel(
    private val container: AppContainer
) : ViewModel() {

    private val _uiState = MutableStateFlow(EmergencyUiState())
    val uiState: StateFlow<EmergencyUiState> = _uiState.asStateFlow()

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
            container.protocolEngine.sendSos(newPayload)
            _uiState.update { it.copy(isAlarmActive = true, incomingSos = newPayload, senderFingerprint = "SELF") }
        }
    }

    fun dismissAlarm() {
        _uiState.update { it.copy(isAlarmActive = false, incomingSos = null, senderFingerprint = "") }
    }

    fun onSosReceived(payload: SosPayload, senderFingerprint: String) {
        _uiState.update { it.copy(isAlarmActive = true, incomingSos = payload, senderFingerprint = senderFingerprint) }
    }
}

class EmergencyViewModelFactory(private val container: AppContainer) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return EmergencyViewModel(container) as T
    }
}
