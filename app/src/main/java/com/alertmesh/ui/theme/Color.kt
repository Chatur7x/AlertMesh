package com.alertmesh.ui.theme

import androidx.compose.ui.graphics.Color

// Palette sampled from the supplied beacon and mesh artwork.
val BgPrimary   = Color(0xFF07111B)
val BgElevated  = Color(0xFF101F2C)
val BgSurface   = Color(0xFF172B3A)
val BgHover     = Color(0xFF203B4B)

val BorderDefault = Color(0xFF31505F)
val BorderSubtle  = Color(0xFF244151)

val TextPrimary   = Color(0xFFF4FAFB)
val TextSecondary = Color(0xFFC1D5DC)
val TextDim       = Color(0xFFA4BDC7)

val AccentGreen  = Color(0xFF62DDB8)
val AccentCyan   = Color(0xFF69E5EF)
val AccentAmber  = Color(0xFFFFCE80)
val AccentRed    = Color(0xFFFF7469)
val AccentViolet = Color(0xFF8B5CF6)   // Crypto / security badges

// Glow variants (very subtle, for backgrounds only)
val GlowGreen  = Color(0x2662DDB8)
val GlowCyan   = Color(0x2669E5EF)
val GlowAmber  = Color(0x26FFCE80)
val GlowRed    = Color(0x26FF7469)
val GlowViolet = Color(0x1A8B5CF6)

// ─── Legacy Aliases (backward compat) ───────────────────────
val TerminalGreen = AccentGreen
val TerminalAmber = AccentAmber
val TerminalRed   = AccentRed
val TerminalCyan  = AccentCyan
val TerminalDim   = TextDim
val TerminalBg    = BgPrimary
val TerminalBgLight = BgElevated
val TerminalWhite = TextPrimary
val TerminalBlue  = AccentCyan
val SosRed = AccentRed

val AppleBgDark = BgPrimary
val AppleWindowBg = BgElevated
val AppleCardBg = BgElevated
val AppleInputBg = BgSurface
val AppleCloseRed = Color(0xFFFF5F56)
val AppleMinimizeYellow = Color(0xFFFFBD2E)
val AppleMaximizeGreen = Color(0xFF27C93F)
val AppleCyan = AccentCyan
val AppleGreen = AccentGreen
val AppleYellow = AccentAmber
val AppleRed = AccentRed
val AppleRedGlow = GlowRed
val ApplePurple = AccentViolet
val AppleBlue = AccentCyan

val CyberBg = BgPrimary
val CyberBgCard = BgElevated
val CyberBgSurface = BgSurface
val CyberBgInput = BgSurface
val NeonGreen = AccentGreen
val NeonGreenGlow = GlowGreen
val NeonCyan = AccentCyan
val NeonCyanGlow = GlowCyan
val NeonAmber = AccentAmber
val NeonAmberGlow = GlowAmber
val NeonRed = AccentRed
val NeonRedGlow = GlowRed
val NeonViolet = AccentViolet
