package com.example.astro

import java.util.Calendar
import java.util.Date

object TransitCalculator {

    /**
     * Returns the house number (1-12) that the given transiting planet currently
     * occupies relative to a person's Moon-rashi (rashiIdx is 0-based: 0=Mesh...11=Meen).
     */
    fun getTransitHouse(rashiIdx: Int, planetName: String, date: Date): Int {
        val cal = Calendar.getInstance()
        cal.time = date
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val hour = cal.get(Calendar.HOUR_OF_DAY).toDouble()

        val planetDegrees = AstroMath.calculatePlanets(year, month, day, hour)
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
}
