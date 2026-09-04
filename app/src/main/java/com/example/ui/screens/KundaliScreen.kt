package com.example.ui.screens

import com.example.ui.components.AstroDisclaimer
import com.example.ui.components.DisclaimerScope
import android.content.Intent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.KundaliEntity
import com.example.ui.MainViewModel
import com.example.ui.components.AstroLoadingIndicator
import com.example.ui.components.CelestialBackground
import com.example.ui.components.DashaHorizontalTimeline
import com.example.ui.components.GlassBadge
import com.example.ui.components.GlassCard
import com.example.ui.components.GoldGlowButton
import com.example.ui.components.M3DatePickerDialog
import com.example.ui.components.M3TimePickerDialog
import com.example.ui.components.NorthIndianChart
import com.example.ui.components.RecentSearchesComponent
import com.example.ui.components.SectionHeader
import com.example.ui.components.SouthIndianChart
import com.example.ui.components.SubTabHeader
import com.example.ui.components.TransitWheelChart
import com.example.ui.theme.DateTimeAccent
import com.example.ui.theme.ElevatedSurface
import com.example.ui.theme.GlassCardBorder
import com.example.ui.theme.RahuKaalDangerColor
import com.example.ui.theme.ShubhSuccessColor
import com.example.util.KundaliImageGenerator
import com.example.util.LanguageManager
import kotlinx.coroutines.launch

@Composable
fun TransitScreen(viewModel: MainViewModel) {
    val birthKundali by viewModel.generatedKundali.collectAsState()
    val transitKundali by viewModel.transitKundali.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (birthKundali == null) {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = LanguageManager.getString(
                                "गोचर देखने हेतु पहले जन्म कुण्डली बनाएं",
                                "Generate Birth Chart First for Transits"
                            ),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 17.sp
                            ),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = LanguageManager.getString(
                                "आपकी जन्म कुण्डली के सापेक्ष वर्तमान ग्रहों की स्थिति व भाव प्रभाव देखने के लिए पहले अपना जन्म विवरण दर्ज करें।",
                                "Please enter your birth details in the Birth Chart tab to view current planetary transits relative to your birth Lagna."
                            ),
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp
                            )
                        )
                        GoldGlowButton(
                            text = LanguageManager.getString("जन्म कुण्डली बनाएं", "Enter Birth Details"),
                            onClick = { viewModel.setKundaliSubTab(0) }
                        )
                    }
                }
            }
        } else {
            val validBirthKundali = birthKundali!!
            item {
                SectionHeader(
                    titleHi = "वर्तमान गोचर",
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
                            text = LanguageManager.getString("गोचर चक्र", "Transit Wheel Visualization"),
                            style = MaterialTheme.typography.titleSmall.copy(
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        val transit = transitKundali
                        if (transit != null && transit.planets.isNotEmpty()) {
                            TransitWheelChart(
                                birthData = validBirthKundali,
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
                    titleHi = "गोचर ग्रह स्थिति",
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
                                val birthPlanet = validBirthKundali.planets.find { it.planetNameEn == transitPlanet.planetNameEn }

                                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = LanguageManager.getString(transitPlanet.planetNameHi.substringBefore(" "), transitPlanet.planetNameEn), style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp), modifier = Modifier.weight(1f))
                                    Text(text = birthPlanet?.let { LanguageManager.getString(it.rashiNameHi, it.rashiNameEn) } ?: "-", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp), modifier = Modifier.weight(1f))
                                    Text(text = LanguageManager.getString(transitPlanet.rashiNameHi, transitPlanet.rashiNameEn), style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, fontWeight = FontWeight.Normal), modifier = Modifier.weight(1f))

                                    // House of transit relative to birth Lagna
                                    val transitHouse = ((transitPlanet.rashiNumber - validBirthKundali.ascendantRashiNumber + 12) % 12) + 1
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
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class, ExperimentalFoundationApi::class)
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

    // Start with clean empty form inputs unless user fills or loads a profile
    var nameInput by remember { mutableStateOf("") }
    var dobInput by remember { mutableStateOf("") }
    var tobInput by remember { mutableStateOf("") }
    var placeInput by remember { mutableStateOf("") }
    var placeSuggestions by remember { mutableStateOf<List<PlaceSuggestion>>(emptyList()) }
    var placeDropdownExpanded by remember { mutableStateOf(false) }
    // The coordinates of the chosen birth place. Null until the user picks a
    // suggestion — the chart cannot be cast without them, and silently falling
    // back to Jaipur is what made every Lagna in the app wrong.
    var placeCoords by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var formValidationError by remember { mutableStateOf<String?>(null) }

    // The calculator validates too, and its complaints are more specific than the
    // form's ("Did you enter day-month-year instead?"). Surface them in the same
    // place and reopen the form so the user can see the field they need to fix.
    val engineInputError by viewModel.kundaliInputError.collectAsState()
    LaunchedEffect(engineInputError) {
        engineInputError?.let {
            formValidationError = it
            showForm = true
            viewModel.clearKundaliInputError()
        }
    }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var isSaved by remember { mutableStateOf(false) }

    if (showDatePicker) {
        M3DatePickerDialog(
            initialDateString = dobInput.ifBlank { "1995-01-01" },
            onDateSelected = { selected ->
                dobInput = selected
                formValidationError = null
            },
            onDismiss = { showDatePicker = false }
        )
    }

    if (showTimePicker) {
        M3TimePickerDialog(
            initialTimeString = tobInput.ifBlank { "12:00" },
            onTimeSelected = { selected ->
                tobInput = selected
                formValidationError = null
            },
            onDismiss = { showTimePicker = false }
        )
    }

    val isStartupComplete by viewModel.isStartupComplete.collectAsState()

    run {
        Scaffold(
            // The outer Scaffold in MainActivity already applies the status bar inset
            // through TopHeaderBar's statusBarsPadding(). Letting this inner Scaffold
            // apply it again is what put an empty band above every sub-tab row.
            contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
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
                        LanguageManager.getString("जन्म कुण्डली", "Birth Chart"),
                        LanguageManager.getString("गुण मिलान", "Matching"),
                        LanguageManager.getString("अंकशास्त्र", "Numerology"),
                        LanguageManager.getString("गोचर", "Transits")
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
                        val currentChart = kundali

                        Column(
                            modifier = Modifier.fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(
                                start = 16.dp,
                                top = 8.dp,
                                end = 16.dp,
                                bottom = paddingValues.calculateBottomPadding() + 16.dp
                                ),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Section Header & Mode Toggle
                                Spacer(modifier = Modifier.height(4.dp))
                                SectionHeader(
                                    titleHi = "जन्म कुण्डली",
                                    titleEn = "Vedic Birth Chart Generator",
                                    actionButtonText = if (currentChart != null && !showForm) {
                                        LanguageManager.getString("विवरण बदलें", "Edit Details")
                                    } else if (currentChart != null) {
                                        LanguageManager.getString("कुण्डली देखें", "View Chart")
                                    } else null,
                                    onActionClick = {
                                        if (currentChart != null) {
                                            if (!showForm) {
                                                nameInput = currentChart.personName
                                                dobInput = currentChart.dateOfBirth
                                                tobInput = currentChart.timeOfBirth
                                                placeInput = currentChart.placeOfBirth
                                            }
                                            showForm = !showForm
                                        }
                                    }
                                )

                                val recentSearches by viewModel.recentSearches.collectAsState()
                                val kundaliSearches = recentSearches.filter { it.type == "KUNDALI" }
                                if (kundaliSearches.isNotEmpty() && (currentChart == null || showForm)) {
                                    RecentSearchesComponent(
                                        recentSearches = kundaliSearches,
                                        onSearchSelected = { search ->
                                            val parts = search.data.split("|")
                                            if (parts.size >= 4) {
                                                nameInput = parts[0]
                                                dobInput = parts[1]
                                                tobInput = parts[2]
                                                placeInput = parts[3]
                                                val lat = parts.getOrNull(4)?.toDoubleOrNull()
                                                val lng = parts.getOrNull(5)?.toDoubleOrNull()
                                                if (lat != null && lng != null) {
                                                    placeCoords = lat to lng
                                                    viewModel.generateKundaliChart(
                                                        parts[0], parts[1], parts[2], parts[3], lat, lng
                                                    )
                                                    showForm = false
                                                } else {
                                                    // A row saved before coordinates were recorded. Refill the
                                                    // form and let the user re-pick the place rather than
                                                    // casting the chart for somewhere they were not born.
                                                    placeCoords = null
                                                    showForm = true
                                                    formValidationError = LanguageManager.getString(
                                                        "कृपया जन्म स्थान दोबारा चुनें",
                                                        "Please pick the birth place again"
                                                    )
                                                }
                                            }
                                        }
                                    )
                                }

                            // ─────────────────────────────────────────────────────────────
                            // INPUT FORM (Shown if no chart generated yet OR user expanded form)
                            // ─────────────────────────────────────────────────────────────
                            if (currentChart == null || showForm) {
                                    val savedProfiles by viewModel.savedProfiles.collectAsState()
                                    var expandedProfileList by remember { mutableStateOf(false) }
                                    var profileSearchQuery by remember { mutableStateOf("") }

                                    val filteredProfiles = if (profileSearchQuery.isBlank()) {
                                        savedProfiles
                                    } else {
                                        savedProfiles.filter { it.name.contains(profileSearchQuery, ignoreCase = true) }
                                    }

                                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                            // Saved Profiles Dropdown Selector (Optional shortcut)
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
                                                        label = { Text(LanguageManager.getString("सहेजे प्रोफाइल से चुनें", "Select from Saved Profiles")) },
                                                        placeholder = { Text(LanguageManager.getString("प्रोफाइल खोजें...", "Search profile...")) },
                                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedProfileList) },
                                                        colors = OutlinedTextFieldDefaults.colors(
                                                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                                                            unfocusedBorderColor = GlassCardBorder,
                                                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                                                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                                        ),
                                                        shape = RoundedCornerShape(14.dp),
                                                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryEditable).fillMaxWidth()
                                                    )

                                                    ExposedDropdownMenu(
                                                        expanded = expandedProfileList,
                                                        onDismissRequest = { expandedProfileList = false },
                                                        modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
                                                    ) {
                                                        filteredProfiles.forEach { profile ->
                                                            DropdownMenuItem(
                                                                text = {
                                                                    Column {
                                                                        Text(profile.name, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                                                        Text("${profile.dateOfBirth} | ${profile.placeOfBirth}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                                    }
                                                                },
                                                                onClick = {
                                                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                                    expandedProfileList = false
                                                                    profileSearchQuery = profile.name
                                                                    nameInput = profile.name
                                                                    dobInput = profile.dateOfBirth
                                                                    tobInput = profile.timeOfBirth
                                                                    placeInput = profile.placeOfBirth
                                                                    // Saved profiles have carried latitude and longitude
                                                                    // all along; nothing was ever reading them.
                                                                    placeCoords = profile.latitude to profile.longitude
                                                                    viewModel.generateKundaliChart(
                                                                        name = profile.name,
                                                                        dob = profile.dateOfBirth,
                                                                        tob = profile.timeOfBirth,
                                                                        place = profile.placeOfBirth,
                                                                        lat = profile.latitude,
                                                                        lng = profile.longitude
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
                                                unfocusedBorderColor = GlassCardBorder,
                                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                                focusedLabelColor = MaterialTheme.colorScheme.primary,
                                                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                            )

                                            // 1. Full Name Input
                                            OutlinedTextField(
                                                value = nameInput,
                                                onValueChange = {
                                                    nameInput = it
                                                    formValidationError = null
                                                },
                                                label = { Text(LanguageManager.getString("पूरा नाम *", "Full Name *")) },
                                                placeholder = { Text(LanguageManager.getString("उदा. राहुल शर्मा", "e.g. Rahul Sharma")) },
                                                shape = RoundedCornerShape(14.dp),
                                                colors = tfColors,
                                                singleLine = true,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .testTag("input_kundali_name")
                                            )

                                            // 2. DOB & TOB Row
                                            //
                                            // The labels are "तिथि"/"समय" rather than
                                            // "जन्म तिथि"/"जन्म समय": these two fields are
                                            // half-width with a trailing icon, which on a
                                            // 320dp phone leaves ~47dp for the floating
                                            // label — the longer Hindi clipped to "जन्म ति".
                                            // The card is already headed जन्म कुण्डली, so the
                                            // shorter labels lose nothing.
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                // Date of Birth Field
                                                Box(modifier = Modifier.weight(1f)) {
                                                    OutlinedTextField(
                                                        value = dobInput,
                                                        onValueChange = {
                                                            dobInput = it
                                                            formValidationError = null
                                                        },
                                                        readOnly = true,
                                                        label = { Text(LanguageManager.getString("तिथि *", "Date *"), maxLines = 1, softWrap = false) },
                                                        placeholder = { Text("YYYY-MM-DD") },
                                                        trailingIcon = {
                                                            Icon(
                                                                imageVector = Icons.Default.CalendarMonth,
                                                                contentDescription = "Select DOB",
                                                                tint = MaterialTheme.colorScheme.primary
                                                            )
                                                        },
                                                        shape = RoundedCornerShape(14.dp),
                                                        colors = tfColors,
                                                        singleLine = true,
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

                                                // Time of Birth Field
                                                Box(modifier = Modifier.weight(1f)) {
                                                    OutlinedTextField(
                                                        value = tobInput,
                                                        onValueChange = {
                                                            tobInput = it
                                                            formValidationError = null
                                                        },
                                                        readOnly = true,
                                                        label = { Text(LanguageManager.getString("समय *", "Time *"), maxLines = 1, softWrap = false) },
                                                        placeholder = { Text("HH:MM") },
                                                        trailingIcon = {
                                                            Icon(
                                                                imageVector = Icons.Default.Schedule,
                                                                contentDescription = "Select TOB",
                                                                tint = MaterialTheme.colorScheme.primary
                                                            )
                                                        },
                                                        shape = RoundedCornerShape(14.dp),
                                                        colors = tfColors,
                                                        singleLine = true,
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

                                            // 3. Place of Birth Field
                                            LaunchedEffect(placeInput) {
                                                if (placeInput.length < 3) {
                                                    placeSuggestions = emptyList()
                                                    placeDropdownExpanded = false
                                                } else {
                                                    delay(400)
                                                    val results = withContext(Dispatchers.IO) {
                                                        try {
                                                            val geocoder = android.location.Geocoder(context, java.util.Locale.getDefault())
                                                            @Suppress("DEPRECATION")
                                                            val addresses = geocoder.getFromLocationName(placeInput, 5)
                                                            addresses?.mapNotNull { addr ->
                                                                // Keep the coordinates. The geocoder hands them over with
                                                                // every result and this code used to throw them away, then
                                                                // fall back to Jaipur for the actual calculation.
                                                                if (!addr.hasLatitude() || !addr.hasLongitude()) return@mapNotNull null
                                                                val city = addr.locality ?: addr.subAdminArea ?: addr.featureName
                                                                val state = addr.adminArea
                                                                val country = addr.countryName
                                                                val label = listOfNotNull(city, state, country)
                                                                    .joinToString(", ")
                                                                    .takeIf { it.isNotBlank() } ?: return@mapNotNull null
                                                                PlaceSuggestion(label, addr.latitude, addr.longitude)
                                                            }?.distinctBy { it.label } ?: emptyList()
                                                        } catch (e: Exception) {
                                                            emptyList()
                                                        }
                                                    }
                                                    placeSuggestions = results
                                                    placeDropdownExpanded = results.isNotEmpty()
                                                }
                                            }

                                            Column(modifier = Modifier.fillMaxWidth()) {
                                                OutlinedTextField(
                                                    value = placeInput,
                                                    onValueChange = {
                                                        placeInput = it
                                                        // Typing after a pick invalidates the coordinates that
                                                        // came with it — force a fresh selection.
                                                        placeCoords = null
                                                        formValidationError = null
                                                    },
                                                    label = { Text(LanguageManager.getString("जन्म स्थान *", "Place of Birth *")) },
                                                    placeholder = { Text(LanguageManager.getString("शहर, राज्य (उदा. जयपुर, राजस्थान)", "City, State (e.g. Jaipur, Rajasthan)")) },
                                                    shape = RoundedCornerShape(14.dp),
                                                    colors = tfColors,
                                                    singleLine = true,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .testTag("input_kundali_place")
                                                )
                                                if (placeSuggestions.isNotEmpty()) {
                                                    Column(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(top = 4.dp)
                                                            .clip(RoundedCornerShape(12.dp))
                                                            .background(ElevatedSurface)
                                                            .border(1.dp, GlassCardBorder, RoundedCornerShape(12.dp))
                                                    ) {
                                                        placeSuggestions.forEach { suggestion ->
                                                            Text(
                                                                text = suggestion.label,
                                                                style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                                                                modifier = Modifier
                                                                    .fillMaxWidth()
                                                                    .clickable {
                                                                        placeInput = suggestion.label
                                                                        placeCoords = suggestion.latitude to suggestion.longitude
                                                                        placeSuggestions = emptyList()
                                                                        placeDropdownExpanded = false
                                                                        formValidationError = null
                                                                    }
                                                                    .padding(horizontal = 14.dp, vertical = 12.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                            }

                                            // Validation Error Alert
                                            if (formValidationError != null) {
                                                Text(
                                                    text = formValidationError!!,
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        color = RahuKaalDangerColor,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 12.sp
                                                    )
                                                )
                                            }

                                            // Submit Button
                                            val isCalculating by viewModel.isCalculating.collectAsState()
                                            if (isCalculating) {
                                                AstroLoadingIndicator()
                                            } else {
                                                GoldGlowButton(
                                                    text = LanguageManager.getString("कुण्डली बनाएं", "Generate Birth Chart"),
                                                    onClick = {
                                                        val coords = placeCoords
                                                        when {
                                                            nameInput.isBlank() || dobInput.isBlank() ||
                                                                tobInput.isBlank() || placeInput.isBlank() -> {
                                                                formValidationError = LanguageManager.getString(
                                                                    "⚠️ कृपया सभी आवश्यक विवरण (*) भरें",
                                                                    "⚠️ Please fill all required fields (*)"
                                                                )
                                                            }
                                                            // Without coordinates the Ascendant is meaningless. Ask for a
                                                            // real pick rather than quietly casting the chart for Jaipur.
                                                            coords == null -> {
                                                                formValidationError = LanguageManager.getString(
                                                                    "⚠️ सूची में से जन्म स्थान चुनें — सही लग्न के लिए स्थान के निर्देशांक आवश्यक हैं",
                                                                    "⚠️ Pick the birth place from the list — the Ascendant needs the place's coordinates"
                                                                )
                                                            }
                                                            else -> {
                                                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                                formValidationError = null
                                                                viewModel.generateKundaliChart(
                                                                    nameInput, dobInput, tobInput, placeInput,
                                                                    coords.first, coords.second
                                                                )
                                                                showForm = false
                                                            }
                                                        }
                                                    },
                                                    modifier = Modifier.fillMaxWidth(),
                                                    testTag = "generate_chart_submit_button"
                                                )
                                            }
                                        }
                                    }
                            }

                            // ─────────────────────────────────────────────────────────────
                            // GENERATED KUNDALI CHART VIEW (When Chart is available)
                            // ─────────────────────────────────────────────────────────────
                            if (currentChart != null) {
                                // Header Card: Name, Details & Quick Action Buttons
                                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = currentChart.personName,
                                                        style = MaterialTheme.typography.titleMedium.copy(
                                                            color = MaterialTheme.colorScheme.primary,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 19.sp
                                                        )
                                                    )
                                                    Text(
                                                        text = LanguageManager.getString(
                            "जन्म: ${currentChart.dateOfBirth} | ${currentChart.timeOfBirth} | ${currentChart.placeOfBirth}",
                            "Born: ${currentChart.dateOfBirth} | ${currentChart.timeOfBirth} | ${currentChart.placeOfBirth}"
                        ),
                                                        style = MaterialTheme.typography.bodySmall.copy(
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                            fontSize = 12.sp
                                                        )
                                                    )
                                                }

                                                // Share icon
                                                Icon(
                                                    imageVector = Icons.Default.Share,
                                                    contentDescription = "Share",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier
                                                        .size(24.dp)
                                                        .clickable {
                                                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                                            val shareText = """
                                                                ✨ Revati Kundali ✨
                                                                Name: ${currentChart.personName}
                                                                DOB: ${currentChart.dateOfBirth} | ${currentChart.timeOfBirth}
                                                                Place: ${currentChart.placeOfBirth}
                                                                
                                                                Lagna (Ascendant): ${currentChart.ascendantRashiHi}
                                                                Moon Sign (Rashi): ${currentChart.moonRashiHi}
                                                                Nakshatra: ${currentChart.moonNakshatraHi}
                                                            """.trimIndent()

                                                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                                type = "text/plain"
                                                                putExtra(
                                                                    Intent.EXTRA_SUBJECT,
                                                                    LanguageManager.getString("Revati कुण्डली", "Revati Kundali")
                                                                )
                                                                putExtra(Intent.EXTRA_TEXT, shareText)
                                                            }
                                                            context.startActivity(
                                                                Intent.createChooser(
                                                                    shareIntent,
                                                                    LanguageManager.getString("कुण्डली साझा करें", "Share Kundali")
                                                                )
                                                            )
                                                        }
                                                )
                                            }

                                            // The three headline facts of a chart, as bento
                                            // tiles rather than badges — three long strings on
                                            // one row could not fit, so they truncated.
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(IntrinsicSize.Min),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                com.example.ui.components.BentoTile(
                                                    label = LanguageManager.getString("लग्न", "Lagna"),
                                                    value = LanguageManager.getString(currentChart.ascendantRashiHi, currentChart.ascendantRashiEn),
                                                    valueSize = 13,
                                                    minHeight = 68,
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .fillMaxHeight()
                                                )
                                                com.example.ui.components.BentoTile(
                                                    label = LanguageManager.getString("चंद्र राशि", "Moon sign"),
                                                    value = LanguageManager.getString(currentChart.moonRashiHi, currentChart.moonRashiEn),
                                                    valueSize = 13,
                                                    minHeight = 68,
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .fillMaxHeight()
                                                )
                                                com.example.ui.components.BentoTile(
                                                    label = LanguageManager.getString("नक्षत्र", "Nakshatra"),
                                                    value = LanguageManager.getString(currentChart.moonNakshatraHi, currentChart.moonNakshatraEn),
                                                    valueSize = 13,
                                                    minHeight = 68,
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .fillMaxHeight()
                                                )
                                            }

                                            // Action Buttons: New Chart (+), Save Profile
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                // New Chart Button
                                                OutlinedButton(
                                                    onClick = {
                                                        nameInput = ""
                                                        dobInput = ""
                                                        tobInput = ""
                                                        placeInput = ""
                                                        placeCoords = null
                                                        showForm = true
                                                        viewModel.resetKundaliForm()
                                                    },
                                                    shape = RoundedCornerShape(12.dp),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = LanguageManager.getString("नया (+)", "New (+)"),
                                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                                    )
                                                }

                                                // Save Profile Button
                                                Button(
                                                    onClick = {
                                                        viewModel.saveNewProfile(
                                                            name = currentChart.personName,
                                                            dob = currentChart.dateOfBirth,
                                                            tob = currentChart.timeOfBirth,
                                                            place = currentChart.placeOfBirth
                                                        )
                                                        isSaved = true
                                                        Toast.makeText(
                                                            context,
                                                            LanguageManager.getString("प्रोफाइल सफलतापूर्वक सहेजा गया!", "Profile Saved Successfully!"),
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    },
                                                    shape = RoundedCornerShape(12.dp),
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = if (isSaved) ShubhSuccessColor else MaterialTheme.colorScheme.primaryContainer,
                                                        contentColor = if (isSaved) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimaryContainer
                                                    ),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Icon(
                                                        imageVector = if (isSaved) Icons.Default.Check else Icons.Default.Bookmark,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = if (isSaved) LanguageManager.getString("सहेजा गया", "Saved") else LanguageManager.getString("सहेजें", "Save"),
                                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                                    )
                                                }
                                            }
                                        }
                                    }

                                // Chart Canvas Section (North & South Indian Styles)
                                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = LanguageManager.getString("कुण्डली", "Chart"),
                                                    modifier = Modifier.weight(1f, fill = false),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    style = MaterialTheme.typography.titleSmall.copy(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                                )
                                                // Style Toggle
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(12.dp))
                                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                                        .border(1.dp, GlassCardBorder, RoundedCornerShape(12.dp))
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
                                                            text = LanguageManager.getString("उत्तर भारतीय", "North Indian"),
                                                            maxLines = 1,
                                                            softWrap = false,
                                                            style = MaterialTheme.typography.labelSmall.copy(
                                                                color = if (isNorthStyle) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                                fontWeight = FontWeight.Medium
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
                                                            text = LanguageManager.getString("दक्षिण भारतीय", "South Indian"),
                                                            maxLines = 1,
                                                            softWrap = false,
                                                            style = MaterialTheme.typography.labelSmall.copy(
                                                                color = if (!isNorthStyle) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                                fontWeight = FontWeight.Medium
                                                            )
                                                        )
                                                    }
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(18.dp))

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
                                                    chartData = currentChart,
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
                                                            text = LanguageManager.getString("कुण्डली चित्र तैयार किया जा रहा है...", "Generating chart image..."),
                                                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.primary)
                                                        )
                                                    }
                                                } else {
                                                    Button(
                                                        onClick = {
                                                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                                            isSharingChart = true
                                                            coroutineScope.launch {
                                                                val uri = KundaliImageGenerator.generateAndShareChart(context, currentChart)
                                                                isSharingChart = false
                                                                if (uri != null) {
                                                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                                        type = "image/png"
                                                                        putExtra(Intent.EXTRA_STREAM, uri)
                                                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                                    }
                                                                    context.startActivity(
                                                                        Intent.createChooser(
                                                                            shareIntent,
                                                                            LanguageManager.getString(
                                                                                "जन्म कुण्डली चित्र साझा करें",
                                                                                "Share Birth Chart Image"
                                                                            )
                                                                        )
                                                                    )
                                                                } else {
                                                                    Toast.makeText(
                                                                        context,
                                                                        LanguageManager.getString(
                                                                            "चित्र नहीं बन सका",
                                                                            "Failed to generate image"
                                                                        ),
                                                                        Toast.LENGTH_SHORT
                                                                    ).show()
                                                                }
                                                            }
                                                        },
                                                        modifier = Modifier
                                                            .fillMaxWidth(0.85f)
                                                            .testTag("share_kundali_image_button"),
                                                        shape = RoundedCornerShape(12.dp),
                                                        colors = ButtonDefaults.buttonColors(
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
                                                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium)
                                                            )
                                                        }
                                                    }
                                                }
                                            } else {
                                                SouthIndianChart(currentChart)
                                            }
                                        }
                                    }

                                // Planetary Positions Table
                                    SectionHeader(
                                        titleHi = "ग्रह स्थिति",
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

                                            currentChart.planets.forEach { planet ->
                                                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                                                    Text(text = LanguageManager.getString(planet.planetNameHi.substringBefore(" "), planet.planetNameEn), style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp), modifier = Modifier.weight(1f))
                                                    Text(text = LanguageManager.getString(planet.rashiNameHi, planet.rashiNameEn), style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp), modifier = Modifier.weight(1.2f))
                                                    Text(text = "${planet.degree}°", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.primary, fontSize = 14.sp, fontWeight = FontWeight.Normal), modifier = Modifier.weight(1f))
                                                    Text(text = LanguageManager.getString("${planet.houseNumber} भाव", "House ${planet.houseNumber}"), style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp), modifier = Modifier.weight(1f))
                                                }
                                            }
                                        }
                                    }

                                // Horizontal Vimshottari Dasha Timeline
                                    DashaHorizontalTimeline(dashaTimeline = currentChart.dashaTimeline)

                                    AstroDisclaimer(scope = DisclaimerScope.READING)

                                    Spacer(modifier = Modifier.height(24.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * A birth-place option from the geocoder, carrying the coordinates the chart needs.
 *
 * The autocomplete used to be a plain List<String>: the geocoder returned latitude
 * and longitude with every result and the code kept only the display label, after
 * which the calculator fell back to Jaipur for everyone.
 */
data class PlaceSuggestion(
    val label: String,
    val latitude: Double,
    val longitude: Double
)
