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
import com.alertmesh.ui.theme.AppleBlue
import com.alertmesh.ui.theme.AppleCardBg
import com.alertmesh.ui.theme.AppleCyan
import com.alertmesh.ui.theme.AppleGreen
import com.alertmesh.ui.theme.AppleRed
import com.alertmesh.ui.theme.AppleWindowBg
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

    // macOS TouchBar styled action chips
    val commands = listOf(
        "/peers" to AppleCyan,
        "/ping" to AppleCyan,
        "/trace" to AppleCyan,
        "/msg" to AppleCyan,
        "/sos" to AppleRed,
        "/fix" to AppleGreen,
        "/metrics" to AppleGreen,
        "/clear" to AppleGreen
    )

    val keys = listOf("Tab", "/", "↑", "↓", "|", "-", "@")

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(AppleWindowBg)
            .horizontalScroll(scrollState)
            .padding(horizontal = 6.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Render TouchBar Command Chips
        for ((cmd, color) in commands) {
            TouchBarChip(
                text = cmd,
                accentColor = color,
                onClick = { onExecuteCommand(cmd) }
            )
        }

        // TouchBar Separator Line
        Box(
            modifier = Modifier
                .padding(horizontal = 2.dp)
                .sizeIn(minWidth = 1.dp, minHeight = 22.dp)
                .background(Color(0xFF383842))
        )

        // Render Special Keys
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
private fun TouchBarChip(text: String, accentColor: Color, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(6.dp),
        color = AppleCardBg,
        border = BorderStroke(0.8.dp, accentColor)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)) {
            Text(
                text = text,
                color = accentColor,
                fontFamily = TerminalFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun KeyButton(text: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .sizeIn(minWidth = 32.dp, minHeight = 28.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(6.dp),
        color = AppleCardBg,
        border = BorderStroke(0.5.dp, Color(0xFF484856))
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
            Text(
                text = text,
                color = TextPrimary,
                fontFamily = TerminalFontFamily,
                fontSize = 11.sp
            )
        }
    }
}
