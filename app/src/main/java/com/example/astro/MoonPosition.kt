package com.example.astro

import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sin

/**
 * The Moon's ecliptic latitude and distance (Meeus ch.47, tables 47.A and 47.B).
 *
 * Longitude alone is enough for Tithi and Nakshatra, which is why AstroMath stops
 * there. Moonrise and moonset need more: the Moon strays up to 5.1 degrees off the
 * ecliptic, and its parallax is nearly a full degree. Ignoring either moves the
 * rise time by twenty minutes or more — which is the whole point of printing it.
 */
object MoonPosition {

    // Table 47.B — latitude. Columns: D, M, M', F, coefficient (1e-6 degrees).
    private val LAT_TERMS = arrayOf(
        intArrayOf(0, 0, 0, 1, 5128122), intArrayOf(0, 0, 1, 1, 280602),
        intArrayOf(0, 0, 1, -1, 277693), intArrayOf(2, 0, 0, -1, 173237),
        intArrayOf(2, 0, -1, 1, 55413), intArrayOf(2, 0, -1, -1, 46271),
        intArrayOf(2, 0, 0, 1, 32573), intArrayOf(0, 0, 2, 1, 17198),
        intArrayOf(2, 0, 1, -1, 9266), intArrayOf(0, 0, 2, -1, 8822),
        intArrayOf(2, -1, 0, -1, 8216), intArrayOf(2, 0, -2, -1, 4324),
        intArrayOf(2, 0, 1, 1, 4200), intArrayOf(2, 1, 0, -1, -3359),
        intArrayOf(2, -1, -1, 1, 2463), intArrayOf(2, -1, 0, 1, 2211),
        intArrayOf(2, -1, -1, -1, 2065), intArrayOf(0, 1, -1, -1, -1870),
        intArrayOf(4, 0, -1, -1, 1828), intArrayOf(0, 1, 0, 1, -1794),
        intArrayOf(0, 0, 0, 3, -1749), intArrayOf(0, 1, -1, 1, -1565),
        intArrayOf(1, 0, 0, 1, -1491), intArrayOf(0, 1, 1, 1, -1475),
        intArrayOf(0, 1, 1, -1, -1410), intArrayOf(0, 1, 0, -1, -1344),
        intArrayOf(1, 0, 0, -1, -1335), intArrayOf(0, 0, 3, 1, 1107),
        intArrayOf(4, 0, 0, -1, 1021), intArrayOf(4, 0, -1, 1, 833),
        intArrayOf(0, 0, 1, -3, 777), intArrayOf(4, 0, -2, 1, 671),
        intArrayOf(2, 0, 0, -3, 607), intArrayOf(2, 0, 2, -1, 596),
        intArrayOf(2, -1, 1, -1, 491), intArrayOf(2, 0, -2, 1, -451),
        intArrayOf(0, 0, 3, -1, 439), intArrayOf(2, 0, 2, 1, 422),
        intArrayOf(2, 0, -3, -1, 421), intArrayOf(2, 1, -1, 1, -366),
        intArrayOf(2, 1, 0, 1, -351), intArrayOf(4, 0, 0, 1, 331),
        intArrayOf(2, -1, 1, 1, 315), intArrayOf(2, -2, 0, -1, 302),
        intArrayOf(0, 0, 1, 3, -283), intArrayOf(2, 1, 1, -1, -229),
        intArrayOf(1, 1, 0, -1, 223), intArrayOf(1, 1, 0, 1, 223),
        intArrayOf(0, 1, -2, -1, -220), intArrayOf(2, 1, -1, -1, -220),
        intArrayOf(1, 0, 1, 1, -185), intArrayOf(2, -1, -2, -1, 181),
        intArrayOf(0, 1, 2, 1, -177), intArrayOf(4, 0, -2, -1, 176),
        intArrayOf(4, -1, -1, -1, 166), intArrayOf(1, 0, 1, -1, -164),
        intArrayOf(4, 0, 1, -1, 132), intArrayOf(1, 0, -1, -1, -119),
        intArrayOf(4, -1, 0, -1, 115), intArrayOf(2, -2, 0, 1, 107)
    )

    // Table 47.A — the cosine (distance) column, in 0.001 km.
    private val DIST_TERMS = arrayOf(
        intArrayOf(0, 0, 1, 0, -20905355), intArrayOf(2, 0, -1, 0, -3699111),
        intArrayOf(2, 0, 0, 0, -2955968), intArrayOf(0, 0, 2, 0, -569925),
        intArrayOf(0, 1, 0, 0, 48888), intArrayOf(0, 0, 0, 2, -3149),
        intArrayOf(2, 0, -2, 0, 246158), intArrayOf(2, -1, -1, 0, -152138),
        intArrayOf(2, 0, 1, 0, -170733), intArrayOf(2, -1, 0, 0, -204586),
        intArrayOf(0, 1, -1, 0, -129620), intArrayOf(1, 0, 0, 0, 108743),
        intArrayOf(0, 1, 1, 0, 104755), intArrayOf(2, 0, 0, -2, 10321),
        intArrayOf(0, 0, 1, -2, 79661), intArrayOf(4, 0, -1, 0, -34782),
        intArrayOf(0, 0, 3, 0, -23210), intArrayOf(4, 0, -2, 0, -21636),
        intArrayOf(2, 1, -1, 0, 24208), intArrayOf(2, 1, 0, 0, 30824),
        intArrayOf(1, 0, -1, 0, -8379), intArrayOf(1, 1, 0, 0, -16675),
        intArrayOf(2, -1, 1, 0, -12831), intArrayOf(2, 0, 2, 0, -10445),
        intArrayOf(4, 0, 0, 0, -11650), intArrayOf(2, 0, -3, 0, 14403),
        intArrayOf(0, 1, -2, 0, -7003), intArrayOf(2, -1, -2, 0, 10056),
        intArrayOf(1, 0, 1, 0, 6322), intArrayOf(2, -2, 0, 0, -9884),
        intArrayOf(0, 1, 2, 0, 5751), intArrayOf(2, -2, -1, 0, -4950),
        intArrayOf(2, 0, 1, -2, 4130), intArrayOf(4, -1, -1, 0, -3958),
        intArrayOf(3, 0, -1, 0, 3258), intArrayOf(2, 1, 1, 0, 2616),
        intArrayOf(4, -1, -2, 0, -1897), intArrayOf(0, 2, -1, 0, -2117),
        intArrayOf(2, 2, -1, 0, 2354), intArrayOf(4, 0, 1, 0, -1423),
        intArrayOf(0, 0, 4, 0, -1117), intArrayOf(4, -1, 0, 0, -1571),
        intArrayOf(1, 0, -2, 0, -1739), intArrayOf(0, 0, 2, -2, -4421),
        intArrayOf(0, 2, 1, 0, 1165), intArrayOf(2, 0, -1, -2, 8752)
    )

    private fun rad(d: Double) = Math.toRadians(d)
    private fun norm(d: Double) = AstroTime.norm360(d)

    private class Args(
        val lp: Double, val d: Double, val m: Double,
        val mp: Double, val f: Double, val e: Double,
        val a1: Double, val a3: Double
    )

    private fun args(t: Double): Args {
        val lp = norm(218.3164477 + 481267.88123421 * t - 0.0015786 * t * t +
            t * t * t / 538841.0 - t * t * t * t / 65194000.0)
        val d = norm(297.8501921 + 445267.1114034 * t - 0.0018819 * t * t +
            t * t * t / 545868.0 - t * t * t * t / 113065000.0)
        val m = norm(357.5291092 + 35999.0502909 * t - 0.0001536 * t * t +
            t * t * t / 24490000.0)
        val mp = norm(134.9633964 + 477198.8675055 * t + 0.0087414 * t * t +
            t * t * t / 69699.0 - t * t * t * t / 14712000.0)
        val f = norm(93.2720950 + 483202.0175233 * t - 0.0036539 * t * t -
            t * t * t / 3526000.0 + t * t * t * t / 863310000.0)
        return Args(
            lp, d, m, mp, f,
            1.0 - 0.002516 * t - 0.0000074 * t * t,
            norm(119.75 + 131.849 * t),
            norm(313.45 + 481266.484 * t)
        )
    }

    /** Geocentric ecliptic latitude of the Moon in degrees, roughly ±5.15. */
    fun latitude(jde: Double): Double {
        val t = AstroTime.julianCenturies(jde)
        val a = args(t)
        var sigma = 0.0
        for (term in LAT_TERMS) {
            var coeff = term[4].toDouble()
            if (term[1] != 0) coeff *= Math.pow(a.e, abs(term[1]).toDouble())
            sigma += coeff * sin(rad(term[0] * a.d + term[1] * a.m + term[2] * a.mp + term[3] * a.f))
        }
        sigma += -2235.0 * sin(rad(a.lp)) +
            382.0 * sin(rad(a.a3)) +
            175.0 * sin(rad(a.a1 - a.f)) +
            175.0 * sin(rad(a.a1 + a.f)) +
            127.0 * sin(rad(a.lp - a.mp)) -
            115.0 * sin(rad(a.lp + a.mp))
        return sigma / 1_000_000.0
    }

    /** Geocentric distance to the Moon in kilometres. */
    fun distanceKm(jde: Double): Double {
        val t = AstroTime.julianCenturies(jde)
        val a = args(t)
        var sigma = 0.0
        for (term in DIST_TERMS) {
            var coeff = term[4].toDouble()
            if (term[1] != 0) coeff *= Math.pow(a.e, abs(term[1]).toDouble())
            sigma += coeff * cos(rad(term[0] * a.d + term[1] * a.m + term[2] * a.mp + term[3] * a.f))
        }
        return 385000.56 + sigma / 1000.0
    }

    /** Equatorial horizontal parallax in degrees — about 0.95, and it matters at the horizon. */
    fun horizontalParallax(jde: Double): Double =
        Math.toDegrees(asin(6378.14 / distanceKm(jde)))

    /** Apparent semi-diameter in degrees. */
    fun semiDiameter(jde: Double): Double =
        Math.toDegrees(asin(1737.4 / distanceKm(jde)))
}
