package com.example

import com.example.ui.screens.ONBOARDING_STEPS
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * `AstroAnalytics` had eighteen logging methods and two call sites; the whole
 * point of wiring `logOnboardingStep` was that a method nobody calls records
 * nothing. So this pins the wiring, not just the data — the same reason
 * `LanguageInitTest` reads the manifest instead of trusting the code.
 */
class OnboardingStepsTest {

    private val root: File = generateSequence(File(System.getProperty("user.dir")!!).absoluteFile) { it.parentFile }
        .first { File(it, "settings.gradle.kts").exists() }

    private val onboardingSource =
        File(root, "app/src/main/java/com/example/ui/screens/OnboardingScreen.kt").readText()

    @Test
    fun `the three onboarding steps are the ones the funnel is read on`() {
        assertEquals(listOf("language", "rashi", "location_notifications"), ONBOARDING_STEPS)
    }

    /**
     * These are analytics parameter values. Localising one splits a single
     * funnel into a Hindi half and an English half that never add up.
     */
    @Test
    fun `no step name carries a script or a capital`() {
        ONBOARDING_STEPS.forEach { step ->
            assertTrue(
                "step name is not a stable lowercase ASCII token: $step",
                step.matches(Regex("[a-z0-9_]+"))
            )
        }
    }

    /** A page with no name in the list would log the wrong step or crash. */
    @Test
    fun `the page count is the step count`() {
        assertTrue(
            "totalPages must come from ONBOARDING_STEPS, not a literal",
            onboardingSource.contains("val totalPages = ONBOARDING_STEPS.size")
        )
        val rendered = Regex("""^\s+(\d+) -> """, RegexOption.MULTILINE)
            .findAll(onboardingSource)
            .map { it.groupValues[1].toInt() }
            .toSet()
        assertEquals("every step index needs a page", ONBOARDING_STEPS.indices.toSet(), rendered)
    }

    /** The reason the method existed unwired for so long: nothing failed. */
    @Test
    fun `the onboarding screen logs every page it shows`() {
        assertTrue(
            "OnboardingScreen must log the step it is on",
            onboardingSource.contains("AstroAnalytics.logOnboardingStep(pageIndex, ONBOARDING_STEPS[pageIndex])")
        )
    }
}
