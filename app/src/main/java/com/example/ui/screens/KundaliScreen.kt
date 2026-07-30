package com.example.ui.screens

import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.filled.Share
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
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
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.Scaffold
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PlanetPosition
import com.example.ui.MainViewModel
import com.example.data.local.RecentSearchEntity
import com.example.ui.components.AstroLoadingIndicator
import com.example.ui.components.DashaHorizontalTimeline
import com.example.ui.components.GlassBadge
import com.example.ui.components.GlassCard
import com.example.ui.components.GoldGlowButton
import com.example.ui.components.NorthIndianChart
import com.example.ui.components.RecentSearchesComponent
import com.example.ui.components.SectionHeader
import com.example.ui.components.SouthIndianChart
import com.example.ui.components.TransitWheelChart
import com.example.util.LanguageManager
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Schedule
import com.example.ui.components.M3DatePickerDialog
import com.example.ui.components.M3TimePickerDialog

import com.example.ui.components.CelestialBackground
import com.example.ui.components.SubTabHeader
import com.example.ui.theme.GlassBorder
import com.example.ui.AppTab
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

@Composable
fun TransitScreen(viewModel: MainViewModel) {
    val birthKundali by viewModel.generatedKundali.collectAsState()
    val transitKundali by viewModel.transitKundali.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SectionHeader(
                titleHi = "वर्तमान गोचर (Current Transits)",
                titleEn = "Planetary Transits"
            )
            Text(
                text = LanguageManager.getString(
                    "यह आपकी जन्म कुण्डली के सापेक्ष वर्तमान ग्रहों की स्थिति दर्शाता है।",
                    "This shows current planetary positions relative to your birth chart."
                ),
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = LanguageManager.getString("गोचर चक्र (Transit Wheel)", "Transit Wheel Visualization"),
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    val transit = transitKundali
                    if (transit != null && transit.planets.isNotEmpty()) {
                        TransitWheelChart(
                            birthData = birthKundali,
                            transitData = transit
                        )
                    } else {
                        AstroLoadingIndicator()
                    }
                }
            }
        }

        item {
            SectionHeader(
                titleHi = "गोचर ग्रह स्थिति (Transit Details)",
                titleEn = "Transit Positions"
            )
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                val transit = transitKundali
                if (transit != null) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)) {
                            Text(text = LanguageManager.getString("ग्रह", "Planet"), style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Normal, fontSize = 13.sp), modifier = Modifier.weight(1f))
                            Text(text = LanguageManager.getString("जन्म राशि", "Birth"), style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Normal, fontSize = 13.sp), modifier = Modifier.weight(1f))
                            Text(text = LanguageManager.getString("गोचर राशि", "Transit"), style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Normal, fontSize = 13.sp), modifier = Modifier.weight(1f))
                            Text(text = LanguageManager.getString("भाव", "House"), style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Normal, fontSize = 13.sp), modifier = Modifier.weight(0.8f))
                        }
                        
                        transit.planets.forEach { transitPlanet ->
                            val birthPlanet = birthKundali.planets.find { it.planetNameEn == transitPlanet.planetNameEn }
                            
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(text = LanguageManager.getString(transitPlanet.planetNameHi.substringBefore(" "), transitPlanet.planetNameEn), style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp), modifier = Modifier.weight(1f))
                                Text(text = birthPlanet?.rashiNameHi ?: "-", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp), modifier = Modifier.weight(1f))
                                Text(text = transitPlanet.rashiNameHi, style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, fontWeight = FontWeight.Normal), modifier = Modifier.weight(1f))
                                
                                // House of transit relative to birth Lagna
                                val transitHouse = ((transitPlanet.rashiNumber - birthKundali.ascendantRashiNumber + 12) % 12) + 1
                                Text(text = "$transitHouse H", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.secondary, fontSize = 14.sp), modifier = Modifier.weight(0.8f))
                            }
                        }
                    }
                } else {
                    AstroLoadingIndicator()
                }
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun KundaliScreen(
    viewModel: MainViewModel,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null
) {
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    var isSharingChart by remember { mutableStateOf(false) }
    val currentSubTab by viewModel.kundaliSubTab.collectAsState()

    val kundali by viewModel.generatedKundali.collectAsState()

    var isNorthStyle by remember { mutableStateOf(true) }
    var showForm by remember { mutableStateOf(false) }

    var nameInput by remember { mutableStateOf(kundali.personName) }
    var dobInput by remember { mutableStateOf(kundali.dateOfBirth) }
    var tobInput by remember { mutableStateOf(kundali.timeOfBirth) }
    var placeInput by remember { mutableStateOf(kundali.placeOfBirth) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    var isSaved by remember { mutableStateOf(false) }

    if (showDatePicker) {
        M3DatePickerDialog(
            initialDateString = dobInput,
            onDateSelected = { selected ->
                dobInput = selected
                viewModel.kundaliDob.value = selected
            },
            onDismiss = { showDatePicker = false }
        )
    }

    if (showTimePicker) {
        M3TimePickerDialog(
            initialTimeString = tobInput,
            onTimeSelected = { selected ->
                tobInput = selected
                viewModel.kundaliTob.value = selected
            },
            onDismiss = { showTimePicker = false }
        )
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
                    LanguageManager.getString("जन्म कुंडली", "Birth Chart"),
                    LanguageManager.getString("गुण मिलान", "Guna Matching"),
                    LanguageManager.getString("अंकशास्त्र व AI", "Astro AI"),
                    LanguageManager.getString("गोचर (Transits)", "Transits")
                ),
                onTabSelected = { 
                    viewModel.setKundaliSubTab(it)
                    if (it == 3) viewModel.calculateCurrentTransits()
                },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            when (currentSubTab) {
                1 -> MatchingScreen(viewModel)
                2 -> NumerologyScreen(viewModel)
                3 -> TransitScreen(viewModel)
                else -> {
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
        item {
            Spacer(modifier = Modifier.height(8.dp))
            SectionHeader(
                titleHi = "जन्म कुण्डली (Vedic Birth Chart D1)",
                titleEn = "Birth Chart Generator",
                actionButtonText = if (showForm) "कुण्डली देखें" else "जन्म विवरण बदलें",
                onActionClick = { showForm = !showForm }
            )
            val recentSearches by viewModel.recentSearches.collectAsState()
            RecentSearchesComponent(
                recentSearches = recentSearches.filter { it.type == "KUNDALI" },
                onSearchSelected = { search ->
                    val parts = search.data.split("|")
                    if (parts.size == 4) {
                        nameInput = parts[0]
                        dobInput = parts[1]
                        tobInput = parts[2]
                        placeInput = parts[3]
                        viewModel.generateKundaliChart(parts[0], parts[1], parts[2], parts[3])
                    }
                }
            )
        }

        // Input Form Card (When expanding form)
        if (showForm) {
            item {
                val savedProfiles by viewModel.savedProfiles.collectAsState()
                var expandedProfileList by remember { mutableStateOf(false) }
                var profileSearchQuery by remember { mutableStateOf("") }

                val filteredProfiles = if (profileSearchQuery.isBlank()) {
                    savedProfiles
                } else {
                    savedProfiles.filter { it.name.contains(profileSearchQuery, ignoreCase = true) }
                }

                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = LanguageManager.getString("जन्म विवरण दर्ज करें", "Enter Birth Details"),
                                style = MaterialTheme.typography.titleSmall.copy(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            )
                        }

                        if (savedProfiles.isNotEmpty()) {
                            ExposedDropdownMenuBox(
                                expanded = expandedProfileList,
                                onExpandedChange = { expandedProfileList = !expandedProfileList },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    value = profileSearchQuery,
                                    onValueChange = {
                                        profileSearchQuery = it
                                        expandedProfileList = true
                                    },
                                    label = { Text("Search Saved Profiles") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedProfileList) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = GlassBorder,
                                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    modifier = Modifier.menuAnchor().fillMaxWidth()
                                )

                                ExposedDropdownMenu(
                                    expanded = expandedProfileList,
                                    onDismissRequest = { expandedProfileList = false },
                                    modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    filteredProfiles.forEach { profile ->
                                        androidx.compose.material3.DropdownMenuItem(
                                            text = { Text(profile.name, color = MaterialTheme.colorScheme.onSurface) },
                                            onClick = {
                                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                expandedProfileList = false
                                                profileSearchQuery = profile.name
                                                nameInput = profile.name
                                                dobInput = profile.dateOfBirth
                                                tobInput = profile.timeOfBirth
                                                placeInput = profile.placeOfBirth
                                                viewModel.kundaliName.value = profile.name
                                                viewModel.kundaliDob.value = profile.dateOfBirth
                                                viewModel.kundaliTob.value = profile.timeOfBirth
                                                viewModel.kundaliPlace.value = profile.placeOfBirth
                                                viewModel.generateKundaliChart(
                                                    name = profile.name,
                                                    dob = profile.dateOfBirth,
                                                    tob = profile.timeOfBirth,
                                                    place = profile.placeOfBirth
                                                )
                                                showForm = false
                                            },
                                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                                        )
                                    }
                                }
                            }
                        }

                        val tfColors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = GlassBorder,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        OutlinedTextField(
                            value = nameInput,
                            onValueChange = {
                                nameInput = it
                                viewModel.kundaliName.value = it
                            },
                            label = { Text(LanguageManager.getString("पूरा नाम", "Full Name")) },
                            colors = tfColors,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_kundali_name")
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                OutlinedTextField(
                                    value = dobInput,
                                    onValueChange = {
                                        dobInput = it
                                        viewModel.kundaliDob.value = it
                                    },
                                    readOnly = true,
                                    label = { Text(LanguageManager.getString("जन्म तिथि", "DOB")) },
                                    trailingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.CalendarMonth,
                                            contentDescription = "Select DOB",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    },
                                    colors = tfColors,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("input_kundali_dob")
                                )
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .clickable {
                                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                            showDatePicker = true
                                        }
                                )
                            }

                            Box(modifier = Modifier.weight(1f)) {
                                OutlinedTextField(
                                    value = tobInput,
                                    onValueChange = {
                                        tobInput = it
                                        viewModel.kundaliTob.value = it
                                    },
                                    readOnly = true,
                                    label = { Text(LanguageManager.getString("जन्म समय", "Time")) },
                                    trailingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Schedule,
                                            contentDescription = "Select TOB",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    },
                                    colors = tfColors,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("input_kundali_tob")
                                )
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .clickable {
                                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                            showTimePicker = true
                                        }
                                )
                            }
                        }

                        OutlinedTextField(
                            value = placeInput,
                            onValueChange = {
                                placeInput = it
                                viewModel.kundaliPlace.value = it
                            },
                            label = { Text(LanguageManager.getString("जन्म स्थान", "Place of Birth")) },
                            colors = tfColors,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_kundali_place")
                        )

                        val isCalculating by viewModel.isCalculating.collectAsState()
                        if (isCalculating) {
                            AstroLoadingIndicator()
                        } else {
                            GoldGlowButton(
                                text = LanguageManager.getString("कुण्डली बनाएं (Generate Chart)", "Generate Chart"),
                                onClick = {
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    viewModel.kundaliName.value = nameInput
                                    viewModel.kundaliDob.value = dobInput
                                    viewModel.kundaliTob.value = tobInput
                                    viewModel.kundaliPlace.value = placeInput
                                    viewModel.generateKundaliChart(nameInput, dobInput, tobInput, placeInput)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                testTag = "generate_chart_submit_button"
                            )
                        }
                    }
                }
            }
        }
        // Person Summary Header Bar
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = kundali.personName,
                            style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        )
                        Text(
                            text = "जन्म: ${kundali.dateOfBirth} | ${kundali.timeOfBirth} | ${kundali.placeOfBirth}",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                val shareText = """
                                    ✨ AstroVeda Kundali ✨
                                    Name: ${kundali.personName}
                                    DOB: ${kundali.dateOfBirth} | ${kundali.timeOfBirth}
                                    Place: ${kundali.placeOfBirth}
                                    
                                    Lagna (Ascendant): ${kundali.ascendantRashiHi}
                                    Moon Sign (Rashi): ${kundali.moonRashiHi}
                                    Nakshatra: ${kundali.moonNakshatraHi}
                                """.trimIndent()

                                val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(android.content.Intent.EXTRA_SUBJECT, "AstroVeda Kundali")
                                    putExtra(android.content.Intent.EXTRA_TEXT, shareText)
                                }
                                context.startActivity(android.content.Intent.createChooser(shareIntent, "Share Kundali"))
                            }
                    )
                }
            }
        }

        // Chart View Section
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "जन्म कुण्डली (Birth Chart)",
                            style = MaterialTheme.typography.titleSmall.copy(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        )
                        // Style Toggle
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .clickable {
                                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        isNorthStyle = true
                                    }
                                    .background(if (isNorthStyle) MaterialTheme.colorScheme.primary else Color.Transparent)
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = LanguageManager.getString("उत्तर भारतीय (North)", "North Indian"),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (isNorthStyle) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = if (isNorthStyle) FontWeight.Normal else FontWeight.Normal
                                    )
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clickable {
                                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        isNorthStyle = false
                                    }
                                    .background(if (!isNorthStyle) MaterialTheme.colorScheme.primary else Color.Transparent)
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = LanguageManager.getString("दक्षिण भारतीय (South)", "South Indian"),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (!isNorthStyle) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = if (!isNorthStyle) FontWeight.Normal else FontWeight.Normal
                                    )
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    if (isNorthStyle) {
                        val chartModifier = if (sharedTransitionScope != null && animatedVisibilityScope != null) {
                            with(sharedTransitionScope) {
                                Modifier.sharedElement(
                                    state = rememberSharedContentState(key = "kundali_chart_shared_element"),
                                    animatedVisibilityScope = animatedVisibilityScope
                                )
                            }
                        } else Modifier

                        NorthIndianChart(
                            chartData = kundali,
                            modifier = chartModifier
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        if (isSharingChart) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                AstroLoadingIndicator(modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = LanguageManager.getString("कुण्डली चित्र तैयार किया जा रहा है...", "Generating high-quality chart image..."),
                                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.primary)
                                )
                            }
                        } else {
                            Button(
                                onClick = {
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    isSharingChart = true
                                    coroutineScope.launch {
                                        val uri = com.example.util.KundaliImageGenerator.generateAndShareChart(context, kundali)
                                        isSharingChart = false
                                        if (uri != null) {
                                            val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                                type = "image/png"
                                                putExtra(android.content.Intent.EXTRA_STREAM, uri)
                                                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                            }
                                            context.startActivity(android.content.Intent.createChooser(shareIntent, "Share Birth Chart Image"))
                                        } else {
                                            android.widget.Toast.makeText(context, "Failed to generate image", android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth(0.85f)
                                    .testTag("share_kundali_image_button"),
                                shape = RoundedCornerShape(12.dp),
                                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = "Share Chart Image",
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = LanguageManager.getString("कुण्डली चित्र शेयर करें", "Share Chart Image"),
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Normal)
                                    )
                                }
                            }
                        }
                    } else {
                        SouthIndianChart(kundali)
                    }
                }
            }
        }

        // Planetary Positions
        item {
            SectionHeader(
                titleHi = "ग्रह स्थिति (Planetary Positions)",
                titleEn = "Planetary Positions"
            )
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)) {
                        Text(text = LanguageManager.getString("ग्रह", "Planet"), style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Normal, fontSize = 13.sp), modifier = Modifier.weight(1f))
                        Text(text = LanguageManager.getString("राशि", "Zodiac"), style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Normal, fontSize = 13.sp), modifier = Modifier.weight(1.2f))
                        Text(text = LanguageManager.getString("अंश", "Deg"), style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Normal, fontSize = 13.sp), modifier = Modifier.weight(1f))
                        Text(text = LanguageManager.getString("भाव", "House"), style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Normal, fontSize = 13.sp), modifier = Modifier.weight(1f))
                    }
                    
                    kundali.planets.forEach { planet ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(text = LanguageManager.getString(planet.planetNameHi.substringBefore(" "), planet.planetNameEn), style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp), modifier = Modifier.weight(1f))
                            Text(text = planet.rashiNameHi, style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp), modifier = Modifier.weight(1.2f))
                            Text(text = "${planet.degree}°", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.primary, fontSize = 14.sp, fontWeight = FontWeight.Normal), modifier = Modifier.weight(1f))
                            Text(text = "${planet.houseNumber} भाव", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp), modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        // Horizontal Vimshottari Dasha Timeline
        item {
            DashaHorizontalTimeline(dashaTimeline = kundali.dashaTimeline)
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
