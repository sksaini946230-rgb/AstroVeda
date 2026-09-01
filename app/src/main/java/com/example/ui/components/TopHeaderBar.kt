package com.example.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.revati.jyotish.R
import com.example.ui.theme.PrimaryButtonText
import com.example.ui.theme.ProBadgeColor
import com.example.ui.theme.ShubhSuccessColor
import com.example.util.AppLanguage
import com.example.util.LanguageManager

import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LinearProgressIndicator

import com.example.ui.components.OfflineStatusChip

import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.RepeatMode
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.alpha

@Composable
fun FirestoreSyncIndicator(isSyncing: Boolean, modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "firestore_sync")
    
    if (isSyncing) {
        val angle by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(1200, easing = androidx.compose.animation.core.LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "sync_rotation"
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier
                .testTag("firestore_sync_indicator_syncing")
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(4.dp)
                )
                .padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Syncing",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(11.dp)
                    .rotate(angle)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = LanguageManager.getString("बैकअप जारी...", "Backing up..."),
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Normal
                )
            )
        }
    } else {
        val pulseAlpha by infiniteTransition.animateFloat(
            initialValue = 0.4f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(1500, easing = androidx.compose.animation.core.FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse_alpha"
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier
                .testTag("firestore_sync_indicator_synced")
                .background(
                    color = ShubhSuccessColor.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(4.dp)
                )
                .padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .clip(CircleShape)
                    .alpha(pulseAlpha)
                    .background(ShubhSuccessColor)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Icon(
                imageVector = Icons.Default.CloudDone,
                contentDescription = "Backed up",
                tint = ShubhSuccessColor,
                modifier = Modifier.size(10.dp)
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = LanguageManager.getString("सुरक्षित", "Synced"),
                style = MaterialTheme.typography.labelSmall.copy(
                    color = ShubhSuccessColor,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Normal
                )
            )
        }
    }
}

@Composable
fun TopHeaderBar(
    isOffline: Boolean = false,
    isSyncing: Boolean = false,
    isFirestoreSyncing: Boolean = false,
    isCloudBackupEnabled: Boolean = false,
    onLanguageToggle: () -> Unit = {},
    onPremiumClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    val view = LocalView.current
    Surface(
        modifier = Modifier.fillMaxWidth().statusBarsPadding(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.fillMaxWidth()) {
            // Header Content
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    // Header height trimmed ~20%: this was vertical = 14.dp.
                    // The logo came down with it so the row stays proportioned.
                    .padding(horizontal = 16.dp, vertical = 11.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // App Icon with specialized border
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary)
                            )
                        )
                        .padding(1.5.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.revati_logo),
                        contentDescription = "Revati Logo",
                        modifier = Modifier
                            .size(31.dp)
                            .clip(CircleShape)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Revati",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 20.sp,
                            letterSpacing = 0.5.sp
                        ),
                        // Without these the title breaks a character per line when the
                        // row runs out of room — which is exactly what happened the
                        // moment the offline chip appeared beside it.
                        maxLines = 1,
                        softWrap = false,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // The tagline shares this row with the offline and sync
                        // chips, and there is not width for both: with the sync
                        // chip up it truncated to "Vedic P…", which says nothing.
                        // Show the chips when there are chips, the tagline when
                        // there are not.
                        if (!isOffline && !isCloudBackupEnabled) {
                            Text(
                            text = LanguageManager.getString(
                                "वैदिक पंचांग एवं कुण्डली 2026",
                                "Vedic Panchang & Kundali 2026"
                            ),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Normal
                            ),
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                            )
                        } else if (isOffline) {
                            OfflineStatusChip(text = LanguageManager.getString("ऑफलाइन", "Offline"))
                        }
                        if (isCloudBackupEnabled) {
                            Spacer(modifier = Modifier.width(6.dp))
                            FirestoreSyncIndicator(isSyncing = isFirestoreSyncing)
                        }
                    }
                }

                // Action Buttons Row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Language Switcher
                    Surface(
                        onClick = {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            onLanguageToggle()
                        },
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                        modifier = Modifier.testTag("language_toggle_button")
                    ) {
                        Text(
                            text = if (LanguageManager.currentLanguage == AppLanguage.HINDI) "ENG" else "हिन्दी",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Normal,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 11.sp
                            ),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Premium PRO Badge
                    Surface(
                        onClick = {
                            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                            onPremiumClick()
                        },
                        shape = RoundedCornerShape(16.dp),
                        color = ProBadgeColor,
                        modifier = Modifier.testTag("premium_upgrade_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "PRO",
                                tint = PrimaryButtonText,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "PRO",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Normal,
                                    color = PrimaryButtonText,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Settings
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), CircleShape)
                            .clickable {
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                onSettingsClick()
                            }
                            .testTag("settings_icon_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
        if (isSyncing) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .testTag("sync_progress_bar"),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                )
            } else {
                Spacer(modifier = Modifier.height(2.dp))
            }
        }
    }
}
