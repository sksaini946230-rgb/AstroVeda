package com.example

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * The two bundled legal pages hard-coded a light palette, so a dark-mode user
 * opened a white page out of a dark app. The obvious fix was worse than the
 * bug: WebView takes `prefers-color-scheme` from the Activity theme's
 * isLightTheme rather than from the system (targetSdk 33 and up), and
 * `Theme.MyApplication`'s parent was `android:Theme.DeviceDefault` — dark on
 * every device — so a media query alone would have turned both pages dark in
 * light mode too.
 *
 * So the media query and the day/night theme pair only work together, and each
 * half is invisible from the other's file. This holds them together.
 */
class LegalPagesDarkModeTest {

    private val root: File = generateSequence(File(System.getProperty("user.dir")!!).absoluteFile) { it.parentFile }
        .first { File(it, "settings.gradle.kts").exists() }

    private val pages = listOf("privacy_policy.html", "terms_of_service.html")
        .map { File(root, "app/src/main/assets/$it") }

    private fun theme(qualifier: String) = File(root, "app/src/main/res/$qualifier/themes.xml")

    @Test
    fun `both legal pages carry a dark palette`() {
        pages.forEach { page ->
            assertTrue("${page.name} is missing", page.exists())
            assertTrue(
                "${page.name} has no dark palette, so it is white inside a dark app",
                page.readText().contains("@media (prefers-color-scheme: dark)")
            )
        }
    }

    /**
     * Every `values-*` variant of the theme needs a `values-night-*` twin. A
     * missing twin does not fail the build — the day theme is simply used at
     * night, and both pages quietly stop following the system again.
     */
    @Test
    fun `the activity theme is a day night pair`() {
        listOf("values" to "values-night", "values-v31" to "values-night-v31").forEach { (day, night) ->
            val dayTheme = theme(day)
            val nightTheme = theme(night)
            assertTrue("$day/themes.xml is missing", dayTheme.exists())
            assertTrue(
                "$night/themes.xml is missing — WebView would report dark in light mode",
                nightTheme.exists()
            )
            assertTrue(
                "$day/themes.xml must inherit a light parent, or the pages go dark in light mode",
                dayTheme.readText().contains("parent=\"android:Theme.DeviceDefault.Light.NoActionBar\"")
            )
            assertFalse(
                "$night/themes.xml must not inherit the light parent",
                nightTheme.readText().contains("DeviceDefault.Light")
            )
            assertTrue(
                "$night/themes.xml must still inherit a DeviceDefault no-action-bar theme",
                nightTheme.readText().contains("parent=\"android:Theme.DeviceDefault.NoActionBar\"")
            )
        }
    }

    /**
     * The window is painted before Compose starts, so this colour is the cold
     * start. Light-only here was a white flash in front of the dark splash.
     */
    @Test
    fun `the window background has a night value too`() {
        listOf("values", "values-night").forEach { qualifier ->
            val colors = File(root, "app/src/main/res/$qualifier/colors.xml")
            assertTrue("$qualifier/colors.xml is missing", colors.exists())
            assertTrue(
                "$qualifier/colors.xml must define app_background",
                colors.readText().contains("name=\"app_background\"")
            )
        }
    }
}
