package com.example.astro

import com.example.data.model.AntardashaPeriod
import com.example.data.model.DashaPeriod
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.roundToInt

data class DashaPlanetInfo(
    val nameHi: String,
    val nameEn: String,
    val durationYears: Double
)

data class NakshatraInfo(
    val index: Int,
    val nameHi: String,
    val lordNameHi: String,
    val lordNameEn: String,
    val degreeInNakshatra: Double,
    val fractionElapsed: Double,
    val fractionRemaining: Double
)

data class VimshottariDashaResult(
    val nakshatraInfo: NakshatraInfo,
    val balanceAtBirthYears: Double,
    val balanceAtBirthFormatted: String,
    val mahadashas: List<DashaPeriod>,
    val currentMahadasha: DashaPeriod?,
    val currentAntardasha: AntardashaPeriod?
)

object VimshottariDashaCalculator {

    const val NAKSHATRA_SPAN_DEG = 360.0 / 27.0 // 13.333333333333334 degrees (13° 20')
    const val TOTAL_VIMSHOTTARI_YEARS = 120.0
    const val DAYS_PER_YEAR = 365.2425

    val VIMSHOTTARI_PLANETS = listOf(
        DashaPlanetInfo("केतु", "Ketu", 7.0),
        DashaPlanetInfo("शुक्र", "Venus", 20.0),
        DashaPlanetInfo("सूर्य", "Sun", 6.0),
        DashaPlanetInfo("चन्द्र", "Moon", 10.0),
        DashaPlanetInfo("मंगल", "Mars", 7.0),
        DashaPlanetInfo("राहु", "Rahu", 18.0),
        DashaPlanetInfo("गुरु", "Jupiter", 16.0),
        DashaPlanetInfo("शनि", "Saturn", 19.0),
        DashaPlanetInfo("बुध", "Mercury", 17.0)
    )

    val NAKSHATRA_NAMES_HI = listOf(
        "अश्विनी", "भरणी", "कृत्तिका", "रोहिणी", "मृगशिरा", "आर्द्रा",
        "पुनर्वसु", "पुष्य", "आश्लेषा", "मघा", "पूर्वाफाल्गुनी", "उत्तराफाल्गुनी",
        "हस्त", "चित्रा", "स्वाती", "विशाखा", "अनुराधा", "ज्येष्ठा",
        "मूल", "पूर्वाषाढा", "उत्तराषाढा", "श्रवण", "धनिष्ठा", "शतभिषा",
        "पूर्वाभाद्रपद", "उत्तराभाद्रपद", "रेवती"
    )

    /**
     * Calculates Nakshatra info from Moon's absolute longitude (0..360 degrees).
     */
    fun getNakshatraInfo(moonLongitude: Double): NakshatraInfo {
        val normalizedLon = ((moonLongitude % 360.0) + 360.0) % 360.0
        val index = (normalizedLon / NAKSHATRA_SPAN_DEG).toInt().coerceIn(0, 26)
        val lordIdx = index % 9
        val planet = VIMSHOTTARI_PLANETS[lordIdx]

        val degreeInNakshatra = normalizedLon % NAKSHATRA_SPAN_DEG
        val fractionElapsed = (degreeInNakshatra / NAKSHATRA_SPAN_DEG).coerceIn(0.0, 1.0)
        val fractionRemaining = (1.0 - fractionElapsed).coerceIn(0.0, 1.0)

        return NakshatraInfo(
            index = index,
            nameHi = NAKSHATRA_NAMES_HI[index],
            lordNameHi = planet.nameHi,
            lordNameEn = planet.nameEn,
            degreeInNakshatra = degreeInNakshatra,
            fractionElapsed = fractionElapsed,
            fractionRemaining = fractionRemaining
        )
    }

    /**
     * Calculates full Vimshottari Dasha timeline (Mahadashas and Antardashas)
     * based on Moon's absolute longitude and birth date.
     */
    fun calculateVimshottariDasha(
        moonLongitude: Double,
        birthDateStr: String,
        currentTimeMs: Long = System.currentTimeMillis()
    ): VimshottariDashaResult {
        val nakshatra = getNakshatraInfo(moonLongitude)
        val startPlanetIdx = nakshatra.index % 9
        val birthPlanet = VIMSHOTTARI_PLANETS[startPlanetIdx]

        val balanceAtBirthYears = birthPlanet.durationYears * nakshatra.fractionRemaining
        val balanceFormatted = formatDurationYears(balanceAtBirthYears)

        val birthCal = parseBirthDate(birthDateStr)
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US)

        val mahadashas = mutableListOf<DashaPeriod>()
        val runningCal = birthCal.clone() as Calendar

        var activeMahadasha: DashaPeriod? = null
        var activeAntardasha: AntardashaPeriod? = null

        // Generate Mahadashas for a full 120-year Vimshottari cycle (9 planets)
        for (i in 0 until 9) {
            val planetIdx = (startPlanetIdx + i) % 9
            val planet = VIMSHOTTARI_PLANETS[planetIdx]

            val isBirthDasha = (i == 0)
            val durationYears = if (isBirthDasha) balanceAtBirthYears else planet.durationYears

            val startDateMs = runningCal.timeInMillis
            val startDateStr = dateFormat.format(runningCal.time)

            // Add duration in days
            val durationDays = (durationYears * DAYS_PER_YEAR).roundToInt()
            runningCal.add(Calendar.DAY_OF_YEAR, durationDays)

            val endDateMs = runningCal.timeInMillis
            val endDateStr = dateFormat.format(runningCal.time)

            val isCurrentMahadasha = currentTimeMs in startDateMs..endDateMs

            // Calculate Antardashas within this Mahadasha
            val antardashas = calculateAntardashas(
                mahadashaPlanetIdx = planetIdx,
                mahadashaStartCal = parseDateToCal(startDateStr, dateFormat),
                isBirthMahadasha = isBirthDasha,
                birthCal = birthCal,
                nakshatraFractionElapsed = nakshatra.fractionElapsed,
                currentTimeMs = currentTimeMs,
                dateFormat = dateFormat
            )

            val dashaPeriod = DashaPeriod(
                planetHi = planet.nameHi,
                planetEn = planet.nameEn,
                startDate = startDateStr,
                endDate = endDateStr,
                durationYears = roundToOneDecimal(durationYears),
                isCurrent = isCurrentMahadasha,
                antardashas = antardashas
            )

            if (isCurrentMahadasha) {
                activeMahadasha = dashaPeriod
                activeAntardasha = antardashas.find { it.isCurrent }
            }

            mahadashas.add(dashaPeriod)
        }

        return VimshottariDashaResult(
            nakshatraInfo = nakshatra,
            balanceAtBirthYears = balanceAtBirthYears,
            balanceAtBirthFormatted = balanceFormatted,
            mahadashas = mahadashas,
            currentMahadasha = activeMahadasha ?: mahadashas.firstOrNull(),
            currentAntardasha = activeAntardasha
        )
    }

    /**
     * Calculates 9 Antardashas (Bhuktis) for a Mahadasha.
     */
    private fun calculateAntardashas(
        mahadashaPlanetIdx: Int,
        mahadashaStartCal: Calendar,
        isBirthMahadasha: Boolean,
        birthCal: Calendar,
        nakshatraFractionElapsed: Double,
        currentTimeMs: Long,
        dateFormat: SimpleDateFormat
    ): List<AntardashaPeriod> {
        val mahadashaPlanet = VIMSHOTTARI_PLANETS[mahadashaPlanetIdx]
        val mDuration = mahadashaPlanet.durationYears
        val antardashaList = mutableListOf<AntardashaPeriod>()

        val runningCal = mahadashaStartCal.clone() as Calendar

        // Calculate theoretical start of the birth Mahadasha before birth (if birth Mahadasha)
        if (isBirthMahadasha && nakshatraFractionElapsed > 0) {
            val elapsedDays = (nakshatraFractionElapsed * mDuration * DAYS_PER_YEAR).roundToInt()
            runningCal.add(Calendar.DAY_OF_YEAR, -elapsedDays)
        }

        for (j in 0 until 9) {
            val subPlanetIdx = (mahadashaPlanetIdx + j) % 9
            val subPlanet = VIMSHOTTARI_PLANETS[subPlanetIdx]

            val antardashaYears = (mDuration * subPlanet.durationYears) / TOTAL_VIMSHOTTARI_YEARS
            val antardashaDays = (antardashaYears * DAYS_PER_YEAR).roundToInt()

            val subStartMs = runningCal.timeInMillis
            val subStartStr = dateFormat.format(runningCal.time)

            runningCal.add(Calendar.DAY_OF_YEAR, antardashaDays)

            val subEndMs = runningCal.timeInMillis
            val subEndStr = dateFormat.format(runningCal.time)

            // For birth Mahadasha, skip sub-periods that ended strictly before birth
            if (isBirthMahadasha && subEndMs < birthCal.timeInMillis) {
                continue
            }

            val adjustedStartStr = if (isBirthMahadasha && subStartMs < birthCal.timeInMillis) {
                dateFormat.format(birthCal.time)
            } else {
                subStartStr
            }

            val effectiveStartMs = if (isBirthMahadasha && subStartMs < birthCal.timeInMillis) {
                birthCal.timeInMillis
            } else {
                subStartMs
            }

            val isCurrent = currentTimeMs in effectiveStartMs..subEndMs
            val durationMonths = roundToOneDecimal(antardashaYears * 12.0)

            antardashaList.add(
                AntardashaPeriod(
                    planetHi = subPlanet.nameHi,
                    planetEn = subPlanet.nameEn,
                    startDate = adjustedStartStr,
                    endDate = subEndStr,
                    durationMonths = durationMonths,
                    isCurrent = isCurrent
                )
            )
        }

        return antardashaList
    }

    private fun parseBirthDate(dobStr: String): Calendar {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        cal.clear()

        val formats = listOf(
            SimpleDateFormat("yyyy-MM-dd", Locale.US),
            SimpleDateFormat("yyyy-M-d", Locale.US),
            SimpleDateFormat("dd/MM/yyyy", Locale.US),
            SimpleDateFormat("dd-MM-yyyy", Locale.US)
        )

        for (fmt in formats) {
            try {
                val d = fmt.parse(dobStr)
                if (d != null) {
                    cal.time = d
                    return cal
                }
            } catch (_: Exception) {}
        }

        // Fallback parsing year
        val yearMatch = Regex("\\d{4}").find(dobStr)
        val year = yearMatch?.value?.toIntOrNull() ?: 1995
        cal.set(year, Calendar.JANUARY, 1)
        return cal
    }

    private fun parseDateToCal(dateStr: String, dateFormat: SimpleDateFormat): Calendar {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        try {
            val d = dateFormat.parse(dateStr)
            if (d != null) cal.time = d
        } catch (_: Exception) {}
        return cal
    }

    fun formatDurationYears(yearsDouble: Double): String {
        val years = yearsDouble.toInt()
        val remainingMonthsDouble = (yearsDouble - years) * 12.0
        val months = remainingMonthsDouble.toInt()
        val days = ((remainingMonthsDouble - months) * 30.4375).roundToInt()

        val parts = mutableListOf<String>()
        if (years > 0) parts.add("$years वर्ष")
        if (months > 0) parts.add("$months महीने")
        if (days > 0 || parts.isEmpty()) parts.add("$days दिन")

        return parts.joinToString(", ")
    }

    private fun roundToOneDecimal(value: Double): Double {
        return (value * 10.0).roundToInt() / 10.0
    }
}
