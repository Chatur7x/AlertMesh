package com.alertmesh.ui.terminal

import android.util.Base64
import androidx.compose.ui.text.AnnotatedString
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.alertmesh.data.db.entity.PeerEntity
import com.alertmesh.di.AppContainer
import com.alertmesh.protocol.model.Condition
import com.alertmesh.protocol.model.MessageType
import com.alertmesh.protocol.model.SosPayload
import com.alertmesh.protocol.routing.DistanceEstimator
import com.alertmesh.ui.terminal.components.commandEcho
import com.alertmesh.ui.terminal.components.errLine
import com.alertmesh.ui.terminal.components.infoLine
import com.alertmesh.ui.terminal.components.okLine
import com.alertmesh.ui.terminal.components.warnLine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TerminalLine(val text: AnnotatedString, val timestamp: Long, val lineType: String = "info")

data class TerminalUiState(
    val lines: List<TerminalLine> = emptyList(),
    val currentInput: String = "",
    val isConnected: Boolean = false,
    val peerCount: Int = 0,
    val currentChannel: Int = 1,
    val hasVoiceNote: Boolean = false,
    val barometerAvailable: Boolean = false,
    val pressureHpa: Float? = null,
    val pressureHistory: List<Float> = emptyList(),
    val relativeHeightMeters: Double? = null
)

class TerminalViewModel(
    private val container: AppContainer
) : ViewModel() {

    private val _uiState = MutableStateFlow(TerminalUiState())
    val uiState: StateFlow<TerminalUiState> = _uiState.asStateFlow()

    private val commandHistory = mutableListOf<String>()
    private var historyIndex = -1
    private val processedAckIds = mutableSetOf<String>()
    private val displayedMsgIds = mutableSetOf<String>()
    private var latestVoicePayload: String? = null

    init {
        onBoot()
        observePeers()
        observeFieldMetrics()
        observeMessages()
    }

    private fun observeFieldMetrics() {
        viewModelScope.launch {
            container.barometerManager.pressureHistory.collect { history ->
                _uiState.update { it.copy(pressureHistory = history) }
            }
        }
        viewModelScope.launch {
            combine(container.barometerManager.pressureHpa,
                container.peerRepository.getAllPeers(), container.transport.discoveredPeers()) { pressure, peers, connected ->
                val peerPressure = if (connected.isEmpty()) null else peers
                    .filter { System.currentTimeMillis() - it.lastSeen < 30_000 }
                    .maxByOrNull { it.lastSeen }?.pressureHpa
                val height = if (pressure != null && peerPressure != null && peerPressure > 0f)
                    DistanceEstimator.barometricHeight(peerPressure, pressure) else null
                pressure to height
            }.collect { (pressure, height) ->
                _uiState.update { it.copy(barometerAvailable = container.barometerManager.hasBarometer,
                    pressureHpa = pressure, relativeHeightMeters = height) }
            }
        }
    }

    private fun observePeers() {
        viewModelScope.launch {
            container.transport.discoveredPeers().collect { peerIds ->
                val onlineCount = peerIds.size
                _uiState.update { it.copy(peerCount = onlineCount, isConnected = onlineCount > 0) }
            }
        }
    }

    /**
     * Observes stored messages to (a) honor delivery: mark a referenced message as "acked"
     * when an ACK arrives, and (b) show received messages on the active channel only.
     */
    private fun observeMessages() {
        viewModelScope.launch {
            val localDeviceId = container.cryptoProvider.getDeviceId()
            container.messageRepository.getAllMessages().collect { messages ->
                // ACK received -> mark the referenced sent message as acknowledged.
                messages.filter { it.type == MessageType.ACK.name }.forEach { ack ->
                    if (ack.msgId !in processedAckIds) {
                        processedAckIds.add(ack.msgId)
                        try {
                            val originalMsgId = String(
                                Base64.decode(ack.payload, Base64.NO_WRAP), Charsets.UTF_8
                            )
                            container.messageRepository.updateMessageStatus(originalMsgId, "acked")
                        } catch (_: Exception) {}
                    }
                }

                // Display received (non-local, non-ACK) messages on the current channel only.
                val currentChannel = uiState.value.currentChannel
                messages.filter {
                    it.origin != localDeviceId &&
                        it.type == MessageType.TEXT.name &&
                        it.channel == currentChannel &&
                        it.msgId !in displayedMsgIds
                }.forEach { msg ->
                    displayedMsgIds.add(msg.msgId)
                    try {
                        val text = String(Base64.decode(msg.payload, Base64.NO_WRAP), Charsets.UTF_8)
                        appendLine(infoLine("[CH ${msg.channel}] ${msg.origin.take(8)}...: $text"), "msg")
                    } catch (_: Exception) {}
                }
                messages.firstOrNull { it.origin != localDeviceId && it.type == MessageType.VOICE.name }?.let { voice ->
                    if (latestVoicePayload != voice.payload) {
                        latestVoicePayload = voice.payload
                        _uiState.update { it.copy(hasVoiceNote = true) }
                        appendLine(infoLine("Voice note received from ${voice.origin.take(8)}. Tap PLAY VOICE to listen."), "msg")
                    }
                }
                messages.firstOrNull { it.origin != localDeviceId && it.type == MessageType.SOS.name }?.let { sos ->
                    if (sos.msgId !in displayedMsgIds) {
                        displayedMsgIds.add(sos.msgId)
                        appendLine(errLine("Incoming SOS from ${sos.origin.take(8)}. Open SOS for details."), "err")
                    }
                }
            }
        }
    }

    fun onBoot() {
        viewModelScope.launch {
            val fingerprint = container.cryptoProvider.getFingerprint()
            val transport = if (container.isEmulator) "Socket (10.0.2.2)" else "Nearby P2P (BLE+WiFi)"
            val peerCount = container.transport.discoveredPeers().first().size
            val messageCount = container.messageRepository.getMessageCount()
            val hasLocation = container.locationManager.getCurrentLocation() != null

            val bootLines = BootSequence.generateBootLines(
                fingerprint = fingerprint,
                transport = transport,
                peerCount = peerCount,
                messageCount = messageCount,
                hasBarometer = container.barometerManager.hasBarometer,
                hasLocation = hasLocation
            )

            _uiState.update { state ->
                state.copy(lines = bootLines.map { TerminalLine(it, System.currentTimeMillis(), "boot") })
            }
        }
    }

    fun onInputChanged(text: String) {
        _uiState.update { it.copy(currentInput = text) }
    }

    fun appendLine(text: AnnotatedString, lineType: String = "info") {
        _uiState.update { state ->
            val newLines = state.lines.toMutableList()
            newLines.add(TerminalLine(text, System.currentTimeMillis(), lineType))
            if (newLines.size > 500) {
                newLines.removeAt(0)
            }
            state.copy(lines = newLines)
        }
    }

    fun onHistoryUp() {
        if (commandHistory.isNotEmpty() && historyIndex < commandHistory.size - 1) {
            historyIndex++
            _uiState.update { it.copy(currentInput = commandHistory[commandHistory.size - 1 - historyIndex]) }
        }
    }

    fun onHistoryDown() {
        if (historyIndex > 0) {
            historyIndex--
            _uiState.update { it.copy(currentInput = commandHistory[commandHistory.size - 1 - historyIndex]) }
        } else if (historyIndex == 0) {
            historyIndex = -1
            _uiState.update { it.copy(currentInput = "") }
        }
    }

    fun onCommand(input: String) {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return

        commandHistory.add(trimmed)
        historyIndex = -1

        val parsed = CommandParser.parse(trimmed)
        if (parsed !is CommandParser.Command.Channel) appendLine(commandEcho(trimmed), "info")
        _uiState.update { it.copy(currentInput = "") }

        when (val cmd = parsed) {
            is CommandParser.Command.UniversalBroadcast -> {
                sendBroadcastMessage(cmd.text)
            }

            is CommandParser.Command.Help -> {
                appendLine(infoLine("=== UNIVERSAL MESH COMMAND SYSTEM ==="), "info")
                appendLine(infoLine("[Plain Text]       - Universal broadcast to all peers on channel"), "info")
                appendLine(infoLine("/send <text>       - Universal message broadcast"), "info")
                appendLine(infoLine("/msg <target> <msg>- Send message to target or broadcast"), "info")
                appendLine(infoLine("/peers             - List all discovered online peers & stats"), "info")
                appendLine(infoLine("/status            - Display local node diagnostic status"), "info")
                appendLine(infoLine("/channel <1-8>     - Set active communication channel"), "info")
                appendLine(infoLine("/sos [note]        - Universal Emergency SOS alert broadcast"), "info")
                appendLine(infoLine("/clear             - Clear terminal log screen"), "info")
            }

            is CommandParser.Command.Peers -> {
                viewModelScope.launch {
                    val peers = container.peerRepository.getAllPeers().first()
                    val connectedCount = container.transport.discoveredPeers().first().size
                    appendLine(okLine("$connectedCount connected mesh peers (${peers.size} known):"), "ok")
                    if (connectedCount == 0) {
                        appendLine(warnLine("No peers connected yet. Run /fix or bring devices near each other."), "warn")
                    } else {
                        val localPress = container.barometerManager.currentPressureHpa
                        peers.forEach { p ->
                            val peerPress = p.pressureHpa
                            val floorStr = if (localPress != null && peerPress != null) {
                                " | " + com.alertmesh.protocol.routing.DistanceEstimator.formatFloorDescription(localPress, peerPress)
                            } else ""
                            appendLine(infoLine(" • Peer [${p.fingerprint}] Bat: ${p.batteryPercent}%$floorStr | ID: ${p.deviceId.take(8)}..."), "msg")
                        }
                    }
                }
            }

            is CommandParser.Command.Msg -> {
                sendTextMessage(cmd.target, cmd.text)
            }

            is CommandParser.Command.Ping -> {
                appendLine(warnLine("Ping is not implemented yet"), "warn")
            }

            is CommandParser.Command.Trace -> {
                appendLine(warnLine("Route tracing is not implemented yet"), "warn")
            }

            is CommandParser.Command.Sync -> {
                appendLine(warnLine("Manual sync is not implemented; peers sync automatically on connect"), "warn")
            }

            is CommandParser.Command.Status -> {
                viewModelScope.launch {
                    val fp = container.cryptoProvider.getFingerprint()
                    val devId = container.cryptoProvider.getDeviceId()
                    val transportStr = if (container.isEmulator) "Socket (10.0.2.2)" else "Nearby P2P (BLE + Wi-Fi Direct)"
                    val loc = container.locationManager.getCurrentLocation()
                    val locStr = if (loc != null) "${"%.4f".format(loc.latitude)}, ${"%.4f".format(loc.longitude)}" else "No GPS Fix"
                    val press = container.barometerManager.currentPressureHpa
                    val pressStr = if (press != null) "${"%.2f".format(press)} hPa (Active)" else if (container.barometerManager.hasBarometer) "Acquiring..." else "Unavailable"
                    appendLine(infoLine("=== NODE DIAGNOSTIC STATUS ==="), "info")
                    appendLine(infoLine("Device ID: $devId"), "info")
                    appendLine(infoLine("Fingerprint: $fp"), "info")
                    appendLine(infoLine("Transport: $transportStr"), "info")
                    appendLine(infoLine("Active Channel: ${uiState.value.currentChannel}"), "info")
                    appendLine(infoLine("Online Peers: ${uiState.value.peerCount}"), "info")
                    appendLine(infoLine("Location: $locStr"), "info")
                    appendLine(infoLine("Barometer: $pressStr"), "info")
                }
            }

            is CommandParser.Command.Fix -> {
                viewModelScope.launch {
                    container.peerRepository.markStale()
                    container.locationManager.startLocationUpdates()
                    appendLine(infoLine("Refreshed location updates and marked stale peers. Check /status for current mesh state."), "info")
                }
            }

            is CommandParser.Command.Channel -> {
                _uiState.update { it.copy(currentChannel = cmd.num) }
            }

            is CommandParser.Command.Sos -> {
                viewModelScope.launch {
                    val noteText = cmd.note ?: "Emergency SOS via Universal Command"
                    val loc = container.locationManager.getCurrentLocation()
                    val sos = SosPayload(
                        peopleCount = 1,
                        injuredCount = 0,
                        condition = Condition.TRAPPED,
                        note = noteText.take(100),
                        latitude = loc?.latitude,
                        longitude = loc?.longitude,
                        accuracy = loc?.accuracy,
                        fixAgeSeconds = loc?.let { it.fixAgeMs / 1000 }
                    )
                    try {
                        val envelope = container.protocolEngine.sendMessage(
                            MessageType.SOS,
                            kotlinx.serialization.protobuf.ProtoBuf.encodeToByteArray(SosPayload.serializer(), sos),
                            channel = 0, ttl = 12
                        )
                        val hasPeers = uiState.value.peerCount > 0
                        container.messageRepository.saveMessage(envelope, 0, if (hasPeers) "sent" else "queued")
                        appendLine(warnLine(if (hasPeers) "SOS submitted; receipt unconfirmed" else "SOS saved locally; no peers connected"), "warn")
                    } catch (e: Exception) { appendLine(errLine("SOS failed: ${e.message}"), "err") }
                }
            }

            is CommandParser.Command.Clear -> {
                _uiState.update { it.copy(lines = emptyList()) }
            }

            is CommandParser.Command.Unknown -> {
                appendLine(errLine("Command not recognized: ${cmd.input}. Type /help for all commands."), "err")
            }

            else -> {
                appendLine(warnLine("Command is unavailable"), "warn")
            }
        }
    }

    fun sendBroadcastMessage(text: String) {
        viewModelScope.launch {
            val textBytes = text.toByteArray(Charsets.UTF_8)
            try {
                val envelope = container.protocolEngine.sendMessage(
                    MessageType.TEXT, textBytes,
                    channel = uiState.value.currentChannel
                )
                val hasPeers = uiState.value.peerCount > 0
                container.messageRepository.saveMessage(envelope, hopCount = 0, status = if (hasPeers) "sent" else "queued")
                appendLine(
                    if (hasPeers) okLine("[SUBMITTED : CH ${uiState.value.currentChannel}] $text")
                    else warnLine("Saved locally; no peers connected. Delivery is not confirmed."),
                    if (hasPeers) "msg" else "warn"
                )
            } catch (e: Exception) {
                appendLine(errLine("Failed to broadcast: ${e.message}"), "err")
            }
        }
    }

    fun sendTextMessage(target: String, text: String) {
        viewModelScope.launch {
            val textBytes = text.toByteArray(Charsets.UTF_8)
            try {
                val envelope = container.protocolEngine.sendMessage(
                    MessageType.TEXT, textBytes,
                    channel = uiState.value.currentChannel,
                    targetPeer = target
                )
                container.messageRepository.saveMessage(envelope, hopCount = 0, status = "sent")
                appendLine(okLine("[SUBMITTED to $target] $text (delivery unconfirmed)"), "msg")
            } catch (e: Exception) {
                appendLine(errLine("Failed to send to $target: ${e.message}"), "err")
            }
        }
    }

    fun sendSos(sosPayload: SosPayload) {
        viewModelScope.launch {
            container.protocolEngine.sendSos(sosPayload)
        }
    }

    fun startVoice() {
        if (!container.audioManager.startRecording()) {
            appendLine(errLine("Microphone unavailable; recording did not start"), "err")
        }
    }

    fun stopVoice() {
        viewModelScope.launch {
            val audioData = container.audioManager.stopRecording()
            if (audioData.isNotEmpty()) {
                try {
                val envelope = container.protocolEngine.sendMessage(MessageType.VOICE, audioData, uiState.value.currentChannel)
                container.messageRepository.saveMessage(envelope, 0, if (uiState.value.peerCount > 0) "sent" else "queued")
                appendLine(okLine("Voice note submitted on Channel ${uiState.value.currentChannel}; delivery unconfirmed"), "ok")
                } catch (e: Exception) { appendLine(errLine("Voice note failed: ${e.message}"), "err") }
            } else {
                appendLine(warnLine("No voice audio recorded"), "warn")
            }
        }
    }

    fun playLatestVoice() {
        val encoded = latestVoicePayload ?: return
        viewModelScope.launch {
            try {
                container.audioManager.playAudio(Base64.decode(encoded, Base64.NO_WRAP))
            } catch (e: Exception) {
                appendLine(errLine("Voice playback failed: ${e.message}"), "err")
            }
        }
    }
}

class TerminalViewModelFactory(private val container: AppContainer) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return TerminalViewModel(container) as T
    }
}
