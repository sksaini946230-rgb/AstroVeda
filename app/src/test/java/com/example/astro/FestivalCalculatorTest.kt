package com.example.astro

import com.example.astro.FestivalCalculator.Observance
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.util.Calendar

/**
 * Forty-eight dates from published panchang — four years, twelve festivals.
 *
 * This is the oracle the engine has to satisfy. It is deliberately not the old
 * hardcoded table: three of that table's twelve 2026 dates were wrong, which is
 * how the bug was found in the first place. These come from drikpanchang.com,
 * checked for 2025 through 2028.
 *
 * A failure here means the engine is wrong, not the expectation. Do not edit an
 * expected date to make a test pass without a published source that says so.
 */
class FestivalCalculatorTest {

    private fun iso(cal: Calendar): String = String.format(
        java.util.Locale.US, "%04d-%02d-%02d",
        cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH)
    )

    private fun check(
        name: String, masaHi: String, pakshaHi: String, tithiHi: String,
        observance: Observance, year: Int, expected: String
    ) {
        val masa = FestivalCalculator.masaIndexFor(masaHi)
        assertNotNull("month name not recognised: $masaHi", masa)
        val tithi = FestivalCalculator.tithiNumberFor(pakshaHi, tithiHi)
        assertNotNull("tithi not recognised: $pakshaHi $tithiHi", tithi)

        val date = FestivalCalculator.dateFor(masa!!, tithi!!, year, observance)
        assertNotNull("$name $year: no date found", date)
        assertEquals("$name $year", expected, iso(date!!))
    }

    // The twelve rules, each with the window tradition decides it in.
    private fun rakshaBandhan(y: Int, d: String) =
        check("Raksha Bandhan", "श्रावण", "शुक्ल पक्ष", "पूर्णिमा", Observance.RAKSHA_BANDHAN, y, d)
    private fun janmashtami(y: Int, d: String) =
        check("Janmashtami", "भाद्रपद", "कृष्ण पक्ष", "अष्टमी", Observance.SUNRISE, y, d)
    private fun ganeshChaturthi(y: Int, d: String) =
        check("Ganesh Chaturthi", "भाद्रपद", "शुक्ल पक्ष", "चतुर्थी", Observance.MADHYAHNA, y, d)
    private fun navratri(y: Int, d: String) =
        check("Navratri", "आश्विन", "शुक्ल पक्ष", "प्रतिपदा", Observance.SUNRISE, y, d)
    private fun dussehra(y: Int, d: String) =
        check("Dussehra", "आश्विन", "शुक्ल पक्ष", "दशमी", Observance.APARAHNA, y, d)
    private fun karwaChauth(y: Int, d: String) =
        check("Karwa Chauth", "कार्तिक", "कृष्ण पक्ष", "चतुर्थी", Observance.CHANDRODAYA, y, d)
    private fun dhanteras(y: Int, d: String) =
        check("Dhanteras", "कार्तिक", "कृष्ण पक्ष", "त्रयोदशी", Observance.PRADOSH, y, d)
    private fun diwali(y: Int, d: String) =
        check("Diwali", "कार्तिक", "अमावस्या", "अमावस्या", Observance.PRADOSH, y, d)
    private fun govardhan(y: Int, d: String) =
        check("Govardhan Puja", "कार्तिक", "शुक्ल पक्ष", "प्रतिपदा", Observance.SUNRISE, y, d)
    private fun chhath(y: Int, d: String) =
        check("Chhath", "कार्तिक", "शुक्ल पक्ष", "षष्ठी", Observance.SUNRISE, y, d)
    private fun gangaur(y: Int, d: String) =
        check("Gangaur", "चैत्र", "शुक्ल पक्ष", "तृतीया", Observance.SUNRISE, y, d)
    private fun teej(y: Int, d: String) =
        check("Hariyali Teej", "श्रावण", "शुक्ल पक्ष", "तृतीया", Observance.SUNRISE, y, d)

    @Test fun `2025`() {
        rakshaBandhan(2025, "2025-08-09")
        // Janmashtami is the one festival kept on two consecutive days: Smarta
        // on the night Ashtami holds nishita, Vaishnava on the day Ashtami holds
        // sunrise. Drik prints both for 2025 — 15 Aug Smarta, 16 Aug ISKCON. The
        // engine reports the udaya-vyapini day throughout, which is always one
        // of the two real dates; FestivalProvider says so in the festival text.
        janmashtami(2025, "2025-08-16")
        ganeshChaturthi(2025, "2025-08-27")
        navratri(2025, "2025-09-22")
        dussehra(2025, "2025-10-02")
        karwaChauth(2025, "2025-10-10")
        dhanteras(2025, "2025-10-18")
        diwali(2025, "2025-10-20")
        govardhan(2025, "2025-10-22")
        chhath(2025, "2025-10-27")
        gangaur(2025, "2025-03-31")
        teej(2025, "2025-07-27")
    }

    @Test fun `2026`() {
        rakshaBandhan(2026, "2026-08-28")
        janmashtami(2026, "2026-09-04")
        ganeshChaturthi(2026, "2026-09-14")
        navratri(2026, "2026-10-11")
        dussehra(2026, "2026-10-20")
        karwaChauth(2026, "2026-10-29")
        dhanteras(2026, "2026-11-06")
        diwali(2026, "2026-11-08")
        govardhan(2026, "2026-11-10")
        chhath(2026, "2026-11-15")
        gangaur(2026, "2026-03-21")
        teej(2026, "2026-08-15")
    }

    @Test fun `2027`() {
        rakshaBandhan(2027, "2027-08-17")
        janmashtami(2027, "2027-08-25")
        ganeshChaturthi(2027, "2027-09-04")
        navratri(2027, "2027-09-30")
        dussehra(2027, "2027-10-09")
        karwaChauth(2027, "2027-10-18")
        dhanteras(2027, "2027-10-27")
        diwali(2027, "2027-10-29")
        govardhan(2027, "2027-10-30")
        chhath(2027, "2027-11-04")
        gangaur(2027, "2027-04-09")
        teej(2027, "2027-08-04")
    }

    @Test fun `2028`() {
        rakshaBandhan(2028, "2028-08-05")
        janmashtami(2028, "2028-08-13")
        ganeshChaturthi(2028, "2028-08-23")
        navratri(2028, "2028-09-19")
        dussehra(2028, "2028-09-27")
        karwaChauth(2028, "2028-10-07")
        dhanteras(2028, "2028-10-15")
        diwali(2028, "2028-10-17")
        govardhan(2028, "2028-10-18")
        chhath(2028, "2028-10-23")
        gangaur(2028, "2028-03-29")
        teej(2028, "2028-07-24")
    }

    // 2029 and 2030 were fetched after the engine was already passing 2025-2028.
    // Nothing was adjusted for them — they are the check that the rules are the
    // real ones and not shaped to fit four years.
    @Test fun `2029`() {
        rakshaBandhan(2029, "2029-08-23")
        janmashtami(2029, "2029-09-01")
        ganeshChaturthi(2029, "2029-09-11")
        navratri(2029, "2029-10-08")
        dussehra(2029, "2029-10-16")
        karwaChauth(2029, "2029-10-26")
        dhanteras(2029, "2029-11-04")
        diwali(2029, "2029-11-05")
        govardhan(2029, "2029-11-06")
        chhath(2029, "2029-11-11")
        gangaur(2029, "2029-04-17")
        teej(2029, "2029-08-12")
    }

    @Test fun `2030`() {
        rakshaBandhan(2030, "2030-08-13")
        janmashtami(2030, "2030-08-21")
        ganeshChaturthi(2030, "2030-09-01")
        navratri(2030, "2030-09-28")
        dussehra(2030, "2030-10-06")
        karwaChauth(2030, "2030-10-15")
        dhanteras(2030, "2030-10-24")
        diwali(2030, "2030-10-26")
        govardhan(2030, "2030-10-27")
        chhath(2030, "2030-11-01")
        gangaur(2030, "2030-04-06")
        teej(2030, "2030-08-02")
    }

    // 2031 carries an Adhika masa — Ganesh Chaturthi slips to 20 September — and
    // was fetched last of all, after the engine was complete.
    @Test fun `2031`() {
        rakshaBandhan(2031, "2031-08-02")
        // Drik lists 9 Aug for Smarta; this is the udaya day, as everywhere else.
        janmashtami(2031, "2031-08-10")
        ganeshChaturthi(2031, "2031-09-20")
        navratri(2031, "2031-10-17")
        dussehra(2031, "2031-10-25")
        karwaChauth(2031, "2031-11-02")
        dhanteras(2031, "2031-11-12")
        diwali(2031, "2031-11-14")
        govardhan(2031, "2031-11-15")
        chhath(2031, "2031-11-20")
        gangaur(2031, "2031-03-26")
        teej(2031, "2031-07-22")
    }
}
