package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ============================================================================
// AstroVeda Color Token System
//
// These names used to be plain top-level constants holding dark values, and
// roughly 370 call sites across 23 files read them directly. That is why the
// app could only ever be dark: even with a light MaterialTheme in place, every
// surface, border and accent came from a hardcoded dark colour.
//
// The names are unchanged, so no call site had to move. What changed is that
// each one is now a @Composable getter reading from the palette in scope, and
// AstroVedaTheme puts the light or the dark palette there depending on the
// system setting.
//
// If you add a token, add it to AstroColors, to both palettes, and to the
// accessor list at the bottom — all four, or the light theme quietly falls back
// to a dark value.
// ============================================================================

/** Every semantic colour the app draws with, in one palette. */
data class AstroColors(
    val appBackground: Color,
    val surfaceBackground: Color,
    val elevatedSurface: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val textLink: Color,
    val primaryButtonBackground: Color,
    val primaryButtonText: Color,
    val primaryButtonPressed: Color,
    val proBadge: Color,
    val navActive: Color,
    val navInactive: Color,
    val dateTimeAccent: Color,
    val shubhSuccess: Color,
    val rahuKaalDanger: Color,
    val glassCardBorder: Color,
    val cosmicGradientEdge: Color,
    val isLight: Boolean
)

private val DarkPalette = AstroColors(
    appBackground = Color(0xFF0B0E1A),
    surfaceBackground = Color(0xFF131728),
    elevatedSurface = Color(0xFF1C2136),
    textPrimary = Color(0xFFF5EDD6),          // never pure white
    textSecondary = Color(0xFF7885A8),
    textTertiary = Color(0xFF3D4A68),
    textLink = Color(0xFF4A8FE8),
    primaryButtonBackground = Color(0xFFD4A84B),
    primaryButtonText = Color(0xFF0B0E1A),
    primaryButtonPressed = Color(0xFFA9822E),
    proBadge = Color(0xFFD4A84B),
    navActive = Color(0xFF2ABF6E),
    // 0xFF3D4A68 here was about 1.7:1 against the bar and read as blank.
    navInactive = Color(0xFF6B7899),
    dateTimeAccent = Color(0xFFE8934A),
    shubhSuccess = Color(0xFF2ABF6E),
    rahuKaalDanger = Color(0xFFE85A4A),
    glassCardBorder = Color(0x1FFFFFFF),
    cosmicGradientEdge = Color(0xFF1A0A2E),
    isLight = false
)

/**
 * The light palette.
 *
 * The accents are not the dark ones reused — gold, orange and green at their
 * dark-theme brightness fail against white. Each is darkened until it clears
 * 4.5:1 on the light surface, and the neutrals are warm paper rather than grey
 * so the gold still reads as gold.
 */
private val LightPalette = AstroColors(
    appBackground = Color(0xFFFBFAF7),
    surfaceBackground = Color(0xFFFFFFFF),
    elevatedSurface = Color(0xFFF2F0EA),
    textPrimary = Color(0xFF1A1A17),
    textSecondary = Color(0xFF5C5A52),
    textTertiary = Color(0xFF9A968B),
    textLink = Color(0xFF1B5FB8),
    primaryButtonBackground = Color(0xFF8A6B18),
    primaryButtonText = Color(0xFFFFFFFF),
    primaryButtonPressed = Color(0xFF6B520F),
    proBadge = Color(0xFF8A6B18),
    navActive = Color(0xFF11663C),
    navInactive = Color(0xFF6E6B63),
    dateTimeAccent = Color(0xFFB5601C),
    shubhSuccess = Color(0xFF177A46),
    rahuKaalDanger = Color(0xFFB3261E),
    glassCardBorder = Color(0x1A000000),
    cosmicGradientEdge = Color(0xFFEDE7F5),
    isLight = true
)

/** Defaults to dark so a stray preview outside the theme still renders. */
val LocalAstroColors = staticCompositionLocalOf { DarkPalette }

internal fun astroPalette(isLight: Boolean) = if (isLight) LightPalette else DarkPalette

// ---------------------------------------------------------------------------
// The accessors. Same names the app has always used.
// ---------------------------------------------------------------------------

val AppBackground: Color @Composable @ReadOnlyComposable get() = LocalAstroColors.current.appBackground
val SurfaceBackground: Color @Composable @ReadOnlyComposable get() = LocalAstroColors.current.surfaceBackground
val ElevatedSurface: Color @Composable @ReadOnlyComposable get() = LocalAstroColors.current.elevatedSurface

val TextPrimary: Color @Composable @ReadOnlyComposable get() = LocalAstroColors.current.textPrimary
val TextSecondary: Color @Composable @ReadOnlyComposable get() = LocalAstroColors.current.textSecondary
val TextTertiary: Color @Composable @ReadOnlyComposable get() = LocalAstroColors.current.textTertiary
val TextLink: Color @Composable @ReadOnlyComposable get() = LocalAstroColors.current.textLink

val PrimaryButtonBackground: Color @Composable @ReadOnlyComposable get() = LocalAstroColors.current.primaryButtonBackground
val PrimaryButtonText: Color @Composable @ReadOnlyComposable get() = LocalAstroColors.current.primaryButtonText
val PrimaryButtonPressed: Color @Composable @ReadOnlyComposable get() = LocalAstroColors.current.primaryButtonPressed
val ProBadgeColor: Color @Composable @ReadOnlyComposable get() = LocalAstroColors.current.proBadge
val PremiumIconColor: Color @Composable @ReadOnlyComposable get() = LocalAstroColors.current.proBadge
val StarRatingFilled: Color @Composable @ReadOnlyComposable get() = LocalAstroColors.current.proBadge

val NavActiveColor: Color @Composable @ReadOnlyComposable get() = LocalAstroColors.current.navActive
val NavInactiveColor: Color @Composable @ReadOnlyComposable get() = LocalAstroColors.current.navInactive

val DateTimeAccent: Color @Composable @ReadOnlyComposable get() = LocalAstroColors.current.dateTimeAccent
val ShubhSuccessColor: Color @Composable @ReadOnlyComposable get() = LocalAstroColors.current.shubhSuccess
val RahuKaalDangerColor: Color @Composable @ReadOnlyComposable get() = LocalAstroColors.current.rahuKaalDanger

val GlassCardBorder: Color @Composable @ReadOnlyComposable get() = LocalAstroColors.current.glassCardBorder
val CosmicGradientEdge: Color @Composable @ReadOnlyComposable get() = LocalAstroColors.current.cosmicGradientEdge
