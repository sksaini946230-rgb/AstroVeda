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

/**
 * A genuine light scheme.
 *
 * This used to be a byte-for-byte copy of the dark one built with
 * darkColorScheme(), so switching to it changed nothing. The gold, orange, green
 * and red accents are darkened here so they hold up on white; the cosmic violet
 * and cream neutrals are replaced with warm paper tones.
 */
private val AstroLightColorScheme = lightColorScheme(
    primary = Color(0xFF8A6B18),          // gold, darkened to read on white
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFF6EFDD),
    onPrimaryContainer = Color(0xFF2A2417),
    secondary = Color(0xFFB5601C),        // the date/time orange, darkened
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFCEDE1),
    onSecondaryContainer = Color(0xFF3A2413),
    tertiary = Color(0xFF177A46),
    onTertiary = Color(0xFFFFFFFF),
    background = Color(0xFFFBFAF7),
    onBackground = Color(0xFF1A1A17),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1A1A17),
    surfaceVariant = Color(0xFFF2F0EA),
    onSurfaceVariant = Color(0xFF5C5A52),
    outline = Color(0xFFDAD6CC),
    outlineVariant = Color(0xFFE8E5DC),
    error = Color(0xFFB3261E),
    onError = Color(0xFFFFFFFF)
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