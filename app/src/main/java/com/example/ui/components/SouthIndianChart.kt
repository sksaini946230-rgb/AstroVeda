package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.KundaliChartData
import com.example.ui.theme.DateTimeAccent
import com.example.ui.theme.ElevatedSurface
import com.example.ui.theme.GlassCardBorder
import com.example.ui.theme.PrimaryButtonBackground
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun SouthIndianChart(
    chartData: KundaliChartData,
    modifier: Modifier = Modifier
) {
    val alphaAnim = remember { Animatable(0f) }
    val scaleAnim = remember { Animatable(0.95f) }
    
    LaunchedEffect(chartData) {
        alphaAnim.animateTo(1f, animationSpec = tween(600))
        scaleAnim.animateTo(1f, animationSpec = tween(600))
    }

    var userScale by remember { mutableFloatStateOf(1f) }
    var userOffset by remember { mutableStateOf(Offset.Zero) }

    // Fixed South Indian Rashi Grid (12 boxes around a 4x4 perimeter)
    // Row 1: 12 (Pisces), 1 (Aries), 2 (Taurus), 3 (Gemini)
    // Row 2: 11 (Aquarius), CENTER 2x2, 4 (Cancer)
    // Row 3: 10 (Capricorn), CENTER 2x2, 5 (Leo)
    // Row 4: 9 (Sagittarius), 8 (Scorpio), 7 (Libra), 6 (Virgo)

    val gridLayout = listOf(
        listOf(12, 1, 2, 3),
        listOf(11, 0, 0, 4),
        listOf(10, 0, 0, 5),
        listOf(9, 8, 7, 6)
    )

    val rashiShortHi = listOf("", "मेष", "वृष", "मिथुन", "कर्क", "सिंह", "कन्या", "तुला", "वृश्चिक", "धनु", "मकर", "कुंभ", "मीन")

    // Map rashi to planets
    val rashiPlanetsMap = mutableMapOf<Int, MutableList<String>>()
    for (r in 1..12) rashiPlanetsMap[r] = mutableListOf()

    chartData.planets.forEach { p ->
        val shortName = p.planetNameHi.substringBefore(" ")
        rashiPlanetsMap[p.rashiNumber]?.add(shortName)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(20.dp))
            .background(ElevatedSurface)
            .border(1.dp, GlassCardBorder, RoundedCornerShape(20.dp))
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    val newScale = (userScale * zoom).coerceIn(1f, 3.5f)
                    val maxOffsetX = (size.width * (newScale - 1f)) / 2f
                    val maxOffsetY = (size.height * (newScale - 1f)) / 2f
                    userScale = newScale
                    userOffset = if (newScale > 1f) {
                        Offset(
                            x = (userOffset.x + pan.x).coerceIn(-maxOffsetX, maxOffsetX),
                            y = (userOffset.y + pan.y).coerceIn(-maxOffsetY, maxOffsetY)
                        )
                    } else {
                        Offset.Zero
                    }
                }
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        if (userScale > 1.1f) {
                            userScale = 1f
                            userOffset = Offset.Zero
                        } else {
                            userScale = 2f
                        }
                    }
                )
            }
            .graphicsLayer {
                alpha = alphaAnim.value
                scaleX = scaleAnim.value * userScale
                scaleY = scaleAnim.value * userScale
                translationX = userOffset.x
                translationY = userOffset.y
            }
            .padding(4.dp)
            .testTag("south_indian_chart"),
        contentAlignment = Alignment.Center
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            for (r in 0..3) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    for (c in 0..3) {
                        val rashiNum = gridLayout[r][c]
                        if (rashiNum == 0) {
                            // Center Empty Cell (Occupies 2x2)
                            if (r == 1 && c == 1) {
                                Box(
                                    modifier = Modifier
                                        .weight(2f)
                                        .fillMaxHeight()
                                        .padding(4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "दक्षिण भारतीय कुण्डली",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = PrimaryButtonBackground,
                                                fontWeight = FontWeight.Normal,
                                                fontSize = 11.sp
                                            )
                                        )
                                        Text(
                                            text = "लग्न: ${chartData.ascendantRashiHi}",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = TextSecondary,
                                                fontSize = 10.sp
                                            )
                                        )
                                    }
                                }
                            }
                        } else {
                            val isLagna = (rashiNum == chartData.ascendantRashiNumber)
                            val planetsInRashi = rashiPlanetsMap[rashiNum] ?: emptyList()

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .border(1.dp, GlassCardBorder)
                                    .padding(4.dp)
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "${rashiShortHi.getOrElse(rashiNum) { "" }} ($rashiNum)",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = PrimaryButtonBackground,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Normal
                                            )
                                        )
                                        if (isLagna) {
                                            Text(
                                                text = " [ल]",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = DateTimeAccent,
                                                    fontWeight = FontWeight.Normal,
                                                    fontSize = 9.sp
                                                )
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = planetsInRashi.joinToString(" "),
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = TextPrimary,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Normal
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Reset zoom overlay
        if (userScale > 1.05f) {
            Surface(
                onClick = {
                    userScale = 1f
                    userOffset = Offset.Zero
                },
                shape = RoundedCornerShape(12.dp),
                color = ElevatedSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, GlassCardBorder),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${String.format(java.util.Locale.US, "%.1f", userScale)}x  रिसेट ↺",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = PrimaryButtonBackground,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Normal
                        )
                    )
                }
            }
        }
    }
}
