package com.example.astro

import com.example.data.model.KundaliChartData
import com.example.data.model.PlanetPosition
import java.util.TimeZone
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

object KundaliCalculator {

    private val RASHI_NAMES_HI = listOf(
        "मेष (Aries)", "वृषभ (Taurus)", "मिथुन (Gemini)", "कर्क (Cancer)",
        "सिंह (Leo)", "कन्या (Virgo)", "तुला (Libra)", "वृश्चिक (Scorpio)",
        "धनु (Sagittarius)", "मकर (Capricorn)", "कुंभ (Aquarius)", "मीन (Pisces)"
    )

    val RASHI_SHORT_HI = listOf(
        "मेष", "वृषभ", "मिथुन", "कर्क", "सिंह", "कन्या", "तुला", "वृश्चिक", "धनु", "मकर", "कुंभ", "मीन"
    )

    private val PLANETS_INFO = listOf(
        Pair("Sun", "सूर्य (Su)"),
        Pair("Moon", "चन्द्र (Mo)"),
        Pair("Mars", "मंगल (Ma)"),
        Pair("Mercury", "बुध (Me)"),
        Pair("Jupiter", "गुरु (Ju)"),
        Pair("Venus", "शुक्र (Ve)"),
        Pair("Saturn", "शनि (Sa)"),
        Pair("Rahu", "राहु (Ra)"),
        Pair("Ketu", "केतु (Ke)")
    )

    val NAKSHATRAS = listOf(
        "अश्विनी", "भरणी", "कृत्तिका", "रोहिणी", "मृगशिरा", "आर्द्रा", "पुनर्वसु", "पुष्य", "अश्लेषा",
        "मघा", "पूर्वाफाल्गुनी", "उत्तराफाल्गुनी", "हस्त", "चित्रा", "स्वाती", "विशाखा", "अनुराधा", "ज्येष्ठा",
        "मूल", "पूर्वाषाढा", "उत्तराषाढा", "श्रवण", "धनिष्ठा", "शतभिषा", "पूर्वाभाद्रपद", "उत्तराभाद्रपद", "रेवती"
    )

    const val NAKSHATRA_SPAN = 360.0 / 27.0

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
    fun generateKundali(birth: BirthData): KundaliChartData {
        val jd = birth.julianDay

        val siderealAscendant = ascendantDegrees(jd, birth.latitude, birth.longitude)
        val ascendantRashiIdx = (siderealAscendant / 30.0).toInt().coerceIn(0, 11)

        val planetDegrees = AstroMath.calculatePlanets(jd)
        // Retrograde is a real direction change, so sample a day either side of birth
        // rather than only forward — a station within the next 24h used to read as direct.
        val degreesBefore = AstroMath.calculatePlanets(jd - 0.5)
        val degreesAfter = AstroMath.calculatePlanets(jd + 0.5)

        val planetPositions = mutableListOf<PlanetPosition>()
        val housePlanetsMap = mutableMapOf<Int, MutableList<String>>()
        for (i in 1..12) housePlanetsMap[i] = mutableListOf()

        PLANETS_INFO.forEach { (en, hi) ->
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

            val shortPlanetName = if (isRetro && en != "Rahu" && en != "Ketu") {
                "${hi.substringBefore(" ")}(व)"
            } else {
                hi.substringBefore(" ")
            }

            planetPositions.add(
                PlanetPosition(
                    planetNameEn = en,
                    planetNameHi = hi,
                    rashiNumber = rashiIdx + 1,
                    rashiNameHi = RASHI_SHORT_HI[rashiIdx],
                    degree = String.format(java.util.Locale.US, "%.2f", degreeInRashi).toDouble(),
                    houseNumber = houseNum,
                    isRetrograde = isRetro,
                    nakshatraHi = NAKSHATRAS[nakshatraIdx]
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
            personName = birth.name,
            dateOfBirth = birth.dateString,
            timeOfBirth = birth.timeString,
            placeOfBirth = birth.placeName,
            ascendantRashiNumber = ascendantRashiIdx + 1,
            ascendantRashiHi = RASHI_NAMES_HI[ascendantRashiIdx],
            moonRashiHi = moonPlanet.rashiNameHi,
            moonNakshatraHi = moonPlanet.nakshatraHi,
            planets = planetPositions,
            housePlanetsMap = housePlanetsMap.mapValues { it.value.toList() },
            currentMahadashaHi = if (currentDasha != null) "${currentDasha.planetHi} (${currentDasha.planetEn})" else "—",
            currentAntardashaHi = if (currentAntardasha != null) "${currentAntardasha.planetHi} (${currentAntardasha.planetEn})" else "—",
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
