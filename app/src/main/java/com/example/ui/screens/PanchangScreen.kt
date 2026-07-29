package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.util.lerp
import kotlin.math.absoluteValue
import java.util.Calendar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.astro.PanchangCalculator
import com.example.data.model.CityLocation
import com.example.data.model.PanchangData
import com.example.ui.MainViewModel
import com.example.ui.components.DailyPanchangCard
import com.example.ui.components.GlassBadge
import com.example.ui.components.GlassCard
import com.example.ui.components.NorthIndianChart
import com.example.ui.components.SectionHeader
import com.example.ui.theme.AuspiciousGreen
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.InauspiciousRed
import com.example.ui.theme.NeutralOrange
import com.example.ui.theme.MinimalistGold
import com.example.ui.theme.PremiumGold
import com.example.util.LanguageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest
import android.content.pm.PackageManager
import android.view.HapticFeedbackConstants
import androidx.core.content.ContextCompat
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView

import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import com.example.ui.components.SubTabHeader
import com.example.ui.AppTab

import com.example.ui.components.CelestialBackground

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun PanchangScreen(
    viewModel: MainViewModel,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null
) {
    val panchang by viewModel.panchangState.collectAsState()
    val generatedKundali by viewModel.generatedKundali.collectAsState()
    val selectedCity by viewModel.selectedCity.collectAsState()
    val isChoghadiyaDaytime by viewModel.choghadiyaDaytime.collectAsState()
    val choghadiyaSlots = viewModel.choghadiyaSlots

    val currentSubTab by viewModel.panchangSubTab.collectAsState()

    var showCityDropdown by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val view = LocalView.current

    val locationPermissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineGranted || coarseGranted) {
            viewModel.detectGPSLocation(context) { success ->
                if (!success) {
                    Toast.makeText(context, "स्थिति प्राप्त करने में असमर्थ (Unable to fetch location)", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(context, "अनुमति अस्वीकार कर दी गई (Permission Denied)", Toast.LENGTH_SHORT).show()
        }
    }

    val isStartupComplete by viewModel.isStartupComplete.collectAsState()

    CelestialBackground(deferred = !isStartupComplete) {
        Scaffold(
            containerColor = Color.Transparent,
            modifier = Modifier.fillMaxSize()
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = paddingValues.calculateTopPadding())
            ) {
            SubTabHeader(
                selectedTab = currentSubTab,
                tabs = listOf(
                    LanguageManager.getString("दैनिक पंचांग", "Daily Panchang"),
                    LanguageManager.getString("मासिक कैलेंडर", "Monthly Calendar")
                ),
                onTabSelected = { viewModel.setPanchangSubTab(it) },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            if (currentSubTab == 1) {
                CalendarScreen(viewModel)
            } else {
                val isPanchangLoading by viewModel.isPanchangLoading.collectAsState()
                PullToRefreshBox(
                    isRefreshing = isPanchangLoading,
                    onRefresh = { viewModel.refreshPanchang() },
                    modifier = Modifier.fillMaxSize().testTag("panchang_swipe_refresh")
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            top = 8.dp,
                            end = 16.dp,
                            bottom = paddingValues.calculateBottomPadding() + 16.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
        // Hero Header (Clean and Minimal with Date Bar)
        item {
            Spacer(modifier = Modifier.height(8.dp))
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = panchang.dateString,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            )
                            Text(
                                text = "${panchang.dayOfWeekHindi} | ${panchang.pakshaHindi}",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }

                        // City Location Picker Pill
                        var expanded by remember { mutableStateOf(false) }
                        var searchQuery by remember { mutableStateOf("") }
                        val cities = PanchangCalculator.popularCities
                        val filteredCities = if (searchQuery.isBlank()) {
                            cities
                        } else {
                            cities.filter { it.cityNameHindi.contains(searchQuery, ignoreCase = true) || it.cityName.contains(searchQuery, ignoreCase = true) }
                        }

                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded },
                            modifier = Modifier.width(180.dp)
                        ) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = {
                                    searchQuery = it
                                    expanded = true
                                },
                                label = { Text("Search City", fontSize = 12.sp, maxLines = 1, softWrap = false, overflow = TextOverflow.Ellipsis) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = GlassBorder,
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                                ),
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryEditable).fillMaxWidth()
                            )

                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("📍 वर्तमान स्थान (Current GPS)") },
                                    onClick = {
                                        expanded = false
                                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                                            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                            viewModel.detectGPSLocation(context) { success ->
                                                if (!success) {
                                                    Toast.makeText(context, "स्थिति प्राप्त करने में असमर्थ (Unable to fetch location)", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        } else {
                                            locationPermissionsLauncher.launch(
                                                arrayOf(
                                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                                )
                                            )
                                        }
                                    }
                                )

                                filteredCities.forEach { city ->
                                    DropdownMenuItem(
                                        text = { Text("${city.cityNameHindi} (${city.cityName})") },
                                        onClick = {
                                            viewModel.setCity(city)
                                            searchQuery = city.cityNameHindi
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Date Navigation Row (Previous Day, Today, Next Day)
                    val selectedDate by viewModel.selectedDate.collectAsState()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = {
                                val cal = Calendar.getInstance().apply { time = selectedDate }
                                cal.add(Calendar.DAY_OF_YEAR, -1)
                                viewModel.setDate(cal.time)
                            },
                            modifier = Modifier.testTag("panchang_prev_day_button")
                        ) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Day", tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("पिछला दिन", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                        }

                        TextButton(
                            onClick = {
                                viewModel.setDate(java.util.Date())
                            },
                            modifier = Modifier.testTag("panchang_today_button")
                        ) {
                            Icon(Icons.Default.Today, contentDescription = "Today", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("आज (Today)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                        }

                        TextButton(
                            onClick = {
                                val cal = Calendar.getInstance().apply { time = selectedDate }
                                cal.add(Calendar.DAY_OF_YEAR, 1)
                                viewModel.setDate(cal.time)
                            },
                            modifier = Modifier.testTag("panchang_next_day_button")
                        ) {
                            Text("अगला दिन", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(2.dp))
                            Icon(Icons.Default.ChevronRight, contentDescription = "Next Day", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }

        // Daily Panchang Summary Card
        item {
            val selectedCity by viewModel.selectedCity.collectAsState()
            DailyPanchangCard(
                panchang = panchang,
                locationName = selectedCity.cityNameHindi
            )
        }

        // Daily Astrology Insights Swiping Carousel (HorizontalPager)
        item {
            val insightsPagerState = rememberPagerState(pageCount = { 4 })

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "✨ दैनिक ज्योतिष अंतर्दृष्टि (Swipe Cards) ✨",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        (0..3).forEach { index ->
                            val isSelected = insightsPagerState.currentPage == index
                            Box(
                                modifier = Modifier
                                    .size(if (isSelected) 8.dp else 5.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                            )
                        }
                    }
                }

                HorizontalPager(
                    state = insightsPagerState,
                    modifier = Modifier.fillMaxWidth().testTag("panchang_insights_pager"),
                    pageSpacing = 12.dp
                ) { page ->
                    val pageOffset = ((insightsPagerState.currentPage - page) + insightsPagerState.currentPageOffsetFraction)
                    val absOffset = pageOffset.absoluteValue

                    val cardScale = lerp(0.93f, 1f, 1f - absOffset.coerceIn(0f, 1f))
                    val cardAlpha = lerp(0.6f, 1f, 1f - absOffset.coerceIn(0f, 1f))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer {
                                scaleX = cardScale
                                scaleY = cardScale
                                alpha = cardAlpha
                            }
                    ) {
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            when (page) {
                                0 -> {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Lightbulb, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("आज का शुभ विचार व पंचांग ज्ञान", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 15.sp)
                                        }
                                        Text(
                                            text = "तिथि ${panchang.tithiHindi} (${panchang.pakshaHindi}) में शुभ कार्यों का शुभारंभ फलदायी रहता है। आज ${panchang.nakshatraHindi} नक्षत्र एवं ${panchang.yogaHindi} योग का प्रभाव रहेगा।",
                                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp, lineHeight = 19.sp)
                                        )
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            GlassBadge("सूर्योदय: ${panchang.sunrise}")
                                            GlassBadge("सूर्यास्त: ${panchang.sunset}")
                                        }
                                    }
                                }
                                1 -> {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.NightsStay, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("चंद्र कला व राहु काल सतर्कता", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary, fontSize = 15.sp)
                                        }
                                        Text(
                                            text = "चंद्र राशि: ${panchang.moonSign} (${panchang.pakshaHindi})। चंद्रोदय: ${panchang.moonrise}।",
                                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp)
                                        )
                                        GlassBadge(
                                            text = "⚠️ राहु काल: ${panchang.rahuKaal} (अशुभ समय)",
                                            textColor = InauspiciousRed,
                                            borderColor = InauspiciousRed
                                        )
                                    }
                                }
                                2 -> {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Schedule, contentDescription = null, tint = AuspiciousGreen)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("चौघड़िया व शुभ मुहूर्त विचार", fontWeight = FontWeight.Bold, color = AuspiciousGreen, fontSize = 15.sp)
                                        }
                                        Text(
                                            text = "अभिजीत मुहूर्त: ${panchang.abhijitMuhurat} (सर्वश्रेष्ठ मुहूर्त)। आज यमगण्‍ड काल ${panchang.yamaganda} में रहेगा।",
                                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp)
                                        )
                                        GlassBadge(
                                            text = "🌟 अभिजीत मुहूर्त: ${panchang.abhijitMuhurat}",
                                            textColor = AuspiciousGreen,
                                            borderColor = AuspiciousGreen
                                        )
                                    }
                                }
                                else -> {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = PremiumGold)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("नक्षत्र ऊर्जा व गोचर प्रभाव", fontWeight = FontWeight.Bold, color = PremiumGold, fontSize = 15.sp)
                                        }
                                        Text(
                                            text = "नक्षत्र ${panchang.nakshatraHindi} (चरण ${panchang.nakshatraPada}) चंद्र प्रभाव ${panchang.moonSign} राशि में कार्यसिद्धि प्रदान करता है।",
                                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp)
                                        )
                                        GlassBadge("सूर्य राशि: ${panchang.sunSign}", textColor = PremiumGold, borderColor = PremiumGold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Vikram Samvat & Masa Info Bar
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    InfoPill("विक्रम संवत", "${panchang.vikramSamvat}")
                    InfoPill("शक संवत", "${panchang.sakaSamvat}")
                    InfoPill("मास (Month)", panchang.masaNameHindi.substringBefore(" "))
                }
            }
        }

        // 5 Core Panchang Elements Section
        item {
            SectionHeader(
                titleHi = "पंचांग के 5 मुख्य अंग (Panchang Elements)",
                titleEn = "Core 5 Panchang Elements",
                subtitleHi = "तिथि, नक्षत्र, योग, करण एवं वार",
                subtitleEn = "Tithi, Nakshatra, Yoga, Karan & Var"
            )
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Tithi Section
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "तिथि (Tithi)",
                                style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                            )
                            GlassBadge(text = panchang.pakshaHindi.substringBefore(" "))
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = panchang.tithiHindi,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 18.sp
                            )
                        )
                        Text(
                            text = panchang.tithiEndTime,
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { (panchang.tithiProgressPercent / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }

                    androidx.compose.material3.HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f), thickness = 1.dp)

                    // Nakshatra Section
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "नक्षत्र (Nakshatra)",
                                style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                            )
                            GlassBadge(text = "चंद्र नक्षत्र: ${panchang.moonSign}")
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${panchang.nakshatraHindi} (चरण ${panchang.nakshatraPada})",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 18.sp
                            )
                        )
                        Text(
                            text = panchang.nakshatraEndTime,
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { 0.65f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }

                    androidx.compose.material3.HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f), thickness = 1.dp)

                    // Yoga & Karan Section
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "योग (Yoga)",
                                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = panchang.yogaHindi,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 16.sp
                                )
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "करण (Karan)",
                                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = panchang.karanHindi,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 16.sp
                                )
                            )
                        }
                    }
                }
            }
        }

        // Sun & Moon Timings
        item {
            SectionHeader(
                titleHi = "सूर्य एवं चन्द्र समय (Sun & Moon Timings)",
                titleEn = "Sun & Moon Timings"
            )
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TimingColumn("सूर्योदय", panchang.sunrise, Icons.Default.WbSunny, MaterialTheme.colorScheme.primary)
                    TimingColumn("सूर्यास्त", panchang.sunset, Icons.Default.WbSunny, MaterialTheme.colorScheme.secondary)
                    TimingColumn("चन्द्रास्त", panchang.moonset, Icons.Default.NightsStay, MaterialTheme.colorScheme.onSurfaceVariant)
                    TimingColumn("चन्द्रोदय", panchang.moonrise, Icons.Default.NightsStay, MaterialTheme.colorScheme.primary)
                }
            }
        }

        // Auspicious / Inauspicious Muhurats
        item {
            SectionHeader(
                titleHi = "शुभ एवं अशुभ मुहूर्त (Auspicious & Rahu Timings)",
                titleEn = "Auspicious & Inauspicious Times"
            )
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    val timings = listOf(
                        Triple("अभिजित मुहूर्त (Abhijit)", panchang.abhijitMuhurat, Pair("अति शुभ (Best)", AuspiciousGreen)),
                        Triple("राहुकाल (Rahu Kaal)", panchang.rahuKaal, Pair("अशुभ (Avoid)", InauspiciousRed)),
                        Triple("गुलिक काल (Gulika)", panchang.gulikaKaal, Pair("मध्यम (Neutral)", NeutralOrange)),
                        Triple("यमगण्ड (Yamaganda)", panchang.yamaganda, Pair("अशुभ (Avoid)", InauspiciousRed))
                    )

                    timings.forEachIndexed { index, timing ->
                        val (title, time, status) = timing
                        val (statusText, color) = status
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.titleSmall.copy(color = color, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = time,
                                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                                )
                            }

                            GlassBadge(
                                text = statusText,
                                backgroundColor = color.copy(alpha = 0.15f),
                                textColor = color,
                                borderColor = color.copy(alpha = 0.4f)
                            )
                        }

                        if (index < timings.lastIndex) {
                            androidx.compose.material3.HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f), thickness = 1.dp)
                        }
                    }
                }
            }
        }

        // Choghadiya Strip Section
        item {
            SectionHeader(
                titleHi = "आज का चौघड़िया (Today's Choghadiya)",
                titleEn = "Choghadiya Time Strip",
                subtitleHi = "शुभ, अमृत, लाभ, चर, उद्वेग, काल व रोग",
                subtitleEn = "Real-time Choghadiya calculations for ${panchang.locationName}"
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isChoghadiyaDaytime) "दिन का चौघड़िया (Day)" else "रात्रि चौघड़िया (Night)",
                    style = MaterialTheme.typography.titleSmall.copy(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                )

                Row {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isChoghadiyaDaytime) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                            .clickable {
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                viewModel.toggleChoghadiyaDayNight(true)
                            }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "दिन (Day)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isChoghadiyaDaytime) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                                fontSize = 11.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (!isChoghadiyaDaytime) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                            .clickable {
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                viewModel.toggleChoghadiyaDayNight(false)
                            }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "रात (Night)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (!isChoghadiyaDaytime) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }
        }

        // Horizontal Choghadiya Strip Cards
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(choghadiyaSlots) { slot ->
                    val statusColor = when (slot.type.name) {
                        "AMRIT", "SHUBH", "LABH" -> AuspiciousGreen
                        "CHAR" -> NeutralOrange
                        else -> InauspiciousRed
                    }

                    GlassCard(
                        modifier = Modifier.width(130.dp),
                        borderColor = statusColor.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column {
                            Text(
                                text = slot.type.nameHi,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 15.sp
                                )
                            )
                            Text(
                                text = slot.type.natureHi,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = statusColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = slot.timeSlotString,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }
            }
        }

        // Quick Kundali Chart Preview Card (Shared Element Transition to Kundali Screen)
        item {
            SectionHeader(
                titleHi = "आज का कुण्डली चार्ट (Kundali Chart Preview)",
                titleEn = "Today's Birth Chart",
                subtitleHi = "टैप करके पूर्ण इंटरएक्टिव कुण्डली देखें",
                subtitleEn = "Tap to view full interactive Kundali"
            )
        }

        item {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        viewModel.selectTab(AppTab.KUNDALI)
                    }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = PremiumGold,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "लग्न: ${generatedKundali.ascendantRashiHi}",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                        TextButton(onClick = {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            viewModel.selectTab(AppTab.KUNDALI)
                        }) {
                            Text(
                                text = "पूर्ण कुण्डली देखें →",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = PremiumGold,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    val chartModifier = if (sharedTransitionScope != null && animatedVisibilityScope != null) {
                        with(sharedTransitionScope) {
                            Modifier
                                .fillMaxWidth(0.85f)
                                .sharedElement(
                                    state = rememberSharedContentState(key = "kundali_chart_shared_element"),
                                    animatedVisibilityScope = animatedVisibilityScope
                                )
                        }
                    } else Modifier.fillMaxWidth(0.85f)

                    NorthIndianChart(
                        chartData = generatedKundali,
                        modifier = chartModifier,
                        onHouseClick = { _, _, _ -> viewModel.selectTab(AppTab.KUNDALI) }
                    )
                }
            }
        }

        item {
            PlanetaryPositionsCard(planets = panchang.planets)
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
}
}
}
}
}

@Composable
fun InfoPill(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        )
    }
}

@Composable
fun PanchangElementCard(
    titleHi: String,
    titleEn: String,
    valueHi: String,
    subValueHi: String,
    progress: Float,
    badgeText: String
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = titleHi,
                    style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                )
                GlassBadge(text = badgeText)
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = valueHi,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 18.sp
                )
            )

            Text(
                text = subValueHi,
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

@Composable
fun SmallElementCard(titleHi: String, valueHi: String) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Text(
                text = titleHi,
                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = valueHi,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 15.sp
                )
            )
        }
    }
}

@Composable
fun TimingColumn(title: String, time: String, icon: ImageVector, iconColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = title, style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp))
        Text(text = time, style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 14.sp))
    }
}

@Composable
fun MuhuratTimeRow(titleHi: String, timeStr: String, statusText: String, color: Color) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        borderColor = color.copy(alpha = 0.25f),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = titleHi,
                    style = MaterialTheme.typography.titleSmall.copy(color = color, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = timeStr,
                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                )
            }

            GlassBadge(
                text = statusText,
                backgroundColor = color.copy(alpha = 0.15f),
                textColor = color,
                borderColor = color.copy(alpha = 0.4f)
            )
        }
    }
}
@Composable
fun PlanetaryPositionsCard(planets: List<com.example.data.model.PlanetPosition>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SectionHeader(
            titleHi = "ग्रह स्थिति (Planetary Positions)",
            titleEn = "Current Astrological Positions"
        )
        Spacer(modifier = Modifier.height(12.dp))

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                planets.forEach { planet ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = planet.planetNameHi.substring(0, 1),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "${planet.planetNameHi} (${planet.planetNameEn})",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 15.sp
                                    )
                                )
                                Text(
                                    text = "नक्षत्र: ${planet.nakshatraHi} | राशि: ${planet.rashiNameHi}",
                                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp)
                                )
                            }
                        }
                        
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${planet.degree}°",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            )
                            if (planet.isRetrograde) {
                                Text(
                                    text = "Retrograde (वक्री)",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = InauspiciousRed,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
