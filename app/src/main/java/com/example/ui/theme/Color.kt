package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// ============================================================================
// AstroVeda Color Token System — STRICT 3-Color Brand Palette
// Gold #C9A84C · Deep Navy #1A1F35 · Emerald #2ABF6E
// Sabhi tokens inhi 3 hues ke tints/shades hain — koi extra hue nahi.
// ============================================================================

// --- Canonical Base ---
val PremiumGold = Color(0xFFC9A84C)
val DeepNavy = Color(0xFF1A1F35)
val EmeraldGreen = Color(0xFF2ABF6E)

// --- Navy Tints (backgrounds/surfaces — sab Navy ke shades) ---
val CosmicDeepNavy = Color(0xFF1A1F35)          // Base background
val CosmicCardSurface = Color(0xFF232A47)       // Navy +8% lighter — card surface
val CosmicSurfaceVariant = Color(0xFF272E4D)    // Navy +10% — variant surface
val CosmicPrimaryContainer = Color(0xFF303761)  // Navy +18% — container
val CosmicOutline = Color(0xFF3A4270)           // Navy +22% — borders

// --- Light mode (Navy tinted toward white, not a new hue) ---
val CelestialCream = Color(0xFFF5F3EC)
val CelestialSurfaceVariant = Color(0xFFEDEBE2)
val CelestialOutline = Color(0xFFD9D6C8)
val CelestialPrimaryContainer = Color(0xFFEDE0BC)
val CelestialOnPrimaryContainer = Color(0xFF1A1F35)

// --- Text (Navy-tinted whites/grays, no new hue) ---
val TextPrimaryDark = Color(0xFFF3F1EA)
val TextSecondaryDark = Color(0xFF9CA3C0)
val TextGold = Color(0xFFC9A84C)
val TextPrimaryLight = Color(0xFF1A1F35)
val TextSecondaryLight = Color(0xFF5C6178)

// --- Gold Tints (all references collapse to single Gold identity) ---
val GoldPrimary = Color(0xFFC9A84C)
val GoldSecondary = Color(0xFFB8934A)
val GoldGlow = Color(0xFFE0C177)
val SacredSaffron = Color(0xFFC9A84C)
val MinimalistGold = Color(0xFFC9A84C)
val DateOrange = Color(0xFFC9A84C)
val NeutralOrange = Color(0xFFB8934A)

// --- Emerald Tints ---
val AuspiciousGreen = Color(0xFF2ABF6E)
val NavigationGreen = Color(0xFF2ABF6E)

// --- Removed hues, aliased to nearest of the 3 (no cyan/purple/red family) ---
val AccentCyan = Color(0xFF2ABF6E)
val AccentPurple = Color(0xFFC9A84C)
// Mars Red — genuine warning/danger color per FSD spec (Rahu Kaal, inauspicious)
val InauspiciousRed = Color(0xFFE85A4A)
val StatusRed = Color(0xFFE85A4A)
val MarsRedGlow = Color(0x38E85A4A)  // 22% opacity for glass tint

// --- Glassmorphism (Gold-tinted, not a new hue) ---
val GlassBorder = Color(0x26C9A84C)
val GlassWhite = Color(0x0DFFFFFF)
