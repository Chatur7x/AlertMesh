package com.alertmesh.ui.terminal.components

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import com.alertmesh.ui.theme.AccentAmber
import com.alertmesh.ui.theme.AccentCyan
import com.alertmesh.ui.theme.AccentGreen
import com.alertmesh.ui.theme.AccentRed
import com.alertmesh.ui.theme.TextDim
import com.alertmesh.ui.theme.TextPrimary
import com.alertmesh.ui.theme.TextSecondary

fun okLine(text: String): AnnotatedString = buildAnnotatedString {
    withStyle(style = SpanStyle(color = AccentGreen)) {
        append("✓ ")
    }
    withStyle(style = SpanStyle(color = TextPrimary)) {
        append(text)
    }
}

fun warnLine(text: String): AnnotatedString = buildAnnotatedString {
    withStyle(style = SpanStyle(color = AccentAmber)) {
        append("⚠ ")
    }
    withStyle(style = SpanStyle(color = TextPrimary)) {
        append(text)
    }
}

fun errLine(text: String): AnnotatedString = buildAnnotatedString {
    withStyle(style = SpanStyle(color = AccentRed)) {
        append("✗ ")
    }
    withStyle(style = SpanStyle(color = TextPrimary)) {
        append(text)
    }
}

fun infoLine(text: String): AnnotatedString = buildAnnotatedString {
    withStyle(style = SpanStyle(color = TextSecondary)) {
        append("i ")
    }
    withStyle(style = SpanStyle(color = TextPrimary)) {
        append(text)
    }
}

fun messageLine(from: String, text: String, hops: Int): AnnotatedString = buildAnnotatedString {
    withStyle(style = SpanStyle(color = AccentCyan)) {
        append("[$from] ")
    }
    withStyle(style = SpanStyle(color = TextPrimary)) {
        append(text)
    }
    withStyle(style = SpanStyle(color = TextDim)) {
        append(" ($hops hops)")
    }
}

fun sosLine(from: String, condition: String): AnnotatedString = buildAnnotatedString {
    withStyle(style = SpanStyle(color = AccentRed)) {
        append("[SOS] ")
    }
    withStyle(style = SpanStyle(color = TextPrimary)) {
        append("From $from: $condition")
    }
}

fun commandEcho(input: String): AnnotatedString = buildAnnotatedString {
    withStyle(style = SpanStyle(color = AccentGreen)) {
        append("› ")
    }
    withStyle(style = SpanStyle(color = TextPrimary)) {
        append(input)
    }
}
