package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// ============================================================================
// AstroVeda Color Token System — Strict Purpose-Driven Palette
// ============================================================================

// BACKGROUNDS
val AppBackground = Color(0xFF0B0E1A)          // Main app background, all screens
val SurfaceBackground = Color(0xFF131728)      // Cards, panchang cards, rashifal cards
val ElevatedSurface = Color(0xFF1C2136)        // Modals, dialogs, bottom sheets

// TEXT
val TextPrimary = Color(0xFFF5EDD6)            // Headings, Tithi/Nakshatra names — NEVER use pure white
val TextSecondary = Color(0xFF7885A8)          // Captions, labels, dates
val TextTertiary = Color(0xFF3D4A68)           // Disabled text, inactive nav labels
val TextLink = Color(0xFF4A8FE8)               // "View more", "Change rashi", tappable text

// PRIMARY ACTIONS — GOLD — reserve ONLY for these purposes
val PrimaryButtonBackground = Color(0xFFD4A84B)
val PrimaryButtonText = Color(0xFF0B0E1A)      // Dark text on gold button
val PrimaryButtonPressed = Color(0xFFA9822E)
val ProBadgeColor = Color(0xFFD4A84B)
val PremiumIconColor = Color(0xFFD4A84B)
val StarRatingFilled = Color(0xFFD4A84B)

// NAVIGATION — GREEN — reserve ONLY for these purposes
val NavActiveColor = Color(0xFF2ABF6E)
// Was 0xFF3D4A68 — on the ElevatedSurface bar that is roughly 1.7:1 contrast,
// far under the 3:1 a UI element needs, so unselected tabs read as blank.
val NavInactiveColor = Color(0xFF6B7899)

// DATE & TIME — ORANGE — reserve ONLY for these purposes
val DateTimeAccent = Color(0xFFE8934A)         // Header dates, sunrise/sunset, Vikram Samvat year

// STATUS — functional only
val ShubhSuccessColor = Color(0xFF2ABF6E)      // Amrit/Shubh Choghadiya badges
val RahuKaalDangerColor = Color(0xFFE85A4A)    // Rahu Kaal, Gulika Kaal, error states

// GLASS CARD BORDER & COSMIC EFFECTS — consistent across all screens
val GlassCardBorder = Color(0x1FFFFFFF)        // rgba(255,255,255,0.12)
val CosmicGradientEdge = Color(0xFF1A0A2E)     // Subtle cosmic violet edge for splash and hero cards
