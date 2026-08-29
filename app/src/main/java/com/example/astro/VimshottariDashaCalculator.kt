package com.example.astro

import com.example.data.model.AntardashaPeriod
import com.example.data.model.DashaPeriod
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.roundToInt
import kotlin.math.roundToLong

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

/**
 * Vimshottari Dasha.
 *
 * The sequence, lordships and balance formula here were already correct. Two
 * things were not: the timeline started from the birth *date* with the birth
 * *time* discarded, and every period boundary was rounded to a whole day and
 * round-tripped through a "dd/MM/yyyy" string, so the error compounded down the
 * 120-year cycle. Everything below now runs in milliseconds from the exact birth
 * moment, and formats to a string only at the very end.
 */
object VimshottariDashaCalculator {

    const val NAKSHATRA_SPAN_DEG = 360.0 / 27.0
    const val TOTAL_VIMSHOTTARI_YEARS = 120.0

    /** Vimshottari uses the sidereal solar year. */
    const val DAYS_PER_YEAR = 365.256363

    private const val MS_PER_DAY = 86_400_000.0

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

    fun getNakshatraInfo(moonLongitude: Double): NakshatraInfo {
        val normalizedLon = AstroTime.norm360(moonLongitude)
        val index = (normalizedLon / NAKSHATRA_SPAN_DEG).toInt().coerceIn(0, 26)
        val planet = VIMSHOTTARI_PLANETS[index % 9]

        val degreeInNakshatra = normalizedLon - index * NAKSHATRA_SPAN_DEG
        val fractionElapsed = (degreeInNakshatra / NAKSHATRA_SPAN_DEG).coerceIn(0.0, 1.0)

        return NakshatraInfo(
            index = index,
            nameHi = NAKSHATRA_NAMES_HI[index],
            lordNameHi = planet.nameHi,
            lordNameEn = planet.nameEn,
            degreeInNakshatra = degreeInNakshatra,
            fractionElapsed = fractionElapsed,
            fractionRemaining = (1.0 - fractionElapsed).coerceIn(0.0, 1.0)
        )
    }

    /**
     * Full 120-year timeline from the exact birth moment.
     *
     * @param moonLongitude sidereal Moon longitude at birth, full precision
     * @param birthJulianDay Julian Day (UT) of the birth moment
     */
    fun calculateVimshottariDasha(
        moonLongitude: Double,
        birthJulianDay: Double,
        currentTimeMs: Long = System.currentTimeMillis()
    ): VimshottariDashaResult {
        val nakshatra = getNakshatraInfo(moonLongitude)
        val startPlanetIdx = nakshatra.index % 9
        val birthPlanet = VIMSHOTTARI_PLANETS[startPlanetIdx]

        val balanceAtBirthYears = birthPlanet.durationYears * nakshatra.fractionRemaining
        val birthMs = AstroTime.millisFromJulianDay(birthJulianDay)

        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US).apply {
            timeZone = AstroTime.IST
        }

        val mahadashas = mutableListOf<DashaPeriod>()
        var activeMahadasha: DashaPeriod? = null
        var activeAntardasha: AntardashaPeriod? = null

        // The birth mahadasha notionally began before birth; that notional start is
        // what the antardashas must be measured from, or every sub-period is skewed.
        val birthDashaNotionalStartMs =
            birthMs - yearsToMs(birthPlanet.durationYears * nakshatra.fractionElapsed)

        var runningMs = birthMs

        for (i in 0 until 9) {
            val planetIdx = (startPlanetIdx + i) % 9
            val planet = VIMSHOTTARI_PLANETS[planetIdx]
            val isBirthDasha = (i == 0)

            val durationYears = if (isBirthDasha) balanceAtBirthYears else planet.durationYears
            val startMs = runningMs
            val endMs = startMs + yearsToMs(durationYears)
            runningMs = endMs

            val antardashaAnchorMs = if (isBirthDasha) birthDashaNotionalStartMs else startMs

            val antardashas = calculateAntardashas(
                mahadashaPlanetIdx = planetIdx,
                anchorMs = antardashaAnchorMs,
                birthMs = birthMs,
                isBirthMahadasha = isBirthDasha,
                currentTimeMs = currentTimeMs,
                dateFormat = dateFormat
            )

            val dashaPeriod = DashaPeriod(
                planetHi = planet.nameHi,
                planetEn = planet.nameEn,
                startDate = dateFormat.format(Date(startMs)),
                endDate = dateFormat.format(Date(endMs)),
                durationYears = roundToOneDecimal(durationYears),
                isCurrent = currentTimeMs in startMs until endMs,
                antardashas = antardashas
            )

            if (dashaPeriod.isCurrent) {
                activeMahadasha = dashaPeriod
                activeAntardasha = antardashas.find { it.isCurrent }
            }

            mahadashas.add(dashaPeriod)
        }

        return VimshottariDashaResult(
            nakshatraInfo = nakshatra,
            balanceAtBirthYears = balanceAtBirthYears,
            balanceAtBirthFormatted = formatDurationYears(balanceAtBirthYears),
            mahadashas = mahadashas,
            // A chart older than 120 years falls off the end of the cycle; report
            // nothing current rather than pretending the first period is running.
            currentMahadasha = activeMahadasha,
            currentAntardasha = activeAntardasha
        )
    }

    private fun calculateAntardashas(
        mahadashaPlanetIdx: Int,
        anchorMs: Long,
        birthMs: Long,
        isBirthMahadasha: Boolean,
        currentTimeMs: Long,
        dateFormat: SimpleDateFormat
    ): List<AntardashaPeriod> {
        val mahadashaPlanet = VIMSHOTTARI_PLANETS[mahadashaPlanetIdx]
        val mDuration = mahadashaPlanet.durationYears
        val result = mutableListOf<AntardashaPeriod>()

        var runningMs = anchorMs

        for (j in 0 until 9) {
            val subPlanet = VIMSHOTTARI_PLANETS[(mahadashaPlanetIdx + j) % 9]
            val antardashaYears = (mDuration * subPlanet.durationYears) / TOTAL_VIMSHOTTARI_YEARS

            val subStartMs = runningMs
            val subEndMs = subStartMs + yearsToMs(antardashaYears)
            runningMs = subEndMs

            // Sub-periods that finished before the native was born are not theirs.
            if (isBirthMahadasha && subEndMs <= birthMs) continue

            val effectiveStartMs = if (isBirthMahadasha) maxOf(subStartMs, birthMs) else subStartMs

            result.add(
                AntardashaPeriod(
                    planetHi = subPlanet.nameHi,
                    planetEn = subPlanet.nameEn,
                    startDate = dateFormat.format(Date(effectiveStartMs)),
                    endDate = dateFormat.format(Date(subEndMs)),
                    durationMonths = roundToOneDecimal(antardashaYears * 12.0),
                    isCurrent = currentTimeMs in effectiveStartMs until subEndMs
                )
            )
        }

        return result
    }

    private fun yearsToMs(years: Double): Long =
        (years * DAYS_PER_YEAR * MS_PER_DAY).roundToLong()

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

    private fun roundToOneDecimal(value: Double): Double = (value * 10.0).roundToInt() / 10.0

    /** Legacy entry point kept for tests that pass a date string; assumes midnight IST. */
    @Deprecated("Pass the exact birth Julian Day instead — the birth time matters.")
    fun calculateVimshottariDasha(
        moonLongitude: Double,
        birthDateStr: String,
        currentTimeMs: Long = System.currentTimeMillis()
    ): VimshottariDashaResult {
        val parts = birthDateStr.trim().split("-")
        val y = parts.getOrNull(0)?.toIntOrNull() ?: 1995
        val m = parts.getOrNull(1)?.toIntOrNull() ?: 1
        val d = parts.getOrNull(2)?.toIntOrNull() ?: 1
        val jd = AstroTime.julianDayFromLocal(y, m, d, 0, 0, TimeZone.getTimeZone("Asia/Kolkata"))
        return calculateVimshottariDasha(moonLongitude, jd, currentTimeMs)
    }
}
