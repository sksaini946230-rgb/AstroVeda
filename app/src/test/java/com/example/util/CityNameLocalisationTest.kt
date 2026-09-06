package com.example.util

import com.example.data.model.CityLocation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * A city's name has to follow the app's language like everything else.
 *
 * CityLocation carries both names, and two screens reached for the Hindi one by
 * field name regardless of the language in force — so an English user was told
 * "Current City: जयपुर" during onboarding, and Settings showed
 * "जयपुर (Rajasthan)". A third screen, a few lines away, did it correctly,
 * which is what a field that is easy to grab by accident looks like.
 *
 * `nameLocal` is the thing to use. These check it works, and then check that no
 * UI file reads the raw Hindi field for display again.
 */
class CityNameLocalisationTest {

    private val jaipur = CityLocation("Jaipur", "जयपुर", "Rajasthan", 26.9124, 75.7873)

    @Test
    fun `the name follows the app language`() {
        LanguageManager.setLanguage(AppLanguage.HINDI)
        assertEquals("जयपुर", jaipur.nameLocal)

        LanguageManager.setLanguage(AppLanguage.ENGLISH)
        assertEquals("Jaipur", jaipur.nameLocal)
    }

    @Test
    fun `a GPS city with no Hindi name reads the same in both languages`() {
        // The geocoder gives one string; MainViewModel puts it in both fields.
        val gps = CityLocation("Bhowali Range", "Bhowali Range", "Uttarakhand", 29.38, 79.50)

        LanguageManager.setLanguage(AppLanguage.HINDI)
        assertEquals("Bhowali Range", gps.nameLocal)
        LanguageManager.setLanguage(AppLanguage.ENGLISH)
        assertEquals("Bhowali Range", gps.nameLocal)
    }

    /**
     * Reading `cityNameHindi` is legitimate for searching, for a
     * LanguageManager pair, and for the city picker, which shows both names
     * side by side on purpose so a user can find their city in either script.
     * What is not legitimate is handing the Hindi name alone to a Text as the
     * thing to display. This catches that shape and allows the other three —
     * narrowed after it first flagged the picker, which is correct as it is.
     */
    @Test
    fun `no screen interpolates the raw Hindi city name into displayed text`() {
        val ui = File("src/main/java/com/example/ui")
        assertTrue("ui sources not found at ${ui.absolutePath}", ui.isDirectory)

        val offenders = ui.walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .flatMap { f ->
                f.readLines().withIndex().filter { (_, line) ->
                    line.contains("cityNameHindi") &&
                        line.contains("\${") &&
                        !line.contains("LanguageManager") &&
                        // A line that shows both names — "जयपुर (Jaipur)" in the
                        // city picker — is bilingual by construction, and is there
                        // so a user can find their city in either script.
                        !line.contains("cityName}")
                }.map { (i, line) -> "${f.name}:${i + 1}  ${line.trim()}" }
            }
            .toList()

        assertEquals(
            "these show the Hindi city name regardless of language — use " +
                "CityLocation.nameLocal:\n" + offenders.joinToString("\n"),
            emptyList<String>(), offenders
        )
    }
}
