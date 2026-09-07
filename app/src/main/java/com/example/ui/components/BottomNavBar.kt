package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.util.LanguageManager

data class NavItem(
    val tab: AppTab,
    val titleHi: String,
    val titleEn: String,
    /** Shown when the tab is selected. */
    val iconFilled: ImageVector,
    /** Shown when it is not. */
    val iconOutlined: ImageVector
)

/**
 * The bottom navigation bar.
 *
 * Rewritten to look like a bar from this decade. What it used to be, and why
 * each of those went:
 *
 *  - **Rounded top corners and a 16dp drop shadow.** A floating, card-like bar
 *    was the 2019 look; every current app — Instagram, YouTube, Gmail — sits
 *    flat against the bottom edge with at most a hairline above it. The shadow
 *    also fought with the ad banner directly above it.
 *  - **A gradient accent line across the top.** Decoration that read as an
 *    artifact rather than a divider. One hairline in the border colour now.
 *  - **A 2.5dp dot above the selected icon.** It appeared and disappeared with
 *    selection, so the icon and label shifted down a couple of pixels every
 *    time a tab changed. The selected state is a pill *behind* the icon now,
 *    which is what Material 3 does and what most current apps do: it animates
 *    in place and moves nothing.
 *  - **One icon for both states, distinguished only by colour.** Filled when
 *    selected and outlined when not is the strongest, cheapest signal there is,
 *    and it survives being looked at in a hurry or by someone who does not
 *    separate the two golds well.
 *
 * Sizes went up rather than down: a 20dp icon and 10.5sp label were small for a
 * bar people tap without looking. 24dp and 11sp, with the pill giving each item
 * a real 32dp-tall target.
 */
@Composable
fun BottomNavBar(
    selectedTab: AppTab,
    onTabSelected: (AppTab) -> Unit
) {
    val items = listOf(
        NavItem(AppTab.PANCHANG, "पंचांग", "Panchang", Icons.Filled.WbSunny, Icons.Outlined.WbSunny),
        NavItem(AppTab.RASHIFAL, "राशिफल", "Horoscope", Icons.Filled.GridView, Icons.Outlined.GridView),
        NavItem(AppTab.KUNDALI, "कुण्डली", "Kundali", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome),
        NavItem(AppTab.MUHURAT, "मुहूर्त", "Muhurat", Icons.Filled.Schedule, Icons.Outlined.Schedule),
        NavItem(AppTab.MORE, "और", "More", Icons.Filled.Settings, Icons.Outlined.Settings)
    )

    val view = LocalView.current

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = ElevatedSurface,
        tonalElevation = 0.dp
    ) {
        Column {
            // A hairline, not a gradient. It separates the bar from the content
            // above it and does nothing else.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(GlassCardBorder.copy(alpha = 0.5f))
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { item ->
                    val isSelected = selectedTab == item.tab
                    val localizedTitle = LanguageManager.getString(item.titleHi, item.titleEn)

                    val contentColor by animateColorAsState(
                        targetValue = if (isSelected) NavActiveColor else NavInactiveColor,
                        animationSpec = spring(stiffness = Spring.StiffnessLow),
                        label = "navContentColor"
                    )

                    // The pill grows from nothing rather than appearing, so the
                    // change reads as movement instead of a flash.
                    val pillWidth by animateDpAsState(
                        targetValue = if (isSelected) 56.dp else 0.dp,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        ),
                        label = "navPillWidth"
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .selectable(
                                selected = isSelected,
                                role = Role.Tab,
                                onClick = {
                                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                    onTabSelected(item.tab)
                                }
                            )
                            .padding(vertical = 2.dp)
                            .testTag("nav_item_${item.tab.name.lowercase()}")
                    ) {
                        Box(
                            modifier = Modifier.height(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(pillWidth)
                                    .height(32.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(NavActiveColor.copy(alpha = 0.16f))
                            )
                            Icon(
                                imageVector = if (isSelected) item.iconFilled else item.iconOutlined,
                                contentDescription = localizedTitle,
                                modifier = Modifier.size(24.dp),
                                tint = contentColor
                            )
                        }

                        Spacer(modifier = Modifier.height(3.dp))

                        Text(
                            text = localizedTitle,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                lineHeight = 14.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                letterSpacing = 0.2.sp
                            ),
                            color = contentColor,
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
