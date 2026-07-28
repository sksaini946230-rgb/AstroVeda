package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.KundaliChartData
import com.example.astro.KundaliCalculator
import kotlin.math.cos
import kotlin.math.sin

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.graphicsLayer

@Composable
fun TransitWheelChart(
    birthData: KundaliChartData,
    transitData: KundaliChartData,
    modifier: Modifier = Modifier
) {
    val alphaAnim = remember { Animatable(0f) }
    val rotateAnim = remember { Animatable(-15f) }
    
    LaunchedEffect(transitData) {
        alphaAnim.animateTo(1f, animationSpec = tween(800))
        rotateAnim.animateTo(0f, animationSpec = tween(800))
    }

    val textMeasurer = rememberTextMeasurer()
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .graphicsLayer {
                alpha = alphaAnim.value
                rotationZ = rotateAnim.value
            }
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.3f))
            .testTag("transit_wheel_chart"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = size.minDimension / 2f
            val ringWidth = radius * 0.25f
            
            // Outer Circle (Transit)
            drawCircle(
                color = onSurfaceVariant.copy(alpha = 0.1f),
                radius = radius,
                center = center,
                style = Stroke(width = 1f)
            )
            
            // Middle Circle (Divider)
            drawCircle(
                color = onSurfaceVariant.copy(alpha = 0.1f),
                radius = radius - ringWidth,
                center = center,
                style = Stroke(width = 1f)
            )
            
            // Inner Circle (Birth)
            drawCircle(
                color = onSurfaceVariant.copy(alpha = 0.1f),
                radius = radius - ringWidth * 2f,
                center = center,
                style = Stroke(width = 1f)
            )

            // Draw Zodiac Divisions (12 Rashis)
            for (i in 0 until 12) {
                val angle = Math.toRadians(i * 30.0 - 90.0)
                val lineStart = Offset(
                    center.x + (radius - ringWidth * 2.5f) * cos(angle).toFloat(),
                    center.y + (radius - ringWidth * 2.5f) * sin(angle).toFloat()
                )
                val lineEnd = Offset(
                    center.x + radius * cos(angle).toFloat(),
                    center.y + radius * sin(angle).toFloat()
                )
                drawLine(
                    color = onSurfaceVariant.copy(alpha = 0.2f),
                    start = lineStart,
                    end = lineEnd,
                    strokeWidth = 1f
                )
                
                // Draw Rashi Symbol/Name
                val textAngle = Math.toRadians(i * 30.0 + 15.0 - 90.0)
                val textPos = Offset(
                    center.x + (radius - ringWidth * 2.2f) * cos(textAngle).toFloat(),
                    center.y + (radius - ringWidth * 2.2f) * sin(textAngle).toFloat()
                )
                
                drawText(
                    textMeasurer = textMeasurer,
                    text = (i + 1).toString(),
                    topLeft = Offset(textPos.x - 8f, textPos.y - 12f),
                    style = TextStyle(
                        color = primaryColor.copy(alpha = 0.6f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            // Draw Connecting Lines (Radar-like)
            transitData.planets.forEach { transitPlanet ->
                val birthPlanet = birthData.planets.find { it.planetNameEn == transitPlanet.planetNameEn }
                if (birthPlanet != null) {
                    val birthDeg = (birthPlanet.rashiNumber - 1) * 30.0 + birthPlanet.degree
                    val transitDeg = (transitPlanet.rashiNumber - 1) * 30.0 + transitPlanet.degree
                    
                    val birthAngle = Math.toRadians(birthDeg - 90.0)
                    val transitAngle = Math.toRadians(transitDeg - 90.0)
                    
                    val birthPos = Offset(
                        center.x + (radius - ringWidth * 1.5f) * cos(birthAngle).toFloat(),
                        center.y + (radius - ringWidth * 1.5f) * sin(birthAngle).toFloat()
                    )
                    val transitPos = Offset(
                        center.x + (radius - ringWidth * 0.5f) * cos(transitAngle).toFloat(),
                        center.y + (radius - ringWidth * 0.5f) * sin(transitAngle).toFloat()
                    )
                    
                    drawLine(
                        color = primaryColor.copy(alpha = 0.2f),
                        start = birthPos,
                        end = transitPos,
                        strokeWidth = 1f,
                        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )
                }
            }

            // Draw Birth Planets (Inner Ring)
            birthData.planets.forEach { planet ->
                val totalDeg = (planet.rashiNumber - 1) * 30.0 + planet.degree
                val angle = Math.toRadians(totalDeg - 90.0)
                val pos = Offset(
                    center.x + (radius - ringWidth * 1.5f) * cos(angle).toFloat(),
                    center.y + (radius - ringWidth * 1.5f) * sin(angle).toFloat()
                )
                
                drawCircle(
                    color = secondaryColor,
                    radius = 6f,
                    center = pos
                )
                
                drawText(
                    textMeasurer = textMeasurer,
                    text = planet.planetNameHi.substringBefore(" "),
                    topLeft = Offset(pos.x - 12f, pos.y - 30f),
                    style = TextStyle(
                        color = onSurface,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }

            // Draw Transit Planets (Outer Ring)
            transitData.planets.forEach { planet ->
                val totalDeg = (planet.rashiNumber - 1) * 30.0 + planet.degree
                val angle = Math.toRadians(totalDeg - 90.0)
                val pos = Offset(
                    center.x + (radius - ringWidth * 0.5f) * cos(angle).toFloat(),
                    center.y + (radius - ringWidth * 0.5f) * sin(angle).toFloat()
                )
                
                drawCircle(
                    color = tertiaryColor,
                    radius = 8f,
                    center = pos
                )
                
                drawText(
                    textMeasurer = textMeasurer,
                    text = planet.planetNameHi.substringBefore(" "),
                    topLeft = Offset(pos.x - 12f, pos.y - 30f),
                    style = TextStyle(
                        color = primaryColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
        
        // Legend Overlay
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(2.dp)).background(secondaryColor))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Birth (जन्म)", style = MaterialTheme.typography.labelSmall.copy(color = onSurfaceVariant, fontSize = 9.sp))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(2.dp)).background(tertiaryColor))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Transit (गोचर)", style = MaterialTheme.typography.labelSmall.copy(color = onSurfaceVariant, fontSize = 9.sp))
            }
        }
    }
}
