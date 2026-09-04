package com.example

import com.example.util.SecurityUtils
import org.junit.Assert.*
import org.junit.Test

class SecurityUtilsTest {

    // These passed for months while SecurityUtils had zero call sites anywhere in
    // the app — the tests were green and the sanitiser never ran. The wiring test
    // at the bottom of this file is the one that actually matters.

    @Test
    fun testSanitizeTextInput_stripsScriptTags() {
        val malicious = "<script>alert('xss')</script>Rahul Saini"
        val cleaned = SecurityUtils.sanitizeTextInput(malicious)
        assertEquals("Rahul Saini", cleaned)
    }

    @Test
    fun testSanitizeTextInput_stripsHtmlTags() {
        val htmlInput = "<b>Vedic</b> <i>Astrology</i>"
        val cleaned = SecurityUtils.sanitizeTextInput(htmlInput)
        assertEquals("Vedic Astrology", cleaned)
    }

    @Test
    fun testSanitizeTextInput_capsLongInput() {
        val longString = "A".repeat(300)
        val cleaned = SecurityUtils.sanitizeTextInput(longString)
        assertEquals(255, cleaned.length)
    }

    @Test
    fun testIsValidDate_validatesCorrectly() {
        assertTrue(SecurityUtils.isValidDate("1996-08-15"))
        assertTrue(SecurityUtils.isValidDate("2000-02-29")) // leap year
        assertFalse(SecurityUtils.isValidDate("1999-02-29")) // not leap year
        assertFalse(SecurityUtils.isValidDate("1996-13-15")) // invalid month
        assertFalse(SecurityUtils.isValidDate("1996-08-32")) // invalid day
        assertFalse(SecurityUtils.isValidDate("invalid-date"))
        assertFalse(SecurityUtils.isValidDate(""))
    }

    @Test
    fun testIsValidTime_validatesCorrectly() {
        assertTrue(SecurityUtils.isValidTime("10:30"))
        assertTrue(SecurityUtils.isValidTime("00:00"))
        assertTrue(SecurityUtils.isValidTime("23:59"))
        assertFalse(SecurityUtils.isValidTime("24:00"))
        assertFalse(SecurityUtils.isValidTime("10:60"))
        assertFalse(SecurityUtils.isValidTime("invalid"))
        assertFalse(SecurityUtils.isValidTime(""))
    }

    @Test
    fun testMaskEmail_masksCorrectly() {
        val masked = SecurityUtils.maskEmail("sksaini@gmail.com")
        assertEquals("s***i@gmail.com", masked)
    }

    @Test
    fun testMaskPhone_masksCorrectly() {
        val masked = SecurityUtils.maskPhone("+919876543210")
        assertEquals("******3210", masked)
    }

    // --- Wiring: sanitisation has to be reachable from real input, not just here ---

    @Test
    fun `BirthData_parse strips markup from the name it stores`() {
        val data = com.example.astro.BirthData.parse(
            name = "<script>alert(1)</script>राहुल",
            dobString = "1994-08-25",
            tobString = "14:15",
            placeName = "<b>Jaipur</b>",
            latitude = 26.9124,
            longitude = 75.7873
        )
        assertEquals("राहुल", data.name)
        assertEquals("Jaipur", data.placeName)
    }

    @Test
    fun `BirthData_parse caps a name that would otherwise reach Firestore unbounded`() {
        val data = com.example.astro.BirthData.parse(
            name = "अ".repeat(5000),
            dobString = "1994-08-25",
            tobString = "14:15",
            placeName = "Jaipur",
            latitude = 26.9124,
            longitude = 75.7873
        )
        assertEquals(SecurityUtils.MAX_TEXT_LENGTH, data.name.length)
    }

    @Test
    fun `a name that is only markup is still rejected as blank`() {
        var threw = false
        try {
            com.example.astro.BirthData.parse(
                name = "<b></b>",
                dobString = "1994-08-25",
                tobString = "14:15",
                placeName = "Jaipur",
                latitude = 26.9124,
                longitude = 75.7873
            )
        } catch (e: com.example.astro.BirthDataException) {
            threw = true
        }
        assertTrue("markup-only name must not pass validation", threw)
    }
}
