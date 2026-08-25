package com.example.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val AstroDarkColorScheme = darkColorScheme(
    primary = SacredSaffron,
    onPrimary = Color.Black,
    primaryContainer = CosmicPrimaryContainer,
    onPrimaryContainer = SacredSaffron,
    secondary = GoldSecondary,
    onSecondary = Color.Black,
    secondaryContainer = CosmicCardSurface,
    onSecondaryContainer = TextPrimaryDark,
    tertiary = GoldGlow,
    onTertiary = Color.Black,
    background = CosmicDeepNavy,
    onBackground = TextPrimaryDark,
    surface = CosmicCardSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = CosmicSurfaceVariant,
    onSurfaceVariant = TextSecondaryDark,
    outline = CosmicOutline
)

private val AstroLightColorScheme = lightColorScheme(
    primary = GoldPrimary,
    onPrimary = DeepNavy,
    primaryContainer = CelestialPrimaryContainer,
    onPrimaryContainer = CelestialOnPrimaryContainer,
    secondary = GoldSecondary,
    onSecondary = DeepNavy,
    secondaryContainer = CelestialSurfaceVariant,
    onSecondaryContainer = TextPrimaryLight,
    tertiary = SacredSaffron,
    onTertiary = DeepNavy,
    background = CelestialCream,
    onBackground = TextPrimaryLight,
    surface = Color.White,
    onSurface = TextPrimaryLight,
    surfaceVariant = CelestialSurfaceVariant,
    onSurfaceVariant = TextSecondaryLight,
    outline = CelestialOutline
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