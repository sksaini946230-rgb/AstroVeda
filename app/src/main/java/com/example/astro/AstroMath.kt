package com.example.astro

import kotlin.math.*

object AstroMath {
    // Calculates Sidereal Longitudes using accurate geocentric ecliptic algorithms with Lahiri Ayanamsa
    fun calculatePlanets(year: Int, month: Int, day: Int, hour: Double): Map<String, Double> {
        val y = if (month <= 2) year - 1 else year
        val m = if (month <= 2) month + 12 else month
        val aVal = y / 100
        val b = 2 - aVal + aVal / 4
        
        val jd = floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + hour / 24.0 + b - 1524.5
        val d = jd - 2451543.5 // Days since 2000 Jan 0.0

        // Lahiri Ayanamsa Calculation
        val t1900 = (jd - 2415020.0) / 36525.0
        val ayanamsa = 22.460148 + 1.396042 * t1900 + 0.000308 * t1900 * t1900

        fun sidereal(deg: Double): Double {
            var s = (deg - ayanamsa) % 360.0
            if (s < 0) s += 360.0
            return s
        }

        // Helper to solve Kepler's equation and get heliocentric ecliptic coordinates (x, y, z)
        fun getHeliocentricCoords(
            N0: Double, Nd: Double,
            i0: Double, id: Double,
            w0: Double, wd: Double,
            a: Double,
            e0: Double, ed: Double,
            M0: Double, Md: Double
        ): Triple<Double, Double, Double> {
            val N = Math.toRadians((N0 + Nd * d) % 360.0)
            val i = Math.toRadians((i0 + id * d) % 360.0)
            val w = Math.toRadians((w0 + wd * d) % 360.0)
            val e = e0 + ed * d
            val M = Math.toRadians((M0 + Md * d) % 360.0)

            // Solve Kepler's equation
            var E = M + e * sin(M)
            for (iter in 1..4) {
                E = M + e * sin(E)
            }

            val xv = a * (cos(E) - e)
            val yv = a * sqrt(1.0 - e * e) * sin(E)

            val r = sqrt(xv * xv + yv * yv)
            val v = atan2(yv, xv)

            val u = v + w

            val x = r * (cos(N) * cos(u) - sin(N) * sin(u) * cos(i))
            val y = r * (sin(N) * cos(u) + cos(N) * sin(u) * cos(i))
            val z = r * sin(u) * sin(i)

            return Triple(x, y, z)
        }

        // 1. Sun (Earth) Heliocentric Coords
        val (xe, ye, _) = getHeliocentricCoords(
            0.0, 0.0,
            0.0, 0.0,
            282.9404, 4.70935E-5,
            1.000000,
            0.016709, -1.151E-9,
            356.0470, 0.9856002585
        )

        // Geocentric Sun coordinates are opposite of Earth's heliocentric coords
        val xs = -xe
        val ys = -ye

        val trueSunLong = Math.toDegrees(atan2(ys, xs)).let { if (it < 0) it + 360.0 else it }

        // Helper to get geocentric longitude of a planet
        fun getGeocentricLong(
            N0: Double, Nd: Double,
            i0: Double, id: Double,
            w0: Double, wd: Double,
            a: Double,
            e0: Double, ed: Double,
            M0: Double, Md: Double
        ): Double {
            val (xp, yp, zp) = getHeliocentricCoords(N0, Nd, i0, id, w0, wd, a, e0, ed, M0, Md)
            // Geocentric = Planet heliocentric - Earth heliocentric
            val xg = xp - xe
            val yg = yp - ye
            var lon = Math.toDegrees(atan2(yg, xg))
            if (lon < 0) lon += 360.0
            return lon
        }

        // 2. Mercury
        val mercuryLong = getGeocentricLong(
            48.3313, 3.24587E-5,
            7.0047, 5.00E-8,
            29.1241, 1.01444E-5,
            0.387098,
            0.205635, 5.59E-10,
            168.6562, 4.0923344368
        )

        // 3. Venus
        val venusLong = getGeocentricLong(
            76.6799, 2.46590E-5,
            3.3946, 2.75E-8,
            54.8910, 1.38374E-5,
            0.723330,
            0.006773, -1.302E-9,
            48.0052, 1.6021302244
        )

        // 4. Mars
        val marsLong = getGeocentricLong(
            49.5574, 2.11081E-5,
            1.8497, -1.78E-8,
            286.5016, 2.92961E-5,
            1.523688,
            0.093405, 2.516E-9,
            18.6021, 0.5240207766
        )

        // 5. Jupiter
        val jupiterLong = getGeocentricLong(
            100.4542, 2.76854E-5,
            1.3030, -1.557E-7,
            273.8777, 1.64505E-5,
            5.20256,
            0.048498, 4.469E-9,
            19.8950, 0.0830853001
        )

        // 6. Saturn
        val saturnLong = getGeocentricLong(
            113.6655, 2.38980E-5,
            2.4886, -1.081E-7,
            339.3939, 2.97661E-5,
            9.55475,
            0.054150, -3.671E-9,
            316.9670, 0.0334442282
        )

        // 7. Moon (Geocentric by default)
        val moonN = Math.toRadians((125.1228 - 0.0529538083 * d) % 360.0)
        val moonI = Math.toRadians(5.1454)
        val moonW = Math.toRadians((318.0634 + 0.1643573223 * d) % 360.0)
        val moonA = 60.2666
        val moonE = 0.054900
        val moonM = Math.toRadians((115.3654 + 13.0649929509 * d) % 360.0)

        var moonEAnon = moonM + moonE * sin(moonM)
        for (iter in 1..4) {
            moonEAnon = moonM + moonE * sin(moonEAnon)
        }

        val moonXv = moonA * (cos(moonEAnon) - moonE)
        val moonYv = moonA * sqrt(1.0 - moonE * moonE) * sin(moonEAnon)
        val moonR = sqrt(moonXv * moonXv + moonYv * moonYv)
        val moonV = atan2(moonYv, moonXv)
        val moonU = moonV + moonW

        val moonX = moonR * (cos(moonN) * cos(moonU) - sin(moonN) * sin(moonU) * cos(moonI))
        val moonY = moonR * (sin(moonN) * cos(moonU) + cos(moonN) * sin(moonU) * cos(moonI))
        var moonLong = Math.toDegrees(atan2(moonY, moonX))
        if (moonLong < 0) moonLong += 360.0

        // 8. Rahu & Ketu
        var rahuLong = (125.1228 - 0.0529538083 * d) % 360.0
        if (rahuLong < 0) rahuLong += 360.0
        val ketuLong = (rahuLong + 180.0) % 360.0

        return mapOf(
            "Sun" to sidereal(trueSunLong),
            "Moon" to sidereal(moonLong),
            "Mars" to sidereal(marsLong),
            "Mercury" to sidereal(mercuryLong),
            "Jupiter" to sidereal(jupiterLong),
            "Venus" to sidereal(venusLong),
            "Saturn" to sidereal(saturnLong),
            "Rahu" to sidereal(rahuLong),
            "Ketu" to sidereal(ketuLong)
        )
    }
}
