package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.ui.theme.PremiumGold
import com.example.util.LanguageManager

data class DiscoveryStep(
    val titleHi: String,
    val titleEn: String,
    val descriptionHi: String,
    val descriptionEn: String,
    val icon: ImageVector? = null
)

@Composable
fun FeatureDiscoveryOverlay(
    steps: List<DiscoveryStep>,
    onComplete: () -> Unit
) {
    var currentStepIndex by remember { mutableIntStateOf(0) }
    val currentStep = steps[currentStepIndex]
    
    val secondaryText = Color(0xFF7885A8) // Requested secondary text color

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f))
            .clickable(enabled = false) {} // Consume clicks
            .zIndex(9999f),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(32.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Step Indicator
            Row(
                modifier = Modifier.padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                steps.forEachIndexed { index, _ ->
                    Box(
                        modifier = Modifier
                            .size(if (index == currentStepIndex) 24.dp else 8.dp, 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (index == currentStepIndex) PremiumGold else secondaryText.copy(alpha = 0.3f)
                            )
                    )
                }
            }

            // Discovery Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF161B22) // Dark surface
                ),
                shape = RoundedCornerShape(24.dp),
                border = BoxDefaults.PremiumBorder()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    currentStep.icon?.let {
                        Icon(
                            imageVector = it,
                            contentDescription = null,
                            modifier = Modifier
                                .size(64.dp)
                                .padding(bottom = 16.dp),
                            tint = PremiumGold
                        )
                    }

                    Text(
                        text = LanguageManager.getString(currentStep.titleHi, currentStep.titleEn),
                        style = MaterialTheme.typography.headlineSmall,
                        color = PremiumGold,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = LanguageManager.getString(currentStep.descriptionHi, currentStep.descriptionEn),
                        style = MaterialTheme.typography.bodyLarge,
                        color = secondaryText,
                        textAlign = TextAlign.Center,
                        lineHeight = 24.sp
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        TextButton(
                            onClick = onComplete,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = secondaryText
                            )
                        ) {
                            Text(LanguageManager.getString("छोड़ें", "Skip"))
                        }

                        Button(
                            onClick = {
                                if (currentStepIndex < steps.size - 1) {
                                    currentStepIndex++
                                } else {
                                    onComplete()
                                }
                            },
                            modifier = Modifier.weight(2f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PremiumGold,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = if (currentStepIndex < steps.size - 1) 
                                    LanguageManager.getString("अगला", "Next") 
                                else 
                                    LanguageManager.getString("शुरू करें", "Get Started"),
                                fontWeight = FontWeight.Bold
                            )
                            if (currentStepIndex < steps.size - 1) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            IconButton(
                onClick = onComplete,
                modifier = Modifier
                    .size(48.dp)
                    .background(Color.White.copy(alpha = 0.1f), CircleShape)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
            }
        }
    }
}

object BoxDefaults {
    @Composable
    fun PremiumBorder() = androidx.compose.foundation.BorderStroke(
        width = 1.dp,
        color = PremiumGold.copy(alpha = 0.2f)
    )
}
