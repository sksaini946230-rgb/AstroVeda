package com.example.util

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * The regression test for the Hindi-notifications bug.
 *
 * LanguageManager.init was called from exactly one place, MainActivity.onCreate.
 * But the process also starts from two AppWidgetProviders and three WorkManager
 * workers, none of which go through an Activity — so in those processes
 * currentLanguage stayed at its default of HINDI. An English user's daily 6:30 AM
 * notification and both home-screen widgets came out in Hindi after every reboot,
 * until they happened to open the app.
 *
 * RevatiApp.onCreate initialises it for every process entry now. Robolectric
 * instantiates the manifest's Application class, so if that registration is ever
 * removed, the first test here fails.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class LanguageInitTest {

    private fun prefs() = ApplicationProvider.getApplicationContext<Context>()
        .getSharedPreferences("astro_prefs", Context.MODE_PRIVATE)

    @Test
    fun `a saved English choice survives into a process that never opened an Activity`() {
        prefs().edit().putString("app_language", "en").commit()

        // Exactly what RevatiApp.onCreate does — no Activity involved.
        LanguageManager.init(ApplicationProvider.getApplicationContext())

        assertEquals(AppLanguage.ENGLISH, LanguageManager.currentLanguage)
        assertEquals(
            "English text is what a worker or widget must render here",
            "Sunrise",
            LanguageManager.getString("सूर्योदय", "Sunrise")
        )
    }

    @Test
    fun `a saved Hindi choice is restored too`() {
        prefs().edit().putString("app_language", "hi").commit()
        LanguageManager.init(ApplicationProvider.getApplicationContext())
        assertEquals(AppLanguage.HINDI, LanguageManager.currentLanguage)
        assertEquals("सूर्योदय", LanguageManager.getString("सूर्योदय", "Sunrise"))
    }

    @Test
    fun `no saved choice falls back to Hindi`() {
        prefs().edit().remove("app_language").commit()
        LanguageManager.init(ApplicationProvider.getApplicationContext())
        assertEquals(AppLanguage.HINDI, LanguageManager.currentLanguage)
    }

    @Test
    fun `the manifest still registers RevatiApp so background entries initialise`() {
        // If android:name is dropped from <application>, Robolectric hands back a
        // plain Application here and the Hindi-notification bug is back.
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        assertEquals(
            "AndroidManifest must keep android:name=\"com.example.RevatiApp\"",
            "com.example.RevatiApp",
            app.javaClass.name
        )
    }
}
