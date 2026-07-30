package com.alertmesh.ui.terminal

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alertmesh.ui.theme.CyberBgCard
import com.alertmesh.ui.theme.CyberBgSurface
import com.alertmesh.ui.theme.NeonCyan
import com.alertmesh.ui.theme.NeonGreen
import com.alertmesh.ui.theme.NeonRed
import com.alertmesh.ui.theme.TerminalFontFamily
import com.alertmesh.ui.theme.TextPrimary

@Composable
fun KeyRow(
    onKeyPress: (String) -> Unit,
    onHistoryUp: () -> Unit,
    onHistoryDown: () -> Unit,
    onExecuteCommand: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    
    // Quick action slash commands & special keys
    val commands = listOf(
        "/peers" to NeonCyan,
        "/ping" to NeonCyan,
        "/trace" to NeonCyan,
        "/msg" to NeonCyan,
        "/sos" to NeonRed,
        "/fix" to NeonGreen,
        "/metrics" to NeonGreen,
        "/clear" to NeonGreen
    )

    val keys = listOf("Tab", "/", "↑", "↓", "|", "-", "@")

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(CyberBgCard)
            .horizontalScroll(scrollState)
            .padding(horizontal = 6.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Render command chips
        for ((cmd, color) in commands) {
            CommandChip(
                text = cmd,
                accentColor = color,
                onClick = { onExecuteCommand(cmd) }
            )
        }

        // Divider spacer
        Box(
            modifier = Modifier
                .padding(horizontal = 2.dp)
                .sizeIn(minWidth = 1.dp, minHeight = 24.dp)
                .background(Color(0xFF2A364F))
        )

        // Render special keys
        for (key in keys) {
            KeyButton(
                text = key,
                onClick = {
                    when (key) {
                        "↑" -> onHistoryUp()
                        "↓" -> onHistoryDown()
                        else -> onKeyPress(key)
                    }
                }
            )
        }
    }
}

@Composable
private fun CommandChip(text: String, accentColor: Color, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(6.dp),
        color = CyberBgSurface,
        border = BorderStroke(1.dp, accentColor)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)) {
            Text(
                text = text,
                color = accentColor,
                fontFamily = TerminalFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun KeyButton(text: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .sizeIn(minWidth = 34.dp, minHeight = 30.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(6.dp),
        color = CyberBgCard,
        border = BorderStroke(0.8.dp, NeonGreen)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
            Text(
                text = text,
                color = TextPrimary,
                fontFamily = TerminalFontFamily,
                fontSize = 12.sp
            )
        }
    }
}
