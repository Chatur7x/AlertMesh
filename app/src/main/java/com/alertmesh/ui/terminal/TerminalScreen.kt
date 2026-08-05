package com.alertmesh.ui.terminal

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alertmesh.ui.theme.*

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

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(BgPrimary)
            .windowInsetsPadding(WindowInsets.systemBars)
    ) {
        val isCompact = maxWidth < 360.dp
        val fontSize = if (isCompact) 12.sp else if (maxWidth > 600.dp) 15.sp else 13.sp
        val hPad = if (isCompact) 12.dp else if (maxWidth > 600.dp) 24.dp else 16.dp

        Column(modifier = Modifier.fillMaxSize()) {

            // ─── Status Bar ─────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BgPrimary)
                    .padding(horizontal = hPad, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Online dot
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (uiState.isConnected) AccentGreen else AccentRed)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "AlertMesh",
                        color = TextPrimary,
                        fontFamily = TerminalFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "${uiState.peerCount} peers",
                        color = TextDim,
                        fontFamily = TerminalFontFamily,
                        fontSize = 12.sp
                    )
                }

                // SOS button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(GlowRed)
                        .pointerInput(Unit) { detectTapGestures { onNavigateToEmergency() } }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "SOS",
                        color = AccentRed,
                        fontFamily = TerminalFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            // Subtle divider
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(BorderSubtle))

            // ─── Log Area ───────────────────────────────────────
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                items(uiState.lines) { line ->
                    val accentColor = when (line.lineType) {
                        "ok" -> AccentGreen
                        "warn" -> AccentAmber
                        "err" -> AccentRed
                        "msg" -> AccentCyan
                        else -> AccentCyan // info
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .drawBehind {
                                // Left accent bar
                                drawRect(
                                    color = accentColor,
                                    topLeft = Offset(0f, 0f),
                                    size = Size(width = 3.dp.toPx(), height = size.height)
                                )
                            }
                            .padding(start = hPad, end = hPad, top = 6.dp, bottom = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = line.text,
                            fontFamily = TerminalFontFamily,
                            fontSize = fontSize,
                            lineHeight = fontSize * 1.5f
                        )
                    }
                }
            }

            // ─── Voice Indicator ────────────────────────────────
            AnimatedVisibility(visible = isVoiceRecording, enter = fadeIn(), exit = fadeOut()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BgElevated)
                        .padding(horizontal = hPad, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Recording voice note — release to send",
                        fontFamily = TerminalFontFamily,
                        fontSize = 12.sp,
                        color = AccentAmber
                    )
                }
            }

            // ─── Quick Commands ─────────────────────────────────
            KeyRow(
                onKeyPress = { key -> viewModel.onInputChanged(uiState.currentInput + key) },
                onHistoryUp = { viewModel.onHistoryUp() },
                onHistoryDown = { viewModel.onHistoryDown() },
                onExecuteCommand = { cmd ->
                    if (cmd == "/sos") onNavigateToEmergency() else viewModel.onCommand(cmd)
                }
            )

            // Subtle divider
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(BorderSubtle))

            // ─── Input Console ──────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BgPrimary)
                    .padding(horizontal = hPad, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "›",
                    color = AccentGreen,
                    fontFamily = TerminalFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.width(8.dp))

                BasicTextField(
                    value = uiState.currentInput,
                    onValueChange = { viewModel.onInputChanged(it) },
                    textStyle = TerminalTypography.bodyMedium.copy(
                        color = TextPrimary,
                        fontSize = fontSize
                    ),
                    cursorBrush = SolidColor(AccentGreen),
                    keyboardOptions = KeyboardOptions(
                        autoCorrectEnabled = false,
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
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )

                Spacer(modifier = Modifier.width(8.dp))

                // PTT Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isVoiceRecording) AccentAmber else BgSurface)
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
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isVoiceRecording) "●" else "PTT",
                        fontFamily = TerminalFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = if (isVoiceRecording) BgPrimary else TextSecondary
                    )
                }
            }
        }
    }
}
