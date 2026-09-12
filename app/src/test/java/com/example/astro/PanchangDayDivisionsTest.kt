package com.example.astro

import com.example.data.model.CityLocation
import com.example.data.model.PanchangData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.GregorianCalendar

/**
 * The day divisions and the lunar month name.
 *
 * Rahu Kaal, Gulika and Yamaganda are eighths of the daylight, in a fixed
 * weekday order; Abhijit and Brahma are muhurtas, which are fifteenths of the
 * day and of the night and not the forty-eight minutes they were written as.
 * None of these is checkable by a reader, and all of them are read to decide
 * when to begin something.
 */
class PanchangDayDivisionsTest {

    private val jaipur = CityLocation("Jaipur", "जयपुर", "Rajasthan", 26.9124, 75.7873)

    private fun panchangOn(y: Int, m: Int, d: Int): PanchangData =
        PanchangCalculator.calculatePanchang(
            GregorianCalendar(AstroTime.IST).apply { clear(); set(y, m - 1, d, 12, 0, 0) }.time,
            jaipur,
            use24Hour = true
        )

    private fun minutes(hhmm: String): Int {
        val (h, m) = hhmm.trim().split(":").map { it.toInt() }
        return h * 60 + m
    }

    private fun range(s: String): Pair<Int, Int> {
        val (from, to) = s.split(" - ")
        return minutes(from) to minutes(to)
    }

    /** The eighth of the daylight each of the three falls in, Sunday first. */
    private val rahuSlot = listOf(7, 1, 6, 4, 5, 3, 2)
    private val gulikaSlot = listOf(6, 5, 4, 3, 2, 1, 0)
    private val yamaSlot = listOf(4, 3, 2, 1, 0, 6, 5)

    @Test
    fun `rahu gulika and yamaganda sit in their weekday eighth of the daylight`() {
        // A whole week, so every row of all three tables is exercised.
        for (day in 13..19) {
            val p = panchangOn(2026, 9, day)
            val sunrise = minutes(p.sunrise)
            val slotLen = (minutes(p.sunset) - sunrise) / 8
            val weekday = GregorianCalendar(AstroTime.IST)
                .apply { clear(); set(2026, 8, day) }
                .get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY

            fun expected(slot: Int) = (sunrise + slot * slotLen) to (sunrise + (slot + 1) * slotLen)
            assertEquals("Rahu Kaal on ${p.dayOfWeek}", expected(rahuSlot[weekday]), range(p.rahuKaal))
            assertEquals("Gulika on ${p.dayOfWeek}", expected(gulikaSlot[weekday]), range(p.gulikaKaal))
            assertEquals("Yamaganda on ${p.dayOfWeek}", expected(yamaSlot[weekday]), range(p.yamaganda))
        }
    }

    /** No two of the three may claim the same eighth on the same day. */
    @Test
    fun `the three inauspicious periods never overlap`() {
        for (weekday in 0..6) {
            assertEquals(
                "weekday $weekday",
                3,
                setOf(rahuSlot[weekday], gulikaSlot[weekday], yamaSlot[weekday]).size
            )
        }
    }

    /**
     * A muhurta is a fifteenth of the daylight, not forty-eight minutes. It was
     * the flat number, which is right only at the equinox: in Jaipur a day
     * muhurta runs from about 41 to 55 minutes over the year.
     */
    @Test
    fun `abhijit is the eighth day muhurta and it breathes with the day`() {
        listOf(Triple(2026, 6, 21), Triple(2026, 12, 21), Triple(2026, 3, 20)).forEach { (y, m, d) ->
            val p = panchangOn(y, m, d)
            val sunrise = minutes(p.sunrise)
            val dayLen = minutes(p.sunset) - sunrise
            val muhurta = dayLen / 15
            val midDay = sunrise + dayLen / 2
            assertEquals(
                "Abhijit on $d/$m/$y",
                (midDay - muhurta / 2) to (midDay + muhurta / 2),
                range(p.abhijitMuhurat)
            )
        }
        // Longest day against shortest: the windows must differ in length.
        val summer = range(panchangOn(2026, 6, 21).abhijitMuhurat).let { it.second - it.first }
        val winter = range(panchangOn(2026, 12, 21).abhijitMuhurat).let { it.second - it.first }
        assertTrue("Abhijit was a fixed 48 minutes all year: $summer vs $winter", summer != winter)
    }

    @Test
    fun `brahma muhurta is the fourteenth night muhurta`() {
        listOf(Triple(2026, 6, 21), Triple(2026, 12, 21)).forEach { (y, m, d) ->
            val p = panchangOn(y, m, d)
            val sunrise = minutes(p.sunrise)
            val nightMuhurta = (1440 - (minutes(p.sunset) - sunrise)) / 15
            assertEquals(
                "Brahma muhurta on $d/$m/$y",
                (sunrise - 2 * nightMuhurta) to (sunrise - nightMuhurta),
                range(p.brahmaMuhurat)
            )
        }
    }

    /**
     * Adhika Jyeshtha ran 17 May to 14 June 2026 and Nija Jyeshtha followed it.
     * Both are "Jyeshtha" to [PanchangElements.masaIndex] — a month is Adhika
     * exactly because the Sun changes no sign during it, so the pair begin with
     * the Sun in the same sign and take the same name. The screen said ज्येष्ठ
     * for fifty-nine days and never said which.
     */
    @Test
    fun `an adhika month says so and the nija month that follows does not`() {
        val adhika = panchangOn(2026, 6, 1)
        assertEquals("Adhika Jyeshtha", adhika.masaName)
        assertEquals("अधिक ज्येष्ठ", adhika.masaNameHindi)

        val nija = panchangOn(2026, 6, 20)
        assertEquals("Jyeshtha", nija.masaName)
        assertEquals("ज्येष्ठ", nija.masaNameHindi)

        // And an ordinary month is never labelled.
        listOf(Triple(2026, 9, 12), Triple(2026, 1, 1), Triple(2026, 3, 19)).forEach { (y, m, d) ->
            assertTrue(
                "$d/$m/$y should not be Adhika",
                !panchangOn(y, m, d).masaName.startsWith("Adhika")
            )
        }
    }
}
