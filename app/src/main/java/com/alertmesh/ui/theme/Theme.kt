package com.alertmesh.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = AccentCyan,
    secondary = AccentCyan,
    tertiary = AccentAmber,
    background = BgPrimary,
    surface = BgSurface,
    onPrimary = BgPrimary,
    onSecondary = BgPrimary,
    onTertiary = BgPrimary,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    error = AccentRed,
    onError = BgPrimary
)

@Composable
fun AlertMeshTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            @Suppress("DEPRECATION")
            window.statusBarColor = Color.Transparent.value.toInt()
            @Suppress("DEPRECATION")
            window.navigationBarColor = Color.Transparent.value.toInt()
            WindowCompat.setDecorFitsSystemWindows(window, false)
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = TerminalTypography,
        content = content
    )
}
