package com.alertmesh.ui.theme

import androidx.compose.ui.graphics.Color

// ─── Ultra-Minimal Premium Dark Palette ─────────────────────
// Inspired by Vercel / Linear / Raycast

// Background Layers (near-black with subtle blue undertone)
val BgPrimary   = Color(0xFFF4F6F5)
val BgElevated  = Color(0xFFFFFFFF)
val BgSurface   = Color(0xFFE9EEEC)
val BgHover     = Color(0xFFDDE5E1)

// Borders (extremely subtle)
val BorderDefault = Color(0xFFD6E0DA)
val BorderSubtle  = Color(0xFFE4EAE6)

// Text
val TextPrimary   = Color(0xFF192720)
val TextSecondary = Color(0xFF52625A)
val TextDim       = Color(0xFF586B60)

// Accent Colors (muted, sophisticated)
val AccentGreen  = Color(0xFF087C58)
val AccentCyan   = Color(0xFF126D76)
val AccentAmber  = Color(0xFF9A620D)
val AccentRed    = Color(0xFFCF414A)
val AccentViolet = Color(0xFF8B5CF6)   // Crypto / security badges

// Glow variants (very subtle, for backgrounds only)
val GlowGreen  = Color(0x1A087C58)
val GlowCyan   = Color(0x1A126D76)
val GlowAmber  = Color(0x1A9A620D)
val GlowRed    = Color(0x1ACF414A)
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
