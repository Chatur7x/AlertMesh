package com.alertmesh.ui.terminal.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alertmesh.ui.theme.AppleCloseRed
import com.alertmesh.ui.theme.AppleCyan
import com.alertmesh.ui.theme.AppleGreen
import com.alertmesh.ui.theme.AppleMaximizeGreen
import com.alertmesh.ui.theme.AppleMinimizeYellow
import com.alertmesh.ui.theme.ApplePurple
import com.alertmesh.ui.theme.AppleRed
import com.alertmesh.ui.theme.AppleRedGlow
import com.alertmesh.ui.theme.AppleWindowBg
import com.alertmesh.ui.theme.TerminalFontFamily
import com.alertmesh.ui.theme.TextPrimary
import com.alertmesh.ui.theme.TextSecondary

@Composable
fun AppleTerminalHeader(
    peerCount: Int,
    currentChannel: Int,
    isConnected: Boolean,
    screenWidthDp: Dp,
    onNavigateToEmergency: () -> Unit
) {
    val isCompact = screenWidthDp < 360.dp

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dotAlpha"
    )
    val sosPulse by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sosPulse"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppleWindowBg)
            .border(BorderStroke(0.5.dp, Color(0xFF33333D)))
            .padding(horizontal = if (isCompact) 8.dp else 12.dp, vertical = 8.dp)
    ) {
        // Top macOS Titlebar Row with Traffic Lights
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 🔴 🟡 🟢 Iconic macOS Window Control Buttons
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(11.dp)
                        .clip(CircleShape)
                        .background(AppleCloseRed)
                )
                Box(
                    modifier = Modifier
                        .size(11.dp)
                        .clip(CircleShape)
                        .background(AppleMinimizeYellow)
                )
                Box(
                    modifier = Modifier
                        .size(11.dp)
                        .clip(CircleShape)
                        .background(AppleMaximizeGreen)
                )

                if (!isCompact) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = " alertmesh — -zsh",
                        fontFamily = TerminalFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = TextPrimary
                    )
                }
            }

            // Right Action: SOS Emergency Button
            Box(
                modifier = Modifier
                    .scale(sosPulse)
                    .clip(RoundedCornerShape(6.dp))
                    .background(AppleRedGlow)
                    .border(BorderStroke(1.dp, AppleRed), RoundedCornerShape(6.dp))
                    .clickable { onNavigateToEmergency() }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🚨 SOS TRIAGE",
                    fontFamily = TerminalFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = AppleRed
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // System Status Badges Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Online status dot & text
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF23232C))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .scale(dotAlpha)
                        .clip(CircleShape)
                        .background(if (isConnected) AppleGreen else AppleRed)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isConnected) "ONLINE" else "OFFLINE",
                    fontFamily = TerminalFontFamily,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isConnected) AppleGreen else AppleRed
                )
            }

            // Peers Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF23232C))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "PEERS: $peerCount",
                    fontFamily = TerminalFontFamily,
                    fontSize = 9.sp,
                    color = AppleCyan
                )
            }

            // Channel Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF23232C))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "CH-$currentChannel",
                    fontFamily = TerminalFontFamily,
                    fontSize = 9.sp,
                    color = AppleGreen
                )
            }

            // Security Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF23232C))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "TINK: Ed25519",
                    fontFamily = TerminalFontFamily,
                    fontSize = 9.sp,
                    color = ApplePurple
                )
            }
        }
    }
}
