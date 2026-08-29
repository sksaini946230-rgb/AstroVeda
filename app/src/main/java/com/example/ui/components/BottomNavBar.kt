package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import android.view.HapticFeedbackConstants
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppTab
import com.example.ui.theme.ElevatedSurface
import com.example.ui.theme.GlassCardBorder
import com.example.ui.theme.NavActiveColor
import com.example.ui.theme.NavInactiveColor
import com.example.ui.theme.TextTertiary
import com.example.util.LanguageManager

data class NavItem(
    val tab: AppTab,
    val titleHi: String,
    val titleEn: String,
    val icon: ImageVector
)

@Composable
fun BottomNavBar(
    selectedTab: AppTab,
    onTabSelected: (AppTab) -> Unit
) {
    val items = listOf(
        NavItem(AppTab.PANCHANG, "पंचांग", "Panchang", Icons.Default.WbSunny),
        NavItem(AppTab.RASHIFAL, "राशिफल", "Horoscope", Icons.Default.GridView),
        NavItem(AppTab.KUNDALI, "कुंडली", "Kundali", Icons.Default.AutoAwesome),
        NavItem(AppTab.MUHURAT, "मुहूर्त", "Muhurat", Icons.Default.Schedule),
        NavItem(AppTab.MORE, "और", "More", Icons.Default.Settings)
    )

    val view = LocalView.current
    val isLightTheme = com.example.ui.theme.LocalAstroColors.current.isLight

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                ambientColor = Color.Black.copy(alpha = if (isLightTheme) 0.08f else 0.3f),
                spotColor = Color.Black.copy(alpha = if (isLightTheme) 0.12f else 0.4f)
            )
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
        color = ElevatedSurface,
        tonalElevation = 0.dp
    ) {
        // Subtle top accent line
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                GlassCardBorder,
                                GlassCardBorder,
                                Color.Transparent
                            )
                        )
                    )
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    // Bar height trimmed ~20%: this was vertical = 8.dp.
                    .padding(horizontal = 4.dp, vertical = 5.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { item ->
                    val isSelected = selectedTab == item.tab
                    val localizedTitle = LanguageManager.getString(item.titleHi, item.titleEn)

                    val iconScale by animateFloatAsState(
                        targetValue = if (isSelected) 1.15f else 1.0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        label = "iconScale"
                    )

                    val iconColor by animateColorAsState(
                        targetValue = if (isSelected) NavActiveColor else NavInactiveColor,
                        animationSpec = spring(stiffness = Spring.StiffnessLow),
                        label = "iconColor"
                    )

                    val textColor by animateColorAsState(
                        // TextTertiary is the disabled-text token; on this bar it was
                        // effectively invisible. Inactive labels share the icon colour now.
                        targetValue = if (isSelected) NavActiveColor else NavInactiveColor,
                        animationSpec = spring(stiffness = Spring.StiffnessLow),
                        label = "textColor"
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .selectable(
                                selected = isSelected,
                                role = Role.Tab,
                                onClick = {
                                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                    onTabSelected(item.tab)
                                }
                            )
                            .padding(vertical = 4.dp)
                            .testTag("nav_item_${item.tab.name.lowercase()}")
                    ) {
                        // Dot indicator for selected tab
                        Box(
                            modifier = Modifier
                                .size(if (isSelected) 3.dp else 0.dp)
                                .clip(CircleShape)
                                .background(NavActiveColor)
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Icon(
                            imageVector = item.icon,
                            contentDescription = localizedTitle,
                            modifier = Modifier
                                .size(21.dp)
                                .scale(iconScale),
                            tint = iconColor
                        )

                        Spacer(modifier = Modifier.height(3.dp))

                        Text(
                            text = localizedTitle,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.5.sp,
                                lineHeight = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                letterSpacing = 0.3.sp
                            ),
                            color = textColor,
                            maxLines = 1
                        )
                    }
                }
            }

            // System navigation bar safe area
            Spacer(modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars))
        }
    }
}
