package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private fun darkScheme(c: AstroColors) = darkColorScheme(
    primary = c.primaryButtonBackground,
    onPrimary = c.primaryButtonText,
    primaryContainer = c.elevatedSurface,
    onPrimaryContainer = c.textPrimary,
    secondary = c.dateTimeAccent,
    onSecondary = c.primaryButtonText,
    secondaryContainer = c.surfaceBackground,
    onSecondaryContainer = c.textPrimary,
    tertiary = c.shubhSuccess,
    onTertiary = c.primaryButtonText,
    background = c.appBackground,
    onBackground = c.textPrimary,
    surface = c.surfaceBackground,
    onSurface = c.textPrimary,
    surfaceVariant = c.elevatedSurface,
    onSurfaceVariant = c.textSecondary,
    outline = c.glassCardBorder,
    outlineVariant = c.textTertiary,
    error = c.rahuKaalDanger,
    onError = c.primaryButtonText
)

private fun lightScheme(c: AstroColors) = lightColorScheme(
    primary = c.primaryButtonBackground,
    onPrimary = c.primaryButtonText,
    primaryContainer = c.elevatedSurface,
    onPrimaryContainer = c.textPrimary,
    secondary = c.dateTimeAccent,
    onSecondary = c.primaryButtonText,
    secondaryContainer = c.surfaceBackground,
    onSecondaryContainer = c.textPrimary,
    tertiary = c.shubhSuccess,
    onTertiary = c.primaryButtonText,
    background = c.appBackground,
    onBackground = c.textPrimary,
    surface = c.surfaceBackground,
    onSurface = c.textPrimary,
    surfaceVariant = c.elevatedSurface,
    onSurfaceVariant = c.textSecondary,
    outline = c.glassCardBorder,
    outlineVariant = c.textTertiary,
    error = c.rahuKaalDanger,
    onError = c.primaryButtonText
)

/**
 * The app theme.
 *
 * [darkTheme] follows the system by default. It used to be hardcoded `true`
 * with the comment "Enforce dark-only theme", and the light scheme beside it was
 * a byte-for-byte copy of the dark one built with darkColorScheme() — so the
 * light path existed on paper and could never actually run.
 *
 * Dynamic colour stays off on purpose. This app's identity is the gold, and
 * letting Android recolour it from the user's wallpaper would take that away.
 */
@Composable
fun AstroVedaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val palette = astroPalette(isLight = !darkTheme)

    CompositionLocalProvider(LocalAstroColors provides palette) {
        MaterialTheme(
            colorScheme = if (darkTheme) darkScheme(palette) else lightScheme(palette),
            typography = Typography,
            content = content
        )
    }
}
