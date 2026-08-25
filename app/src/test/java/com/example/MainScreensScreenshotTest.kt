package com.example

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.ui.MainViewModel
import com.example.ui.screens.KundaliScreen
import com.example.ui.screens.PanchangScreen
import com.example.ui.screens.RashifalScreen
import com.example.ui.theme.AstroVedaTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@OptIn(ExperimentalSharedTransitionApi::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [34])
class MainScreensScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var viewModel: MainViewModel

    @Before
    fun setup() {
        println("Working directory: " + System.getProperty("user.dir"))
        viewModel = MainViewModel(RuntimeEnvironment.getApplication())
    }

    @Test
    fun panchang_screen_dark_mode() {
        composeTestRule.setContent {
            AstroVedaTheme(darkTheme = true) {
                PanchangScreen(viewModel = viewModel)
            }
        }
        composeTestRule.waitForIdle()
        println("Capturing panchang_dark.png in screenshots dir...")
        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/panchang_dark.png")
    }

    @Test
    fun rashifal_screen_dark_mode() {
        composeTestRule.setContent {
            AstroVedaTheme(darkTheme = true) {
                RashifalScreen(viewModel = viewModel)
            }
        }
        composeTestRule.waitForIdle()
        println("Capturing rashifal_dark.png in screenshots dir...")
        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/rashifal_dark.png")
    }

    @Test
    fun kundali_screen_dark_mode() {
        composeTestRule.setContent {
            AstroVedaTheme(darkTheme = true) {
                KundaliScreen(viewModel = viewModel)
            }
        }
        composeTestRule.waitForIdle()
        println("Capturing kundali_dark.png in screenshots dir...")
        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/kundali_dark.png")
    }
}
