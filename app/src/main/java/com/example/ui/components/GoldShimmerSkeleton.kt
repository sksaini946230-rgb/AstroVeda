package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.PrimaryButtonBackground

/**
 * Gold-tinted shimmer skeleton loader per FSD PAN-016.
 * Replaces spinners with elegant animated placeholder blocks.
 * Uses a moving linear gradient of gold hues.
 */
@Composable
fun GoldShimmer(
    modifier: Modifier = Modifier,
    widthFraction: Float = 1f,
    height: Dp = 16.dp,
    borderRadius: Dp = 8.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1200f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )

    val shimmerBrush = Brush.linearGradient(
        colors = listOf(
            PrimaryButtonBackground.copy(alpha = 0.08f),
            PrimaryButtonBackground.copy(alpha = 0.22f),
            PrimaryButtonBackground.copy(alpha = 0.08f)
        ),
        start = Offset(translateAnim - 300f, 0f),
        end = Offset(translateAnim, 0f)
    )

    Box(
        modifier = modifier
            .fillMaxWidth(widthFraction)
            .height(height)
            .clip(RoundedCornerShape(borderRadius))
            .background(shimmerBrush)
    )
}

/**
 * Skeleton layout that mimics a Panchang card placeholder.
 * Shows gold shimmer blocks in card-like arrangement.
 */
@Composable
fun PanchangCardSkeleton(modifier: Modifier = Modifier) {
    GlassCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            GoldShimmer(widthFraction = 0.5f, height = 22.dp, borderRadius = 6.dp)
            Spacer(modifier = Modifier.height(12.dp))
            GoldShimmer(widthFraction = 0.8f, height = 16.dp, borderRadius = 4.dp)
            Spacer(modifier = Modifier.height(8.dp))
            GoldShimmer(widthFraction = 0.6f, height = 16.dp, borderRadius = 4.dp)
        }
    }
}

/**
 * Skeleton for a mini grid card (Yoga, Karan, etc.)
 */
@Composable
fun MiniCardSkeleton(modifier: Modifier = Modifier) {
    GlassCard(modifier = modifier) {
        Column(modifier = Modifier.padding(12.dp)) {
            GoldShimmer(widthFraction = 0.6f, height = 12.dp, borderRadius = 4.dp)
            Spacer(modifier = Modifier.height(8.dp))
            GoldShimmer(widthFraction = 1f, height = 18.dp, borderRadius = 4.dp)
        }
    }
}

/**
 * Full Panchang loading skeleton: hero card + 6 mini cards in grid
 */
@Composable
fun PanchangLoadingSkeleton(modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(horizontal = 16.dp)) {
        // Hero card skeleton
        PanchangCardSkeleton()
        Spacer(modifier = Modifier.height(16.dp))
        // Grid row 1: 3 mini cards
        Row(modifier = Modifier.fillMaxWidth()) {
            MiniCardSkeleton(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.width(8.dp))
            MiniCardSkeleton(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.width(8.dp))
            MiniCardSkeleton(modifier = Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(8.dp))
        // Grid row 2: 3 mini cards
        Row(modifier = Modifier.fillMaxWidth()) {
            MiniCardSkeleton(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.width(8.dp))
            MiniCardSkeleton(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.width(8.dp))
            MiniCardSkeleton(modifier = Modifier.weight(1f))
        }
    }
}

/**
 * Rashifal card skeleton for horoscope loading state
 */
@Composable
fun RashifalLoadingSkeleton(modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(horizontal = 16.dp)) {
        // Rashi selector skeleton
        Row(modifier = Modifier.fillMaxWidth()) {
            repeat(5) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(4.dp)
                ) {
                    GoldShimmer(height = 48.dp, borderRadius = 24.dp)
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        // Period tabs skeleton
        GoldShimmer(widthFraction = 0.7f, height = 36.dp, borderRadius = 18.dp)
        Spacer(modifier = Modifier.height(16.dp))
        // Content cards
        repeat(3) {
            PanchangCardSkeleton()
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}
