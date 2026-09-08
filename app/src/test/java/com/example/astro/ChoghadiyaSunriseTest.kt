package com.example.astro

import com.example.data.model.CityLocation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.GregorianCalendar

/**
 * The Choghadiya must open at the same sunrise the rest of the app shows.
 *
 * It did not. `ChoghadiyaCalculator` carried its own inline solar formula —
 * day-of-year, a first-order equation of time, a fixed 82.5°E meridian — while
 * the Panchang and the widget used `RiseSetCalculator`. On 7 Sep 2026 at Rampur
 * the widget read "SUNRISE 05:54 AM" and the Choghadiya's first slot opened at
 * 06:03, nine minutes later, with both numbers on screen at the same time.
 *
 * The slots are eighths of the interval between sunrise and sunset, so the
 * whole table slides with the error: every boundary someone reads to choose a
 * moment was out by those nine minutes.
 *
 * This compares the two rather than asserting a time, so it stays true as the
 * ephemeris is refined, and it checks several places and dates because an
 * agreement at one longitude on one day can be luck.
 */
class ChoghadiyaSunriseTest {

    private data class Place(val name: String, val lat: Double, val lon: Double)

    private val places = listOf(
        Place("Rampur", 28.8155, 79.0250),
        Place("Jaipur", 26.9124, 75.7873),
        // Far east and far west of the 82.5°E meridian the old formula assumed,
        // which is where a fixed meridian goes most wrong.
        Place("Dibrugarh", 27.4728, 94.9120),
        Place("Dwarka", 22.2394, 68.9678),
        // Far south, where the day length differs most from the north.
        Place("Kanyakumari", 8.0883, 77.5385)
    )

    /** Exactly what PanchangCalculator does: through Calendar, truncating. */
    private fun panchangSunriseMinutes(y: Int, m: Int, d: Int, p: Place): Int {
        val zone = AstroTime.IST
        val midnightJd = AstroTime.julianDayFromLocal(y, m, d, 0, 0, zone)
        val rise = RiseSetCalculator.sunRiseSet(midnightJd, p.lat, p.lon).riseJd
            ?: return -1
        val cal = GregorianCalendar(zone).apply { timeInMillis = AstroTime.millisFromJulianDay(rise) }
        return cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
    }

    private fun choghadiyaFirstSlotMinutes(y: Int, m: Int, d: Int, p: Place): Int {
        val cal = GregorianCalendar(AstroTime.IST).apply {
            clear(); set(y, m - 1, d, 12, 0, 0)
        }
        val slots = ChoghadiyaCalculator.getChoghadiyaSlots(
            cal.time, isDaytime = true, lat = p.lat, lon = p.lon, use24Hour = true
        )
        val hhmm = slots.first().startTime.split(":")
        return hhmm[0].toInt() * 60 + hhmm[1].toInt()
    }

    @Test
    fun `the day Choghadiya opens at the Panchang's sunrise`() {
        val off = mutableListOf<String>()
        // Solstices and equinoxes, where sunrise moves fastest and slowest.
        val dates = listOf(
            Triple(2026, 3, 20), Triple(2026, 6, 21),
            Triple(2026, 9, 7), Triple(2026, 12, 21)
        )
        for (p in places) {
            for ((y, m, d) in dates) {
                val panchang = panchangSunriseMinutes(y, m, d, p)
                if (panchang < 0) continue
                val chog = choghadiyaFirstSlotMinutes(y, m, d, p)
                // Exactly equal. They were a minute apart while one side
                // rounded and the other truncated, which is visible on the
                // Panchang screen where both numbers appear together.
                if (panchang != chog) {
                    off += "${p.name} $y-$m-$d: panchang=$panchang chogh=$chog (${chog - panchang} min)"
                }
            }
        }
        assertTrue(
            "Choghadiya and the Panchang disagree about sunrise:\n" + off.joinToString("\n"),
            off.isEmpty()
        )
    }

    @Test
    fun `the eight day slots exactly span sunrise to sunset`() {
        val p = places[0]
        val zone = AstroTime.IST
        val midnightJd = AstroTime.julianDayFromLocal(2026, 9, 7, 0, 0, zone)
        val sun = RiseSetCalculator.sunRiseSet(midnightJd, p.lat, p.lon)
        fun toMin(jd: Double): Int {
            val c = GregorianCalendar(zone).apply { timeInMillis = AstroTime.millisFromJulianDay(jd) }
            return c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE)
        }
        val rise = toMin(sun.riseJd!!)
        val set = toMin(sun.setJd!!)

        val cal = GregorianCalendar(zone).apply { clear(); set(2026, Calendar.SEPTEMBER, 7, 12, 0, 0) }
        val slots = ChoghadiyaCalculator.getChoghadiyaSlots(
            cal.time, isDaytime = true, lat = p.lat, lon = p.lon, use24Hour = true
        )
        assertEquals(8, slots.size)

        fun mins(s: String) = s.split(":").let { it[0].toInt() * 60 + it[1].toInt() }
        assertEquals("first slot should start at sunrise", rise, mins(slots.first().startTime))
        // Eight eighths, so the last one ends at sunset give or take the rounding.
        assertTrue(
            "last slot should end at sunset, was ${slots.last().endTime} vs $set",
            Math.abs(mins(slots.last().endTime) - set) <= 2
        )
    }
}
