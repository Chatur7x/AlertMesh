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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alertmesh.ui.theme.CyberBgCard
import com.alertmesh.ui.theme.CyberBgSurface
import com.alertmesh.ui.theme.NeonCyan
import com.alertmesh.ui.theme.NeonGreen
import com.alertmesh.ui.theme.NeonGreenGlow
import com.alertmesh.ui.theme.NeonRed
import com.alertmesh.ui.theme.NeonRedGlow
import com.alertmesh.ui.theme.NeonViolet
import com.alertmesh.ui.theme.TerminalFontFamily
import com.alertmesh.ui.theme.TextPrimary
import com.alertmesh.ui.theme.TextSecondary

@Composable
fun TerminalHeader(
    peerCount: Int,
    currentChannel: Int,
    isConnected: Boolean,
    onNavigateToEmergency: () -> Unit
) {
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
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sosPulse"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CyberBgCard)
            .border(
                BorderStroke(
                    1.dp,
                    Brush.horizontalGradient(
                        colors = listOf(NeonGreenGlow, NeonCyan, NeonViolet, NeonGreenGlow)
                    )
                )
            )
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App Title & Tactical Status
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Pulsing Online Dot
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .scale(dotAlpha)
                        .clip(CircleShape)
                        .background(if (isConnected) NeonGreen else NeonRed)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "ALERTMESH // TACTICAL NODE",
                        fontFamily = TerminalFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = if (isConnected) "P2P MESH ACTIVE • CH-$currentChannel" else "OFFLINE • STANDBY",
                        fontFamily = TerminalFontFamily,
                        fontSize = 10.sp,
                        color = if (isConnected) NeonGreen else NeonRed
                    )
                }
            }

            // Quick Emergency Trigger Button with Glow
            Box(
                modifier = Modifier
                    .scale(sosPulse)
                    .clip(RoundedCornerShape(6.dp))
                    .background(NeonRedGlow)
                    .border(BorderStroke(1.dp, NeonRed), RoundedCornerShape(6.dp))
                    .clickable { onNavigateToEmergency() }
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🚨 SOS TRIAGE",
                    fontFamily = TerminalFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = NeonRed
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // System Pill Badges Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Peers Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(CyberBgSurface)
                    .border(BorderStroke(0.5.dp, NeonCyan), RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "PEERS: $peerCount",
                    fontFamily = TerminalFontFamily,
                    fontSize = 10.sp,
                    color = NeonCyan
                )
            }

            // Crypto Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(CyberBgSurface)
                    .border(BorderStroke(0.5.dp, NeonViolet), RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "TINK: Ed25519/AES-GCM",
                    fontFamily = TerminalFontFamily,
                    fontSize = 10.sp,
                    color = NeonViolet
                )
            }

            // Routing Mode Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(CyberBgSurface)
                    .border(BorderStroke(0.5.dp, NeonGreen), RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "TTL: 8 • DEDUP: OK",
                    fontFamily = TerminalFontFamily,
                    fontSize = 10.sp,
                    color = NeonGreen
                )
            }
        }
    }
}
