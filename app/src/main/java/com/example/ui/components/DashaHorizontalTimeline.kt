package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DashaPeriod
import com.example.ui.theme.DateTimeAccent
import com.example.ui.theme.ElevatedSurface
import com.example.ui.theme.GlassCardBorder
import com.example.ui.theme.PrimaryButtonBackground
import com.example.ui.theme.PrimaryButtonText
import com.example.ui.theme.TextPrimary

@Composable
fun DashaHorizontalTimeline(
    dashaTimeline: List<DashaPeriod>,
    modifier: Modifier = Modifier
) {
    if (dashaTimeline.isEmpty()) return

    val currentIdx = remember(dashaTimeline) {
        val idx = dashaTimeline.indexOfFirst { it.isCurrent }
        if (idx >= 0) idx else 0
    }

    var selectedIdx by remember(dashaTimeline) { mutableStateOf(currentIdx) }

    val listState = rememberLazyListState()

    // Auto scroll to current active Dasha node on launch
    LaunchedEffect(currentIdx) {
        if (currentIdx in dashaTimeline.indices) {
            listState.animateScrollToItem(currentIdx)
        }
    }

    GlassCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header with hint
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "समय चक्र (Horizontal Dasha Timeline)",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    )
                    Text(
                        text = "अतीत एवं भविष्य के दशा काल देखने के लिए स्वाइप करें:",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    )
                }

                // Scroll to active button
                Surface(
                    onClick = {
                        selectedIdx = currentIdx
                    },
                    shape = RoundedCornerShape(12.dp),
                    color = ElevatedSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, GlassCardBorder)
                ) {
                    Text(
                        text = "वर्तमान 🎯",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = PrimaryButtonBackground,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Normal
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Horizontal Timeline Nodes
            LazyRow(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dasha_horizontal_timeline"),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                itemsIndexed(dashaTimeline) { index, dasha ->
                    val isSelected = selectedIdx == index
                    val isCurrent = dasha.isCurrent

                    val nodeBorderColor by animateColorAsState(
                        targetValue = when {
                            isSelected && isCurrent -> PrimaryButtonBackground
                            isSelected -> MaterialTheme.colorScheme.primary
                            isCurrent -> DateTimeAccent
                            else -> GlassCardBorder
                        },
                        animationSpec = tween(300),
                        label = "nodeBorder"
                    )

                    val nodeBgColor by animateColorAsState(
                        targetValue = when {
                            isCurrent -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                            isSelected -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            else -> GlassCardBorder.copy(alpha = 0.08f)
                        },
                        animationSpec = tween(300),
                        label = "nodeBg"
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Timeline Node Card
                        Box(
                            modifier = Modifier
                                .width(125.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(nodeBgColor)
                                .border(
                                    width = if (isSelected || isCurrent) 2.dp else 1.dp,
                                    color = nodeBorderColor,
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable { selectedIdx = index }
                                .padding(10.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Status Indicator
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    if (isCurrent) {
                                        Box(
                                            modifier = Modifier
                                                .size(18.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    Brush.radialGradient(
                                                        colors = listOf(PrimaryButtonBackground, DateTimeAccent)
                                                    )
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Star,
                                                contentDescription = "Current Dasha",
                                                tint = PrimaryButtonText,
                                                modifier = Modifier.size(11.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "सक्रिय",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = PrimaryButtonBackground,
                                                fontWeight = FontWeight.Normal,
                                                fontSize = 10.sp
                                            )
                                        )
                                    } else if (index < currentIdx) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Past Dasha",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(
                                            text = "पूर्ण",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                                fontSize = 10.sp
                                            )
                                        )
                                    } else {
                                        Text(
                                            text = "भावी",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                                fontSize = 10.sp
                                            )
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // Planet Name
                                Text(
                                    text = dasha.planetHi,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = if (isCurrent || isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                )

                                Text(
                                    text = dasha.planetEn,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp
                                    )
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                // Duration pill
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isCurrent) DateTimeAccent.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                    modifier = Modifier.padding(top = 2.dp)
                                ) {
                                    Text(
                                        text = "${dasha.durationYears} वर्ष",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = if (isCurrent) DateTimeAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontWeight = FontWeight.Normal,
                                            fontSize = 11.sp
                                        ),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                // Date Range
                                Text(
                                    text = "${dasha.startDate}\n${dasha.endDate}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                        fontSize = 10.sp,
                                        lineHeight = 12.sp
                                    )
                                )
                            }
                        }

                        // Connecting track line between nodes
                        if (index < dashaTimeline.size - 1) {
                            Box(
                                modifier = Modifier
                                    .width(16.dp)
                                    .height(2.dp)
                                    .background(
                                        if (index < currentIdx) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                                        else GlassCardBorder
                                    )
                            )
                        }
                    }
                }
            }

            // Selected Dasha Antardasha Detail Panel
            val selectedDasha = dashaTimeline.getOrNull(selectedIdx)
            if (selectedDasha != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        .border(1.dp, GlassCardBorder, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "महादशा: ${selectedDasha.planetHi} (${selectedDasha.planetEn})",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            )
                            Text(
                                text = "अवधि: ${selectedDasha.startDate} से ${selectedDasha.endDate} (${selectedDasha.durationYears} वर्ष)",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp
                                )
                            )
                        }

                        if (selectedDasha.isCurrent) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = DateTimeAccent.copy(alpha = 0.2f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, DateTimeAccent)
                            ) {
                                Text(
                                    text = "★ वर्तमान प्रभाव",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = DateTimeAccent,
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 10.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    if (selectedDasha.antardashas.isNotEmpty()) {
                        Text(
                            text = "अंतर्दशा विवरण (Bhukti Sub-periods):",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.Normal,
                                fontSize = 12.sp
                            ),
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        selectedDasha.antardashas.forEach { sub ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        if (sub.isCurrent) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                        else Color.Transparent
                                    )
                                    .padding(horizontal = 6.dp, vertical = 3.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "• ${sub.planetHi} (${sub.planetEn})",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = if (sub.isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                        fontWeight = if (sub.isCurrent) FontWeight.Normal else FontWeight.Normal,
                                        fontSize = 12.sp
                                    )
                                )
                                Text(
                                    text = "${sub.startDate} - ${sub.endDate} (${sub.durationMonths} माह)",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (sub.isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = if (sub.isCurrent) FontWeight.Normal else FontWeight.Normal,
                                        fontSize = 11.sp
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
