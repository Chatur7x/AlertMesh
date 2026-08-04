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
                append("AlertMesh v0.1.0-rc1 . build 2026.07.30")
            }
        })
        lines.add(okLine("Keystore unlocked"))
        lines.add(okLine("Identity: $fingerprint"))
        lines.add(okLine("Transport: $transport"))
        lines.add(okLine("Router online ttl=8 dedup=5000 suppress=2"))
        lines.add(okLine("Store: $messageCount messages . $peerCount peers known"))
        
        if (!hasBarometer) {
            lines.add(warnLine("Barometer absent - height reporting disabled"))
        }
        
        if (!hasLocation) {
            lines.add(warnLine("Location services OFF - run /fix"))
        }
        
        lines.add(okLine("Listening."))
        lines.add(buildAnnotatedString {
            withStyle(style = SpanStyle(color = AccentGreen)) {
                append("type /help for commands")
            }
        })
        
        return lines
    }
}
