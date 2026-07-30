package com.alertmesh.ui.theme

import androidx.compose.ui.graphics.Color

// Beast Cyber-Tactical Palette
val CyberBg = Color(0xFF05080E)           // Deep space tactical black
val CyberBgCard = Color(0xFF0D131F)       // Translucent dark slate card
val CyberBgSurface = Color(0xFF141C2E)    // Elevated surface dark blue
val CyberBgInput = Color(0xFF0A0F1A)      // Dark input field background

val NeonGreen = Color(0xFF00FF66)        // Matrix neon green (online/success)
val NeonGreenGlow = Color(0x3300FF66)    // Semi-transparent green glow
val NeonCyan = Color(0xFF00E5FF)         // Electric cyan (peer IDs/crypto)
val NeonCyanGlow = Color(0x3300E5FF)     // Semi-transparent cyan glow
val NeonAmber = Color(0xFFFFB300)        // Tactical warning amber
val NeonAmberGlow = Color(0x33FFB300)    // Semi-transparent amber glow
val NeonRed = Color(0xFFFF0055)          // Intense crimson (SOS/emergency)
val NeonRedGlow = Color(0x44FF0055)      // Semi-transparent red glow
val NeonViolet = Color(0xFFB026FF)       // Quantum key violet

val TextPrimary = Color(0xFFF0F4F8)      // Bright crisp white
val TextSecondary = Color(0xFF8A99AD)    // Muted slate gray
val TextDim = Color(0xFF4A586E)          // Subdued border/dim gray

// Legacy alias compatibility
val TerminalGreen = NeonGreen
val TerminalAmber = NeonAmber
val TerminalRed = NeonRed
val TerminalCyan = NeonCyan
val TerminalDim = TextDim
val TerminalBg = CyberBg
val TerminalBgLight = CyberBgCard
val TerminalWhite = TextPrimary
val TerminalBlue = NeonCyan
val SosRed = NeonRed
