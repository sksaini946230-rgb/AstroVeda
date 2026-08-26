package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
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
fun NorthIndianChart(
    chartData: KundaliChartData,
    modifier: Modifier = Modifier,
    onHouseClick: (houseNum: Int, rashiNum: Int, planets: List<String>) -> Unit = { _, _, _ -> }
) {
    val alphaAnim = remember { Animatable(0f) }
    val scaleAnim = remember { Animatable(0.95f) }

    LaunchedEffect(chartData) {
        alphaAnim.animateTo(1f, animationSpec = tween(600))
        scaleAnim.animateTo(1f, animationSpec = tween(600))
    }

    var selectedHouse by remember { mutableStateOf<Int?>(1) }
    var userScale by remember { mutableFloatStateOf(1f) }
    var userOffset by remember { mutableStateOf(Offset.Zero) }

    val textMeasurer = rememberTextMeasurer()

    val lagnaRashi = chartData.ascendantRashiNumber

    // House to Rashi calculation (Counter-clockwise in Vedic Astrology)
    fun getRashiForHouse(h: Int): Int {
        return (lagnaRashi + h - 2) % 12 + 1
    }

    val rashiNamesHi = listOf(
        "", "मेष", "वृषभ", "मिथुन", "कर्क", "सिंह", "कन्या",
        "तुला", "वृश्चिक", "धनु", "मकर", "कुंभ", "मीन"
    )

    val houseSignificances = mapOf(
        1 to "प्रथम भाव (लग्न) • तनु भाव: व्यक्तित्व, शरीर, स्वास्थ्य, आत्म-विश्वास",
        2 to "द्वितीय भाव • धन भाव: संपत्ति, कुटुंब, वाणी, संचित धन",
        3 to "तृतीय भाव • सहज भाव: पराक्रम, भ्राता, संचार, छोटी यात्राएं",
        4 to "चतुर्थ भाव • सुख भाव: माता, गृह, वाहन, मानसिक शांति",
        5 to "पंचम भाव • सुत/बुद्धि भाव: संतान, विद्या, बुद्धि, पूर्व पुण्य",
        6 to "षष्ठ भाव • रिपु/रोग भाव: शत्रु, रोग, ऋण, प्रतियोगिता",
        7 to "सप्तम भाव • कलत्र भाव: विवाह, जीवनसाथी, साझेदारी, व्यापार",
        8 to "अष्टम भाव • आयु भाव: आयु, रहस्य, गुप्त धन, परिवर्तन",
        9 to "नवम भाव • भाग्य/धर्म भाव: भाग्य, धर्म, पिता, उच्च शिक्षा",
        10 to "दशम भाव • कर्म भाव: व्यवसाय, पद-प्रतिष्ठा, राज्य, कर्म",
        11 to "एकादश भाव • आय/लाभ भाव: लाभ, इच्छा-पूर्ति, मित्र, बड़े भाई",
        12 to "द्वादश भाव • व्यय भाव: मोक्ष, व्यय, विदेश, शयन सुख"
    )

    // Return list of normalized polygon vertices [0..1] x [0..1] for each house
    fun getNormalizedHouseVertices(houseNum: Int): List<Offset> {
        return when (houseNum) {
            1 -> listOf(Offset(0.5f, 0f), Offset(0.75f, 0.25f), Offset(0.5f, 0.5f), Offset(0.25f, 0.25f))
            2 -> listOf(Offset(0f, 0f), Offset(0.5f, 0f), Offset(0.25f, 0.25f))
            3 -> listOf(Offset(0f, 0f), Offset(0.25f, 0.25f), Offset(0f, 0.5f))
            4 -> listOf(Offset(0.25f, 0.25f), Offset(0.5f, 0.5f), Offset(0.25f, 0.75f), Offset(0f, 0.5f))
            5 -> listOf(Offset(0f, 0.5f), Offset(0.25f, 0.75f), Offset(0f, 1f))
            6 -> listOf(Offset(0f, 1f), Offset(0.25f, 0.75f), Offset(0.5f, 1f))
            7 -> listOf(Offset(0.5f, 0.5f), Offset(0.75f, 0.75f), Offset(0.5f, 1f), Offset(0.25f, 0.75f))
            8 -> listOf(Offset(0.5f, 1f), Offset(0.75f, 0.75f), Offset(1f, 1f))
            9 -> listOf(Offset(0.75f, 0.75f), Offset(1f, 0.5f), Offset(1f, 1f))
            10 -> listOf(Offset(0.5f, 0.5f), Offset(0.75f, 0.25f), Offset(1f, 0.5f), Offset(0.75f, 0.75f))
            11 -> listOf(Offset(0.75f, 0.25f), Offset(1f, 0.5f), Offset(1f, 0f))
            12 -> listOf(Offset(0.5f, 0f), Offset(0.75f, 0.25f), Offset(1f, 0f))
            else -> emptyList()
        }
    }

    // Mathematical point in convex polygon test using cross-products
    fun isPointInPolygon(p: Offset, vertices: List<Offset>): Boolean {
        if (vertices.size < 3) return false
        var positive = false
        var negative = false
        for (i in vertices.indices) {
            val v1 = vertices[i]
            val v2 = vertices[(i + 1) % vertices.size]
            val crossProduct = (v2.x - v1.x) * (p.y - v1.y) - (v2.y - v1.y) * (p.x - v1.x)
            if (crossProduct > 0f) positive = true
            if (crossProduct < 0f) negative = true
            if (positive && negative) return false
        }
        return true
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
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
                .graphicsLayer {
                    alpha = alphaAnim.value
                    scaleX = scaleAnim.value * userScale
                    scaleY = scaleAnim.value * userScale
                    translationX = userOffset.x
                    translationY = userOffset.y
                }
                .testTag("north_indian_chart_canvas"),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(chartData, userScale, userOffset) {
                        detectTapGestures(
                            onDoubleTap = {
                                if (userScale > 1.1f) {
                                    userScale = 1f
                                    userOffset = Offset.Zero
                                } else {
                                    userScale = 2f
                                }
                            },
                            onTap = { tapOffset ->
                                val w = size.width.toFloat()
                                val h = size.height.toFloat()
                                if (w > 0 && h > 0) {
                                    val center = Offset(w / 2f, h / 2f)
                                    val chartPoint = center + (tapOffset - center - userOffset) / userScale
                                    val normPoint = Offset(chartPoint.x / w, chartPoint.y / h)
                                    for (houseNum in 1..12) {
                                        val vertices = getNormalizedHouseVertices(houseNum)
                                        if (isPointInPolygon(normPoint, vertices)) {
                                            selectedHouse = houseNum
                                            val rashi = getRashiForHouse(houseNum)
                                            val planets = chartData.housePlanetsMap[houseNum] ?: emptyList()
                                            onHouseClick(houseNum, rashi, planets)
                                            break
                                        }
                                    }
                                }
                            }
                        )
                    }
            ) {
                val w = size.width
                val h = size.height
                val strokeWidth = 2.5f

                val chartLineColor = PrimaryButtonBackground
                val gridBorderColor = PrimaryButtonBackground.copy(alpha = 0.45f)

                // 1. Draw Background Fills & Highlights
                for (houseNum in 1..12) {
                    val normVertices = getNormalizedHouseVertices(houseNum)
                    val path = Path().apply {
                        if (normVertices.isNotEmpty()) {
                            moveTo(normVertices[0].x * w, normVertices[0].y * h)
                            for (i in 1 until normVertices.size) {
                                lineTo(normVertices[i].x * w, normVertices[i].y * h)
                            }
                            close()
                        }
                    }

                    if (selectedHouse == houseNum) {
                        // Glowing golden highlight for selected house
                        drawPath(
                            path = path,
                            color = PrimaryButtonBackground.copy(alpha = 0.25f),
                            style = Fill
                        )
                        drawPath(
                            path = path,
                            color = PrimaryButtonBackground,
                            style = Stroke(width = 3.5f)
                        )
                    } else if (houseNum == 1) {
                        // Soft Lagna house fill
                        drawPath(
                            path = path,
                            color = PrimaryButtonBackground.copy(alpha = 0.08f),
                            style = Fill
                        )
                    }
                }

                // 2. Draw Outer Square
                drawRect(
                    color = gridBorderColor,
                    style = Stroke(width = strokeWidth)
                )

                // 3. Main Diagonals
                drawLine(chartLineColor, Offset(0f, 0f), Offset(w, h), strokeWidth = strokeWidth)
                drawLine(chartLineColor, Offset(w, 0f), Offset(0f, h), strokeWidth = strokeWidth)

                // 4. Inner Diamond
                val pTop = Offset(w / 2f, 0f)
                val pRight = Offset(w, h / 2f)
                val pBottom = Offset(w / 2f, h)
                val pLeft = Offset(0f, h / 2f)

                val diamondPath = Path().apply {
                    moveTo(pTop.x, pTop.y)
                    lineTo(pRight.x, pRight.y)
                    lineTo(pBottom.x, pBottom.y)
                    lineTo(pLeft.x, pLeft.y)
                    close()
                }
                drawPath(diamondPath, color = chartLineColor, style = Stroke(width = strokeWidth))

                // 5. Draw Rashi Numbers & Planets in 12 Houses
                val houseCenters = mapOf(
                    1 to Offset(w * 0.5f, h * 0.25f),
                    2 to Offset(w * 0.25f, h * 0.12f),
                    3 to Offset(w * 0.12f, h * 0.25f),
                    4 to Offset(w * 0.25f, h * 0.5f),
                    5 to Offset(w * 0.12f, h * 0.75f),
                    6 to Offset(w * 0.25f, h * 0.88f),
                    7 to Offset(w * 0.5f, h * 0.75f),
                    8 to Offset(w * 0.75f, h * 0.88f),
                    9 to Offset(w * 0.88f, h * 0.75f),
                    10 to Offset(w * 0.75f, h * 0.5f),
                    11 to Offset(w * 0.88f, h * 0.25f),
                    12 to Offset(w * 0.75f, h * 0.12f)
                )

                for (houseNum in 1..12) {
                    val pos = houseCenters[houseNum] ?: Offset(0f, 0f)
                    val rashiNum = getRashiForHouse(houseNum)
                    val planets = chartData.housePlanetsMap[houseNum] ?: emptyList()

                    val rashiText = "$rashiNum"
                    val planetsText = if (planets.isNotEmpty()) planets.joinToString(" ") else ""

                    // Draw Rashi Number in Primary Text
                    val rashiMeas = textMeasurer.measure(
                        text = rashiText,
                        style = TextStyle(
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    drawText(
                        textLayoutResult = rashiMeas,
                        topLeft = Offset(pos.x - rashiMeas.size.width / 2f, pos.y - 22f)
                    )

                    // Draw Lagna indicator tag for House 1
                    if (houseNum == 1) {
                        val lagnaMeas = textMeasurer.measure(
                            text = "लग्न",
                            style = TextStyle(
                                color = PrimaryButtonBackground,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        )
                        drawText(
                            textLayoutResult = lagnaMeas,
                            topLeft = Offset(pos.x - lagnaMeas.size.width / 2f, pos.y - 38f)
                        )
                    }

                    // Draw Planet names & degrees in DateTimeAccent
                    val housePlanets = chartData.planets.filter { it.houseNumber == houseNum }

                    if (userScale > 1.25f && housePlanets.isNotEmpty()) {
                        // Zoomed-in detailed rendering with exact degrees & nakshatra
                        val startY = pos.y + 2f
                        val fontSizeSp = if (userScale > 1.8f) 8.5.sp else 9.5.sp
                        val lineSpacingPx = fontSizeSp.toPx() * 1.3f

                        housePlanets.forEachIndexed { idx, p ->
                            val degInt = p.degree.toInt()
                            val minInt = ((p.degree - degInt) * 60).toInt()
                            val degStr = "${degInt}°${String.format(java.util.Locale.US, "%02d", minInt)}'"
                            val retroStr = if (p.isRetrograde) "(R)" else ""
                            val nakshatraStr = if (userScale > 1.8f && p.nakshatraHi.isNotEmpty()) "[${p.nakshatraHi.take(4)}]" else ""

                            val shortName = p.planetNameHi.substringBefore(" ")
                            val detailedText = listOf(shortName, degStr, retroStr, nakshatraStr)
                                .filter { it.isNotEmpty() }
                                .joinToString(" ")

                            val planetMeas = textMeasurer.measure(
                                text = detailedText,
                                style = TextStyle(
                                    color = DateTimeAccent,
                                    fontSize = fontSizeSp,
                                    fontWeight = FontWeight.Bold
                                )
                            )

                            drawText(
                                textLayoutResult = planetMeas,
                                topLeft = Offset(pos.x - planetMeas.size.width / 2f, startY + (idx * lineSpacingPx))
                            )
                        }
                    } else if (planetsText.isNotEmpty()) {
                        // Zoomed-out concise rendering
                        val planetMeas = textMeasurer.measure(
                            text = planetsText,
                            style = TextStyle(
                                color = DateTimeAccent,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        drawText(
                            textLayoutResult = planetMeas,
                            topLeft = Offset(pos.x - planetMeas.size.width / 2f, pos.y + 2f)
                        )
                    }
                }
            }

            // Zoom reset chip overlay when zoomed
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
                            text = "${String.format(java.util.Locale.US, "%.1f", userScale)}x ${if (userScale > 1.25f) "• अंश/डिग्री" else ""}  रिसेट ↺",
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

        Spacer(modifier = Modifier.height(8.dp))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "उत्तर भारतीय कुण्डली",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = PrimaryButtonBackground,
                    fontWeight = FontWeight.Normal,
                    fontSize = 10.sp
                )
            )
            Text(
                text = "लग्न: ${chartData.ascendantRashiHi}",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = TextSecondary,
                    fontSize = 9.sp
                )
            )
        }

        // Selected House Detail Sheet
        selectedHouse?.let { houseNum ->
            val rashiNum = getRashiForHouse(houseNum)
            val rashiName = rashiNamesHi.getOrElse(rashiNum) { "" }
            val planets = chartData.housePlanetsMap[houseNum] ?: emptyList()
            val significance = houseSignificances[houseNum] ?: ""

            Spacer(modifier = Modifier.height(10.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = ElevatedSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, GlassCardBorder),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = significance.substringBefore(" • "),
                            style = MaterialTheme.typography.titleSmall.copy(
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "राशि: $rashiName ($rashiNum)",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = PrimaryButtonBackground,
                                fontWeight = FontWeight.Normal
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = significance.substringAfter(" • ", ""),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "स्थित ग्रह: ",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextPrimary,
                                fontWeight = FontWeight.Normal,
                                fontSize = 11.sp
                            )
                        )
                        if (planets.isNotEmpty()) {
                            Text(
                                text = planets.joinToString(", "),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = DateTimeAccent,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 12.sp
                                )
                            )
                        } else {
                            Text(
                                text = "— इस भाव में कोई ग्रह नहीं",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
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

