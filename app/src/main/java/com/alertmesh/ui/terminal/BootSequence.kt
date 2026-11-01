package com.alertmesh.ui.terminal

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import com.alertmesh.ui.terminal.components.okLine
import com.alertmesh.ui.terminal.components.warnLine
import com.alertmesh.ui.theme.AccentGreen

object BootSequence {
    fun generateBootLines(
        fingerprint: String,
        transport: String,
        peerCount: Int,
        messageCount: Int,
        hasBarometer: Boolean,
        hasLocation: Boolean
    ): List<AnnotatedString> {
        val lines = mutableListOf<AnnotatedString>()
        
        lines.add(buildAnnotatedString {
            withStyle(style = SpanStyle(color = AccentGreen, fontWeight = FontWeight.Bold)) {
                append("AlertMesh terminal")
            }
        })
        lines.add(warnLine("Identity keys are not persisted yet"))
        lines.add(okLine("Identity: $fingerprint"))
        lines.add(okLine("Transport: $transport"))
        lines.add(warnLine("Mesh connection status is shown above"))
        lines.add(okLine("Store: $messageCount messages . $peerCount peers known"))
        
        if (!hasBarometer) {
            lines.add(warnLine("Barometer absent - height reporting disabled"))
        }
        
        if (!hasLocation) {
            lines.add(warnLine("No current location fix"))
        }
        
        lines.add(warnLine("Waiting for connected peers"))
        lines.add(buildAnnotatedString {
            withStyle(style = SpanStyle(color = AccentGreen)) {
                append("type /help for commands")
            }
        })
        
        return lines
    }
}
