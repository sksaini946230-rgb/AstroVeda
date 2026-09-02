package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppBackground
import com.example.ui.theme.CosmicGradientEdge
import com.example.ui.theme.ElevatedSurface
import com.example.ui.theme.PrimaryButtonBackground
import com.example.ui.theme.SurfaceBackground
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlin.math.sin
import kotlin.random.Random

/**
 * Animated cosmic splash screen per FSD S01 spec:
 * - Deep space radial gradient background
 * - 50 floating star particles
 * - App name scale animation (0→1 with spring-like ease)
 * - Gold glow pulse on logo
 * - Hindi tagline type-in animation
 * - Auto-navigate after 1500ms
 */
@Composable
fun SplashScreen(
    onSplashComplete: () -> Unit
) {
    // === Animation states ===
    val logoAlpha = remember { Animatable(0f) }
    val nameAlpha = remember { Animatable(0f) }
    val taglineCharCount = remember { mutableIntStateOf(0) }

    val fullTagline = com.example.util.LanguageManager.getString("ज्योतिष की शक्ति", "The power of Jyotish")

    // Gold glow pulsing
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowPulse"
    )

    // Star particles data (generated once)
    val stars = remember {
        List(50) {
            StarParticle(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                size = Random.nextFloat() * 2f + 1f,
                alpha = Random.nextFloat() * 0.3f + 0.1f,
                speed = Random.nextFloat() * 0.5f + 0.2f,
                phase = Random.nextFloat() * 6.28f
            )
        }
    }
    val starTime by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "starFloat"
    )

    // === Orchestrated animation sequence ===
    LaunchedEffect(Unit) {
        // Logo scale in: 0→1 with overshoot-like easing (600ms)
        logoAlpha.animateTo(1f, animationSpec = tween(200))
        // App name fade in 600ms after logo
        delay(300)
        nameAlpha.animateTo(1f, animationSpec = tween(600))
        // Tagline type-in animation (300ms after name)
        delay(100)
        for (i in 1..fullTagline.length) {
            taglineCharCount.intValue = i
            delay(40)
        }
        // Wait for total ~1500ms then navigate
        delay(200)
        onSplashComplete()
    }

    // === UI ===
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        SurfaceBackground, // Deep Space center
                        AppBackground, // Void Black edges
                        CosmicGradientEdge // Subtle cosmic violet edge
                    ),
                    radius = 1200f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Floating star particles
        // Read outside the DrawScope, which is not composable.
        val starColor = TextPrimary
        val goldColor = PrimaryButtonBackground
        Canvas(modifier = Modifier.fillMaxSize()) {
            stars.forEach { star ->
                val offsetY = sin((starTime + star.phase).toDouble()).toFloat() * 8f
                drawCircle(
                    color = starColor.copy(alpha = star.alpha),
                    radius = star.size,
                    center = Offset(
                        x = star.x * size.width,
                        y = star.y * size.height + offsetY
                    )
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Gold glow behind the OM symbol
            Box(contentAlignment = Alignment.Center) {
                // Glow circle
                Canvas(
                    modifier = Modifier
                        .size(120.dp)
                        .alpha(glowAlpha)
                ) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                goldColor.copy(alpha = 0.4f),
                                goldColor.copy(alpha = 0.1f),
                                Color.Transparent
                            )
                        ),
                        radius = size.minDimension / 2f
                    )
                }
                // The mark is handed over from the system splash, which draws the
                // launcher icon in the launcher's own mask — a rounded square on
                // most phones. Clipping it to a circle here meant the logo
                // visibly changed shape a beat into every cold start, and
                // scaling it up from nothing made the corners flick in and out.
                // Same shape, same scale: the handover stops being visible.
                Image(
                    painter = painterResource(id = app.revati.jyotish.R.drawable.revati_logo),
                    contentDescription = "Revati Logo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(112.dp)
                        .clip(RoundedCornerShape(26.dp))
                        .alpha(logoAlpha.value)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // App name with gradient gold
            Text(
                text = "Revati",
                fontSize = 42.sp,
                fontWeight = FontWeight.ExtraBold,
                color = PrimaryButtonBackground,
                modifier = Modifier.alpha(nameAlpha.value),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Hindi tagline with type-in effect
            @Suppress("AutoboxingStateValueProperty")
            val displayedTagline = fullTagline.take(taglineCharCount.intValue)
            Text(
                text = displayedTagline,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary.copy(alpha = 0.85f),
                textAlign = TextAlign.Center
            )
        }
    }
}

private data class StarParticle(
    val x: Float,
    val y: Float,
    val size: Float,
    val alpha: Float,
    val speed: Float,
    val phase: Float
)
