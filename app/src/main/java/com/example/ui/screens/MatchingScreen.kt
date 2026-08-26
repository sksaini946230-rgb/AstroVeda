package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Male
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.view.HapticFeedbackConstants
import com.example.data.model.GunaKootDetail
import com.example.service.MatchingPdfReportService
import com.example.ui.MainViewModel
import com.example.ui.components.AstroLoadingIndicator
import com.example.ui.components.GlassBadge
import com.example.ui.components.GlassCard
import com.example.ui.components.GoldGlowButton
import com.example.ui.components.M3DatePickerDialog
import com.example.ui.components.RecentSearchesComponent
import com.example.ui.components.SectionHeader
import com.example.ui.theme.RahuKaalDangerColor
import com.example.ui.theme.ShubhSuccessColor
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
                            Text(
                                text = LanguageManager.getString("वर का विवरण (Boy's Details):", "Boy's Details:"),
                                style = MaterialTheme.typography.titleSmall.copy(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            )
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
                            Text(
                                text = LanguageManager.getString("कन्या का विवरण (Girl's Details):", "Girl's Details:"),
                                style = MaterialTheme.typography.titleSmall.copy(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            )
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
                val scoreColor = when {
                    gunaResult.totalObtainedGuna >= 25.0 -> ShubhSuccessColor
                    gunaResult.totalObtainedGuna >= 18.0 -> MaterialTheme.colorScheme.primary
                    else -> RahuKaalDangerColor
                }

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
                                color = scoreColor
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
                            color = scoreColor,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        GlassBadge(
                            text = LanguageManager.getString(gunaResult.compatibilityVerdictHi, gunaResult.compatibilityVerdictEn),
                            backgroundColor = scoreColor.copy(alpha = 0.2f),
                            textColor = scoreColor,
                            borderColor = scoreColor
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        val context = LocalContext.current
                        val haptic = LocalHapticFeedback.current
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
                                    text = LanguageManager.getString("PDF रिपोर्ट शेयर / प्रिंट करें (Export PDF)", "Export / Share PDF Report"),
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

            // Prominent Doshas Highlight Section (Nadi Dosha & Bhakoot Dosha & Mangal Dosha)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // 1. Nadi Dosha Card (Most Critical)
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = if (gunaResult.hasNadiDosha) 1.5.dp else 1.dp,
                                color = if (gunaResult.hasNadiDosha) RahuKaalDangerColor.copy(alpha = 0.8f) else ShubhSuccessColor.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(16.dp)
                            )
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (gunaResult.hasNadiDosha) Icons.Default.Warning else Icons.Default.CheckCircle,
                                        contentDescription = "Nadi Dosha",
                                        tint = if (gunaResult.hasNadiDosha) RahuKaalDangerColor else ShubhSuccessColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = LanguageManager.getString("नाड़ी दोष विचार (Nadi Dosha)", "Nadi Dosha Status"),
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            color = if (gunaResult.hasNadiDosha) RahuKaalDangerColor else ShubhSuccessColor,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                    )
                                }
                                GlassBadge(
                                    text = if (gunaResult.hasNadiDosha) LanguageManager.getString("दोष उपस्थित (0/8)", "Dosha Present (0/8)") else LanguageManager.getString("दोष मुक्त (8/8)", "No Dosha (8/8)"),
                                    textColor = if (gunaResult.hasNadiDosha) RahuKaalDangerColor else ShubhSuccessColor,
                                    borderColor = if (gunaResult.hasNadiDosha) RahuKaalDangerColor else ShubhSuccessColor
                                )
                            }
                            Text(
                                text = LanguageManager.getString(gunaResult.nadiDoshaStatusHi, gunaResult.nadiDoshaStatusEn),
                                style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface, fontSize = 13.5.sp, lineHeight = 19.sp)
                            )
                        }
                    }

                    // 2. Bhakoot Dosha Card
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = if (gunaResult.hasBhakootDosha) 1.5.dp else 1.dp,
                                color = if (gunaResult.hasBhakootDosha) RahuKaalDangerColor.copy(alpha = 0.8f) else ShubhSuccessColor.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(16.dp)
                            )
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (gunaResult.hasBhakootDosha) Icons.Default.Warning else Icons.Default.CheckCircle,
                                        contentDescription = "Bhakoot Dosha",
                                        tint = if (gunaResult.hasBhakootDosha) RahuKaalDangerColor else ShubhSuccessColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = LanguageManager.getString("भकूट दोष विचार (Bhakoot Dosha)", "Bhakoot Dosha Status"),
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            color = if (gunaResult.hasBhakootDosha) RahuKaalDangerColor else ShubhSuccessColor,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                    )
                                }
                                GlassBadge(
                                    text = if (gunaResult.hasBhakootDosha) LanguageManager.getString("दोष उपस्थित (0/7)", "Dosha Present (0/7)") else LanguageManager.getString("दोष मुक्त (7/7)", "No Dosha (7/7)"),
                                    textColor = if (gunaResult.hasBhakootDosha) RahuKaalDangerColor else ShubhSuccessColor,
                                    borderColor = if (gunaResult.hasBhakootDosha) RahuKaalDangerColor else ShubhSuccessColor
                                )
                            }
                            Text(
                                text = LanguageManager.getString(gunaResult.bhakootDoshaStatusHi, gunaResult.bhakootDoshaStatusEn),
                                style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface, fontSize = 13.5.sp, lineHeight = 19.sp)
                            )
                        }
                    }

                    // 3. Mangal Dosha Card
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "Mangal Dosha",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = LanguageManager.getString("मंगल दोष विचार (Mangal Dosha Analysis)", "Mangal Dosha Analysis"),
                                        style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    )
                                }
                            }
                            Text(
                                text = LanguageManager.getString(gunaResult.mangalDoshaStatusHi, gunaResult.mangalDoshaStatusEn),
                                style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface, fontSize = 13.5.sp, lineHeight = 19.sp)
                            )
                        }
                    }
                }
            }

            // Birth Attributes Comparison Card
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = LanguageManager.getString("वर-कन्या ग्रह मिलान विवरण (Birth Attribute Comparison)", "Birth Attribute Comparison"),
                            style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), thickness = 1.dp)

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = LanguageManager.getString("तत्व / गुण", "Attribute"), style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold), modifier = Modifier.weight(1f))
                            Text(text = LanguageManager.getString("वर (Boy)", "Boy"), style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold), modifier = Modifier.weight(1.2f))
                            Text(text = LanguageManager.getString("कन्या (Girl)", "Girl"), style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold), modifier = Modifier.weight(1.2f))
                        }

                        val attributes = listOf(
                            Triple(LanguageManager.getString("चन्द्र राशि", "Moon Sign"), gunaResult.boyMoonRashi, gunaResult.girlMoonRashi),
                            Triple(LanguageManager.getString("नक्षत्र", "Nakshatra"), gunaResult.boyNakshatra, gunaResult.girlNakshatra),
                            Triple(LanguageManager.getString("नाड़ी", "Nadi"), gunaResult.boyNadi, gunaResult.girlNadi),
                            Triple(LanguageManager.getString("गण", "Gana"), gunaResult.boyGana, gunaResult.girlGana),
                            Triple(LanguageManager.getString("योनि", "Yoni"), gunaResult.boyYoni, gunaResult.girlYoni),
                            Triple(LanguageManager.getString("वर्ण", "Varna"), gunaResult.boyVarna, gunaResult.girlVarna),
                            Triple(LanguageManager.getString("वश्य", "Vashya"), gunaResult.boyVashya, gunaResult.girlVashya)
                        )

                        attributes.forEach { (attr, boyVal, girlVal) ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = attr, style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp), modifier = Modifier.weight(1f))
                                Text(text = boyVal, style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold, fontSize = 13.sp), modifier = Modifier.weight(1.2f))
                                Text(text = girlVal, style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold, fontSize = 13.sp), modifier = Modifier.weight(1.2f))
                            }
                        }
                    }
                }
            }

            // Summary Reading Card
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = LanguageManager.getString("विवाह निष्कर्ष रिपोर्ट (Summary Report):", "Summary Report & Guidance:"),
                            style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        )
                        Text(
                            text = LanguageManager.getString(gunaResult.summaryReadingHi, gunaResult.summaryReadingEn),
                            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, lineHeight = 21.sp)
                        )
                    }
                }
            }

            // Ashtakoot 8 Breakdown Table Header
            item {
                SectionHeader(
                    titleHi = "अष्टकूट 36-गुण विवरण (8 Koota Breakdown)",
                    titleEn = "Ashtakoot 36-Score Breakdown"
                )
            }

            // Detailed 8 Kootas Cards
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    gunaResult.kootDetails.forEach { koot ->
                        val kootColor = if (koot.obtainedPoints == koot.maxPoints) {
                            ShubhSuccessColor
                        } else if (koot.obtainedPoints > 0.0) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            RahuKaalDangerColor
                        }

                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = LanguageManager.getString(koot.kootNameHi, koot.kootNameEn),
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.5.sp
                                        )
                                    )
                                    GlassBadge(
                                        text = "${koot.obtainedPoints} / ${koot.maxPoints}",
                                        textColor = kootColor,
                                        borderColor = kootColor
                                    )
                                }

                                LinearProgressIndicator(
                                    progress = { (koot.obtainedPoints / koot.maxPoints).toFloat().coerceIn(0f, 1f) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(2.dp)),
                                    color = kootColor,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )

                                Text(
                                    text = LanguageManager.getString(koot.descriptionHi, koot.descriptionEn),
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 12.5.sp,
                                        lineHeight = 17.sp
                                    )
                                )
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
