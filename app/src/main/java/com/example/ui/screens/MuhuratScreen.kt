package com.example.ui.screens

import com.example.util.LanguageManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Scaffold
import androidx.compose.foundation.layout.PaddingValues
import android.view.HapticFeedbackConstants
import androidx.compose.ui.platform.LocalView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChoghadiyaSlot
import com.example.data.model.ChoghadiyaType
import com.example.data.model.MuhuratItem
import com.example.ui.MainViewModel
import com.example.ui.components.GlassBadge
import com.example.ui.components.GlassCard
import com.example.ui.components.SectionHeader
import com.example.ui.theme.DateTimeAccent
import com.example.ui.theme.RahuKaalDangerColor
import com.example.ui.theme.ShubhSuccessColor

@Composable
fun MuhuratScreen(viewModel: MainViewModel) {
    val view = LocalView.current
    val isDaytime by viewModel.choghadiyaDaytime.collectAsState()
    val slots = viewModel.choghadiyaSlots
    val muhurats = viewModel.upcomingMuhurats

    Scaffold(
        containerColor = Color.Transparent,
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                top = paddingValues.calculateTopPadding() + 8.dp,
                end = 16.dp,
                bottom = paddingValues.calculateBottomPadding() + 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            SectionHeader(
                titleHi = "आज का चौघड़िया (Today's Choghadiya)",
                titleEn = "Choghadiya Timings"
            )
        }

        // Day / Night Toggle Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isDaytime) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                        .clickable {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            viewModel.toggleChoghadiyaDayNight(true)
                        }
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                        .testTag("choghadiya_day_button")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.WbSunny,
                            contentDescription = "Day",
                            tint = if (isDaytime) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = LanguageManager.getString("दिन का चौघड़िया (Day)", "Day Choghadiya"),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Normal,
                                color = if (isDaytime) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                                fontSize = 12.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (!isDaytime) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                        .clickable {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            viewModel.toggleChoghadiyaDayNight(false)
                        }
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                        .testTag("choghadiya_night_button")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.NightsStay,
                            contentDescription = "Night",
                            tint = if (!isDaytime) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = LanguageManager.getString("रात का चौघड़िया (Night)", "Night Choghadiya"),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Normal,
                                color = if (!isDaytime) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                                fontSize = 12.sp
                            )
                        )
                    }
                }
            }
        }

        // Choghadiya Slots List
        items(slots) { slot ->
            ChoghadiyaRow(slot)
        }

        // Event-Based Muhurat Finder Header
        item {
            SectionHeader(
                titleHi = "कार्यानुसार शुभ मुहूर्त (Event Muhurat Finder)",
                titleEn = "Event Muhurat Finder",
                subtitleHi = "विवाह, गृह प्रवेश, व्यापार, वाहन व यात्रा मुहूर्त",
                subtitleEn = "Wedding, Housewarming, Business & Vehicle"
            )
        }

        // Muhurat List
        items(muhurats) { item ->
            EventMuhuratCard(item)
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
}

@Composable
fun ChoghadiyaRow(slot: ChoghadiyaSlot) {
    val statusColor = when (slot.type) {
        ChoghadiyaType.AMRIT, ChoghadiyaType.SHUBH, ChoghadiyaType.LABH -> ShubhSuccessColor
        ChoghadiyaType.CHAR -> DateTimeAccent
        else -> RahuKaalDangerColor
    }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        borderColor = statusColor.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = slot.type.nameHi,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 16.sp
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "(${slot.type.natureHi})",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = statusColor,
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp
                        )
                    )
                }

                Text(
                    text = "${LanguageManager.getString("समय", "Time")}: ${slot.timeSlotString}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 14.sp
                    )
                )
            }

            GlassBadge(
                text = "स्वामी: ${slot.rulerPlanetHi}",
                backgroundColor = statusColor.copy(alpha = 0.15f),
                textColor = statusColor,
                borderColor = statusColor
            )
        }
    }
}

@Composable
fun EventMuhuratCard(item: MuhuratItem) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = LanguageManager.getString(item.categoryHi, item.categoryEn),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 16.sp
                    )
                )

                GlassBadge(
                    text = item.qualityHi,
                    backgroundColor = ShubhSuccessColor.copy(alpha = 0.2f),
                    textColor = ShubhSuccessColor,
                    borderColor = ShubhSuccessColor
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${item.dateString} (${item.dayOfWeekHi}) | ${item.startTime} - ${item.endTime}",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp
                )
            )

            Text(
                text = "${LanguageManager.getString("तिथि", "Tithi")}: ${item.tithiHi} | ${LanguageManager.getString("नक्षत्र", "Nakshatra")}: ${item.nakshatraHi}",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = item.descriptionHi,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 14.sp,
                    lineHeight = 19.sp
                )
            )
        }
    }
}
