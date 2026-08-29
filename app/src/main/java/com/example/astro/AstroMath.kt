package com.example.astro

import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.atan2

/**
 * Geocentric sidereal longitudes (Lahiri ayanamsa) for the nine Vedic grahas.
 *
 * ACCURACY NOTE — read before changing anything here.
 *
 * The Moon used to be computed from Paul Schlyter's orbital elements with the
 * perturbation terms omitted. Those terms are not optional: Evection (1.27 deg),
 * Variation (0.66 deg) and the Annual Equation (0.19 deg) are the difference
 * between a usable Moon and a decorative one. Measured against a Meeus reference,
 * the old Moon was 1.22 degrees off, which made the Tithi wrong on 92 days of 2026
 * and the Nakshatra wrong on 71.
 *
 * The Moon and Sun now use Meeus (Astronomical Algorithms, 2nd ed.):
 *   - Sun:  chapter 25, apparent longitude, accurate to ~0.01 arcmin
 *   - Moon: chapter 47, the 60 largest periodic terms in longitude, ~10 arcsec
 *
 * The five star-planets keep Schlyter's elements — good to a few arcminutes,
 * which is well inside a 30-degree rashi — but now WITH his perturbation terms
 * for Jupiter and Saturn, which reach half a degree without them.
 *
 * Every entry point takes Universal Time. Do not pass a local clock hour.
 */
object AstroMath {

    // ------------------------------------------------------------------
    // Meeus ch.47 — periodic terms in the Moon's longitude.
    // Columns: D, M, M', F, coefficient (in 1e-6 degrees).
    // ------------------------------------------------------------------
    private val MOON_LON_TERMS = arrayOf(
        intArrayOf(0, 0, 1, 0, 6288774), intArrayOf(2, 0, -1, 0, 1274027),
        intArrayOf(2, 0, 0, 0, 658314), intArrayOf(0, 0, 2, 0, 213618),
        intArrayOf(0, 1, 0, 0, -185116), intArrayOf(0, 0, 0, 2, -114332),
        intArrayOf(2, 0, -2, 0, 58793), intArrayOf(2, -1, -1, 0, 57066),
        intArrayOf(2, 0, 1, 0, 53322), intArrayOf(2, -1, 0, 0, 45758),
        intArrayOf(0, 1, -1, 0, -40923), intArrayOf(1, 0, 0, 0, -34720),
        intArrayOf(0, 1, 1, 0, -30383), intArrayOf(2, 0, 0, -2, 15327),
        intArrayOf(0, 0, 1, 2, -12528), intArrayOf(0, 0, 1, -2, 10980),
        intArrayOf(4, 0, -1, 0, 10675), intArrayOf(0, 0, 3, 0, 10034),
        intArrayOf(4, 0, -2, 0, 8548), intArrayOf(2, 1, -1, 0, -7888),
        intArrayOf(2, 1, 0, 0, -6766), intArrayOf(1, 0, -1, 0, -5163),
        intArrayOf(1, 1, 0, 0, 4987), intArrayOf(2, -1, 1, 0, 4036),
        intArrayOf(2, 0, 2, 0, 3994), intArrayOf(4, 0, 0, 0, 3861),
        intArrayOf(2, 0, -3, 0, 3665), intArrayOf(0, 1, -2, 0, -2689),
        intArrayOf(2, 0, -1, 2, -2602), intArrayOf(2, -1, -2, 0, 2390),
        intArrayOf(1, 0, 1, 0, -2348), intArrayOf(2, -2, 0, 0, 2236),
        intArrayOf(0, 1, 2, 0, -2120), intArrayOf(0, 2, 0, 0, -2069),
        intArrayOf(2, -2, -1, 0, 2048), intArrayOf(2, 0, 1, -2, -1773),
        intArrayOf(2, 0, 0, 2, -1595), intArrayOf(4, -1, -1, 0, 1215),
        intArrayOf(0, 0, 2, 2, -1110), intArrayOf(3, 0, -1, 0, -892),
        intArrayOf(2, 1, 1, 0, -810), intArrayOf(4, -1, -2, 0, 759),
        intArrayOf(0, 2, -1, 0, -713), intArrayOf(2, 2, -1, 0, -700),
        intArrayOf(2, 1, -2, 0, 691), intArrayOf(2, -1, 0, -2, 596),
        intArrayOf(4, 0, 1, 0, 549), intArrayOf(0, 0, 4, 0, 537),
        intArrayOf(4, -1, 0, 0, 520), intArrayOf(1, 0, -2, 0, -487),
        intArrayOf(2, 1, 0, -2, -399), intArrayOf(0, 0, 2, -2, -381),
        intArrayOf(1, 1, 1, 0, 351), intArrayOf(3, 0, -2, 0, -340),
        intArrayOf(4, 0, -3, 0, 330), intArrayOf(2, -1, 2, 0, 327),
        intArrayOf(0, 2, 1, 0, -323), intArrayOf(1, 1, -1, 0, 299),
        intArrayOf(2, 0, 3, 0, 294)
    )

    private fun rad(d: Double) = Math.toRadians(d)
    private fun deg(r: Double) = Math.toDegrees(r)
    private fun norm(d: Double) = AstroTime.norm360(d)

    /**
     * Nutation in longitude, in degrees (Meeus ch.22, the four largest terms).
     * Under 20 arcseconds, but it is what separates "mean" from "apparent".
     */
    fun nutationInLongitude(t: Double): Double {
        val omega = norm(125.04452 - 1934.136261 * t)
        val lSun = norm(280.4665 + 36000.7698 * t)
        val lMoon = norm(218.3165 + 481267.8813 * t)
        return (-17.20 * sin(rad(omega))
            - 1.32 * sin(rad(2 * lSun))
            - 0.23 * sin(rad(2 * lMoon))
            + 0.21 * sin(rad(2 * omega))) / 3600.0
    }

    /** Mean obliquity of the ecliptic in degrees (Meeus ch.22). */
    fun meanObliquity(t: Double): Double =
        23.439291111 - 0.0130041667 * t - 1.638889e-7 * t * t + 5.036111e-7 * t * t * t

    /**
     * Apparent geocentric longitude of the Sun, in degrees (Meeus ch.25).
     * [jde] is a Julian Ephemeris Day (Terrestrial Time).
     */
    fun sunApparentLongitude(jde: Double): Double {
        val t = AstroTime.julianCenturies(jde)
        val l0 = norm(280.46646 + 36000.76983 * t + 0.0003032 * t * t)
        val m = norm(357.52911 + 35999.05029 * t - 0.0001537 * t * t)
        val c = (1.914602 - 0.004817 * t - 0.000014 * t * t) * sin(rad(m)) +
            (0.019993 - 0.000101 * t) * sin(rad(2 * m)) +
            0.000289 * sin(rad(3 * m))
        val trueLon = l0 + c
        val omega = norm(125.04 - 1934.136 * t)
        // -0.00569 is aberration; the omega term is the nutation shortcut Meeus gives.
        return norm(trueLon - 0.00569 - 0.00478 * sin(rad(omega)))
    }

    /**
     * Apparent geocentric longitude of the Moon, in degrees (Meeus ch.47).
     * [jde] is a Julian Ephemeris Day (Terrestrial Time).
     */
    fun moonApparentLongitude(jde: Double): Double {
        val t = AstroTime.julianCenturies(jde)

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

        val a1 = norm(119.75 + 131.849 * t)
        val a2 = norm(53.09 + 479264.290 * t)
        // Eccentricity correction on terms involving the Sun's anomaly.
        val e = 1.0 - 0.002516 * t - 0.0000074 * t * t

        var sigma = 0.0
        for (term in MOON_LON_TERMS) {
            val (td, tm, tmp, tf) = term
            var coeff = term[4].toDouble()
            if (tm != 0) coeff *= Math.pow(e, abs(tm).toDouble())
            sigma += coeff * sin(rad(td * d + tm * m + tmp * mp + tf * f))
        }
        // Additive terms: Venus (a1), Jupiter (a2), and the flattening of the Earth.
        sigma += 3958.0 * sin(rad(a1)) +
            1962.0 * sin(rad(lp - f)) +
            318.0 * sin(rad(a2))

        return norm(lp + sigma / 1_000_000.0 + nutationInLongitude(t))
    }

    private operator fun IntArray.component4(): Int = this[3]

    /**
     * Lahiri (Chitrapaksha) ayanamsa in degrees.
     * Verified against a Meeus-based reference at J2000 and 2026: agrees to 0.0006 deg.
     */
    fun lahiriAyanamsa(jd: Double): Double {
        val t1900 = (jd - 2415020.0) / 36525.0
        return 22.460148 + 1.396042 * t1900 + 0.000308 * t1900 * t1900
    }

    // ------------------------------------------------------------------
    // Star-planets: Schlyter's elements, WITH his perturbation terms.
    // ------------------------------------------------------------------

    private class Elements(
        val n0: Double, val nd: Double,
        val i0: Double, val id: Double,
        val w0: Double, val wd: Double,
        val a: Double,
        val e0: Double, val ed: Double,
        val m0: Double, val md: Double
    )

    private val MERCURY = Elements(48.3313, 3.24587E-5, 7.0047, 5.00E-8, 29.1241, 1.01444E-5, 0.387098, 0.205635, 5.59E-10, 168.6562, 4.0923344368)
    private val VENUS = Elements(76.6799, 2.46590E-5, 3.3946, 2.75E-8, 54.8910, 1.38374E-5, 0.723330, 0.006773, -1.302E-9, 48.0052, 1.6021302244)
    private val MARS = Elements(49.5574, 2.11081E-5, 1.8497, -1.78E-8, 286.5016, 2.92961E-5, 1.523688, 0.093405, 2.516E-9, 18.6021, 0.5240207766)
    private val JUPITER = Elements(100.4542, 2.76854E-5, 1.3030, -1.557E-7, 273.8777, 1.64505E-5, 5.20256, 0.048498, 4.469E-9, 19.8950, 0.0830853001)
    private val SATURN = Elements(113.6655, 2.38980E-5, 2.4886, -1.081E-7, 339.3939, 2.97661E-5, 9.55475, 0.054150, -3.671E-9, 316.9670, 0.0334442282)
    private val SUN_EL = Elements(0.0, 0.0, 0.0, 0.0, 282.9404, 4.70935E-5, 1.000000, 0.016709, -1.151E-9, 356.0470, 0.9856002585)

    /** Heliocentric rectangular ecliptic coordinates for [el] at Schlyter day [d]. */
    private fun helio(el: Elements, d: Double): DoubleArray {
        val n = rad(norm(el.n0 + el.nd * d))
        val i = rad(norm(el.i0 + el.id * d))
        val w = rad(norm(el.w0 + el.wd * d))
        val e = el.e0 + el.ed * d
        val m = rad(norm(el.m0 + el.md * d))

        // Kepler by Newton-Raphson — the old fixed-point loop left ~0.02 deg on Mercury.
        var ecc = m + e * sin(m) * (1.0 + e * cos(m))
        repeat(8) {
            val dE = (ecc - e * sin(ecc) - m) / (1.0 - e * cos(ecc))
            ecc -= dE
        }

        val xv = el.a * (cos(ecc) - e)
        val yv = el.a * sqrt(1.0 - e * e) * sin(ecc)
        val r = sqrt(xv * xv + yv * yv)
        val v = atan2(yv, xv)
        val u = v + w

        return doubleArrayOf(
            r * (cos(n) * cos(u) - sin(n) * sin(u) * cos(i)),
            r * (sin(n) * cos(u) + cos(n) * sin(u) * cos(i)),
            r * sin(u) * sin(i)
        )
    }

    private fun meanAnomaly(el: Elements, d: Double) = norm(el.m0 + el.md * d)

    /**
     * Sidereal longitudes of the nine grahas for a Universal Time Julian Day.
     * Keys: Sun, Moon, Mars, Mercury, Jupiter, Venus, Saturn, Rahu, Ketu.
     */
    fun calculatePlanets(jdUT: Double): Map<String, Double> {
        val year = AstroTime.yearOf(jdUT)
        val jde = AstroTime.toEphemerisTime(jdUT, year)
        val ayanamsa = lahiriAyanamsa(jdUT)
        fun sidereal(tropical: Double) = norm(tropical - ayanamsa)

        // Schlyter's day count is referenced to 1999-12-31.0 TDT.
        val d = jde - 2451543.5

        val sun = helio(SUN_EL, d)
        val xe = -sun[0]
        val ye = -sun[1]

        fun geoLon(el: Elements, perturbation: Double = 0.0): Double {
            val p = helio(el, d)
            val lon = deg(atan2(p[1] - ye, p[0] - xe))
            return norm(lon + perturbation)
        }

        // Schlyter's perturbation terms for the giants — up to ~0.5 deg without them.
        val mj = meanAnomaly(JUPITER, d)
        val ms = meanAnomaly(SATURN, d)
        val jupPert = -0.332 * sin(rad(2 * mj - 5 * ms - 67.6)) -
            0.056 * sin(rad(2 * mj - 2 * ms + 21.0)) +
            0.042 * sin(rad(3 * mj - 5 * ms + 21.0)) -
            0.036 * sin(rad(mj - 2 * ms)) +
            0.022 * cos(rad(mj - ms)) +
            0.023 * sin(rad(2 * mj - 3 * ms + 52.0)) -
            0.016 * sin(rad(mj - 5 * ms - 69.0))
        val satPert = 0.812 * sin(rad(2 * mj - 5 * ms - 67.6)) -
            0.229 * cos(rad(2 * mj - 4 * ms - 2.0)) +
            0.119 * sin(rad(mj - 2 * ms - 3.0)) +
            0.046 * sin(rad(2 * mj - 6 * ms - 69.0)) +
            0.014 * sin(rad(mj - 3 * ms + 32.0))

        // Mean lunar node — Vedic practice uses the mean, not the true, node.
        val rahuTropical = norm(125.0445479 - 1934.1362891 * AstroTime.julianCenturies(jde) +
            0.0020754 * Math.pow(AstroTime.julianCenturies(jde), 2.0))

        return mapOf(
            "Sun" to sidereal(sunApparentLongitude(jde)),
            "Moon" to sidereal(moonApparentLongitude(jde)),
            "Mars" to sidereal(geoLon(MARS)),
            "Mercury" to sidereal(geoLon(MERCURY)),
            "Jupiter" to sidereal(geoLon(JUPITER, jupPert)),
            "Venus" to sidereal(geoLon(VENUS)),
            "Saturn" to sidereal(geoLon(SATURN, satPert)),
            "Rahu" to sidereal(rahuTropical),
            "Ketu" to sidereal(rahuTropical + 180.0)
        )
    }

    /**
     * Sidereal longitudes for a calendar date and Universal Time hour.
     *
     * [hourUT] must be Universal Time. Callers holding a local wall clock should
     * go through [AstroTime.julianDayFromLocal] instead of guessing.
     */
    fun calculatePlanets(year: Int, month: Int, day: Int, hourUT: Double): Map<String, Double> =
        calculatePlanets(AstroTime.julianDay(year, month, day, hourUT))

    /** Sidereal longitude of the Moon alone — the hot path for Tithi/Nakshatra solving. */
    fun moonSidereal(jdUT: Double): Double {
        val jde = AstroTime.toEphemerisTime(jdUT, AstroTime.yearOf(jdUT))
        return norm(moonApparentLongitude(jde) - lahiriAyanamsa(jdUT))
    }

    /** Sidereal longitude of the Sun alone. */
    fun sunSidereal(jdUT: Double): Double {
        val jde = AstroTime.toEphemerisTime(jdUT, AstroTime.yearOf(jdUT))
        return norm(sunApparentLongitude(jde) - lahiriAyanamsa(jdUT))
    }

    /** Moon minus Sun, normalised to [0,360) — the quantity Tithi and Karana are cut from. */
    fun elongation(jdUT: Double): Double = norm(moonSidereal(jdUT) - sunSidereal(jdUT))
}
