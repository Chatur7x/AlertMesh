package com.alertmesh.ui.terminal

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alertmesh.ui.terminal.components.TerminalHeader
import com.alertmesh.ui.theme.CyberBg
import com.alertmesh.ui.theme.CyberBgCard
import com.alertmesh.ui.theme.CyberBgInput
import com.alertmesh.ui.theme.NeonAmber
import com.alertmesh.ui.theme.NeonCyan
import com.alertmesh.ui.theme.NeonGreen
import com.alertmesh.ui.theme.NeonRed
import com.alertmesh.ui.theme.TerminalFontFamily
import com.alertmesh.ui.theme.TerminalTypography
import com.alertmesh.ui.theme.TextPrimary

@Composable
fun TerminalScreen(
    viewModel: TerminalViewModel,
    onNavigateToEmergency: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    var isVoiceRecording by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.lines.size) {
        if (uiState.lines.isNotEmpty()) {
            listState.animateScrollToItem(uiState.lines.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBg)
    ) {
        // 1. Tactical HUD Header Bar
        TerminalHeader(
            peerCount = uiState.peerCount,
            currentChannel = uiState.currentChannel,
            isConnected = uiState.isConnected,
            onNavigateToEmergency = onNavigateToEmergency
        )

        // 2. Main Terminal Log Scrollback
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            items(uiState.lines) { line ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(CyberBgCard.copy(alpha = 0.5f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = line.text,
                        style = TerminalTypography.bodyMedium,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // Voice Recording Visualizer Overlay
        AnimatedVisibility(
            visible = isVoiceRecording,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CyberBgCard)
                    .border(BorderStroke(1.dp, NeonAmber))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "🎙️ RECORDING VOICE NOTE (AMR-NB 12.2kbps)... RELEASE TO SEND",
                        fontFamily = TerminalFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonAmber
                    )
                }
            }
        }

        // 3. Quick Action Command Chips & KeyRow
        KeyRow(
            onKeyPress = { key ->
                viewModel.onInputChanged(uiState.currentInput + key)
            },
            onHistoryUp = { /* History up */ },
            onHistoryDown = { /* History down */ },
            onExecuteCommand = { cmd ->
                if (cmd == "/sos") {
                    onNavigateToEmergency()
                } else {
                    viewModel.onCommand(cmd)
                }
            }
        )

        // 4. Futuristic Input Console with Push-To-Talk Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CyberBgInput)
                .border(BorderStroke(0.5.dp, NeonGreen.copy(alpha = 0.4f)))
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "❯ ",
                color = NeonGreen,
                fontWeight = FontWeight.Bold,
                style = TerminalTypography.bodyMedium
            )

            BasicTextField(
                value = uiState.currentInput,
                onValueChange = { viewModel.onInputChanged(it) },
                textStyle = TerminalTypography.bodyMedium.copy(color = TextPrimary),
                cursorBrush = SolidColor(NeonGreen),
                keyboardOptions = KeyboardOptions(
                    autoCorrect = false,
                    keyboardType = KeyboardType.Ascii,
                    imeAction = ImeAction.Send
                ),
                keyboardActions = KeyboardActions(
                    onSend = {
                        val input = uiState.currentInput
                        if (input == "/sos") {
                            viewModel.onInputChanged("")
                            onNavigateToEmergency()
                        } else {
                            viewModel.onCommand(input)
                        }
                    }
                ),
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 4.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.width(6.dp))

            // Push-to-Talk (PTT) Walkie-Talkie Button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isVoiceRecording) NeonAmber else CyberBgCard)
                    .border(BorderStroke(1.dp, if (isVoiceRecording) NeonAmber else NeonCyan), RoundedCornerShape(6.dp))
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                isVoiceRecording = true
                                viewModel.startVoice()
                                tryAwaitRelease()
                                isVoiceRecording = false
                                viewModel.stopVoice()
                            }
                        )
                    }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isVoiceRecording) "🔴 PTT" else "📻 PTT",
                    fontFamily = TerminalFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = if (isVoiceRecording) CyberBg else NeonCyan
                )
            }
        }
    }
}
