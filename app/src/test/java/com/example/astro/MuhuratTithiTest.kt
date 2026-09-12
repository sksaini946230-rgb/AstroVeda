package com.example.astro

import com.example.data.model.CityLocation
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * No muhurat is offered on a tithi the tradition keeps for nothing.
 *
 * Two faults hid each other here. The exclusion list had Navami and Chaturdashi
 * but not Chaturthi — "चतुर्दशी" does not contain "चतुर्थी" — and two of the five
 * categories, Vehicle and Travel, never consulted the list at all. Fixing the
 * list alone changed nothing for those two, and it took a screenshot of the real
 * screen to show a शुभ यात्रा sitting on a Chaturthi.
 */
class MuhuratTithiTest {

    private val excluded = listOf("चतुर्थी", "नवमी", "चतुर्दशी", "अष्टमी", "अमावस्या")

    private val cities = listOf(
        CityLocation("Jaipur", "जयपुर", "Rajasthan", 26.9124, 75.7873),
        CityLocation("Guwahati", "गुवाहाटी", "Assam", 26.1445, 91.7362),
        CityLocation("Kochi", "कोच्चि", "Kerala", 9.9312, 76.2673)
    )

    @Test
    fun `every category avoids the rikta and the other excluded tithis`() {
        cities.forEach { city ->
            val muhurats = MuhuratCalculator.getUpcomingMuhurats(city)
            assertTrue("no muhurats at all for ${city.cityName}", muhurats.isNotEmpty())
            muhurats.forEach { m ->
                excluded.forEach { bad ->
                    assertTrue(
                        "${m.categoryEn} in ${city.cityName} was offered on ${m.tithiHi}",
                        !m.tithiHi.contains(bad)
                    )
                }
            }
        }
    }

    /** Chaturdashi must not be what catches Chaturthi, or the other way round. */
    @Test
    fun `chaturthi and chaturdashi are two different tithis`() {
        assertTrue(!"चतुर्दशी".contains("चतुर्थी"))
        assertTrue(!"चतुर्थी".contains("चतुर्दशी"))
        assertTrue(AstroNames.TITHI_HI.contains("चतुर्थी"))
        assertTrue(AstroNames.TITHI_HI.contains("चतुर्दशी"))
    }
}
