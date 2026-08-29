package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.astro.PanchangCalculator
import com.example.data.model.PanchangData
import com.example.data.model.karanaLocal
import com.example.data.model.masaLocal
import com.example.data.model.nakshatraLocal
import com.example.data.model.pakshaLocal
import com.example.data.model.tithiLocal
import com.example.data.model.yogaLocal
import com.example.ui.theme.DateTimeAccent
import com.example.ui.theme.ElevatedSurface
import com.example.ui.theme.GlassCardBorder
import com.example.ui.theme.RahuKaalDangerColor
import com.example.ui.theme.ShubhSuccessColor
import com.example.ui.theme.SurfaceBackground
import com.example.util.LanguageManager

/**
 * The day's Panchang as a bento grid.
 *
 * The screen used to be one long column of same-sized cards, which is why it
 * read as thin — you could only ever see one fact at a time and had to scroll
 * for the next. A bento gives the facts different weights: the Tithi gets the
 * big tile because it is what people open the app for, the moon sits beside it,
 * and Rahu Kaal spans the full width because it is the one people plan around.
 *
 * The visual language is unchanged — the same glass surfaces, the same gold on
 * dark. Only the arrangement is new, so this drops in and out without touching
 * the theme.
 */

/** One bento cell. [accent] tints the value and the left rail. */
@Composable
private fun BentoTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    sub: String? = null,
    accent: Color = DateTimeAccent,
    valueSize: Int = 18,
    minHeight: Int = 88,
    emphasis: Boolean = false,
    singleLineValue: Boolean = false
) {
    Box(
        modifier = modifier
            .heightIn(min = minHeight.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(
                if (emphasis) {
                    Brush.verticalGradient(listOf(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.surface))
                } else {
                    Brush.verticalGradient(listOf(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.surface))
                }
            )
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp,
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Medium
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = accent,
                    fontSize = valueSize.sp,
                    lineHeight = (valueSize * 1.35).sp,
                    fontWeight = FontWeight.Bold
                ),
                maxLines = if (singleLineValue) 1 else 2,
                softWrap = !singleLineValue,
                overflow = TextOverflow.Ellipsis
            )
            if (sub != null) {
                Text(
                    text = sub,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.5.sp,
                        lineHeight = 16.sp
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun BentoPanchangGrid(
    panchang: PanchangData,
    modifier: Modifier = Modifier
) {
    val gap = 10.dp
    val moon = PanchangCalculator.getMoonPhaseInfo(panchang.pakshaHindi, panchang.tithiHindi)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(gap)
    ) {
        // Row 1 — the Tithi is the headline, so it takes two thirds and the
        // largest type on the screen. The moon rides alongside it.
        Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
            BentoTile(
                label = LanguageManager.getString("तिथि", "Tithi"),
                value = panchang.tithiLocal,
                sub = panchang.tithiEndTime,
                accent = MaterialTheme.colorScheme.primary,
                valueSize = 24,
                minHeight = 128,
                emphasis = true,
                modifier = Modifier.weight(2f)
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 128.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.surface)))
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp))
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(text = moon.emoji, fontSize = 30.sp)
                    Text(
                        text = "${moon.illuminationPercent}%",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = LanguageManager.getString(moon.nameHindi, moon.nameEn),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp,
                            lineHeight = 14.sp
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Row 2 — sunrise and sunset, the two everyone checks first.
        Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
            BentoTile(
                label = LanguageManager.getString("सूर्योदय", "Sunrise"),
                value = panchang.sunrise,
                accent = MaterialTheme.colorScheme.secondary,
                valueSize = 20,
                minHeight = 76,
                singleLineValue = true,
                modifier = Modifier.weight(1f)
            )
            BentoTile(
                label = LanguageManager.getString("सूर्यास्त", "Sunset"),
                value = panchang.sunset,
                accent = MaterialTheme.colorScheme.secondary,
                valueSize = 20,
                minHeight = 76,
                singleLineValue = true,
                modifier = Modifier.weight(1f)
            )
        }

        // Row 3 — Nakshatra wide, its Pada as a small companion.
        Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
            BentoTile(
                label = LanguageManager.getString("नक्षत्र", "Nakshatra"),
                value = panchang.nakshatraLocal,
                sub = panchang.nakshatraEndTime,
                accent = MaterialTheme.colorScheme.primary,
                valueSize = 18,
                minHeight = 96,
                modifier = Modifier.weight(2f)
            )
            BentoTile(
                label = LanguageManager.getString("चरण", "Pada"),
                value = "${panchang.nakshatraPada}",
                accent = MaterialTheme.colorScheme.onSurface,
                valueSize = 26,
                minHeight = 96,
                modifier = Modifier.weight(1f)
            )
        }

        // Row 4 — Rahu Kaal spans the full width. It is the one thing on this
        // screen people actively plan around, and it is the only red on it.
        BentoTile(
            label = LanguageManager.getString("राहु काल", "Rahu Kaal"),
            value = panchang.rahuKaal,
            sub = LanguageManager.getString("इस अवधि में शुभ कार्य टालें", "Avoid starting anything auspicious"),
            accent = MaterialTheme.colorScheme.error,
            valueSize = 19,
            minHeight = 84,
            singleLineValue = true,
                modifier = Modifier.fillMaxWidth()
        )

        // Row 5 — the two auspicious windows.
        Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
            BentoTile(
                label = LanguageManager.getString("अभिजित मुहूर्त", "Abhijit"),
                value = panchang.abhijitMuhurat,
                accent = MaterialTheme.colorScheme.tertiary,
                valueSize = 13,
                minHeight = 78,
                singleLineValue = true,
                modifier = Modifier.weight(1f)
            )
            BentoTile(
                label = LanguageManager.getString("ब्रह्म मुहूर्त", "Brahma Muhurta"),
                value = panchang.brahmaMuhurat,
                accent = MaterialTheme.colorScheme.tertiary,
                valueSize = 13,
                minHeight = 78,
                singleLineValue = true,
                modifier = Modifier.weight(1f)
            )
        }

        // Row 6 — Yoga and Karana, the quieter two of the five limbs.
        Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
            BentoTile(
                label = LanguageManager.getString("योग", "Yoga"),
                value = panchang.yogaLocal,
                accent = MaterialTheme.colorScheme.onSurface,
                valueSize = 16,
                minHeight = 76,
                modifier = Modifier.weight(1f)
            )
            BentoTile(
                label = LanguageManager.getString("करण", "Karana"),
                value = panchang.karanaLocal,
                accent = MaterialTheme.colorScheme.onSurface,
                valueSize = 16,
                minHeight = 76,
                modifier = Modifier.weight(1f)
            )
        }

        // Row 7 — the calendar footing: month, paksha and the two eras.
        Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
            BentoTile(
                label = LanguageManager.getString("मास व पक्ष", "Month & Paksha"),
                value = panchang.masaLocal,
                sub = panchang.pakshaLocal,
                accent = MaterialTheme.colorScheme.primary,
                valueSize = 17,
                minHeight = 86,
                modifier = Modifier.weight(1f)
            )
            BentoTile(
                label = LanguageManager.getString("संवत", "Samvat"),
                value = "${panchang.vikramSamvat}",
                sub = LanguageManager.getString("शक ${panchang.sakaSamvat}", "Saka ${panchang.sakaSamvat}"),
                accent = MaterialTheme.colorScheme.primary,
                valueSize = 17,
                minHeight = 86,
                modifier = Modifier.weight(1f)
            )
        }

        // Moonrise and moonset last — genuinely useful, but not what anyone opens
        // the app for. They read "—" on the days the Moon skips one.
        Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
            BentoTile(
                label = LanguageManager.getString("चन्द्रोदय", "Moonrise"),
                value = panchang.moonrise,
                accent = MaterialTheme.colorScheme.onSurfaceVariant,
                valueSize = 16,
                minHeight = 70,
                singleLineValue = true,
                modifier = Modifier.weight(1f)
            )
            BentoTile(
                label = LanguageManager.getString("चन्द्रास्त", "Moonset"),
                value = panchang.moonset,
                accent = MaterialTheme.colorScheme.onSurfaceVariant,
                valueSize = 16,
                minHeight = 70,
                singleLineValue = true,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(2.dp))
    }
}
