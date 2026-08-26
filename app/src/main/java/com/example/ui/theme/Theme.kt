package com.example.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val AstroDarkColorScheme = darkColorScheme(
    primary = PrimaryButtonBackground,
    onPrimary = PrimaryButtonText,
    primaryContainer = ElevatedSurface,
    onPrimaryContainer = TextPrimary,
    secondary = DateTimeAccent,
    onSecondary = PrimaryButtonText,
    secondaryContainer = SurfaceBackground,
    onSecondaryContainer = TextPrimary,
    tertiary = NavActiveColor,
    onTertiary = PrimaryButtonText,
    background = AppBackground,
    onBackground = TextPrimary,
    surface = SurfaceBackground,
    onSurface = TextPrimary,
    surfaceVariant = ElevatedSurface,
    onSurfaceVariant = TextSecondary,
    outline = GlassCardBorder,
    outlineVariant = TextTertiary,
    error = RahuKaalDangerColor,
    onError = PrimaryButtonText
)

private val AstroLightColorScheme = darkColorScheme(
    primary = PrimaryButtonBackground,
    onPrimary = PrimaryButtonText,
    primaryContainer = ElevatedSurface,
    onPrimaryContainer = TextPrimary,
    secondary = DateTimeAccent,
    onSecondary = PrimaryButtonText,
    secondaryContainer = SurfaceBackground,
    onSecondaryContainer = TextPrimary,
    tertiary = NavActiveColor,
    onTertiary = PrimaryButtonText,
    background = AppBackground,
    onBackground = TextPrimary,
    surface = SurfaceBackground,
    onSurface = TextPrimary,
    surfaceVariant = ElevatedSurface,
    onSurfaceVariant = TextSecondary,
    outline = GlassCardBorder,
    outlineVariant = TextTertiary,
    error = RahuKaalDangerColor,
    onError = PrimaryButtonText
)

@Composable
fun AstroVedaTheme(
    darkTheme: Boolean = true, // Enforce dark-only theme
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> AstroDarkColorScheme
        else -> AstroLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}