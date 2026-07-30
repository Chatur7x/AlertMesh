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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alertmesh.ui.terminal.components.AppleTerminalHeader
import com.alertmesh.ui.theme.AppleBgDark
import com.alertmesh.ui.theme.AppleCardBg
import com.alertmesh.ui.theme.AppleCyan
import com.alertmesh.ui.theme.AppleGreen
import com.alertmesh.ui.theme.AppleInputBg
import com.alertmesh.ui.theme.AppleRed
import com.alertmesh.ui.theme.AppleWindowBg
import com.alertmesh.ui.theme.AppleYellow
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

    // Dynamic Screen-Size Auto Scaling Box
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(AppleBgDark)
    ) {
        val screenWidth = maxWidth
        val isSmallScreen = screenWidth < 360.dp
        val isLargeScreen = screenWidth > 600.dp

        // Dynamic responsive sizing calculations
        val fontSize = if (isSmallScreen) 11.sp else if (isLargeScreen) 15.sp else 13.sp
        val lineHeight = if (isSmallScreen) 16.sp else if (isLargeScreen) 22.sp else 18.sp
        val horizontalPadding = if (isSmallScreen) 4.dp else if (isLargeScreen) 12.dp else 8.dp

        Column(modifier = Modifier.fillMaxSize()) {
            // 1. Apple macOS Terminal Window Header Bar (Traffic Lights 🔴 🟡 🟢)
            AppleTerminalHeader(
                peerCount = uiState.peerCount,
                currentChannel = uiState.currentChannel,
                isConnected = uiState.isConnected,
                screenWidthDp = screenWidth,
                onNavigateToEmergency = onNavigateToEmergency
            )

            // 2. Terminal Log Window Body
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                items(uiState.lines) { line ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .background(AppleCardBg.copy(alpha = 0.4f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = line.text,
                            style = TerminalTypography.bodyMedium.copy(
                                fontSize = fontSize,
                                lineHeight = lineHeight
                            )
                        )
                    }
                }
            }

            // 3. Audio Recording TouchBar Indicator
            AnimatedVisibility(
                visible = isVoiceRecording,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AppleWindowBg)
                        .border(BorderStroke(1.dp, AppleYellow))
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🎙️ RECORDING VOICE NOTE (AMR-NB 12.2kbps)... RELEASE TO SEND",
                        fontFamily = TerminalFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppleYellow
                    )
                }
            }

            // 4. macOS TouchBar Quick Action Bar
            KeyRow(
                onKeyPress = { key ->
                    viewModel.onInputChanged(uiState.currentInput + key)
                },
                onHistoryUp = { /* History Up */ },
                onHistoryDown = { /* History Down */ },
                onExecuteCommand = { cmd ->
                    if (cmd == "/sos") {
                        onNavigateToEmergency()
                    } else {
                        viewModel.onCommand(cmd)
                    }
                }
            )

            // 5. Apple zsh Terminal Console Input Line
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AppleInputBg)
                    .border(BorderStroke(0.5.dp, Color(0xFF33333E)))
                    .padding(horizontal = horizontalPadding, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "chatur@alertmesh % ",
                    color = AppleGreen,
                    fontFamily = TerminalFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = fontSize
                )

                BasicTextField(
                    value = uiState.currentInput,
                    onValueChange = { viewModel.onInputChanged(it) },
                    textStyle = TerminalTypography.bodyMedium.copy(
                        color = TextPrimary,
                        fontSize = fontSize
                    ),
                    cursorBrush = SolidColor(AppleGreen),
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
                        .padding(start = 2.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.width(6.dp))

                // Push-To-Talk (PTT) Walkie-Talkie Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isVoiceRecording) AppleYellow else AppleCardBg)
                        .border(BorderStroke(1.dp, if (isVoiceRecording) AppleYellow else AppleCyan), RoundedCornerShape(6.dp))
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
                        fontSize = 11.sp,
                        color = if (isVoiceRecording) AppleInputBg else AppleCyan
                    )
                }
            }
        }
    }
}
