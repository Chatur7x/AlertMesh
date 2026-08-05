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
import com.alertmesh.ui.theme.AccentAmber
import com.alertmesh.ui.theme.AccentCyan
import com.alertmesh.ui.theme.AccentGreen
import com.alertmesh.ui.theme.AccentRed
import com.alertmesh.ui.theme.BgPrimary
import com.alertmesh.ui.theme.BgSurface
import com.alertmesh.ui.theme.BorderSubtle
import com.alertmesh.ui.theme.TerminalFontFamily

@Composable
fun KeyRow(
    onKeyPress: (String) -> Unit,
    onHistoryUp: () -> Unit,
    onHistoryDown: () -> Unit,
    onExecuteCommand: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    val commands = listOf(
        "/peers" to AccentCyan,
        "/status" to AccentCyan,
        "/ping" to AccentCyan,
        "/fix" to AccentGreen,
        "/send" to AccentCyan,
        "/sos" to AccentRed,
        "/help" to AccentAmber,
        "/clear" to AccentGreen
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(BgPrimary)
            .horizontalScroll(scrollState)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CommandChip(text = "▲", accentColor = AccentGreen, onClick = onHistoryUp)
        CommandChip(text = "▼", accentColor = AccentGreen, onClick = onHistoryDown)

        for ((cmd, color) in commands) {
            CommandChip(
                text = cmd,
                accentColor = color,
                onClick = { onExecuteCommand(cmd) }
            )
        }
    }
}

@Composable
private fun CommandChip(text: String, accentColor: Color, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = BgSurface,
        border = BorderStroke(1.dp, BorderSubtle)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
            Text(
                text = text,
                color = accentColor,
                fontFamily = TerminalFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp
            )
        }
    }
}
