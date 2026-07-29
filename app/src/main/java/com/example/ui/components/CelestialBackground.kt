package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.animation.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.R
import kotlinx.coroutines.delay

@Composable
fun CelestialBackground(
    deferred: Boolean = false,
    content: @Composable () -> Unit
) {
    var showBackground by remember { mutableStateOf(!deferred) }
    
    LaunchedEffect(deferred) {
        if (deferred) {
            delay(100) // Small delay to prioritize critical UI rendering
            showBackground = true
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Base cosmic background with smooth entry
        AnimatedVisibility(
            visible = showBackground,
            enter = fadeIn(animationSpec = androidx.compose.animation.core.tween(1000))
        ) {
            Image(
                painter = painterResource(id = R.drawable.divine_cosmic_background_1784865807496),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        
        // Gradient overlay for better text readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background.copy(alpha = 0.55f),
                            MaterialTheme.colorScheme.background.copy(alpha = 0.25f),
                            MaterialTheme.colorScheme.background.copy(alpha = 0.8f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
        )
        
        content()
    }
}
