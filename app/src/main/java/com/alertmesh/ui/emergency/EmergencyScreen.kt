package com.alertmesh.ui.emergency

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.alertmesh.protocol.model.SosPayload
import com.alertmesh.ui.theme.SosRed
import com.alertmesh.ui.theme.TerminalBg
import com.alertmesh.ui.theme.TerminalTypography
import com.alertmesh.ui.theme.TerminalWhite

@Composable
fun EmergencyScreen(
    viewModel: EmergencyViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val bgColor = if (uiState.isAlarmActive) SosRed.copy(alpha = 0.2f) else TerminalBg

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        if (uiState.isAlarmActive) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "EMERGENCY ACTIVE",
                    color = SosRed,
                    style = TerminalTypography.titleLarge,
                    modifier = Modifier.scale(pulseScale)
                )
                Spacer(modifier = Modifier.height(24.dp))
                uiState.incomingSos?.let { sos ->
                    Text(text = "Sender: ${uiState.senderFingerprint}", color = TerminalWhite)
                    Text(text = "Condition: ${sos.condition}", color = TerminalWhite)
                    Text(text = "People: ${sos.peopleCount} (Injured: ${sos.injuredCount})", color = TerminalWhite)
                    Text(text = "Note: ${sos.note}", color = TerminalWhite)
                }
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = { viewModel.dismissAlarm() },
                    colors = ButtonDefaults.buttonColors(containerColor = TerminalBg)
                ) {
                    Text("DISMISS ALARM", color = TerminalWhite)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onNavigateBack,
                    colors = ButtonDefaults.buttonColors(containerColor = TerminalBg)
                ) {
                    Text("BACK TO TERMINAL", color = TerminalWhite)
                }
            }
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = { /* Handled by sending logic inside form, or simple SOS trigger here */ },
                    shape = CircleShape,
                    modifier = Modifier
                        .size(200.dp)
                        .scale(pulseScale),
                    colors = ButtonDefaults.buttonColors(containerColor = SosRed)
                ) {
                    Text("SOS", style = TerminalTypography.titleLarge.copy(color = Color.White))
                }
                Spacer(modifier = Modifier.height(32.dp))
                TriageForm(
                    onSubmit = { payload ->
                        viewModel.triggerSos(payload)
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onNavigateBack,
                    colors = ButtonDefaults.buttonColors(containerColor = TerminalBg)
                ) {
                    Text("CANCEL", color = TerminalWhite)
                }
            }
        }
    }
}
