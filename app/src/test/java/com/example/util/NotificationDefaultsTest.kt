package com.example.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * The daily alert hour has drifted twice, both times because the same number
 * was written down in more than one place.
 *
 * First the worker and the ViewModel read the same preference key with
 * different fallbacks — Settings said 7:00 AM and the notification came at
 * 6:30. That was fixed. Then onboarding was found still promising
 * "प्रातः 06:00 बजे" in a hardcoded sentence, so a new user was told one time
 * and served another.
 *
 * These check the formatting, and then check the thing that actually keeps the
 * two honest: that no source file writes the fallback as a bare number any
 * more.
 */
class NotificationDefaultsTest {

    @Test
    fun `the hour reads back as a clock time`() {
        assertEquals("07:00 AM", NotificationDefaults.formatHour(7))
        assertEquals("06:00 AM", NotificationDefaults.formatHour(6))
        assertEquals("12:00 PM", NotificationDefaults.formatHour(12))
        assertEquals("12:00 AM", NotificationDefaults.formatHour(0))
        assertEquals("09:00 PM", NotificationDefaults.formatHour(21))
    }

    /**
     * Every reader of these preference keys must fall back to the constant, not
     * to a literal. A literal is how the two drifted apart the first time.
     */
    @Test
    fun `nothing falls back to a bare number for the alert hour`() {
        val src = File("src/main/java/com/example")
        assertTrue("source tree not found at ${src.absolutePath}", src.isDirectory)

        val offenders = src.walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .flatMap { f ->
                f.readLines().withIndex().filter { (_, line) ->
                    Regex("""getInt\(\s*"(notification_hour|muhurat_notification_hour)"\s*,\s*\d+\s*\)""")
                        .containsMatchIn(line)
                }.map { (i, line) -> "${f.name}:${i + 1}  ${line.trim()}" }
            }
            .toList()

        assertEquals(
            "these read the alert hour with a literal fallback instead of " +
                "NotificationDefaults:\n" + offenders.joinToString("\n"),
            emptyList<String>(), offenders
        )
    }
}
