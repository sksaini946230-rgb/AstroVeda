package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.Male
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.CalendarMonth
import com.example.ui.components.M3DatePickerDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.clip
import android.view.HapticFeedbackConstants
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GunaKootDetail
import com.example.service.MatchingPdfReportService
import com.example.data.local.RecentSearchEntity
import com.example.ui.MainViewModel
import com.example.ui.components.GlassBadge
import com.example.ui.components.GlassCard
import com.example.ui.components.GoldGlowButton
import com.example.ui.components.EmptyStateComponent
import com.example.ui.components.RecentSearchesComponent
import com.example.ui.components.AstroLoadingIndicator
import com.example.ui.components.SectionHeader
import com.example.ui.theme.AuspiciousGreen
import com.example.ui.theme.InauspiciousRed
import com.example.util.LanguageManager

@Composable
fun MatchingScreen(viewModel: MainViewModel) {
    val haptic = LocalHapticFeedback.current
    val view = LocalView.current
    val gunaResult by viewModel.gunaResult.collectAsState()

    var boyName by remember { mutableStateOf(viewModel.matchBoyName.value) }
    var boyDob by remember { mutableStateOf(viewModel.matchBoyDob.value) }

    var girlName by remember { mutableStateOf(viewModel.matchGirlName.value) }
    var girlDob by remember { mutableStateOf(viewModel.matchGirlDob.value) }

    var showBoyDatePicker by remember { mutableStateOf(false) }
    var showGirlDatePicker by remember { mutableStateOf(false) }

    var showForm by remember { mutableStateOf(true) }

    if (showBoyDatePicker) {
        M3DatePickerDialog(
            initialDateString = boyDob,
            onDateSelected = { selected ->
                boyDob = selected
                viewModel.matchBoyDob.value = selected
            },
            onDismiss = { showBoyDatePicker = false }
        )
    }

    if (showGirlDatePicker) {
        M3DatePickerDialog(
            initialDateString = girlDob,
            onDateSelected = { selected ->
                girlDob = selected
                viewModel.matchGirlDob.value = selected
            },
            onDismiss = { showGirlDatePicker = false }
        )
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
            Spacer(modifier = Modifier.height(8.dp))
            SectionHeader(
                titleHi = "गुण मिलान (36 Guna Kundali Matching)",
                titleEn = "Kundali Matching (Ashtakoot)"
            )
            val recentSearches by viewModel.recentSearches.collectAsState()
            RecentSearchesComponent(
                recentSearches = recentSearches.filter { it.type == "MATCHING" },
                onSearchSelected = { search ->
                    val parts = search.data.split("|")
                    if (parts.size == 4) {
                        boyName = parts[0]
                        boyDob = parts[1]
                        girlName = parts[2]
                        girlDob = parts[3]
                        viewModel.calculateGunaMatching()
                    }
                }
            )
        }

        // Boy & Girl Details Form Card
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    val tfColors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Boy Details
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Male, contentDescription = "Boy", tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = LanguageManager.getString("वर का विवरण (Boy's Details):", "Boy's Details:"), style = MaterialTheme.typography.titleSmall.copy(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold))
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = boyName,
                            onValueChange = {
                                boyName = it
                                viewModel.matchBoyName.value = it
                            },
                            label = { Text(LanguageManager.getString("वर का नाम (Boy Name)", "Boy Name")) },
                            colors = tfColors,
                            modifier = Modifier.weight(1.2f).testTag("input_boy_name")
                        )

                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = boyDob,
                                onValueChange = {
                                    boyDob = it
                                    viewModel.matchBoyDob.value = it
                                },
                                readOnly = true,
                                label = { Text(LanguageManager.getString("जन्म तिथि", "DOB")) },
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.CalendarMonth,
                                        contentDescription = "Select Boy DOB",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                colors = tfColors,
                                modifier = Modifier.fillMaxWidth().testTag("input_boy_dob")
                            )
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clickable {
                                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                        showBoyDatePicker = true
                                    }
                            )
                        }
                    }

                    // Girl Details
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Female, contentDescription = "Girl", tint = MaterialTheme.colorScheme.secondary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = LanguageManager.getString("कन्या का विवरण (Girl's Details):", "Girl's Details:"), style = MaterialTheme.typography.titleSmall.copy(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold))
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = girlName,
                            onValueChange = {
                                girlName = it
                                viewModel.matchGirlName.value = it
                            },
                            label = { Text(LanguageManager.getString("कन्या का नाम (Girl Name)", "Girl Name")) },
                            colors = tfColors,
                            modifier = Modifier.weight(1.2f).testTag("input_girl_name")
                        )

                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = girlDob,
                                onValueChange = {
                                    girlDob = it
                                    viewModel.matchGirlDob.value = it
                                },
                                readOnly = true,
                                label = { Text(LanguageManager.getString("जन्म तिथि", "DOB")) },
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.CalendarMonth,
                                        contentDescription = "Select Girl DOB",
                                        tint = MaterialTheme.colorScheme.secondary
                                    )
                                },
                                colors = tfColors,
                                modifier = Modifier.fillMaxWidth().testTag("input_girl_dob")
                            )
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clickable {
                                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                        showGirlDatePicker = true
                                    }
                            )
                        }
                    }

                    val isCalculating by viewModel.isCalculating.collectAsState()
                    if (isCalculating) {
                        AstroLoadingIndicator()
                    } else {
                        GoldGlowButton(
                            text = LanguageManager.getString("गुण मिलान करें (Calculate 36 Guna)", "Calculate 36 Guna Score"),
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                viewModel.matchBoyName.value = boyName
                                viewModel.matchBoyDob.value = boyDob
                                viewModel.matchGirlName.value = girlName
                                viewModel.matchGirlDob.value = girlDob
                                viewModel.calculateGunaMatching()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "calculate_guna_button"
                        )
                    }
                }
            }
        }
        // Score Card Summary
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${gunaResult.boyName} ♥ ${gunaResult.girlName}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 18.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "${gunaResult.totalObtainedGuna} / 36.0",
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (gunaResult.totalObtainedGuna >= 18) AuspiciousGreen else InauspiciousRed
                        )
                    )

                    Text(
                        text = LanguageManager.getString("कुल प्राप्त गुण (Obtained Guna Score)", "Total Guna Match Score"),
                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    LinearProgressIndicator(
                        progress = { (gunaResult.totalObtainedGuna / 36.0).toFloat().coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = if (gunaResult.totalObtainedGuna >= 18) AuspiciousGreen else InauspiciousRed,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    GlassBadge(
                        text = LanguageManager.getString(gunaResult.compatibilityVerdictHi, gunaResult.compatibilityVerdictEn),
                        backgroundColor = (if (gunaResult.totalObtainedGuna >= 18) AuspiciousGreen else InauspiciousRed).copy(alpha = 0.2f),
                        textColor = if (gunaResult.totalObtainedGuna >= 18) AuspiciousGreen else InauspiciousRed,
                        borderColor = if (gunaResult.totalObtainedGuna >= 18) AuspiciousGreen else InauspiciousRed
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    val context = LocalContext.current
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                val pdfFile = MatchingPdfReportService.generatePdfReport(context, gunaResult)
                                if (pdfFile != null) {
                                    MatchingPdfReportService.sharePdfReport(context, pdfFile)
                                }
                            }
                            .padding(vertical = 12.dp)
                            .testTag("share_pdf_report_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.PictureAsPdf,
                                contentDescription = "PDF Report",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "PDF रिपोर्ट शेयर / प्रिंट करें (Export PDF Report)",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            )
                        }
                    }
                }
            }
        }

        // Mangal Dosha & Summary Reading Card Consolidated
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Mangal Dosha Section
                    Column {
                        Text(
                            text = "मंगल दोष विचार (Mangal Dosha Analysis)",
                            style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = gunaResult.mangalDoshaStatusHi,
                            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, lineHeight = 20.sp)
                        )
                    }

                    androidx.compose.material3.HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f), thickness = 1.dp)

                    // Summary Report Section
                    Column {
                        Text(
                            text = "विवाह निष्कर्ष रिपोर्ट (Summary Report):",
                            style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = gunaResult.summaryReadingHi,
                            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, lineHeight = 20.sp)
                        )
                    }
                }
            }
        }

        // Ashtakoot 8 Breakdown Table Header
        item {
            SectionHeader(
                titleHi = "अष्टकूट विवरण (8 Koota Breakdown Table)",
                titleEn = "Ashtakoot Score Table"
            )
        }

        // Koota Details Table
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "कूट (Koota)", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Normal, fontSize = 13.sp), modifier = Modifier.weight(1.5f))
                        Text(text = "अधिकतम", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Normal, fontSize = 13.sp), modifier = Modifier.weight(1f))
                        Text(text = "प्राप्त", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Normal, fontSize = 13.sp), modifier = Modifier.weight(1f))
                    }

                    gunaResult.kootDetails.forEach { koot ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = koot.kootNameHi, style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp), modifier = Modifier.weight(1.5f))
                            Text(text = "${koot.maxPoints}", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp), modifier = Modifier.weight(1f))
                            Text(text = "${koot.obtainedPoints}", style = MaterialTheme.typography.bodySmall.copy(color = AuspiciousGreen, fontWeight = FontWeight.Normal, fontSize = 14.sp), modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
}
