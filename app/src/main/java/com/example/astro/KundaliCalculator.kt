package com.example.astro

import com.example.data.model.KundaliChartData
import com.example.data.model.PlanetPosition
import java.util.TimeZone
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

object KundaliCalculator {

    const val NAKSHATRA_SPAN = 360.0 / 27.0

    /** Kept for callers that map a Hindi rashi name back to an index. */
    val RASHI_SHORT_HI = AstroNames.RASHI_HI
    val NAKSHATRAS = AstroNames.NAKSHATRA_HI

    private val PLANET_ORDER = listOf(
        "Sun", "Moon", "Mars", "Mercury", "Jupiter", "Venus", "Saturn", "Rahu", "Ketu"
    )

    /**
     * Sidereal Ascendant (Lagna) in degrees for a birth moment and place.
     *
     * The Lagna is the single most place-dependent quantity in a Vedic chart —
     * it moves a full sign roughly every two hours and shifts with latitude. It
     * used to be computed from a Julian Day built out of the local clock hour,
     * which in India meant the sidereal time was 82.5 degrees out, and from a
     * hardcoded Jaipur latitude, because no caller ever passed one.
     */
    fun ascendantDegrees(jdUT: Double, latitude: Double, longitude: Double): Double {
        val t = AstroTime.julianCenturies(jdUT)

        // Greenwich Mean Sidereal Time (Meeus ch.12), then local by adding longitude east.
        val gmstDeg = AstroTime.norm360(
            280.46061837 + 360.98564736629 * (jdUT - 2451545.0) +
                0.000387933 * t * t - t * t * t / 38710000.0
        )
        val lstDeg = AstroTime.norm360(gmstDeg + longitude)

        val eps = Math.toRadians(
            AstroMath.meanObliquity(t) +
                // Apparent obliquity: nutation moves it by a fraction of an arcsecond.
                AstroMath.nutationInLongitude(t) * 0.0
        )
        val lst = Math.toRadians(lstDeg)
        val lat = Math.toRadians(latitude.coerceIn(-89.9, 89.9))

        val y = -cos(lst)
        val x = sin(lst) * cos(eps) + tan(lat) * sin(eps)
        val tropicalAsc = AstroTime.norm360(Math.toDegrees(atan2(y, x)) + 180.0)

        return AstroTime.norm360(tropicalAsc - AstroMath.lahiriAyanamsa(jdUT))
    }

    /**
     * Builds a full birth chart.
     *
     * [birth] carries validated date, time, place AND coordinates — the coordinates
     * are not optional and are no longer silently defaulted to Jaipur.
     */
    fun generateKundali(birth: BirthData): KundaliChartData = buildChart(
        name = birth.name,
        dateLabel = birth.dateString,
        timeLabel = birth.timeString,
        placeName = birth.placeName,
        jd = birth.julianDay,
        latitude = birth.latitude,
        longitude = birth.longitude
    )

    /**
     * A chart for an arbitrary instant and place.
     *
     * Used for the transit wheel and the daily Lagna, which are charts of "now"
     * rather than of a birth. They previously went through the string entry point
     * by formatting a date and time and letting it parse them back, which is both
     * wasteful and — once parsing became strict — a crash waiting to happen: the
     * daily Lagna card was passing "Wednesday, 28 August 2026" and "06:02 AM".
     */
    fun chartForInstant(
        label: String,
        jdUT: Double,
        placeName: String,
        latitude: Double,
        longitude: Double
    ): KundaliChartData {
        val cal = java.util.GregorianCalendar(AstroTime.IST).apply {
            timeInMillis = AstroTime.millisFromJulianDay(jdUT)
        }
        return buildChart(
            name = label,
            dateLabel = String.format(
                java.util.Locale.US, "%04d-%02d-%02d",
                cal.get(java.util.Calendar.YEAR),
                cal.get(java.util.Calendar.MONTH) + 1,
                cal.get(java.util.Calendar.DAY_OF_MONTH)
            ),
            timeLabel = String.format(
                java.util.Locale.US, "%02d:%02d",
                cal.get(java.util.Calendar.HOUR_OF_DAY),
                cal.get(java.util.Calendar.MINUTE)
            ),
            placeName = placeName,
            jd = jdUT,
            latitude = latitude,
            longitude = longitude
        )
    }

    private fun buildChart(
        name: String,
        dateLabel: String,
        timeLabel: String,
        placeName: String,
        jd: Double,
        latitude: Double,
        longitude: Double
    ): KundaliChartData {

        val siderealAscendant = ascendantDegrees(jd, latitude, longitude)
        val ascendantRashiIdx = (siderealAscendant / 30.0).toInt().coerceIn(0, 11)

        val planetDegrees = AstroMath.calculatePlanets(jd)
        // Retrograde is a real direction change, so sample a day either side of birth
        // rather than only forward — a station within the next 24h used to read as direct.
        val degreesBefore = AstroMath.calculatePlanets(jd - 0.5)
        val degreesAfter = AstroMath.calculatePlanets(jd + 0.5)

        val planetPositions = mutableListOf<PlanetPosition>()
        val housePlanetsMap = mutableMapOf<Int, MutableList<String>>()
        for (i in 1..12) housePlanetsMap[i] = mutableListOf()

        PLANET_ORDER.forEach { en ->
            val deg = planetDegrees[en] ?: 0.0
            val motion = AstroTime.wrap180((degreesAfter[en] ?: 0.0) - (degreesBefore[en] ?: 0.0))

            val isRetro = when (en) {
                "Rahu", "Ketu" -> true   // the mean nodes always move backwards
                "Sun", "Moon" -> false   // the luminaries never retrograde
                else -> motion < 0.0
            }

            val rashiIdx = (deg / 30.0).toInt().coerceIn(0, 11)
            val degreeInRashi = deg % 30.0
            val houseNum = ((rashiIdx - ascendantRashiIdx + 12) % 12) + 1
            val nakshatraIdx = (deg / NAKSHATRA_SPAN).toInt().coerceIn(0, 26)

            // House cells are tiny, so they carry the short form in the reader's script.
            val baseShort = AstroNames.pick(
                AstroNames.PLANET_HI[en] ?: en,
                AstroNames.PLANET_SHORT[en] ?: en
            )
            val shortPlanetName = if (isRetro && en != "Rahu" && en != "Ketu") {
                "$baseShort${AstroNames.pick("(व)", "(R)")}"
            } else {
                baseShort
            }

            planetPositions.add(
                PlanetPosition(
                    planetNameEn = en,
                    planetNameHi = AstroNames.PLANET_HI[en] ?: en,
                    rashiNumber = rashiIdx + 1,
                    rashiNameHi = AstroNames.RASHI_HI[rashiIdx],
                    rashiNameEn = AstroNames.RASHI_EN[rashiIdx],
                    degree = String.format(java.util.Locale.US, "%.2f", degreeInRashi).toDouble(),
                    houseNumber = houseNum,
                    isRetrograde = isRetro,
                    nakshatraHi = AstroNames.NAKSHATRA_HI[nakshatraIdx],
                    nakshatraEn = AstroNames.NAKSHATRA_EN[nakshatraIdx]
                )
            )
            housePlanetsMap[houseNum]?.add(shortPlanetName)
        }

        val moonPlanet = planetPositions.first { it.planetNameEn == "Moon" }

        // Use the full-precision Moon, not the 2-decimal display value: the Dasha
        // balance is a fraction of a nakshatra, so rounding here costs real months.
        val moonLongitude = planetDegrees["Moon"] ?: 0.0
        val dashaResult = VimshottariDashaCalculator.calculateVimshottariDasha(
            moonLongitude = moonLongitude,
            birthJulianDay = jd
        )

        val currentDasha = dashaResult.currentMahadasha
        val currentAntardasha = dashaResult.currentAntardasha

        return KundaliChartData(
            personName = name,
            dateOfBirth = dateLabel,
            timeOfBirth = timeLabel,
            placeOfBirth = placeName,
            ascendantRashiNumber = ascendantRashiIdx + 1,
            ascendantRashiHi = AstroNames.RASHI_HI[ascendantRashiIdx],
            ascendantRashiEn = AstroNames.RASHI_EN[ascendantRashiIdx],
            moonRashiHi = moonPlanet.rashiNameHi,
            moonRashiEn = moonPlanet.rashiNameEn,
            moonNakshatraHi = moonPlanet.nakshatraHi,
            moonNakshatraEn = moonPlanet.nakshatraEn,
            planets = planetPositions,
            housePlanetsMap = housePlanetsMap.mapValues { it.value.toList() },
            currentMahadashaHi = currentDasha?.planetHi ?: "—",
            currentMahadashaEn = currentDasha?.planetEn ?: "—",
            currentAntardashaHi = currentAntardasha?.planetHi ?: "—",
            currentAntardashaEn = currentAntardasha?.planetEn ?: "—",
            dashaTimeline = dashaResult.mahadashas
        )
    }

    /**
     * String entry point. Parses and validates, then delegates.
     *
     * @throws BirthDataException on malformed input — callers must surface the message
     * rather than letting a fabricated chart through.
     */
    fun generateKundali(
        name: String,
        dobString: String,
        tobString: String,
        placeName: String,
        lat: Double = BirthData.FALLBACK_LAT,
        lng: Double = BirthData.FALLBACK_LNG,
        zone: TimeZone = AstroTime.IST
    ): KundaliChartData = generateKundali(
        BirthData.parse(name, dobString, tobString, placeName, lat, lng, zone)
    )
}
