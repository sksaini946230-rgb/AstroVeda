package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MinimalistGold
import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics
import java.io.PrintWriter
import java.io.StringWriter

/**
 * Global CompositionLocal to allow deeply nested Composables to report exceptions or failures
 * (like calculation errors, database errors, or API failures) to the nearest [ErrorBoundary].
 */
val LocalErrorBoundaryHandler = staticCompositionLocalOf<((Throwable) -> Unit)?> { null }

/**
 * A robust, beautiful Material 3 Error Boundary component that wraps content and provides
 * a centralized handler for reporting runtime exceptions.
 * When an error is caught or reported, it replaces the content with a gorgeous, user-friendly
 * error recovery screen with a "Retry" button.
 */
@Composable
fun ErrorBoundary(
    modifier: Modifier = Modifier,
    externalError: Throwable? = null,
    onClearError: () -> Unit = {},
    onRetry: () -> Unit = {},
    content: @Composable () -> Unit
) {
    var internalError by remember { mutableStateOf<Throwable?>(null) }
    val errorState = externalError ?: internalError

    // Handler that nested child components can use to report async or event-driven exceptions
    val errorHandler: (Throwable) -> Unit = remember {
        { throwable ->
            internalError = throwable
            try {
                Firebase.crashlytics.recordException(throwable)
            } catch (e: Exception) {
                // Ignore if not initialized
            }
        }
    }

    CompositionLocalProvider(LocalErrorBoundaryHandler provides errorHandler) {
        if (errorState != null) {
            ErrorStateUI(
                error = errorState,
                onRetry = {
                    internalError = null
                    onClearError()
                    onRetry()
                },
                modifier = modifier
            )
        } else {
            Box(modifier = modifier) {
                content()
            }
        }
    }
}

/**
 * A visually stunning cosmic-themed error fallback UI compliant with AstroVeda's design language.
 */
@Composable
fun ErrorStateUI(
    error: Throwable,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDetails by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0F0C1B), // Cosmic deep space
            Color(0xFF15102A),
            Color(0xFF0F0C1B)
        )
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundGradient)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Error Icon with a pulsing celestial glow
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(40.dp))
                    .background(Color(0x1AFF5252))
                    .border(2.dp, Color(0xFFFF5252), RoundedCornerShape(40.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ErrorOutline,
                    contentDescription = "Error Icon",
                    tint = Color(0xFFFF5252),
                    modifier = Modifier.size(44.dp)
                )
            }

            // Centralized Error Heading in Hindi & English
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "ब्रह्मांडीय संरेखण बाधित",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        textAlign = TextAlign.Center
                    )
                )
                Text(
                    text = "Cosmic Alignment Interrupted",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                )
            }

            // User-friendly descriptive error card using com.example.ui.components.GlassCard
            GlassCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "गणना या नेटवर्क के दौरान एक अप्रत्याशित समस्या आई है। कृपया पुन: प्रयास करें।",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.White.copy(alpha = 0.9f),
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp
                        )
                    )
                    
                    Text(
                        text = "An unexpected issue occurred during calculation or network request. Please try again.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        )
                    )
                }
            }

            // Primary Action Buttons
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("error_retry_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Retry",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "पुनः प्रयास करें (Retry)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            // Optional Expandable Technical Debugger Section
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TextButton(
                    onClick = { showDetails = !showDetails },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        imageVector = if (showDetails) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Toggle Details",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (showDetails) "तकनीकी विवरण छिपाएं (Hide Details)" else "तकनीकी विवरण देखें (Show Technical Details)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                AnimatedVisibility(
                    visible = showDetails,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 200.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF07050E))
                            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        val sw = StringWriter()
                        error.printStackTrace(PrintWriter(sw))
                        val stackTrace = sw.toString()

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(bottom = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.BugReport,
                                    contentDescription = "Bug",
                                    tint = Color(0xFFFF5252),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = error.javaClass.simpleName,
                                    color = Color(0xFFFF5252),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                            Text(
                                text = error.message ?: "No error message provided.",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Text(
                                text = stackTrace,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp,
                                lineHeight = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
