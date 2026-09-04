package com.example

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.Density
import com.example.ui.MainViewModel
import com.example.ui.screens.KundaliScreen
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
        content: @Composable (MainViewModel) -> Unit
    ) {
        RuntimeEnvironment.setQualifiers("+w${widthDp}dp-h640dp")
        val vm = MainViewModel(RuntimeEnvironment.getApplication())
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

    @Test fun settings_320() = shoot("settings_320", 320) { SettingsScreen(viewModel = it) }
    @Test fun settings_360() = shoot("settings_360", 360) { SettingsScreen(viewModel = it) }
}
