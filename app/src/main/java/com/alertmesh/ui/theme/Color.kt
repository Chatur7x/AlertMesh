package com.alertmesh.ui.theme

import androidx.compose.ui.graphics.Color

// ─── Ultra-Minimal Premium Dark Palette ─────────────────────
// Inspired by Vercel / Linear / Raycast

// Background Layers (near-black with subtle blue undertone)
val BgPrimary   = Color(0xFF09090B)   // Main background — near pure black
val BgElevated  = Color(0xFF111113)   // Cards & elevated surfaces
val BgSurface   = Color(0xFF18181B)   // Input fields, secondary surfaces
val BgHover     = Color(0xFF1F1F23)   // Hover/pressed states

// Borders (extremely subtle)
val BorderDefault = Color(0xFF27272A)  // Default border
val BorderSubtle  = Color(0xFF1E1E21)  // Subtle dividers

// Text
val TextPrimary   = Color(0xFFFAFAFA)  // Primary text — crisp white
val TextSecondary = Color(0xFFA1A1AA)  // Secondary/muted text
val TextDim       = Color(0xFF52525B)  // Timestamps, metadata

// Accent Colors (muted, sophisticated)
val AccentGreen  = Color(0xFF22C55E)   // Success / online / OK
val AccentCyan   = Color(0xFF06B6D4)   // Info / peer IDs / crypto
val AccentAmber  = Color(0xFFF59E0B)   // Warning
val AccentRed    = Color(0xFFEF4444)   // Error / SOS / emergency
val AccentViolet = Color(0xFF8B5CF6)   // Crypto / security badges

// Glow variants (very subtle, for backgrounds only)
val GlowGreen  = Color(0x1A22C55E)
val GlowCyan   = Color(0x1A06B6D4)
val GlowAmber  = Color(0x1AF59E0B)
val GlowRed    = Color(0x2AEF4444)
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
