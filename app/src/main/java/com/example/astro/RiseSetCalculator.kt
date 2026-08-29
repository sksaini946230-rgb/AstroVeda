package com.example.astro

import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/**
 * Rise and set times, solved rather than approximated.
 *
 * The old code used a closed-form sunrise formula driven by `dayOfYear` where the
 * algorithm wanted days-since-J2000, so the year was ignored entirely and leap
 * years were never handled. Moonrise and moonset were not computed at all —
 * moonrise was literally "sunset minus one hour" and moonset "sunrise plus ten
 * hours", which the Panchang screen then labelled चन्द्रोदय and चन्द्रास्त.
 *
 * This walks the body's true altitude across the day and bisects each horizon
 * crossing. It costs a few hundred ephemeris evaluations, which is nothing next
 * to being right, and it works identically for the Sun and the fast-moving Moon.
 */
object RiseSetCalculator {

    /** Standard refraction + solar semi-diameter: the Sun's centre sits here at sunrise. */
    private const val SUN_ALTITUDE = -0.8333

    /** Atmospheric refraction alone; the Moon's parallax and semi-diameter vary daily. */
    private const val REFRACTION = -0.5667

    data class RiseSet(val riseJd: Double?, val setJd: Double?)

    private fun rad(d: Double) = Math.toRadians(d)

    /** Apparent sidereal time at Greenwich, in degrees. */
    private fun gmst(jdUT: Double): Double {
        val t = AstroTime.julianCenturies(jdUT)
        return AstroTime.norm360(
            280.46061837 + 360.98564736629 * (jdUT - 2451545.0) +
                0.000387933 * t * t - t * t * t / 38710000.0
        )
    }

    /** Converts apparent ecliptic coordinates to altitude above the true horizon. */
    private fun altitude(
        lambdaDeg: Double, betaDeg: Double, jdUT: Double,
        latitude: Double, longitude: Double
    ): Double {
        val t = AstroTime.julianCenturies(jdUT)
        val eps = rad(AstroMath.meanObliquity(t))
        val lam = rad(lambdaDeg)
        val beta = rad(betaDeg)

        val ra = atan2(sin(lam) * cos(eps) - Math.tan(beta) * sin(eps), cos(lam))
        val dec = asin(sin(beta) * cos(eps) + cos(beta) * sin(eps) * sin(lam))

        val h = rad(AstroTime.norm360(gmst(jdUT) + longitude - Math.toDegrees(ra)))
        val lat = rad(latitude)

        return Math.toDegrees(
            asin(sin(lat) * sin(dec) + cos(lat) * cos(dec) * cos(h))
        )
    }

    private fun sunAltitude(jdUT: Double, lat: Double, lng: Double): Double {
        val jde = AstroTime.toEphemerisTime(jdUT, AstroTime.yearOf(jdUT))
        return altitude(AstroMath.sunApparentLongitude(jde), 0.0, jdUT, lat, lng) - SUN_ALTITUDE
    }

    private fun moonAltitude(jdUT: Double, lat: Double, lng: Double): Double {
        val jde = AstroTime.toEphemerisTime(jdUT, AstroTime.yearOf(jdUT))
        // The Moon's own horizon offset: refraction, minus parallax lifting it,
        // plus its semi-diameter (upper limb convention).
        val h0 = REFRACTION - MoonPosition.horizontalParallax(jde) + MoonPosition.semiDiameter(jde)
        return altitude(
            AstroMath.moonApparentLongitude(jde),
            MoonPosition.latitude(jde),
            jdUT, lat, lng
        ) - h0
    }

    /**
     * Finds horizon crossings of [altitudeFn] within [startJd, startJd + 1).
     * Returns the first rising crossing and the first setting crossing found.
     */
    private fun solve(
        startJd: Double,
        latitude: Double,
        longitude: Double,
        altitudeFn: (Double, Double, Double) -> Double
    ): RiseSet {
        val steps = 144            // ten-minute coarse scan
        val dt = 1.0 / steps
        var rise: Double? = null
        var set: Double? = null

        var prevJd = startJd
        var prevAlt = altitudeFn(prevJd, latitude, longitude)

        for (i in 1..steps) {
            val jd = startJd + i * dt
            val alt = altitudeFn(jd, latitude, longitude)

            if (prevAlt <= 0.0 && alt > 0.0 && rise == null) {
                rise = bisect(prevJd, jd, latitude, longitude, altitudeFn)
            } else if (prevAlt > 0.0 && alt <= 0.0 && set == null) {
                set = bisect(prevJd, jd, latitude, longitude, altitudeFn)
            }
            if (rise != null && set != null) break

            prevJd = jd
            prevAlt = alt
        }
        return RiseSet(rise, set)
    }

    private fun bisect(
        loIn: Double, hiIn: Double,
        latitude: Double, longitude: Double,
        altitudeFn: (Double, Double, Double) -> Double
    ): Double {
        var lo = loIn
        var hi = hiIn
        val loAlt = altitudeFn(lo, latitude, longitude)
        // 30 halvings of a ten-minute window lands well under a second.
        repeat(30) {
            val mid = (lo + hi) / 2.0
            val midAlt = altitudeFn(mid, latitude, longitude)
            if ((loAlt <= 0.0) == (midAlt <= 0.0)) lo = mid else hi = mid
        }
        return (lo + hi) / 2.0
    }

    /**
     * Sunrise and sunset for the local day beginning at [localMidnightJd] (UT Julian Day).
     * Either may be null inside a polar day or night.
     */
    fun sunRiseSet(localMidnightJd: Double, latitude: Double, longitude: Double): RiseSet =
        solve(localMidnightJd, latitude, longitude, ::sunAltitude)

    /**
     * Moonrise and moonset for the local day beginning at [localMidnightJd].
     * Either may legitimately be null: the Moon skips a rise or a set roughly
     * once a month, because its day is about 24h50m long.
     */
    fun moonRiseSet(localMidnightJd: Double, latitude: Double, longitude: Double): RiseSet =
        solve(localMidnightJd, latitude, longitude, ::moonAltitude)
}
