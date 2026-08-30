@file:OptIn(androidx.compose.ui.text.ExperimentalTextApi::class)

package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import app.revati.jyotish.R

// Outfit → primary Latin/English typeface (res/font/outfit.xml)
// Noto Sans Devanagari → Hindi/Sanskrit script ke liye (res/font/noto_sans_devanagari.xml)
// PEHLE: dono FontFamily.SansSerif (system Roboto) pe point kar rahe the — fonts
// res/font/ me bundled the par kabhi wire hi nahi hue, isliye pura app default
// Android typography me render ho raha tha.
// Register each weight explicitly against the variable font's "wght" axis.
// Previously only one static instance was registered, so every FontWeight
// (Bold, SemiBold, Normal, etc.) resolved to the same glyphs -- this is why
// English (Outfit) text looked thin/inconsistent while Hindi (Noto) text
// looked heavier, regardless of the FontWeight requested in code.
val OutfitFontFamily = FontFamily(
    Font(R.font.outfit_variable, weight = FontWeight.Normal, variationSettings = FontVariation.Settings(FontVariation.weight(400))),
    Font(R.font.outfit_variable, weight = FontWeight.Medium, variationSettings = FontVariation.Settings(FontVariation.weight(500))),
    Font(R.font.outfit_variable, weight = FontWeight.SemiBold, variationSettings = FontVariation.Settings(FontVariation.weight(600))),
    Font(R.font.outfit_variable, weight = FontWeight.Bold, variationSettings = FontVariation.Settings(FontVariation.weight(700))),
    Font(R.font.outfit_variable, weight = FontWeight.ExtraBold, variationSettings = FontVariation.Settings(FontVariation.weight(800)))
)
val NotoSansDevanagariFontFamily = FontFamily(
    Font(R.font.noto_sans_devanagari_variable, weight = FontWeight.Normal, variationSettings = FontVariation.Settings(FontVariation.weight(400))),
    Font(R.font.noto_sans_devanagari_variable, weight = FontWeight.Medium, variationSettings = FontVariation.Settings(FontVariation.weight(500))),
    Font(R.font.noto_sans_devanagari_variable, weight = FontWeight.SemiBold, variationSettings = FontVariation.Settings(FontVariation.weight(600))),
    Font(R.font.noto_sans_devanagari_variable, weight = FontWeight.Bold, variationSettings = FontVariation.Settings(FontVariation.weight(700))),
    Font(R.font.noto_sans_devanagari_variable, weight = FontWeight.ExtraBold, variationSettings = FontVariation.Settings(FontVariation.weight(800)))
)

// Primary typography scale for AstroVeda
/**
 * Latin first, Devanagari second.
 *
 * Compose walks a FontFamily in order and falls through to the next entry for
 * glyphs a face does not contain. Outfit covers Latin and has NO Devanagari at
 * all — its cmap has nothing in U+0900..U+097F — so listing Noto after it means
 * English renders in Outfit and Hindi renders in the Devanagari face that ships
 * in this APK.
 *
 * Before this, every style pointed at Outfit alone and all 1400-plus Hindi
 * strings in the app fell back to whatever the system happened to provide. That
 * is why Hindi and English never matched in weight or rhythm, and why 647 KB of
 * bundled Noto was dead weight.
 */
val AstroFontFamily = FontFamily(
    Font(R.font.outfit_variable, weight = FontWeight.Normal, variationSettings = FontVariation.Settings(FontVariation.weight(400))),
    Font(R.font.outfit_variable, weight = FontWeight.Medium, variationSettings = FontVariation.Settings(FontVariation.weight(500))),
    Font(R.font.outfit_variable, weight = FontWeight.SemiBold, variationSettings = FontVariation.Settings(FontVariation.weight(600))),
    Font(R.font.outfit_variable, weight = FontWeight.Bold, variationSettings = FontVariation.Settings(FontVariation.weight(700))),
    Font(R.font.outfit_variable, weight = FontWeight.ExtraBold, variationSettings = FontVariation.Settings(FontVariation.weight(800))),
    Font(R.font.noto_sans_devanagari_variable, weight = FontWeight.Normal, variationSettings = FontVariation.Settings(FontVariation.weight(400))),
    Font(R.font.noto_sans_devanagari_variable, weight = FontWeight.Medium, variationSettings = FontVariation.Settings(FontVariation.weight(500))),
    Font(R.font.noto_sans_devanagari_variable, weight = FontWeight.SemiBold, variationSettings = FontVariation.Settings(FontVariation.weight(600))),
    Font(R.font.noto_sans_devanagari_variable, weight = FontWeight.Bold, variationSettings = FontVariation.Settings(FontVariation.weight(700))),
    Font(R.font.noto_sans_devanagari_variable, weight = FontWeight.ExtraBold, variationSettings = FontVariation.Settings(FontVariation.weight(800)))
)

/**
 * Devanagari needs more vertical room than Latin.
 *
 * Compose defaults `includeFontPadding` to false, which trims the space above the
 * ascender. Latin barely notices; Devanagari loses the top of the shirorekha and
 * the whole of an i-matra, so "पिछला दिन" renders as "ापछला ादन". Turning the
 * padding back on and refusing to trim the line box gives the script its headroom.
 *
 * This only became visible once Hindi actually started rendering in Noto — the
 * old Outfit-only typography had no Devanagari glyphs to clip.
 */
private val DevanagariSafe = PlatformTextStyle(includeFontPadding = true)

private val FullLineHeight = LineHeightStyle(
    alignment = LineHeightStyle.Alignment.Center,
    trim = LineHeightStyle.Trim.None
)

private fun TextStyle.devanagariSafe(): TextStyle = copy(
    platformStyle = DevanagariSafe,
    lineHeightStyle = FullLineHeight
)

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = AstroFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 57.sp,
        lineHeight = 77.sp,
        letterSpacing = (-0.25).sp,
        platformStyle = DevanagariSafe,
        lineHeightStyle = FullLineHeight
    ),
    displayMedium = TextStyle(
        fontFamily = AstroFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 45.sp,
        lineHeight = 61.sp,
        letterSpacing = 0.sp,
        platformStyle = DevanagariSafe,
        lineHeightStyle = FullLineHeight
    ),
    displaySmall = TextStyle(
        fontFamily = AstroFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 36.sp,
        lineHeight = 49.sp,
        letterSpacing = 0.sp,
        platformStyle = DevanagariSafe,
        lineHeightStyle = FullLineHeight
    ),
    headlineLarge = TextStyle(
        fontFamily = AstroFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        lineHeight = 43.sp,
        letterSpacing = 0.sp,
        platformStyle = DevanagariSafe,
        lineHeightStyle = FullLineHeight
    ),
    headlineMedium = TextStyle(
        fontFamily = AstroFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 38.sp,
        letterSpacing = 0.sp,
        platformStyle = DevanagariSafe,
        lineHeightStyle = FullLineHeight
    ),
    headlineSmall = TextStyle(
        fontFamily = AstroFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp,
        platformStyle = DevanagariSafe,
        lineHeightStyle = FullLineHeight
    ),
    titleLarge = TextStyle(
        fontFamily = AstroFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 30.sp,
        letterSpacing = 0.sp,
        platformStyle = DevanagariSafe,
        lineHeightStyle = FullLineHeight
    ),
    titleMedium = TextStyle(
        fontFamily = AstroFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.15.sp,
        platformStyle = DevanagariSafe,
        lineHeightStyle = FullLineHeight
    ),
    titleSmall = TextStyle(
        fontFamily = AstroFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 19.sp,
        letterSpacing = 0.1.sp,
        platformStyle = DevanagariSafe,
        lineHeightStyle = FullLineHeight
    ),
    bodyLarge = TextStyle(
        fontFamily = AstroFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.5.sp,
        platformStyle = DevanagariSafe,
        lineHeightStyle = FullLineHeight
    ),
    bodyMedium = TextStyle(
        fontFamily = AstroFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 19.sp,
        letterSpacing = 0.25.sp,
        platformStyle = DevanagariSafe,
        lineHeightStyle = FullLineHeight
    ),
    bodySmall = TextStyle(
        fontFamily = AstroFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp,
        platformStyle = DevanagariSafe,
        lineHeightStyle = FullLineHeight
    ),
    labelLarge = TextStyle(
        fontFamily = AstroFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 19.sp,
        letterSpacing = 0.1.sp,
        platformStyle = DevanagariSafe,
        lineHeightStyle = FullLineHeight
    ),
    labelMedium = TextStyle(
        fontFamily = AstroFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
        platformStyle = DevanagariSafe,
        lineHeightStyle = FullLineHeight
    ),
    labelSmall = TextStyle(
        fontFamily = AstroFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 15.sp,
        letterSpacing = 0.5.sp,
        platformStyle = DevanagariSafe,
        lineHeightStyle = FullLineHeight
    )
)