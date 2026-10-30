package com.example.airteldatamonitor.theme

import androidx.compose.ui.graphics.Color

// =============================================================================
// Airtel Designer System 2.0 - Semantic Tokens
// =============================================================================

// Iconic Airtel Crimson Brand Anchor
val AirtelRed = Color(0xFFE60000)
val AirtelRedDim = Color(0xFFCC0000)
val AirtelRedMuted = Color(0x26E60000) // 15% opacity for badges & accents
val AirtelRedSubtle = Color(0x14E60000) // 8% opacity

// Obsidian Dark Palette (Luxury Dark Mode)
val DarkCanvas = Color(0xFF090B0E)
val DarkCard = Color(0xFF131720)
val DarkCardVariant = Color(0xFF181F2C)
val DarkCardElevated = Color(0xFF1E2636)
val DarkCardBorder = Color(0x1AFFFFFF) // 10% white rim specular highlight
val DarkTextPrimary = Color(0xFFF8FAFC)
val DarkTextSecondary = Color(0xFF94A3B8)
val DarkTextMuted = Color(0xFF64748B)

// Editorial Paper Palette (Clean Light Mode)
val LightCanvas = Color(0xFFF4F6F9)
val LightCard = Color(0xFFFFFFFF)
val LightCardVariant = Color(0xFFF8FAFC)
val LightCardElevated = Color(0xFFFFFFFF)
val LightCardBorder = Color(0xFFE2E8F0)
val LightTextPrimary = Color(0xFF0F172A) // Deep ink slate
val LightTextSecondary = Color(0xFF64748B)
val LightTextMuted = Color(0xFF94A3B8)

// Status & Telemetry Indicators (Disciplined: Emerald only for active connectivity)
val StatusOnline = Color(0xFF10B981)
val StatusOnlineDim = Color(0xFF059669)
val StatusOnlineMuted = Color(0x2610B981)
val StatusWarning = Color(0xFFF59E0B)
val StatusError = Color(0xFFEF4444)
val StatusInfo = Color(0xFF38BDF8)

// Compatibility Mappings for existing screens
val DarkBackground = DarkCanvas
val DarkSurface = DarkCard
val DarkSurfaceVariant = DarkCardVariant
val DarkSurfaceBorder = DarkCardBorder
val DarkSurfaceElevated = DarkCardElevated

// Aliasing previous neon tokens to refined emerald status
val NeonGreenAccent = StatusOnline
val NeonGreenDim = StatusOnlineDim
val NeonGreenMuted = StatusOnlineMuted
val NeonGreenGlow = StatusOnlineMuted
val NeonGreenHighlight = Color(0xFF34D399)

val TextPrimary = DarkTextPrimary
val TextSecondary = DarkTextSecondary
val TextMuted = DarkTextMuted

val StatusSuccess = StatusOnline
val ChartBarBackground = Color(0xFF1E2636)
val DemoBadgeBg = Color(0xFF231C0D)
val DemoBadgeBorder = Color(0xFF854D0E)
val DemoBadgeText = Color(0xFFFCD34D)
