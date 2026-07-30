package com.alertmesh.ui.emergency

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alertmesh.ui.theme.CyberBg
import com.alertmesh.ui.theme.CyberBgCard
import com.alertmesh.ui.theme.NeonAmber
import com.alertmesh.ui.theme.NeonCyan
import com.alertmesh.ui.theme.NeonGreen
import com.alertmesh.ui.theme.NeonRed
import com.alertmesh.ui.theme.NeonRedGlow
import com.alertmesh.ui.theme.TerminalFontFamily
import com.alertmesh.ui.theme.TextPrimary
import com.alertmesh.ui.theme.TextSecondary

@Composable
fun EmergencyScreen(
    viewModel: EmergencyViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    val infiniteTransition = rememberInfiniteTransition(label = "radarPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    val ringAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ringAlpha"
    )

    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBg)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        if (uiState.isAlarmActive) {
            // ALARM ACTIVE HUD DISPLAY
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
            ) {
                // Radar Pulse Visual Ring
                Box(contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .background(NeonRedGlow.copy(alpha = ringAlpha))
                    )
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .clip(CircleShape)
                            .background(NeonRed),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🚨",
                            fontSize = 42.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "CRITICAL EMERGENCY ALARM",
                    color = NeonRed,
                    fontFamily = TerminalFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    modifier = Modifier.scale(pulseScale)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Active Emergency HUD Card
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CyberBgCard)
                        .border(BorderStroke(1.5.dp, NeonRed), RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    uiState.incomingSos?.let { sos ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("SENDER:", color = TextSecondary, fontFamily = TerminalFontFamily, fontSize = 11.sp)
                            Text(uiState.senderFingerprint, color = NeonCyan, fontFamily = TerminalFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("TRIAGE STATUS:", color = TextSecondary, fontFamily = TerminalFontFamily, fontSize = 11.sp)
                            Text(sos.condition.name, color = NeonRed, fontFamily = TerminalFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("PEOPLE / INJURED:", color = TextSecondary, fontFamily = TerminalFontFamily, fontSize = 11.sp)
                            Text("${sos.peopleCount} Total (${sos.injuredCount} Injured)", color = TextPrimary, fontFamily = TerminalFontFamily, fontSize = 12.sp)
                        }

                        if (sos.note.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("NOTE:", color = TextSecondary, fontFamily = TerminalFontFamily, fontSize = 11.sp)
                            Text(sos.note, color = NeonAmber, fontFamily = TerminalFontFamily, fontSize = 12.sp)
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text("GPS FIX:", color = TextSecondary, fontFamily = TerminalFontFamily, fontSize = 11.sp)
                        Text(
                            text = if (sos.latitude != null && sos.latitude != 0.0)
                                "${sos.latitude}, ${sos.longitude} (±${sos.accuracy ?: 15.0}m)"
                            else
                                "WARM FIX ACQUIRING (No direct satellites)",
                            color = NeonGreen,
                            fontFamily = TerminalFontFamily,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { viewModel.dismissAlarm() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonRed)
                ) {
                    Text("ACKNOWLEDGE & DISMISS ALARM", color = Color.White, fontFamily = TerminalFontFamily, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, TextSecondary)
                ) {
                    Text("RETURN TO TERMINAL CONSOLE", color = TextPrimary, fontFamily = TerminalFontFamily)
                }
            }
        } else {
            // TRIGGER SOS STANDBY HUD
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
            ) {
                // Tactical SOS Button with Radar Ring
                Box(contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(170.dp)
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .background(NeonRedGlow.copy(alpha = ringAlpha))
                    )
                    Button(
                        onClick = { /* Form submission below handles payload trigger */ },
                        shape = CircleShape,
                        modifier = Modifier.size(130.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonRed)
                    ) {
                        Text(
                            text = "SOS",
                            fontFamily = TerminalFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                TriageForm(
                    onSubmit = { payload ->
                        viewModel.triggerSos(payload)
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, TextSecondary)
                ) {
                    Text("CANCEL & RETURN TO TERMINAL", color = TextPrimary, fontFamily = TerminalFontFamily)
                }
            }
        }
    }
}
