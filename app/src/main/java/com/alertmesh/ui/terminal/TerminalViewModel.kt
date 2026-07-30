package com.alertmesh.ui.terminal

import androidx.compose.ui.text.AnnotatedString
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.alertmesh.di.AppContainer
import com.alertmesh.protocol.model.SosPayload
import com.alertmesh.ui.terminal.components.commandEcho
import com.alertmesh.ui.terminal.components.errLine
import com.alertmesh.ui.terminal.components.infoLine
import com.alertmesh.ui.terminal.components.okLine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TerminalLine(val text: AnnotatedString, val timestamp: Long)

data class TerminalUiState(
    val lines: List<TerminalLine> = emptyList(),
    val currentInput: String = "",
    val isConnected: Boolean = false,
    val peerCount: Int = 0,
    val currentChannel: Int = 1
)

class TerminalViewModel(
    private val container: AppContainer
) : ViewModel() {

    private val _uiState = MutableStateFlow(TerminalUiState())
    val uiState: StateFlow<TerminalUiState> = _uiState.asStateFlow()

    init {
        onBoot()
        viewModelScope.launch {
            _uiState.update { it.copy(
                isConnected = true, 
                peerCount = container.peerRepository.getOnlineCount()
            ) }
        }
    }

    fun onBoot() {
        viewModelScope.launch {
            val fingerprint = container.cryptoProvider.getFingerprint()
            val transport = "BLE+WiFi"
            val peerCount = container.peerRepository.getOnlineCount()
            val messageCount = container.messageRepository.getMessageCount()
            val hasLocation = container.locationManager.getCurrentLocation() != null

            val bootLines = BootSequence.generateBootLines(
                fingerprint = fingerprint,
                transport = transport,
                peerCount = peerCount,
                messageCount = messageCount,
                hasBarometer = false,
                hasLocation = hasLocation
            )

            _uiState.update { state ->
                state.copy(lines = bootLines.map { TerminalLine(it, System.currentTimeMillis()) })
            }
        }
    }

    fun onInputChanged(text: String) {
        _uiState.update { it.copy(currentInput = text) }
    }

    fun appendLine(text: AnnotatedString) {
        _uiState.update { state ->
            val newLines = state.lines.toMutableList()
            newLines.add(TerminalLine(text, System.currentTimeMillis()))
            if (newLines.size > 500) {
                newLines.removeAt(0)
            }
            state.copy(lines = newLines)
        }
    }

    fun onCommand(input: String) {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return
        
        appendLine(commandEcho(trimmed))
        _uiState.update { it.copy(currentInput = "") }
        
        when (val cmd = CommandParser.parse(trimmed)) {
            is CommandParser.Command.Help -> {
                appendLine(infoLine("Available commands:"))
                appendLine(infoLine("/help - Show this message"))
                appendLine(infoLine("/peers - List known peers"))
                appendLine(infoLine("/ping <target> - Ping a peer"))
                appendLine(infoLine("/msg <target> <text> - Send a message"))
                appendLine(infoLine("/clear - Clear terminal"))
            }
            is CommandParser.Command.Peers -> {
                viewModelScope.launch {
                    val count = container.peerRepository.getOnlineCount()
                    appendLine(okLine("Found $count online peers"))
                }
            }
            is CommandParser.Command.Msg -> {
                sendTextMessage(cmd.target, cmd.text)
                appendLine(okLine("Message sent to ${cmd.target}"))
            }
            is CommandParser.Command.Clear -> {
                _uiState.update { it.copy(lines = emptyList()) }
            }
            is CommandParser.Command.Sos -> {
                appendLine(errLine("Use Emergency UI for SOS"))
            }
            is CommandParser.Command.Unknown -> {
                if (trimmed.startsWith("/")) {
                    appendLine(errLine("Unknown command. Type /help"))
                } else {
                    appendLine(errLine("Input must start with / for commands, or use Msg to chat"))
                }
            }
            else -> {
                appendLine(okLine("Command executed: ${cmd.javaClass.simpleName}"))
            }
        }
    }

    fun sendTextMessage(target: String, text: String) {
        // Assume text sending logic inside protocolEngine
    }

    fun sendSos(sosPayload: SosPayload) {
        viewModelScope.launch {
            container.protocolEngine.sendSos(sosPayload)
        }
    }

    fun startVoice() {
        container.audioManager.startRecording()
    }
    
    fun stopVoice() {
        container.audioManager.stopRecording()
    }
}

class TerminalViewModelFactory(private val container: AppContainer) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return TerminalViewModel(container) as T
    }
}
