package com.example.astro

import java.util.Calendar
import java.util.Date

object TransitCalculator {

    /**
     * Returns the house number (1-12) that the given transiting planet currently
     * occupies relative to a person's Moon-rashi (rashiIdx is 0-based: 0=Mesh...11=Meen).
     */
    fun getTransitHouse(rashiIdx: Int, planetName: String, date: Date): Int {
        // This used to pull the local clock HOUR (dropping the minutes) and pass it
        // as if it were Universal Time — a 5.5 hour error in India, which moved the
        // Moon by three degrees and could land it in the wrong sign entirely.
        // A Date is already an absolute instant, so convert it directly.
        val planetDegrees = AstroMath.calculatePlanets(AstroTime.julianDay(date))
        val deg = planetDegrees[planetName] ?: 0.0
        val planetRashiIdx = (deg / 30.0).toInt().coerceIn(0, 11)

        return ((planetRashiIdx - rashiIdx + 12) % 12) + 1
    }

    /** Mid-week (Wednesday) date of the week containing `date`. */
    fun midWeekDate(date: Date): Date {
        val cal = Calendar.getInstance()
        cal.time = date
        cal.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY)
        return cal.time
    }

    /**
     * Driver planet + reference date for a given period.
     * TODAY  -> Moon, evaluated today
     * WEEK   -> Moon, evaluated mid-week (Wednesday) for stable weekly variation
     * MONTH  -> Sun, evaluated today (Sun changes rashi ~monthly, matching cadence)
     */
    fun getDriverHouse(rashiIdx: Int, period: String, today: Date = Date()): Int {
        return when (period) {
            "WEEK" -> getTransitHouse(rashiIdx, "Moon", midWeekDate(today))
            "MONTH" -> getTransitHouse(rashiIdx, "Sun", today)
            else -> getTransitHouse(rashiIdx, "Moon", today)
        }
    }

    /** Hindi short name for a planet, for use in generated text. */
    fun planetNameHi(planetName: String): String = when (planetName) {
        "Sun" -> "सूर्य"
        "Moon" -> "चन्द्रमा"
        "Mars" -> "मंगल"
        "Mercury" -> "बुध"
        "Jupiter" -> "गुरु"
        "Venus" -> "शुक्र"
        "Saturn" -> "शनि"
        "Rahu" -> "राहु"
        "Ketu" -> "केतु"
        else -> planetName
    }

    fun driverPlanetForPeriod(period: String): String = when (period) {
        "MONTH" -> "Sun"
        else -> "Moon" // TODAY and WEEK both use Moon
    }

    /** Hindi period phrase used inside reading templates ("आज" / "इस सप्ताह" / "इस माह"). */
    fun periodWordHi(period: String): String = when (period) {
        "WEEK" -> "इस सप्ताह"
        "MONTH" -> "इस माह"
        else -> "आज"
    }

    /** English period phrase, lowercase, for mid-sentence use ("today" / "this week" / "this month"). */
    fun periodWordEn(period: String): String = when (period) {
        "WEEK" -> "this week"
        "MONTH" -> "this month"
        else -> "today"
    }

    /** English period phrase, capitalized, for sentence-start use ("Today" / "This week" / "This month"). */
    fun periodWordEnCap(period: String): String = when (period) {
        "WEEK" -> "This week"
        "MONTH" -> "This month"
        else -> "Today"
    }
}
