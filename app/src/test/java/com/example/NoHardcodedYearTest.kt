package com.example

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * No user-visible string may carry a hardcoded year.
 *
 * A year written into a literal is a defect with a date on it. Five of them
 * were found in September 2026 and one of them was not cosmetic:
 * `NumerologyValidator` capped the birth year at 2026, so on 1 January 2027 a
 * baby born that morning could not have been entered and the message would have
 * told the parent the year was out of range. The others were a calendar
 * subtitle that said 2026 whatever month you scrolled to, two taglines, an
 * About-box title, a sample question offering to ask about a year that would
 * pass, and an offline news bulletin describing an eclipse that had already
 * happened.
 *
 * Comments may say 2026 — that is where the dates belong.
 */
class NoHardcodedYearTest {

    private val root: File = generateSequence(File(System.getProperty("user.dir")!!).absoluteFile) { it.parentFile }
        .first { File(it, "settings.gradle.kts").exists() }

    /** A year in this range in a string literal is almost certainly "now". */
    private val year = Regex("""\b20[2-3]\d\b""")

    private fun literals(line: String): List<String> =
        Regex(""""((?:[^"\\]|\\.)*)"""").findAll(line).map { it.groupValues[1] }.toList()

    @Test
    fun `no string literal in the app carries a recent year`() {
        val offenders = mutableListOf<String>()
        File(root, "app/src/main/java/com/example").walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .forEach { file ->
                var inBlockComment = false
                file.readLines().forEachIndexed { i, raw ->
                    var line = raw
                    if (inBlockComment) {
                        val end = line.indexOf("*/")
                        if (end < 0) return@forEachIndexed
                        line = line.substring(end + 2)
                        inBlockComment = false
                    }
                    val start = line.indexOf("/*")
                    if (start >= 0 && !line.substring(start).contains("*/")) {
                        line = line.substring(0, start)
                        inBlockComment = true
                    }
                    val slashes = line.indexOf("//")
                    if (slashes >= 0 && line.take(slashes).count { it == '"' } % 2 == 0) {
                        line = line.take(slashes)
                    }
                    literals(line).filter { year.containsMatchIn(it) }.forEach {
                        offenders += "${file.name}:${i + 1}  \"$it\""
                    }
                }
            }
        assertTrue(
            "a year written into a string stops being true:\n" + offenders.joinToString("\n"),
            offenders.isEmpty()
        )
    }
}
