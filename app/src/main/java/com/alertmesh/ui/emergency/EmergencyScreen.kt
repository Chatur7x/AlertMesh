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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import com.alertmesh.ui.theme.AppleBgDark
import com.alertmesh.ui.theme.AppleCardBg
import com.alertmesh.ui.theme.AppleCloseRed
import com.alertmesh.ui.theme.AppleCyan
import com.alertmesh.ui.theme.AppleGreen
import com.alertmesh.ui.theme.AppleMaximizeGreen
import com.alertmesh.ui.theme.AppleMinimizeYellow
import com.alertmesh.ui.theme.AppleRed
import com.alertmesh.ui.theme.AppleRedGlow
import com.alertmesh.ui.theme.AppleWindowBg
import com.alertmesh.ui.theme.AppleYellow
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
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    val ringAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ringAlpha"
    )

    val scrollState = rememberScrollState()

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(AppleBgDark)
    ) {
        val screenWidth = maxWidth
        val isSmallScreen = screenWidth < 360.dp

        Column(modifier = Modifier.fillMaxSize()) {
            // macOS Window Control Title Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AppleWindowBg)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(11.dp).clip(CircleShape).background(AppleCloseRed))
                Spacer(modifier = Modifier.width(6.dp))
                Box(modifier = Modifier.size(11.dp).clip(CircleShape).background(AppleMinimizeYellow))
                Spacer(modifier = Modifier.width(6.dp))
                Box(modifier = Modifier.size(11.dp).clip(CircleShape).background(AppleMaximizeGreen))
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = " alertmesh — emergency-hud",
                    fontFamily = TerminalFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = TextPrimary
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(if (isSmallScreen) 8.dp else 16.dp),
                contentAlignment = Alignment.Center
            ) {
                if (uiState.isAlarmActive) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(scrollState)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Box(
                                modifier = Modifier
                                    .size(150.dp)
                                    .scale(pulseScale)
                                    .clip(CircleShape)
                                    .background(AppleRedGlow.copy(alpha = ringAlpha))
                            )
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(CircleShape)
                                    .background(AppleRed),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🚨", fontSize = 38.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "CRITICAL EMERGENCY ALARM",
                            color = AppleRed,
                            fontFamily = TerminalFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            modifier = Modifier.scale(pulseScale)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(AppleCardBg)
                                .border(BorderStroke(1.5.dp, AppleRed), RoundedCornerShape(12.dp))
                                .padding(14.dp)
                        ) {
                            uiState.incomingSos?.let { sos ->
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("SENDER:", color = TextSecondary, fontFamily = TerminalFontFamily, fontSize = 11.sp)
                                    Text(uiState.senderFingerprint, color = AppleCyan, fontFamily = TerminalFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("CONDITION:", color = TextSecondary, fontFamily = TerminalFontFamily, fontSize = 11.sp)
                                    Text(sos.condition.name, color = AppleRed, fontFamily = TerminalFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("PEOPLE / INJURED:", color = TextSecondary, fontFamily = TerminalFontFamily, fontSize = 11.sp)
                                    Text("${sos.peopleCount} Total (${sos.injuredCount} Injured)", color = TextPrimary, fontFamily = TerminalFontFamily, fontSize = 12.sp)
                                }
                                if (sos.note.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("NOTE:", color = TextSecondary, fontFamily = TerminalFontFamily, fontSize = 11.sp)
                                    Text(sos.note, color = AppleYellow, fontFamily = TerminalFontFamily, fontSize = 12.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = { viewModel.dismissAlarm() },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AppleRed)
                        ) {
                            Text("ACKNOWLEDGE & DISMISS ALARM", color = Color.White, fontFamily = TerminalFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, TextSecondary)
                        ) {
                            Text("RETURN TO TERMINAL CONSOLE", color = TextPrimary, fontFamily = TerminalFontFamily, fontSize = 12.sp)
                        }
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(scrollState)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Box(
                                modifier = Modifier
                                    .size(160.dp)
                                    .scale(pulseScale)
                                    .clip(CircleShape)
                                    .background(AppleRedGlow.copy(alpha = ringAlpha))
                            )
                            Button(
                                onClick = { /* Form trigger */ },
                                shape = CircleShape,
                                modifier = Modifier.size(120.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = AppleRed)
                            ) {
                                Text("SOS", fontFamily = TerminalFontFamily, fontWeight = FontWeight.Bold, fontSize = 26.sp, color = Color.White)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        TriageForm(
                            onSubmit = { payload ->
                                viewModel.triggerSos(payload)
                            }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, TextSecondary)
                        ) {
                            Text("CANCEL & RETURN TO TERMINAL", color = TextPrimary, fontFamily = TerminalFontFamily, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
