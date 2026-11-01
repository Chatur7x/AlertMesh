package com.alertmesh.ui.terminal

import android.animation.ValueAnimator
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alertmesh.R
import com.alertmesh.ui.theme.*

@Composable
fun TerminalScreen(viewModel: TerminalViewModel, onNavigateToEmergency: () -> Unit) {
    val state by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    var recording by remember { mutableStateOf(false) }
    var showStartup by remember { mutableStateOf(false) }
    val visibleLines = if (showStartup) state.lines else state.lines.filter { it.lineType != "boot" }
    val motionEnabled = Build.VERSION.SDK_INT < 26 || ValueAnimator.areAnimatorsEnabled()
    val sosInteraction = remember { MutableInteractionSource() }
    val sosPressed by sosInteraction.collectIsPressedAsState()
    val sosScale by animateFloatAsState(
        targetValue = if (sosPressed) 0.985f else 1f,
        animationSpec = if (motionEnabled) tween(150) else snap(), label = "SOS press"
    )
    val peerColor by animateColorAsState(
        targetValue = if (state.peerCount > 0) AccentGreen else AccentAmber,
        animationSpec = if (motionEnabled) tween(300) else snap(), label = "Mesh state"
    )
    LaunchedEffect(visibleLines.size, showStartup) {
        if (showStartup) listState.scrollToItem(0)
        else if (visibleLines.isNotEmpty()) listState.animateScrollToItem(visibleLines.lastIndex)
    }

    Column(Modifier.fillMaxSize().background(BgPrimary).windowInsetsPadding(WindowInsets.systemBars)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Image(painterResource(R.drawable.ic_alertmesh_mark), null, Modifier.size(46.dp))
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text("AlertMesh", color = TextPrimary, fontFamily = AppFontFamily, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                Text("OFFLINE RESPONSE NETWORK", color = TextDim, fontFamily = AppFontFamily, fontSize = 11.sp, letterSpacing = 0.7.sp)
            }
            Text("CH ${state.currentChannel}", color = AccentCyan, fontFamily = TerminalFontFamily, fontSize = 12.sp,
                modifier = Modifier.heightIn(min = 48.dp).clip(RoundedCornerShape(12.dp)).background(BgSurface)
                    .clickable { viewModel.onCommand("/channel ${state.currentChannel % 8 + 1}") }
                    .padding(horizontal = 12.dp, vertical = 15.dp)
                    .semantics { contentDescription = "Channel ${state.currentChannel}. Tap to change channel" })
        }

        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
            Row(Modifier.fillMaxWidth().animateContentSize(if (motionEnabled) tween(260) else snap())
                .clip(RoundedCornerShape(18.dp)).background(BgElevated)
                .border(1.dp, BorderDefault, RoundedCornerShape(18.dp)).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(12.dp).clip(CircleShape).background(peerColor))
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(if (state.peerCount > 0) "Mesh connected" else "No peers connected",
                        color = TextPrimary, fontFamily = AppFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    Text(if (state.peerCount > 0) "${state.peerCount} peer(s) · delivery unconfirmed"
                        else "Nearby delivery unavailable · check radios and permissions",
                        color = TextSecondary, fontFamily = AppFontFamily, fontSize = 12.sp)
                }
            }
            Spacer(Modifier.height(14.dp))
            Button(onClick = onNavigateToEmergency,
                modifier = Modifier.fillMaxWidth().height(58.dp).graphicsLayer(scaleX = sosScale, scaleY = sosScale),
                shape = RoundedCornerShape(16.dp), interactionSource = sosInteraction,
                colors = ButtonDefaults.buttonColors(containerColor = AccentRed)) {
                Text("Send SOS", color = BgPrimary, fontFamily = AppFontFamily, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Peers" to "/peers", "Status" to "/status", "Help" to "/help").forEach { (label, cmd) ->
                    Text(label, color = AccentCyan, fontFamily = AppFontFamily, fontSize = 14.sp, textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp)
                            .clickable { viewModel.onCommand(cmd) }.padding(vertical = 14.dp))
                }
            }
        }

        Row(Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Text("Activity", color = TextPrimary, fontFamily = AppFontFamily, fontWeight = FontWeight.Bold,
                fontSize = 18.sp, modifier = Modifier.weight(1f))
            Text(if (showStartup) "HIDE STARTUP" else "STARTUP LOG", color = AccentCyan,
                fontFamily = AppFontFamily, fontSize = 12.sp,
                modifier = Modifier.clickable { showStartup = !showStartup }.padding(8.dp))
        }
        LazyColumn(state = listState, modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (visibleLines.isEmpty()) item {
                Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(BgElevated)
                    .padding(20.dp)) {
                    Text("No activity yet", color = TextPrimary, fontFamily = AppFontFamily,
                        fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(6.dp))
                    Text("Messages, voice notes, and alerts will appear here.", color = TextSecondary,
                        fontFamily = AppFontFamily, fontSize = 13.sp)
                }
            }
            items(visibleLines) { line ->
                val accent = when (line.lineType) {
                    "err" -> AccentRed; "warn" -> AccentAmber; "ok" -> AccentGreen; else -> AccentCyan
                }
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(BgElevated).padding(12.dp)) {
                    Box(Modifier.size(6.dp).clip(CircleShape).background(accent))
                    Spacer(Modifier.width(10.dp))
                    Text(line.text, color = TextSecondary, fontFamily = TerminalFontFamily, fontSize = 12.sp, lineHeight = 18.sp)
                }
            }
        }
        if (state.hasVoiceNote) Text("▶  Play latest voice note", color = AccentCyan, fontFamily = AppFontFamily,
            modifier = Modifier.fillMaxWidth().clickable { viewModel.playLatestVoice() }
                .padding(horizontal = 20.dp, vertical = 14.dp), fontSize = 14.sp)
        if (motionEnabled) {
            AnimatedVisibility(recording, enter = fadeIn(tween(180)) + expandVertically(),
                exit = fadeOut(tween(120)) + shrinkVertically()) {
                Text("Recording… release to send", color = AccentAmber, fontFamily = AppFontFamily,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp), fontSize = 13.sp)
            }
        } else if (recording) {
            Text("Recording… release to send", color = AccentAmber, fontFamily = AppFontFamily,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp), fontSize = 13.sp)
        }

        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(20.dp)).background(BgElevated).padding(10.dp),
            verticalAlignment = Alignment.CenterVertically) {
            BasicTextField(value = state.currentInput, onValueChange = viewModel::onInputChanged,
                modifier = Modifier.weight(1f).height(52.dp).clip(RoundedCornerShape(14.dp))
                    .background(BgSurface).padding(horizontal = 14.dp, vertical = 15.dp),
                singleLine = true, textStyle = TerminalTypography.bodyMedium.copy(color = TextPrimary),
                cursorBrush = SolidColor(AccentCyan), keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { submitInput(state.currentInput, viewModel, onNavigateToEmergency) }),
                decorationBox = { inner -> Box {
                    if (state.currentInput.isEmpty()) Text("Message or /command", color = TextDim, fontFamily = AppFontFamily, fontSize = 14.sp)
                    inner()
                } })
            Spacer(Modifier.width(8.dp))
            Text("Send", color = BgPrimary, fontFamily = AppFontFamily, fontWeight = FontWeight.Bold,
                modifier = Modifier.clip(RoundedCornerShape(14.dp)).background(AccentCyan)
                    .clickable { submitInput(state.currentInput, viewModel, onNavigateToEmergency) }
                    .padding(horizontal = 14.dp, vertical = 16.dp))
            Spacer(Modifier.width(8.dp))
            Box(Modifier.size(52.dp).clip(RoundedCornerShape(14.dp)).background(if (recording) AccentAmber else BgSurface)
                .pointerInput(Unit) { detectTapGestures(onPress = {
                    recording = true; viewModel.startVoice(); tryAwaitRelease(); recording = false; viewModel.stopVoice()
                }) }.semantics { contentDescription = "Hold to record a voice note" }, contentAlignment = Alignment.Center) {
                Text("MIC", color = if (recording) BgPrimary else TextPrimary, fontFamily = AppFontFamily,
                    fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

private fun submitInput(input: String, viewModel: TerminalViewModel, onEmergency: () -> Unit) {
    if (input.isBlank()) return
    if (input.trim() == "/sos") { viewModel.onInputChanged(""); onEmergency() }
    else viewModel.onCommand(input)
}
