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
import androidx.compose.ui.unit.dp
import com.alertmesh.ui.theme.TerminalBgLight
import com.alertmesh.ui.theme.TerminalGreen
import com.alertmesh.ui.theme.TerminalTypography

@Composable
fun KeyRow(
    onKeyPress: (String) -> Unit,
    onHistoryUp: () -> Unit,
    onHistoryDown: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val keys = listOf("Tab", "/", "↑", "↓", "Esc", "Ctrl", "|", "-", "@")

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(TerminalBgLight)
            .horizontalScroll(scrollState)
            .padding(horizontal = 4.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
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
private fun KeyButton(text: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .sizeIn(minWidth = 36.dp, minHeight = 32.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(4.dp),
        color = TerminalBgLight,
        border = BorderStroke(1.dp, TerminalGreen)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
            Text(
                text = text,
                color = TerminalGreen,
                style = TerminalTypography.bodyMedium
            )
        }
    }
}
