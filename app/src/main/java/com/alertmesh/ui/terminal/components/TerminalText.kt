package com.alertmesh.ui.terminal.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import com.alertmesh.ui.theme.*

fun buildTerminalLine(prefix: String, prefixColor: Color, body: String, bodyColor: Color): AnnotatedString {
    return buildAnnotatedString {
        withStyle(style = SpanStyle(color = prefixColor, fontWeight = FontWeight.Bold)) {
            append(prefix)
        }
        append(" ")
        withStyle(style = SpanStyle(color = bodyColor)) {
            append(body)
        }
    }
}

fun okLine(text: String): AnnotatedString {
    return buildAnnotatedString {
        withStyle(style = SpanStyle(color = TerminalGreen, fontWeight = FontWeight.Bold)) {
            append("[ OK ]")
        }
        append(" ")
        withStyle(style = SpanStyle(color = TerminalWhite)) {
            append(text)
        }
    }
}

fun warnLine(text: String): AnnotatedString {
    return buildAnnotatedString {
        withStyle(style = SpanStyle(color = TerminalAmber, fontWeight = FontWeight.Bold)) {
            append("[ WARN ]")
        }
        append(" ")
        withStyle(style = SpanStyle(color = TerminalAmber)) {
            append(text)
        }
    }
}

fun errLine(text: String): AnnotatedString {
    return buildAnnotatedString {
        withStyle(style = SpanStyle(color = TerminalRed, fontWeight = FontWeight.Bold)) {
            append("[ ERR ]")
        }
        append(" ")
        withStyle(style = SpanStyle(color = TerminalRed)) {
            append(text)
        }
    }
}

fun infoLine(text: String): AnnotatedString {
    return buildAnnotatedString {
        withStyle(style = SpanStyle(color = TerminalCyan)) {
            append(text)
        }
        withStyle(style = SpanStyle(color = TerminalWhite)) {
            append("")
        }
    }
}

fun messageLine(from: String, text: String, hops: Int): AnnotatedString {
    return buildAnnotatedString {
        withStyle(style = SpanStyle(color = TerminalBlue)) {
            append("<$from>")
        }
        append(" ")
        withStyle(style = SpanStyle(color = TerminalWhite)) {
            append(text)
        }
        append(" ")
        withStyle(style = SpanStyle(color = TerminalDim)) {
            append("(hops:$hops)")
        }
    }
}

fun sosLine(from: String, condition: String): AnnotatedString {
    return buildAnnotatedString {
        withStyle(style = SpanStyle(color = TerminalRed, fontWeight = FontWeight.Bold)) {
            append("[ SOS ]")
        }
        append(" ")
        withStyle(style = SpanStyle(color = TerminalRed)) {
            append("FROM: $from - $condition")
        }
    }
}

fun commandEcho(input: String): AnnotatedString {
    return buildAnnotatedString {
        withStyle(style = SpanStyle(color = TerminalBlue)) {
            append("> ")
            append(input)
        }
    }
}
