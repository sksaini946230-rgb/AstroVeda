package com.example.ui.screens

import com.example.data.model.tithiLocal
import com.example.data.model.nakshatraLocal
import com.example.data.model.yogaLocal
import com.example.data.model.karanaLocal
import com.example.data.model.masaLocal
import com.example.data.model.pakshaLocal
import com.example.data.model.varaLocal
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Scaffold
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import android.view.HapticFeedbackConstants
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FestivalData
import com.example.ui.MainViewModel
import com.example.ui.components.GlassBadge
import com.example.ui.components.GlassCard
import com.example.ui.components.SectionHeader
import com.example.ui.theme.GlassCardBorder
import com.example.util.LanguageManager
import com.example.util.AppLanguage

fun getLocalizedDay(dayHi: String): String {
    return if (LanguageManager.currentLanguage == AppLanguage.HINDI) {
        dayHi
    } else {
        when (dayHi) {
            "सोमवार" -> "Monday"
            "मंगलवार" -> "Tuesday"
            "बुधवार" -> "Wednesday"
            "गुरुवार" -> "Thursday"
            "शुक्रवार" -> "Friday"
            "शनिवार" -> "Saturday"
            "रविवार" -> "Sunday"
            else -> dayHi
        }
    }
}

@Composable
fun CalendarScreen(viewModel: MainViewModel) {
    val view = LocalView.current
    val panchang by viewModel.panchangState.collectAsState()
    var selectedRegion by remember { mutableStateOf("ALL") }
    var selectedFestivalDetail by remember { mutableStateOf<FestivalData?>(null) }

    val filteredFestivals = viewModel.festivals.filter {
        selectedRegion == "ALL" || it.regionFilter == "ALL" || it.regionFilter == selectedRegion
    }

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
            // Month Header - Simplified without redundant GlassCard container
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = LanguageManager.getString("विक्रम संवत ${panchang.vikramSamvat} | ${panchang.masaLocal}", "Vikram Samvat ${panchang.vikramSamvat} | ${panchang.masaLocal}"),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 17.sp
                        )
                    )
                    Text(
                        text = "शक संवत ${panchang.sakaSamvat} • 2026 हिन्दू पंचांग कैलेण्डर",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Event,
                        contentDescription = "Calendar",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Monthly Interactive Calendar Grid with Gold Dot Markers
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "मासिक कैलेंडर (Monthly View)",
                            style = MaterialTheme.typography.titleSmall.copy(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        )
                        GlassBadge(text = "सोम - रवि")
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Days of Week Header
                    val daysOfWeek = listOf("सोम", "मंगल", "बुध", "गुरु", "शुक्र", "शनि", "रवि")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        daysOfWeek.forEach { day ->
                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = day,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MaterialTheme.colorScheme.secondary,
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 13.sp
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 30 Days Grid (5 rows of 7 days)
                    val festivalDaysList = listOf(4, 8, 14, 15, 20, 28, 29) // Days with festivals
                    var dateCounter = 1
                    for (row in 0 until 5) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            for (col in 0 until 7) {
                                if (dateCounter <= 31) {
                                    val dayNum = dateCounter
                                    val hasFestival = festivalDaysList.contains(dayNum)
                                    val isToday = (dayNum == 22)
                                    val festivalForDay = when (dayNum) {
                                        4 -> viewModel.festivals.getOrNull(0)
                                        8 -> viewModel.festivals.getOrNull(1)
                                        14 -> viewModel.festivals.getOrNull(2)
                                        15 -> viewModel.festivals.getOrNull(3)
                                        20 -> viewModel.festivals.getOrNull(4)
                                        28 -> viewModel.festivals.getOrNull(5)
                                        29 -> viewModel.festivals.getOrNull(6)
                                        else -> viewModel.festivals.getOrNull((dayNum - 1) % viewModel.festivals.size)
                                    }

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(42.dp)
                                            .padding(2.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                when {
                                                    isToday -> MaterialTheme.colorScheme.primary
                                                    hasFestival -> MaterialTheme.colorScheme.surfaceVariant
                                                    else -> MaterialTheme.colorScheme.surface
                                                }
                                            )
                                            .border(
                                                1.dp,
                                                if (isToday) MaterialTheme.colorScheme.primary else GlassCardBorder,
                                                RoundedCornerShape(8.dp)
                                            )
                                            .clickable {
                                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                                festivalForDay?.let { selectedFestivalDetail = it }
                                            }
                                            .testTag("calendar_day_$dayNum"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = "$dayNum",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = if (isToday || hasFestival) FontWeight.Normal else FontWeight.Normal,
                                                    color = if (isToday) MaterialTheme.colorScheme.onPrimary else if (hasFestival) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                                    fontSize = 14.sp
                                                )
                                            )
                                            if (hasFestival && !isToday) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(4.dp)
                                                        .clip(CircleShape)
                                                        .background(MaterialTheme.colorScheme.secondary)
                                                )
                                            }
                                        }
                                    }
                                    dateCounter++
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Upcoming 7 Festivals Section below Calendar View
        item {
            SectionHeader(
                titleHi = "आगामी 7 प्रमुख व्रत व त्योहार (Next 7 Upcoming Festivals)",
                titleEn = "Next 7 Upcoming Festivals",
                subtitleHi = "तिथि, वार व पूजा मुहूर्त सहित शीघ्र आने वाले 7 पर्व",
                subtitleEn = "Upcoming major fasting days and festival dates"
            )
        }

        itemsIndexed(filteredFestivals.take(7)) { index, festival ->
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        selectedFestivalDetail = festival
                    }
                    .testTag("upcoming_festival_${index + 1}")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Number Badge (#1 to #7)
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                            .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "#${index + 1}",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Normal,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 12.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = LanguageManager.getString(festival.nameHi, festival.nameEn),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 15.sp
                            )
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${festival.dateString} (${getLocalizedDay(festival.dayNameHi)})",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 12.sp
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "• ${festival.monthNameHi} ${festival.tithiHi}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }

                    if (festival.regionFilter != "ALL") {
                        GlassBadge(
                            text = if (festival.regionFilter == "RAJASTHAN") LanguageManager.getString("राजस्थान", "Rajasthan") else LanguageManager.getString("उत्तर भारत", "North India"),
                            backgroundColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                            textColor = MaterialTheme.colorScheme.secondary,
                            borderColor = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }

        // Region Filters Pill Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = "Filter",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "क्षेत्र (Region):",
                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                )
                Spacer(modifier = Modifier.width(8.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val regions = listOf(
                        Pair("ALL", "सभी (All India)"),
                        Pair("NORTH", "उत्तर भारत (North)"),
                        Pair("RAJASTHAN", "राजस्थान (Rajasthan)")
                    )
                    items(regions) { (code, label) ->
                        val isSelected = (selectedRegion == code)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                .border(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                                .clickable { selectedRegion = code }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .testTag("filter_region_$code")
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }
            }
        }

        // Festival List Header
        item {
            SectionHeader(
                titleHi = "प्रमुख हिन्दू पर्व एवं त्यौहार (Major Festivals)",
                titleEn = "Major Festivals & Fasting Days",
                subtitleHi = "राजस्थान व उत्तर भारत विशेष पर्वों सहित",
                subtitleEn = "Including regional Rajasthan & North Indian festivals"
            )
        }

        // Festivals List
        items(filteredFestivals) { festival ->
            FestivalCard(
                festival = festival,
                onClick = { selectedFestivalDetail = festival }
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    selectedFestivalDetail?.let { festival ->
        FestivalDetailDialog(
            festival = festival,
            onDismiss = { selectedFestivalDetail = null }
        )
    }
}
}

@Composable
fun FestivalCard(
    festival: FestivalData,
    onClick: () -> Unit = {}
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = LanguageManager.getString(festival.nameHi, festival.nameEn),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 16.sp
                        )
                    )
                    Text(
                        text = "${festival.dateString} (${getLocalizedDay(festival.dayNameHi)})",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp
                        )
                    )
                }

                if (festival.regionFilter != "ALL") {
                    GlassBadge(
                        text = if (festival.regionFilter == "RAJASTHAN") LanguageManager.getString("राजस्थान विशेष", "Rajasthan Spl") else LanguageManager.getString("उत्तर भारत", "North India"),
                        backgroundColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                        textColor = MaterialTheme.colorScheme.secondary,
                        borderColor = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = LanguageManager.getString(festival.significanceHi, festival.significanceEn),
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, lineHeight = 20.sp)
            )

            if (festival.pujaVidhiHi.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(8.dp)
                ) {
                    Text(
                        text = "पूजन विधि: ${festival.pujaVidhiHi}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun FestivalDetailDialog(
    festival: FestivalData,
    onDismiss: () -> Unit
) {
    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = LanguageManager.getString(festival.nameHi, festival.nameEn),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 19.sp
                        )
                    )
                    Text(
                        text = "${festival.dateString} (${getLocalizedDay(festival.dayNameHi)})",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp
                        )
                    )
                }
                IconButton(
                    onClick = onDismiss
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = com.example.util.LanguageManager.getString("कैलेंडर विवरण बंद करें (Close Calendar Details)", "Close Calendar Details"),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Badges Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    GlassBadge(text = "${festival.monthNameHi} ${festival.pakshaHi}")
                    GlassBadge(text = "तिथि: ${festival.tithiHi}", textColor = MaterialTheme.colorScheme.primary, borderColor = MaterialTheme.colorScheme.primary)
                    if (festival.regionFilter != "ALL") {
                        GlassBadge(
                            text = if (festival.regionFilter == "RAJASTHAN") LanguageManager.getString("राजस्थान विशेष", "Rajasthan Spl") else LanguageManager.getString("उत्तर भारत", "North India"),
                            textColor = MaterialTheme.colorScheme.secondary,
                            borderColor = MaterialTheme.colorScheme.secondary
                        )
                    }
                }

                // Section 1: Significance (धार्मिक व आध्यात्मिक महत्व)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.dp, GlassCardBorder, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "धार्मिक एवं आध्यात्मिक महत्व (Significance)",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 15.sp
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = LanguageManager.getString(festival.significanceHi, festival.significanceEn),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp,
                                lineHeight = 20.sp
                            )
                        )
                    }
                }

                // Section 2: Rituals & Puja Vidhi (पूजा विधि व नियम)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.dp, GlassCardBorder, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Book,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "पूजा विधि व नियम (Rituals & Puja Vidhi)",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 15.sp
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (festival.pujaVidhiHi.isNotBlank()) festival.pujaVidhiHi else "प्रातःकाल स्नान कर शुद्ध वस्त्र धारण करें एवं इष्टदेव का ध्यान करते हुए पूजन व अर्घ्य अर्पित करें।",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp,
                                lineHeight = 20.sp
                            )
                        )
                    }
                }

                // Section 3: Regional History (प्रांतीय इतिहास व परंपराएं)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.dp, GlassCardBorder, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "प्रांतीय इतिहास व लोक परंपराएं (Regional History)",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 15.sp
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = festival.regionalHistoryHi,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp,
                                lineHeight = 20.sp
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("close_festival_detail_button")
            ) {
                Text(
                    text = "बंद करें (Close)",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surfaceVariant
    )
}
