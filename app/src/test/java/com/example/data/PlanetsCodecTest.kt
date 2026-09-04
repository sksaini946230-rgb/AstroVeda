package com.example.data

import com.example.data.model.PlanetPosition
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The panchang cache round-trip.
 *
 * This is what lets a cache hit stop re-running the whole ephemeris. If it
 * decodes wrongly, the ग्रह स्थिति card shows wrong planets rather than a blank
 * one, which is worse — so it is worth pinning.
 */
class PlanetsCodecTest {

    private val sample = listOf(
        PlanetPosition(
            planetNameEn = "Sun", planetNameHi = "सूर्य", rashiNumber = 5,
            rashiNameHi = "सिंह", rashiNameEn = "Leo", degree = 12.3456,
            houseNumber = 10, isRetrograde = false,
            nakshatraHi = "मघा", nakshatraEn = "Magha"
        ),
        PlanetPosition(
            planetNameEn = "Saturn", planetNameHi = "शनि", rashiNumber = 11,
            rashiNameHi = "कुम्भ", rashiNameEn = "Aquarius", degree = 29.9999,
            houseNumber = 4, isRetrograde = true,
            nakshatraHi = "शतभिषा", nakshatraEn = "Shatabhisha"
        )
    )

    @Test
    fun `round trips every field including Devanagari and the retrograde flag`() {
        val decoded = com.example.data.local.testDecode(
            com.example.data.local.testEncode(sample)
        )
        assertEquals(sample, decoded)
    }

    @Test
    fun `an empty list round trips to an empty list`() {
        assertTrue(
            com.example.data.local.testDecode(
                com.example.data.local.testEncode(emptyList())
            ).isEmpty()
        )
    }

    @Test
    fun `a blank or corrupt value decodes to empty rather than throwing`() {
        assertTrue(com.example.data.local.testDecode("").isEmpty())
        assertTrue(com.example.data.local.testDecode("   ").isEmpty())
        // A row written by some future format, or a truncated one: the caller
        // treats an empty list as a cache miss and recomputes, which is correct.
        assertTrue(com.example.data.local.testDecode("garbage-without-separators").isEmpty())
    }

    @Test
    fun `degree precision survives the round trip`() {
        val decoded = com.example.data.local.testDecode(
            com.example.data.local.testEncode(sample)
        )
        assertEquals(12.3456, decoded[0].degree, 1e-9)
        assertEquals(29.9999, decoded[1].degree, 1e-9)
    }
}
