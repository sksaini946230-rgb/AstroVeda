package com.example.astro

import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar
import java.util.TimeZone

/**
 * Julian Day and timezone helpers.
 *
 * WHY THIS EXISTS: every calculator used to feed the device's *local* clock hour
 * straight into the ephemeris and into the Julian Day. In India that is a 5.5 hour
 * error, which put the Ascendant ~82.5 degrees (almost three signs) off and the
 * Moon ~3 degrees off — enough to change the Nakshatra outright. Every ephemeris
 * entry point now takes Universal Time, and the conversion happens here, once.
 */
object AstroTime {

    /** Indian Standard Time — the zone all Panchang output is presented in. */
    val IST: TimeZone = TimeZone.getTimeZone("Asia/Kolkata")

    /**
     * Julian Day for a Universal Time instant.
     *
     * Uses the Gregorian correction only for dates on/after 1582-10-15, as the
     * calendar reform requires; before that the Julian calendar applies.
     */
    fun julianDay(year: Int, month: Int, day: Int, hourUT: Double): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val isGregorian = (year > 1582) ||
            (year == 1582 && (month > 10 || (month == 10 && day >= 15)))
        val b = if (isGregorian) {
            val a = Math.floorDiv(y, 100)
            2 - a + Math.floorDiv(a, 4)
        } else {
            0
        }
        return Math.floor(365.25 * (y + 4716)) +
            Math.floor(30.6001 * (m + 1)) +
            day + hourUT / 24.0 + b - 1524.5
    }

    /** Julian Day for an absolute instant. */
    fun julianDay(date: Date): Double = 2440587.5 + date.time / 86_400_000.0

    /** Julian Day from an epoch-millis instant. */
    fun julianDayFromMillis(millis: Long): Double = 2440587.5 + millis / 86_400_000.0

    /** Converts a Julian Day back to an absolute instant. */
    fun millisFromJulianDay(jd: Double): Long = ((jd - 2440587.5) * 86_400_000.0).toLong()

    /**
     * Julian Day for a wall-clock date/time interpreted in [zone].
     *
     * This is the entry point for birth data: the user types their local birth
     * time, and the zone (from the birth *place*, not the device) turns it into UT.
     */
    fun julianDayFromLocal(
        year: Int,
        month: Int,
        day: Int,
        hour: Int,
        minute: Int,
        zone: TimeZone
    ): Double {
        val cal = GregorianCalendar(zone).apply {
            clear()
            set(year, month - 1, day, hour, minute, 0)
        }
        return julianDay(cal.time)
    }

    /** Centuries of Terrestrial Time since J2000.0. */
    fun julianCenturies(jd: Double): Double = (jd - 2451545.0) / 36525.0

    /**
     * Approximate difference TT - UT in seconds (Espenak/Meeus polynomial,
     * 2005-2050 branch, which covers every date this app realistically sees;
     * older dates fall back to a wider fit).
     *
     * Small — under a minute and a half — but it costs nothing to include and it
     * keeps the Moon honest to the arc-minute.
     */
    fun deltaTSeconds(year: Int): Double = when {
        year in 2005..2050 -> {
            val t = year - 2000.0
            62.92 + 0.32217 * t + 0.005589 * t * t
        }
        year in 1986..2004 -> {
            val t = year - 2000.0
            63.86 + 0.3345 * t - 0.060374 * t * t + 0.0017275 * t * t * t +
                0.000651814 * t * t * t * t + 0.00002373599 * t * t * t * t * t
        }
        year in 1961..1985 -> {
            val t = year - 1975.0
            45.45 + 1.067 * t - t * t / 260.0 - t * t * t / 718.0
        }
        year in 1941..1960 -> {
            val t = year - 1950.0
            29.07 + 0.407 * t - t * t / 233.0 + t * t * t / 2547.0
        }
        year in 1900..1940 -> {
            val t = year - 1900.0
            -2.79 + 1.494119 * t - 0.0598939 * t * t + 0.0061966 * t * t * t -
                0.000197 * t * t * t * t
        }
        else -> {
            val u = (year - 1820.0) / 100.0
            -20.0 + 32.0 * u * u
        }
    }

    /** Julian Ephemeris Day (Terrestrial Time) for a UT Julian Day. */
    fun toEphemerisTime(jdUT: Double, year: Int): Double =
        jdUT + deltaTSeconds(year) / 86400.0

    /** Extracts the calendar year from a Julian Day (UT). */
    fun yearOf(jd: Double): Int {
        val cal = GregorianCalendar(TimeZone.getTimeZone("UTC"))
        cal.timeInMillis = millisFromJulianDay(jd)
        return cal.get(Calendar.YEAR)
    }

    /** Normalises an angle into [0, 360). */
    fun norm360(deg: Double): Double {
        val d = deg % 360.0
        return if (d < 0) d + 360.0 else d
    }

    /** Wraps an angle difference into (-180, 180]. */
    fun wrap180(deg: Double): Double {
        var d = deg % 360.0
        if (d > 180.0) d -= 360.0
        if (d <= -180.0) d += 360.0
        return d
    }
}
