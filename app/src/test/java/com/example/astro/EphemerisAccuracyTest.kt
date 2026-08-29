package com.example.astro

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Golden tests for the ephemeris.
 *
 * These are the tests the suite did not have. Every other astro test in this
 * project asserts self-consistency — same input gives same output, different
 * input gives different output — which is exactly why a Moon that was 1.22
 * degrees wrong survived 56 green tests for months.
 *
 * The expected values come from an independent implementation of Meeus
 * (Astronomical Algorithms 2nd ed., ch.25 for the Sun and ch.47 for the Moon)
 * with the same Lahiri ayanamsa and the same delta-T model. Tolerances are set
 * at 0.02 degrees, roughly 70 arcseconds — an order of magnitude tighter than
 * anything a Panchang reader can perceive, and 60x tighter than the old error.
 *
 * If one of these fails, the ephemeris changed. Do not loosen the tolerance to
 * make it pass.
 */
class EphemerisAccuracyTest {

    private val tol = 0.02

    private data class Golden(
        val y: Int, val m: Int, val d: Int, val hourUT: Double,
        val sun: Double, val moon: Double, val ayanamsa: Double
    )

    private val golden = listOf(
        Golden(2026, 8, 28, 0.0, 130.4994, 308.3839, 24.2288),
        Golden(2026, 1, 1, 0.0, 256.3510, 42.4981, 24.2196),
        Golden(2026, 3, 15, 6.0, 330.4447, 283.5134, 24.2225),
        Golden(2026, 6, 21, 12.0, 65.9200, 150.9907, 24.2262),
        Golden(2000, 1, 1, 12.0, 256.5168, 199.4671, 23.8565),
        Golden(1994, 8, 25, 8.75, 128.1502, 355.5035, 23.7817),
        Golden(2026, 12, 31, 18.0, 255.8362, 177.2083, 24.2336)
    )

    private fun angleDiff(a: Double, b: Double): Double = Math.abs(AstroTime.wrap180(a - b))

    @Test
    fun sunMatchesMeeusReference() {
        for (g in golden) {
            val jd = AstroTime.julianDay(g.y, g.m, g.d, g.hourUT)
            val actual = AstroMath.sunSidereal(jd)
            assertTrue(
                "Sun on ${g.y}-${g.m}-${g.d}: expected ${g.sun}, got $actual",
                angleDiff(actual, g.sun) < tol
            )
        }
    }

    @Test
    fun moonMatchesMeeusReference() {
        for (g in golden) {
            val jd = AstroTime.julianDay(g.y, g.m, g.d, g.hourUT)
            val actual = AstroMath.moonSidereal(jd)
            assertTrue(
                "Moon on ${g.y}-${g.m}-${g.d}: expected ${g.moon}, got $actual",
                angleDiff(actual, g.moon) < tol
            )
        }
    }

    @Test
    fun ayanamsaMatchesLahiri() {
        for (g in golden) {
            val jd = AstroTime.julianDay(g.y, g.m, g.d, g.hourUT)
            assertEquals(g.ayanamsa, AstroMath.lahiriAyanamsa(jd), 0.001)
        }
    }

    @Test
    fun elongationDrivesTithiConsistently() {
        for (g in golden) {
            val jd = AstroTime.julianDay(g.y, g.m, g.d, g.hourUT)
            val expected = AstroTime.norm360(g.moon - g.sun)
            assertTrue(
                "Elongation on ${g.y}-${g.m}-${g.d}",
                angleDiff(AstroMath.elongation(jd), expected) < tol * 2
            )
        }
    }

    /** J2000.0 is 2451545.0 by definition — a sanity check on the JD routine itself. */
    @Test
    fun julianDayMatchesKnownEpochs() {
        assertEquals(2451545.0, AstroTime.julianDay(2000, 1, 1, 12.0), 1e-6)
        assertEquals(2451544.5, AstroTime.julianDay(2000, 1, 1, 0.0), 1e-6)
        assertEquals(2440587.5, AstroTime.julianDay(1970, 1, 1, 0.0), 1e-6)
        // The day the Gregorian calendar began.
        assertEquals(2299160.5, AstroTime.julianDay(1582, 10, 15, 0.0), 1e-6)
    }

    @Test
    fun moonMovesAboutThirteenDegreesPerDay() {
        val jd = AstroTime.julianDay(2026, 5, 10, 0.0)
        val delta = AstroTime.wrap180(AstroMath.moonSidereal(jd + 1.0) - AstroMath.moonSidereal(jd))
        assertTrue("Moon daily motion was $delta", delta in 11.5..15.5)
    }

    @Test
    fun ketuIsAlwaysOppositeRahu() {
        for (g in golden) {
            val p = AstroMath.calculatePlanets(g.y, g.m, g.d, g.hourUT)
            val diff = Math.abs(AstroTime.wrap180(p["Rahu"]!! - p["Ketu"]!!))
            assertEquals(180.0, diff, 1e-6)
        }
    }

    /** Every graha must land inside [0,360). Guards against a stray unnormalised angle. */
    @Test
    fun allLongitudesNormalised() {
        for (g in golden) {
            for ((name, lon) in AstroMath.calculatePlanets(g.y, g.m, g.d, g.hourUT)) {
                assertTrue("$name out of range: $lon", lon >= 0.0 && lon < 360.0)
            }
        }
    }
}
