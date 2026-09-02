package com.example.ui.screens

import com.example.ui.components.AstroDisclaimer
import com.example.ui.components.DisclaimerScope
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.fillMaxHeight
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
        // The outer Scaffold in MainActivity already applies the status bar inset
        // through TopHeaderBar's statusBarsPadding(). Letting this inner Scaffold
        // apply it again is what put an empty band above every sub-tab row.
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SectionHeader(
                    titleHi = "गुण मिलान",
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
                                text = LanguageManager.getString("वर का विवरण:", "Boy's Details:"),
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
                                label = { Text(LanguageManager.getString("वर का नाम", "Boy Name")) },
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
                                    // "जन्म तिथि" does not fit this narrow field and
                                    // wrapped to two lines, making the box taller than
                                    // the name field beside it — only in Hindi.
                                    label = {
                                        Text(
                                            LanguageManager.getString("तिथि", "DOB"),
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    },
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
                                text = LanguageManager.getString("कन्या का विवरण:", "Girl's Details:"),
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
                                label = { Text(LanguageManager.getString("कन्या का नाम", "Girl Name")) },
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
                                    // "जन्म तिथि" does not fit this narrow field and
                                    // wrapped to two lines, making the box taller than
                                    // the name field beside it — only in Hindi.
                                    label = {
                                        Text(
                                            LanguageManager.getString("तिथि", "DOB"),
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    },
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
                        val inputError by viewModel.matchingInputError.collectAsState()
                        inputError?.let { message ->
                            Text(
                                text = message,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = RahuKaalDangerColor,
                                    fontSize = 13.sp
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp)
                            )
                        }

                        if (isCalculating) {
                            AstroLoadingIndicator()
                        } else {
                            GoldGlowButton(
                                text = LanguageManager.getString("गुण मिलान करें", "Calculate 36 Guna Score"),
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

            val result = gunaResult
            if (result == null) {
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = LanguageManager.getString("✨ वैदिक अष्टकूट मिलान", "✨ Vedic Ashtakoot matching — 36 gunas"),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 16.sp
                                )
                            )
                            Text(
                                text = LanguageManager.getString(
                                    "वर एवं कन्या का नाम और जन्म तिथि दर्ज करके 'गुण मिलान करें' पर टैप करें। 8 कूट (वर्ण, वश्य, तारा, योनि, ग्रह मैत्री, गण, भकूट, नाड़ी) एवं नाड़ी व भकूट दोष का सम्पूर्ण विश्लेषण प्राप्त होगा।",
                                    "Enter Boy & Girl birth details and tap 'Calculate 36 Guna' to get complete Ashtakoot compatibility, Nadi Dosha, Bhakoot Dosha, and Manglik analysis."
                                ),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 13.5.sp,
                                    lineHeight = 19.sp,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            )
                        }
                    }
                }
            } else {
                // Score Card Summary
                item {
                    val scoreColor = when {
                        result.totalObtainedGuna >= 25.0 -> ShubhSuccessColor
                        result.totalObtainedGuna >= 18.0 -> MaterialTheme.colorScheme.primary
                        else -> RahuKaalDangerColor
                    }

                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${result.boyName} ♥ ${result.girlName}",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 18.sp
                                )
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "${result.totalObtainedGuna} / 36.0",
                                style = MaterialTheme.typography.displayMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = scoreColor
                                )
                            )

                            Text(
                                text = LanguageManager.getString("कुल प्राप्त गुण", "Total Guna Match Score"),
                                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            LinearProgressIndicator(
                                progress = { (result.totalObtainedGuna / 36.0).toFloat().coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = scoreColor,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            GlassBadge(
                                text = LanguageManager.getString(result.compatibilityVerdictHi, result.compatibilityVerdictEn),
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
                                        val pdfFile = MatchingPdfReportService.generatePdfReport(context, result)
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
                                        text = LanguageManager.getString("PDF रिपोर्ट शेयर / प्रिंट करें", "Export / Share PDF Report"),
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
                                    width = if (result.hasNadiDosha) 1.5.dp else 1.dp,
                                    color = if (result.hasNadiDosha) RahuKaalDangerColor.copy(alpha = 0.8f) else ShubhSuccessColor.copy(alpha = 0.4f),
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
                                            imageVector = if (result.hasNadiDosha) Icons.Default.Warning else Icons.Default.CheckCircle,
                                            contentDescription = "Nadi Dosha",
                                            tint = if (result.hasNadiDosha) RahuKaalDangerColor else ShubhSuccessColor,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = LanguageManager.getString("नाड़ी दोष विचार", "Nadi Dosha Status"),
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                color = if (result.hasNadiDosha) RahuKaalDangerColor else ShubhSuccessColor,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp
                                            )
                                        )
                                    }
                                    GlassBadge(
                                        text = if (result.hasNadiDosha) LanguageManager.getString("दोष उपस्थित (0/8)", "Dosha Present (0/8)") else LanguageManager.getString("दोष मुक्त (8/8)", "No Dosha (8/8)"),
                                        textColor = if (result.hasNadiDosha) RahuKaalDangerColor else ShubhSuccessColor,
                                        borderColor = if (result.hasNadiDosha) RahuKaalDangerColor else ShubhSuccessColor
                                    )
                                }
                                Text(
                                    text = LanguageManager.getString(result.nadiDoshaStatusHi, result.nadiDoshaStatusEn),
                                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface, fontSize = 13.5.sp, lineHeight = 19.sp)
                                )
                            }
                        }

                        // 2. Bhakoot Dosha Card
                        GlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = if (result.hasBhakootDosha) 1.5.dp else 1.dp,
                                    color = if (result.hasBhakootDosha) RahuKaalDangerColor.copy(alpha = 0.8f) else ShubhSuccessColor.copy(alpha = 0.4f),
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
                                            imageVector = if (result.hasBhakootDosha) Icons.Default.Warning else Icons.Default.CheckCircle,
                                            contentDescription = "Bhakoot Dosha",
                                            tint = if (result.hasBhakootDosha) RahuKaalDangerColor else ShubhSuccessColor,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = LanguageManager.getString("भकूट दोष विचार", "Bhakoot Dosha Status"),
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                color = if (result.hasBhakootDosha) RahuKaalDangerColor else ShubhSuccessColor,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp
                                            )
                                        )
                                    }
                                    GlassBadge(
                                        text = if (result.hasBhakootDosha) LanguageManager.getString("दोष उपस्थित (0/7)", "Dosha Present (0/7)") else LanguageManager.getString("दोष मुक्त (7/7)", "No Dosha (7/7)"),
                                        textColor = if (result.hasBhakootDosha) RahuKaalDangerColor else ShubhSuccessColor,
                                        borderColor = if (result.hasBhakootDosha) RahuKaalDangerColor else ShubhSuccessColor
                                    )
                                }
                                Text(
                                    text = LanguageManager.getString(result.bhakootDoshaStatusHi, result.bhakootDoshaStatusEn),
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
                                            text = LanguageManager.getString("मंगल दोष विचार", "Mangal Dosha Analysis"),
                                            style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                        )
                                    }
                                }
                                Text(
                                    text = LanguageManager.getString(result.mangalDoshaStatusHi, result.mangalDoshaStatusEn),
                                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface, fontSize = 13.5.sp, lineHeight = 19.sp)
                                )
                            }
                        }
                    }
                }

                item { AstroDisclaimer(scope = DisclaimerScope.MATCHING) }

                // Birth Attributes Comparison Card
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = LanguageManager.getString("वर-कन्या ग्रह मिलान विवरण", "Birth Attribute Comparison"),
                                style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), thickness = 1.dp)

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = LanguageManager.getString("तत्व / गुण", "Attribute"), style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold), modifier = Modifier.weight(1f))
                                Text(text = LanguageManager.getString("वर", "Boy"), style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold), modifier = Modifier.weight(1.2f))
                                Text(text = LanguageManager.getString("कन्या", "Girl"), style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold), modifier = Modifier.weight(1.2f))
                            }

                            val attributes = listOf(
                                Triple(LanguageManager.getString("चन्द्र राशि", "Moon Sign"), result.boyMoonRashi, result.girlMoonRashi),
                                Triple(LanguageManager.getString("नक्षत्र", "Nakshatra"), result.boyNakshatra, result.girlNakshatra),
                                Triple(LanguageManager.getString("नाड़ी", "Nadi"), result.boyNadi, result.girlNadi),
                                Triple(LanguageManager.getString("गण", "Gana"), result.boyGana, result.girlGana),
                                Triple(LanguageManager.getString("योनि", "Yoni"), result.boyYoni, result.girlYoni),
                                Triple(LanguageManager.getString("वर्ण", "Varna"), result.boyVarna, result.girlVarna),
                                Triple(LanguageManager.getString("वश्य", "Vashya"), result.boyVashya, result.girlVashya)
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
                                text = LanguageManager.getString("विवाह निष्कर्ष रिपोर्ट:", "Summary Report & Guidance:"),
                                style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            )
                            Text(
                                text = LanguageManager.getString(result.summaryReadingHi, result.summaryReadingEn),
                                style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, lineHeight = 21.sp)
                            )
                        }
                    }
                }

                // Ashtakoot 8 Breakdown Table Header
                item {
                    SectionHeader(
                        titleHi = "अष्टकूट 36-गुण विवरण",
                        titleEn = "Ashtakoot 36-Score Breakdown"
                    )
                }

                // The eight koots as a bento. Each was a full-width card with a
                // progress bar and a paragraph, so reading the breakdown meant
                // eight scrolls. Two per row shows the whole score at once; the
                // description moves into the tile's sub-line.
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        result.kootDetails.chunked(2).forEach { pair ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(IntrinsicSize.Min),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                pair.forEach { koot ->
                                    val kootColor = when {
                                        koot.obtainedPoints == koot.maxPoints -> ShubhSuccessColor
                                        koot.obtainedPoints > 0.0 -> MaterialTheme.colorScheme.primary
                                        else -> RahuKaalDangerColor
                                    }
                                    com.example.ui.components.BentoTile(
                                        label = LanguageManager.getString(koot.kootNameHi, koot.kootNameEn),
                                        value = "${koot.obtainedPoints} / ${koot.maxPoints}",
                                        // The descriptions end in "(Boy: X, Girl: Y)",
                                        // which the comparison table above already
                                        // shows; in a tile it only ate the line.
                                        sub = LanguageManager.getString(koot.descriptionHi, koot.descriptionEn)
                                            .substringBefore(" (").trim(),
                                        accent = kootColor,
                                        valueSize = 20,
                                        minHeight = 108,
                                        singleLineValue = true,
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                    )
                                }
                                // Eight is even, but guard a lone tile anyway.
                                if (pair.size == 1) Spacer(modifier = Modifier.weight(1f))
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
