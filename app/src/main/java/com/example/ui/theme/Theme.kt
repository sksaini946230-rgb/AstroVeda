package com.example.ui.theme

import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val AstroDarkColorScheme = darkColorScheme(
    primary = SacredSaffron,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF1F2937),
    onPrimaryContainer = SacredSaffron,
    secondary = GoldSecondary,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF161B22),
    onSecondaryContainer = Color.White,
    tertiary = GoldGlow,
    onTertiary = Color.Black,
    background = CosmicDeepNavy,
    onBackground = Color(0xFFF0F2F5),
    surface = CosmicCardSurface,
    onSurface = Color(0xFFF0F2F5),
    surfaceVariant = Color(0xFF0D1117),
    onSurfaceVariant = Color(0xFF8B949E),
    outline = Color(0xFF30363D)
)

private val AstroLightColorScheme = lightColorScheme(
    primary = GoldPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFF8E1),
    onPrimaryContainer = Color(0xFF5D4037),
    secondary = GoldSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF5F7FA),
    onSecondaryContainer = Color(0xFF0D1117),
    tertiary = SacredSaffron,
    onTertiary = Color.White,
    background = CelestialCream,
    onBackground = Color(0xFF1C1C1E),
    surface = Color.White,
    onSurface = Color(0xFF1C1C1E),
    surfaceVariant = Color(0xFFFBFBFB),
    onSurfaceVariant = Color(0xFF6B7280),
    outline = Color(0xFFE5E7EB)
)

@Composable
fun AstroVedaTheme(
    darkTheme: Boolean = true, // Enforce dark-only theme
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val targetColorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> AstroDarkColorScheme
        else -> AstroLightColorScheme
    }

    val primary by animateColorAsState(targetValue = targetColorScheme.primary, label = "primary")
    val onPrimary by animateColorAsState(targetValue = targetColorScheme.onPrimary, label = "onPrimary")
    val primaryContainer by animateColorAsState(targetValue = targetColorScheme.primaryContainer, label = "primaryContainer")
    val onPrimaryContainer by animateColorAsState(targetValue = targetColorScheme.onPrimaryContainer, label = "onPrimaryContainer")
    val secondary by animateColorAsState(targetValue = targetColorScheme.secondary, label = "secondary")
    val onSecondary by animateColorAsState(targetValue = targetColorScheme.onSecondary, label = "onSecondary")
    val secondaryContainer by animateColorAsState(targetValue = targetColorScheme.secondaryContainer, label = "secondaryContainer")
    val onSecondaryContainer by animateColorAsState(targetValue = targetColorScheme.onSecondaryContainer, label = "onSecondaryContainer")
    val tertiary by animateColorAsState(targetValue = targetColorScheme.tertiary, label = "tertiary")
    val onTertiary by animateColorAsState(targetValue = targetColorScheme.onTertiary, label = "onTertiary")
    val background by animateColorAsState(targetValue = targetColorScheme.background, label = "background")
    val onBackground by animateColorAsState(targetValue = targetColorScheme.onBackground, label = "onBackground")
    val surface by animateColorAsState(targetValue = targetColorScheme.surface, label = "surface")
    val onSurface by animateColorAsState(targetValue = targetColorScheme.onSurface, label = "onSurface")
    val surfaceVariant by animateColorAsState(targetValue = targetColorScheme.surfaceVariant, label = "surfaceVariant")
    val onSurfaceVariant by animateColorAsState(targetValue = targetColorScheme.onSurfaceVariant, label = "onSurfaceVariant")
    val outline by animateColorAsState(targetValue = targetColorScheme.outline, label = "outline")

    val animatedColorScheme = targetColorScheme.copy(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = primaryContainer,
        onPrimaryContainer = onPrimaryContainer,
        secondary = secondary,
        onSecondary = onSecondary,
        secondaryContainer = secondaryContainer,
        onSecondaryContainer = onSecondaryContainer,
        tertiary = tertiary,
        onTertiary = onTertiary,
        background = background,
        onBackground = onBackground,
        surface = surface,
        onSurface = onSurface,
        surfaceVariant = surfaceVariant,
        onSurfaceVariant = onSurfaceVariant,
        outline = outline
    )

    MaterialTheme(
        colorScheme = animatedColorScheme,
        typography = Typography,
        content = content
    )
}
