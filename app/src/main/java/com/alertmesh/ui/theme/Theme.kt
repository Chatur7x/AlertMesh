package com.alertmesh.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView

private val DarkColorScheme = darkColorScheme(
    primary = NeonGreen,
    secondary = NeonCyan,
    tertiary = NeonViolet,
    background = CyberBg,
    surface = CyberBgCard,
    surfaceVariant = CyberBgSurface,
    onPrimary = CyberBg,
    onSecondary = CyberBg,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onError = TextPrimary,
    error = NeonRed
)

@Composable
fun AlertMeshTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = CyberBg.toArgb()
            window.navigationBarColor = CyberBg.toArgb()
        }
    }

    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = TerminalTypography,
        content = content
    )
}
