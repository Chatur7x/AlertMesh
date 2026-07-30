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
        withStyle(style = SpanStyle(color = AppleGreen, fontWeight = FontWeight.Bold)) {
            append("[ OK ]")
        }
        append(" ")
        withStyle(style = SpanStyle(color = TextPrimary)) {
            append(text)
        }
    }
}

fun warnLine(text: String): AnnotatedString {
    return buildAnnotatedString {
        withStyle(style = SpanStyle(color = AppleYellow, fontWeight = FontWeight.Bold)) {
            append("[ WARN ]")
        }
        append(" ")
        withStyle(style = SpanStyle(color = AppleYellow)) {
            append(text)
        }
    }
}

fun errLine(text: String): AnnotatedString {
    return buildAnnotatedString {
        withStyle(style = SpanStyle(color = AppleRed, fontWeight = FontWeight.Bold)) {
            append("[ ERR ]")
        }
        append(" ")
        withStyle(style = SpanStyle(color = AppleRed)) {
            append(text)
        }
    }
}

fun infoLine(text: String): AnnotatedString {
    return buildAnnotatedString {
        withStyle(style = SpanStyle(color = AppleCyan, fontWeight = FontWeight.SemiBold)) {
            append(text)
        }
    }
}

fun messageLine(from: String, text: String, hops: Int): AnnotatedString {
    return buildAnnotatedString {
        withStyle(style = SpanStyle(color = AppleCyan, fontWeight = FontWeight.Bold)) {
            append("<$from>")
        }
        append(" ")
        withStyle(style = SpanStyle(color = TextPrimary)) {
            append(text)
        }
        append(" ")
        withStyle(style = SpanStyle(color = TextDim)) {
            append("[hops:$hops]")
        }
    }
}

fun sosLine(from: String, condition: String): AnnotatedString {
    return buildAnnotatedString {
        withStyle(style = SpanStyle(color = AppleRed, fontWeight = FontWeight.Bold)) {
            append("[ 🚨 SOS ALARM ]")
        }
        append(" ")
        withStyle(style = SpanStyle(color = AppleRed, fontWeight = FontWeight.SemiBold)) {
            append("NODE <$from> REPORTING: $condition")
        }
    }
}

fun commandEcho(input: String): AnnotatedString {
    return buildAnnotatedString {
        withStyle(style = SpanStyle(color = AppleGreen, fontWeight = FontWeight.Bold)) {
            append("node@alertmesh % ")
        }
        withStyle(style = SpanStyle(color = AppleCyan)) {
            append(input)
        }
    }
}
