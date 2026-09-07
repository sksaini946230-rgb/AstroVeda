package com.example

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.Density
import com.example.ui.MainViewModel
import com.example.ui.screens.KundaliScreen
import com.example.ui.screens.MatchingScreen
import com.example.ui.screens.PanchangScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.AstroVedaTheme
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Renders the screens most at risk of running out of horizontal room, at the
 * widths this app actually meets, so margins can be checked without a drawer
 * full of phones.
 *
 * 320dp is the floor — a Redmi 9A, a Galaxy J2, still a real share of installs
 * in India. 360dp is where most Android phones sit. Hindi labels run about half
 * again as long as their English equivalents, so a row that looks generous on
 * the Pixel 8 this was built against is the first thing to break at 320dp.
 *
 * These write PNGs to src/test/screenshots/ and assert nothing. That is
 * deliberate, and it cost a day to arrive at: an automated check was built first
 * that walked the semantics tree looking for text whose glyphs ran past the
 * width it was given. It found all three real bugs — the shortcut tiles, the
 * kundali date/time labels, the PRO badge crushed to zero — but it also reported
 * the sub-tab headers and the PRO upgrade banner, and rendering those proved
 * they fit perfectly. TextLayoutResult, reached through the semantics tree,
 * hands back whichever layout pass ran last, and anything measured
 * speculatively — Rows with weights, IntrinsicSize.Min — leaves behind a result
 * describing a width nobody ever saw. There was no way to tell those apart from
 * the genuine ones, and a check that cries wolf gets switched off.
 *
 * So: look at the pictures. It is how every UI bug in this app has actually been
 * found. Run
 *
 *     ./gradlew testDebugUnitTest --tests "com.example.ScreenSizeScreenshotTest"
 *
 * and open src/test/screenshots/.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34])
class ScreenSizeScreenshotTest {

    @get:Rule
    val rule = createComposeRule()

    private fun shoot(
        name: String,
        widthDp: Int,
        dark: Boolean = false,
        fontScale: Float = 1f,
        prepare: (MainViewModel) -> Unit = {},
        content: @Composable (MainViewModel) -> Unit
    ) {
        RuntimeEnvironment.setQualifiers("+w${widthDp}dp-h640dp")
        val vm = MainViewModel(RuntimeEnvironment.getApplication())
        prepare(vm)
        rule.setContent {
            val base = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(base.density, fontScale)) {
                AstroVedaTheme(darkTheme = dark) { content(vm) }
            }
        }
        rule.waitForIdle()
        rule.onRoot().captureRoboImage(filePath = "src/test/screenshots/$name.png")
    }

    @Test fun panchang_320() = shoot("panchang_320", 320) { PanchangScreen(viewModel = it) }
    @Test fun panchang_360() = shoot("panchang_360", 360) { PanchangScreen(viewModel = it) }
    @Test fun panchang_320_dark() =
        shoot("panchang_320_dark", 320, dark = true) { PanchangScreen(viewModel = it) }
    @Test fun panchang_320_large_text() =
        shoot("panchang_320_large_text", 320, fontScale = 1.3f) { PanchangScreen(viewModel = it) }

    @Test fun kundali_320() = shoot("kundali_320", 320) { KundaliScreen(viewModel = it) }
    @Test fun kundali_360() = shoot("kundali_360", 360) { KundaliScreen(viewModel = it) }

    /**
     * Guna Milan **with the form filled in**, which is the only state in which
     * its date field can be judged.
     *
     * MatchingScreen seeds its local state from the ViewModel on first
     * composition, so setting the flows before composing renders the real
     * screen rather than a copy of it — unlike the kundali pair below, this
     * cannot drift out of step with production.
     *
     * What it caught: the boy and girl rows each gave the date `weight(1f)` of
     * 2.2 at the default 16sp, and "1995-06-15" wrapped to a second line on a
     * 360dp phone. The screen showed "1995-06" over "-15" for a date the user
     * had just picked. The same code appeared twice, so the fault did too.
     */
    private fun filledMatch(vm: MainViewModel) {
        vm.matchBoyName.value = "Rahul"
        vm.matchBoyDob.value = "1995-06-15"
        vm.matchGirlName.value = "Priya"
        vm.matchGirlDob.value = "1997-11-22"
    }

    @Test fun matching_filled_360() =
        shoot("matching_filled_360", 360, prepare = ::filledMatch) { MatchingScreen(viewModel = it) }

    // 412dp is what most current phones report; 360dp is the narrow end and the
    // device this app is tested on. Both are here because the point of the
    // measured threshold is that the row splits on the wide one and stacks on
    // the narrow one — a single width cannot show that it does either.
    // The bottom navigation bar on its own, both themes and both languages.
    //
    // It is drawn at the foot of every screen and was never rendered by itself,
    // so the only way to look at it was to find it under whatever else was on
    // the page. These four shots are the whole bar and nothing else — selected
    // pill, filled-vs-outlined icons, and the Hindi labels, which are the ones
    // that run out of room first.
    @Test fun navbar_360() = shoot("navbar_360", 360) { NavBarOnly() }

    @Test fun navbar_360_dark() = shoot("navbar_360_dark", 360, dark = true) { NavBarOnly() }

    @Test fun navbar_320() = shoot("navbar_320", 320) { NavBarOnly() }

    @Test fun navbar_320_large_text() =
        shoot("navbar_320_large_text", 320, fontScale = 1.3f) { NavBarOnly() }

    @Composable
    private fun NavBarOnly() {
        com.example.ui.components.BottomNavBar(
            selectedTab = com.example.ui.AppTab.KUNDALI,
            onTabSelected = {}
        )
    }

    @Test fun matching_filled_412() =
        shoot("matching_filled_412", 412, prepare = ::filledMatch) { MatchingScreen(viewModel = it) }

    @Test fun matching_filled_320() =
        shoot("matching_filled_320", 320, prepare = ::filledMatch) { MatchingScreen(viewModel = it) }

    @Test fun matching_filled_320_large_text() =
        shoot("matching_filled_320_large_text", 320, fontScale = 1.3f, prepare = ::filledMatch) {
            MatchingScreen(viewModel = it)
        }

    @Test fun settings_320() = shoot("settings_320", 320) { SettingsScreen(viewModel = it) }
    @Test fun settings_360() = shoot("settings_360", 360) { SettingsScreen(viewModel = it) }

    /**
     * The kundali date and time fields **with a value in them**.
     *
     * The kundali_320 and kundali_360 shots above render the form empty, which
     * is why they never showed this: the fields clip only once they hold
     * something. Filling the form on a real phone showed "1994-08-2" — the date
     * just entered, missing its last digit, with nothing to say whether the app
     * had read it correctly. Two fields share the row, so each has about 159dp
     * on a 360dp phone, and the default content padding and trailing icon take
     * roughly 80 of it.
     *
     * This mirrors the production configuration rather than driving the real
     * screen, because the field is readOnly and its value lives in local
     * remember state a test cannot reach. So it checks the arithmetic, not the
     * screen: change the font size, the icon size or the row in KundaliScreen
     * and this has to change with them, or it is showing a picture of something
     * the app no longer does.
     */
    @Test fun kundali_datetime_filled_360() = shoot("kundali_datetime_filled_360", 360) {
        FilledDateTimeRow()
    }

    @Test fun kundali_datetime_filled_320() = shoot("kundali_datetime_filled_320", 320) {
        FilledDateTimeRow()
    }

    @Test fun kundali_datetime_filled_320_large_text() =
        shoot("kundali_datetime_filled_320_large_text", 320, fontScale = 1.3f) {
            FilledDateTimeRow()
        }

    /** The pair as KundaliScreen builds it, with values in. Keep in step with it. */
    @Composable
    private fun FilledDateTimeRow() {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            BoxWithConstraints(Modifier.fillMaxWidth()) {
                val measurer = androidx.compose.ui.text.rememberTextMeasurer()
                val valueStyle = LocalTextStyle.current.copy(fontSize = 13.sp)
                val widestValue = measurer.measure(
                    androidx.compose.ui.text.AnnotatedString("0000-00-00"), valueStyle
                ).size.width
                val roomPerField = with(androidx.compose.ui.platform.LocalDensity.current) {
                    (((maxWidth - 10.dp) / 2) - 66.dp).toPx()
                }
                val stack = widestValue > roomPerField
                @Composable fun fields(mod: Modifier) {
                OutlinedTextField(
                    value = "1994-08-25",
                    onValueChange = {},
                    readOnly = true,
                    singleLine = true,
                    label = { Text("तिथि *", maxLines = 1, softWrap = false) },
                    textStyle = LocalTextStyle.current.copy(fontSize = 13.sp),
                    trailingIcon = {
                        Icon(
                            Icons.Default.CalendarMonth,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    shape = RoundedCornerShape(14.dp),
                    modifier = mod
                )
                OutlinedTextField(
                    value = "14:15",
                    onValueChange = {},
                    readOnly = true,
                    singleLine = true,
                    label = { Text("समय *", maxLines = 1, softWrap = false) },
                    textStyle = LocalTextStyle.current.copy(fontSize = 13.sp),
                    trailingIcon = {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    shape = RoundedCornerShape(14.dp),
                    modifier = mod
                )
                }
                if (stack) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) { fields(Modifier.fillMaxWidth()) }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) { fields(Modifier.weight(1f)) }
                }
            }
        }
    }
}
