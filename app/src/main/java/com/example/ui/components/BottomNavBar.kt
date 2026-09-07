package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.view.HapticFeedbackConstants
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
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

/** Side margin, so the capsule floats clear of both edges. */
private val BAR_SIDE_MARGIN = 12.dp

/** Gap under the capsule, above the system navigation area. */
private val BAR_BOTTOM_MARGIN = 10.dp

/** Ring of space between the capsule's edge and the pills inside it. */
private val BAR_INNER_PADDING = 6.dp

private val ICON_SIZE = 22.dp

/** Between the icon and its label. They are one object, so this is small. */
private val ICON_LABEL_GAP = 6.dp

/** Space inside the selected pill, left and right of its contents. */
private val PILL_H_PADDING = 12.dp

/** Above and below the icon inside a pill. Sets the bar's height. */
private val PILL_V_PADDING = 9.dp

private val LABEL_SIZES = listOf(12.sp, 11.sp, 10.sp, 9.sp, 8.5.sp)

/**
 * A floating capsule navigation bar.
 *
 * It clears all three edges, is fully rounded, and the selected tab is a
 * capsule of its own holding **the icon and the label side by side** with a
 * small gap. Unselected tabs are the icon alone.
 *
 * That last part is not a style choice, it is what makes the bar fit. Five
 * labels cannot share a 320dp screen: measured, "Horoscope" needs 60px at 8.5sp
 * once the user's font scale is 1.3x, and five items on that screen have 50px
 * each. The first attempt at this shrank the type until it fitted and, at large
 * font scales, the words still ran into each other. Showing one label at a time
 * gives that label roughly a third of the bar instead of a fifth, which is
 * enough room in every combination this app is used in — and it is what current
 * floating navigation bars do, for the same reason.
 *
 * **Nothing is allowed to clip, on any screen.** The label size is measured
 * rather than chosen: the widest label is rendered at each candidate size and
 * the largest one that fits the selected pill wins. Two things that had to be
 * got right, both of which were wrong the first time:
 *
 *  - The widest label is not the longest one. "कुण्डली" has more characters
 *    than "राशिफल" and is 5px narrower; picking by `length` measured the wrong
 *    string. Every label is measured now and the maximum taken.
 *  - The English labels are the long ones, not the Hindi. "Horoscope" is nearly
 *    twice the width of "राशिफल".
 *
 * If even the smallest size does not fit — a very narrow screen at a very large
 * font scale — the label is dropped and the icons stand alone. An icon with no
 * label is usable; half a word is not.
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

    // The selected pill is this many times the width of an unselected one. The
    // label has to live in the difference.
    val selectedWeightWithLabel = 2.8f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(start = BAR_SIDE_MARGIN, end = BAR_SIDE_MARGIN, bottom = BAR_BOTTOM_MARGIN)
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val measurer = rememberTextMeasurer()
            val density = LocalDensity.current
            val baseStyle = LocalTextStyle.current
            val labels = items.map { LanguageManager.getString(it.titleHi, it.titleEn) }

            // What the selected pill can actually give its label: its share of
            // the row, less the icon, the gap and its own padding.
            val labelRoomPx = with(density) {
                val inner = maxWidth - BAR_INNER_PADDING * 2
                val unit = inner / (selectedWeightWithLabel + (items.size - 1))
                ((unit * selectedWeightWithLabel) - ICON_SIZE - ICON_LABEL_GAP - PILL_H_PADDING * 2).toPx()
            }

            val labelSize: TextUnit? = remember(labels, labelRoomPx, density, baseStyle) {
                LABEL_SIZES.firstOrNull { candidate ->
                    // Every label, not the longest one — see the note above.
                    labels.maxOf { label ->
                        measurer.measure(
                            AnnotatedString(label),
                            baseStyle.copy(fontSize = candidate),
                            maxLines = 1
                        ).size.width
                    } <= labelRoomPx
                }
            }

            // The pill is exactly as tall as the icon plus its padding, and the
            // bar is the pill plus its ring — so both are true capsules at every
            // font scale rather than rounded rectangles that happen to look
            // close. The label sits inside that height; it never adds to it.
            // With no label to make room for, every pill is the same size —
            // otherwise the selected one is a wide empty capsule around a
            // centred icon, which is what the first version of this fallback
            // looked like.
            val selectedWeight = if (labelSize == null) 1f else selectedWeightWithLabel

            val pillHeight = ICON_SIZE + PILL_V_PADDING * 2
            val barHeight = pillHeight + BAR_INNER_PADDING * 2

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(barHeight)
                    .shadow(
                        elevation = 10.dp,
                        shape = RoundedCornerShape(percent = 50),
                        clip = false
                    )
                    .clip(RoundedCornerShape(percent = 50))
                    // Soft, not a drawn outline. At 0.6 this read as a bright
                    // ring on the dark theme and pulled more attention than the
                    // bar it was edging; the shadow already does the work of
                    // lifting the capsule off the content.
                    .border(1.dp, GlassCardBorder.copy(alpha = 0.28f), RoundedCornerShape(percent = 50)),
                color = ElevatedSurface,
                tonalElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(BAR_INNER_PADDING),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items.forEachIndexed { index, item ->
                        val isSelected = selectedTab == item.tab
                        val label = labels[index]

                        // Animating the weight is what makes the pill slide and
                        // grow instead of jumping between tabs.
                        val weight by animateFloatAsState(
                            targetValue = if (isSelected) selectedWeight else 1f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            ),
                            label = "navWeight"
                        )

                        val contentColor by animateColorAsState(
                            targetValue = if (isSelected) NavActiveColor else NavInactiveColor,
                            animationSpec = spring(stiffness = Spring.StiffnessLow),
                            label = "navContentColor"
                        )

                        val pillAlpha by animateFloatAsState(
                            targetValue = if (isSelected) 1f else 0f,
                            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                            label = "navPillAlpha"
                        )

                        Box(
                            modifier = Modifier
                                .weight(weight)
                                .height(pillHeight)
                                .clip(RoundedCornerShape(percent = 50))
                                .selectable(
                                    selected = isSelected,
                                    role = Role.Tab,
                                    onClick = {
                                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                        onTabSelected(item.tab)
                                    }
                                )
                                .testTag("nav_item_${item.tab.name.lowercase()}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .alpha(pillAlpha)
                                    .background(NavActiveColor.copy(alpha = 0.16f))
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = PILL_H_PADDING)
                            ) {
                                Icon(
                                    imageVector = if (isSelected) item.iconFilled else item.iconOutlined,
                                    contentDescription = label,
                                    modifier = Modifier.size(ICON_SIZE),
                                    tint = contentColor
                                )

                                // Only the selected tab carries its label, and
                                // only when one fits.
                                if (isSelected && labelSize != null) {
                                    Spacer(modifier = Modifier.width(ICON_LABEL_GAP))
                                    Text(
                                        text = label,
                                        style = baseStyle.copy(
                                            fontSize = labelSize,
                                            fontWeight = FontWeight.SemiBold,
                                            letterSpacing = 0.sp
                                        ),
                                        color = contentColor,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
