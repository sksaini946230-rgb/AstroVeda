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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.sqrt
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

/**
 * How wide the bar is allowed to get, beyond which it centres instead.
 *
 * No phone in portrait reaches this — the widest common one is 412dp, which
 * leaves a 388dp bar — so this changes nothing on the screens the app is
 * actually used on. What it fixes is everything wider. The same phone turned
 * sideways is 800dp, and five tabs sharing that give a pill 155dp wide against
 * 47dp tall: a ratio of 3.3, where the design being followed is 1.6 and a
 * portrait phone is 1.4. It read as a stretched band with the labels marooned
 * far apart. At 420dp the pill comes out at 1.7, which is the design's own
 * proportion, and the bar sits centred with the content behind it visible on
 * either side.
 */
private val BAR_MAX_WIDTH = 420.dp

/** Gap under the capsule, above the system navigation area. */
private val BAR_BOTTOM_MARGIN = 12.dp

/**
 * Ring of space between the bar's edge and the pills inside it.
 *
 * It is also what decides whether the outermost pill can sit against the bar's
 * rounded end without being clipped by it. Two capsules, the pill inside the
 * bar: the pill's end cap has radius `pillHeight / 2`, the bar's has
 * `barHeight / 2`, and the difference between those two radii is exactly this
 * padding. A circle of radius r sits inside a circle of radius R when their
 * centres are no more than R - r apart — so the pill's end may start anywhere
 * from flush with the bar's box to twice this padding in, and at exactly this
 * padding the gap between the two curves is even the whole way round. That is
 * the value used, which is why the pill hugs the bar's outer edge instead of
 * floating in the middle of it.
 */
private val BAR_INNER_PADDING = 4.dp

private val ICON_SIZE = 18.dp

/** Between the icon and the label under it. They read as one object. */
private val ICON_LABEL_GAP = 2.dp

/**
 * Above the icon and below the label, inside the selected pill.
 *
 * Counter-intuitively this cannot be squeezed to nothing, because it is what
 * makes the pill's end cap *flatter*. The width a capsule loses to its own
 * curve at the label is `R - sqrt(R^2 - (C/2)^2)`, where C is the icon-plus-gap-
 * plus-label stack and R is C/2 plus this padding — so a bigger R is a shallower
 * curve and more usable width. At 320dp in English the difference between 5dp
 * and 6dp here is the difference between showing the labels and dropping them.
 *
 * Deliberately tight. The pill has to come out wider than it is tall or the
 * 50% corner radius turns it into a circle instead of a capsule — which is
 * exactly what a first pass at this looked like: 55dp wide by 52dp tall on a
 * 360dp screen, a green disc with the label spilling past its sides. Every
 * dp taken off here goes straight into that ratio.
 */
private val PILL_V_PADDING = 6.dp

/** Taller, for the icon-only fallback, so the bar does not become a sliver. */
private val PILL_V_PADDING_NO_LABEL = 10.dp

/**
 * Keeps one pill from touching the next during the crossfade.
 *
 * Deliberately tiny: the pills are meant to fill their share of the bar, as
 * they do in the design this follows, and every dp here is a dp off the label.
 */
private val PILL_H_INSET = 1.dp

/**
 * Extra breathing room between the label and the pill's curve — deliberately
 * none.
 *
 * [cornerInsetAt] is asked for the width available at the *bottom of the
 * label's line box*, which sits below the lowest ink in the label: descenders,
 * and on the Devanagari side the font padding that `Type.kt` adds to protect
 * the matras. So the figure it returns already understates the room by a
 * couple of dp, and adding a margin on top of that was costing a whole step of
 * the size ladder at 320dp — the difference between an 8.5 label and no label.
 */
private val LABEL_H_MARGIN = 0.dp

/**
 * The selected pill is a true capsule — radius exactly half its height.
 *
 * Which means the label has to fit inside the curve, not merely inside the
 * pill's width. Near the bottom of a capsule there is a great deal less width
 * than the shape has: [cornerInsetAt] computes how much, and the label size is
 * chosen against what is left. An earlier attempt dodged this by dropping the
 * radius to 32% of the height, and that is not a capsule — it is a rounded box.
 *
 * The way to keep the capsule *and* the label is to make the pill's contents
 * shorter, which is why the icon is 18dp and the gap under it is 2dp. The
 * arithmetic, for a 60dp-wide pill on a 360dp screen: with a 37dp-tall stack
 * the corner eats 9dp a side and 42dp of label width survives; with a 31dp
 * stack it eats 7dp and 46dp survives. Every dp off the stack is worth about
 * 1.3dp of label.
 */
private const val PILL_CORNER_FRACTION = 0.5f

/**
 * The ladder of label sizes, largest first, in the order they are tried.
 *
 * Used twice over: first as `sp`, which honours the user's font scale, and then
 * — only for a user who has enlarged text — as `dp`, which ignores it. The
 * second pass is what keeps the labels on screen on a narrow phone at a 1.3x or
 * 1.6x font scale, where the enlarged text simply cannot fit five ways across
 * the bar. Shrinking the label back towards the size everyone else sees is a
 * far smaller loss than dropping it, and the floor is the same 8.5 either way,
 * so nobody ends up with a label smaller than the narrow-screen default.
 */
private val LABEL_STEPS = listOf(12f, 11f, 10.5f, 10f, 9.5f, 9f, 8.5f, 8f)

/**
 * How much wider than tall the selected pill has to be to still read as a
 * capsule rather than a disc.
 *
 * This is a shape requirement expressed as a constraint on the label, because
 * the label is what makes the pill tall. Without it the two languages came out
 * as different shapes on the same phone: Devanagari's line box is taller than
 * the Latin one at the same size, and its labels are narrow enough that a large
 * size fits — so Hindi picked 12 and produced a 53x49 pill, a circle, while
 * English picked a smaller size and produced 53x43. Same bar, same screen, two
 * shapes.
 *
 * 1.6 is what the design being followed has, and five tabs of icon-over-label
 * cannot reach it on a phone: at 320dp the pill is 57dp wide, so 1.6 would mean
 * a 36dp pill around a 32dp stack. 1.3 is reachable everywhere and is past the
 * point where the eye stops reading the shape as round.
 */
private const val MIN_PILL_RATIO = 1.3f

/** The size that fits, and how tall its line actually is at that size. */
private data class LabelFit(val size: TextUnit, val heightDp: Dp)

/**
 * A floating capsule navigation bar.
 *
 * It clears all three edges, is fully rounded, and every tab carries its label
 * under its icon. The selected tab sits inside a tinted capsule of its own that
 * holds the icon and the label together.
 *
 * **Nothing is allowed to clip, on any screen.** Five labels on a narrow phone
 * is the hard case, so the label size is measured rather than chosen: every
 * label is rendered at each candidate size, and the largest size whose widest
 * label fits one fifth of the bar wins. Three things that had to be right, all
 * three of which have been wrong here before:
 *
 *  - **The widest label is not the longest one.** "कुण्डली" has more characters
 *    than "राशिफल" and is 5px narrower, so picking by `length` measures the
 *    wrong string. Every label is measured and the maximum taken.
 *  - **The English labels are the long ones**, not the Hindi. "Horoscope" is
 *    nearly twice the width of "राशिफल", so a bar checked only in Hindi proves
 *    nothing.
 *  - **The measurement has to use the style that is actually drawn.** The
 *    previous version measured at the default weight and drew SemiBold, which is
 *    wider — so it under-measured every label by a few percent, in the direction
 *    that clips.
 *
 * The bar's height comes from the measured label, not from a constant, so it
 * grows with the user's font scale instead of cropping the descenders — and
 * Devanagari matras, which are the first thing to go, are inside that measured
 * height because `Type.kt` sets `includeFontPadding = true`.
 *
 * If even the smallest step does not fit — which now takes a screen narrower
 * than any this app has been asked to run on — the labels are dropped and the
 * icons stand alone. An icon with no label is usable; half a word is not.
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

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(start = BAR_SIDE_MARGIN, end = BAR_SIDE_MARGIN, bottom = BAR_BOTTOM_MARGIN)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .widthIn(max = BAR_MAX_WIDTH)
                .fillMaxWidth()
                .align(Alignment.Center)
        ) {
            val measurer = rememberTextMeasurer()
            val density = LocalDensity.current
            val baseStyle = LocalTextStyle.current
            val labelsHi = remember(items) { items.map { it.titleHi } }
            val labelsEn = remember(items) { items.map { it.titleEn } }
            val labels = items.map { LanguageManager.getString(it.titleHi, it.titleEn) }

            // Every tab gets the same share of the bar; the pill is that share
            // less the gap that keeps one pill clear of the next.
            val pillWidth = (maxWidth - BAR_INNER_PADDING * 2) / items.size - PILL_H_INSET * 2

            // sp first, so an enlarged font setting is honoured wherever there
            // is room for it; dp after that, and only for a user who enlarged
            // the text, so the fallback can never make a label *bigger* than
            // the size that user asked for.
            val candidates: List<TextUnit> = remember(density) {
                val scaled = LABEL_STEPS.map { it.sp }
                if (density.fontScale <= 1f) scaled
                else scaled + with(density) { LABEL_STEPS.map { it.dp.toSp() } }
            }

            // Solved once, over both languages at the same time — the widest
            // label of either and the tallest line box of either.
            //
            // Doing it per language made the bar change height when the
            // language was switched: 53.5dp in Hindi against 51dp in English,
            // measured on the device, growing upward from a fixed bottom edge.
            // Two causes, and the obvious one is the smaller: Devanagari's line
            // box is about 1dp taller than the Latin one at the same size, and,
            // the larger part, the two languages were choosing different sizes —
            // "राशिफल" is 33px wide at 11sp and fits, "Horoscope" is 53px at
            // 11sp and does not, so English dropped to 10.5.
            //
            // Solving the two separately and then taking the taller pill fixed
            // the height and broke the shape: at 320dp Hindi has no size that
            // satisfies [MIN_PILL_RATIO], so it fell through to the pass that
            // gives up the ratio, took the largest size that merely fits, and
            // handed English a 53x49 pill — a disc, which is the one thing the
            // ratio exists to prevent. One size for both languages is what
            // actually holds: the bar is then identical in either language by
            // construction, and it is sized by "Horoscope", which is the widest
            // string in the app in either script anyway.
            val allLabels = remember(labelsHi, labelsEn) { labelsHi + labelsEn }

            val labelFit: LabelFit? = remember(allLabels, pillWidth, candidates, density, baseStyle) {
                fun fit(candidate: TextUnit, keepShape: Boolean): LabelFit? {
                    // Measured in the style that is drawn, weight included.
                    val style = labelStyle(baseStyle, candidate, FontWeight.SemiBold)
                    var widest = 0
                    var tallest = 0
                    allLabels.forEach { label ->
                        val laid = measurer.measure(AnnotatedString(label), style, maxLines = 1)
                        if (laid.size.width > widest) widest = laid.size.width
                        if (laid.size.height > tallest) tallest = laid.size.height
                    }
                    val labelHeight = with(density) { tallest.toDp() }
                    val pillHeight = pillHeightFor(labelHeight)
                    if (keepShape && pillWidth.value / pillHeight.value < MIN_PILL_RATIO) return null
                    // How much width is still straight where the label crosses
                    // the pill's corners.
                    val inset = cornerInsetAt(
                        radius = pillHeight * PILL_CORNER_FRACTION,
                        distanceFromEdge = PILL_V_PADDING
                    )
                    val room = with(density) { (pillWidth - inset * 2 - LABEL_H_MARGIN * 2).toPx() }
                    return if (widest <= room) LabelFit(candidate, labelHeight) else null
                }
                // Shape first. If no size can satisfy both, the label wins: a
                // slightly round pill is a smaller loss than five bare icons.
                candidates.firstNotNullOfOrNull { fit(it, keepShape = true) }
                    ?: candidates.firstNotNullOfOrNull { fit(it, keepShape = false) }
            }

            // The pill is exactly as tall as its contents plus its padding, and
            // the bar is the pill plus its ring — so both are true capsules at
            // every font scale rather than rounded rectangles that happen to
            // look close.
            val pillHeight = if (labelFit == null) {
                ICON_SIZE + PILL_V_PADDING_NO_LABEL * 2
            } else {
                pillHeightFor(labelFit.heightDp)
            }
            // Always a capsule. Everything else here bends so that it can stay one.
            val pillShape = RoundedCornerShape(percent = 50)
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
                    horizontalArrangement = Arrangement.spacedBy(0.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items.forEachIndexed { index, item ->
                        val isSelected = selectedTab == item.tab
                        val label = labels[index]

                        val contentColor by animateColorAsState(
                            targetValue = if (isSelected) NavActiveColor else NavInactiveColor,
                            animationSpec = spring(stiffness = Spring.StiffnessLow),
                            label = "navContentColor"
                        )

                        // The pill fades rather than jumping, which is what makes
                        // the selection read as moving along the bar.
                        val pillAlpha by animateFloatAsState(
                            targetValue = if (isSelected) 1f else 0f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            ),
                            label = "navPillAlpha"
                        )

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(pillHeight)
                                .padding(horizontal = PILL_H_INSET)
                                .clip(pillShape)
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
                                    .background(NavActiveColor.copy(alpha = 0.18f))
                            )

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = if (isSelected) item.iconFilled else item.iconOutlined,
                                    // The visible label already says this. With
                                    // no label to read, the icon has to.
                                    contentDescription = if (labelFit == null) label else null,
                                    modifier = Modifier.size(ICON_SIZE),
                                    tint = contentColor
                                )

                                if (labelFit != null) {
                                    Spacer(modifier = Modifier.height(ICON_LABEL_GAP))
                                    Text(
                                        text = label,
                                        style = labelStyle(
                                            baseStyle,
                                            labelFit.size,
                                            if (isSelected) FontWeight.SemiBold else FontWeight.Medium
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

/**
 * The one place a nav label's style is built, so what is measured and what is
 * drawn cannot drift apart. Line height is left to the theme, which sets
 * `includeFontPadding = true` for Devanagari; overriding it here would crop the
 * matras that setting exists to protect.
 */
private fun labelStyle(base: TextStyle, size: TextUnit, weight: FontWeight): TextStyle =
    base.copy(fontSize = size, fontWeight = weight, letterSpacing = 0.sp)

/** The pill's height for a label of this measured height. */
private fun pillHeightFor(labelHeight: Dp): Dp =
    ICON_SIZE + ICON_LABEL_GAP + labelHeight + PILL_V_PADDING * 2

/**
 * How far a rounded corner of [radius] eats into the pill's width at a point
 * [distanceFromEdge] above its bottom edge.
 *
 * This is the reason the label is sized against the pill and not against the
 * slot: near the bottom of a heavily rounded shape there is a good deal less
 * width available than the shape's own width suggests.
 */
private fun cornerInsetAt(radius: Dp, distanceFromEdge: Dp): Dp {
    val r = radius.value
    val d = distanceFromEdge.value
    if (d >= r) return 0.dp
    return (r - sqrt(r * r - (r - d) * (r - d))).dp
}
