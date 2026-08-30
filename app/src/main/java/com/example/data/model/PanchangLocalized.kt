package com.example.data.model

import com.example.util.LanguageManager

/**
 * Language-aware readers for the Panchang and chart models.
 *
 * Both data classes have carried Hindi and English fields all along, but the
 * calculators wrote the same combined Hindi string into both — so the English
 * halves were dead and every screen read the Hindi one directly. Now that the
 * fields hold what they claim to, these pick the right one, and a call site
 * changes by a single word: `panchang.tithiHindi` becomes `panchang.tithiLocal`.
 *
 * Kept out of the data classes themselves so those stay plain data with no
 * dependency on the language state.
 */

private fun pick(hi: String, en: String) =
    LanguageManager.getString(hi, en.ifBlank { hi })

val PanchangData.tithiLocal: String get() = pick(tithiHindi, tithi)
val PanchangData.nakshatraLocal: String get() = pick(nakshatraHindi, nakshatra)
val PanchangData.yogaLocal: String get() = pick(yogaHindi, yoga)
val PanchangData.karanaLocal: String get() = pick(karanHindi, karan)
val PanchangData.masaLocal: String get() = pick(masaNameHindi, masaName)
val PanchangData.pakshaLocal: String get() = pick(pakshaHindi, paksha)
val PanchangData.varaLocal: String get() = pick(dayOfWeekHindi, dayOfWeek)

val PlanetPosition.rashiLocal: String get() = pick(rashiNameHi, rashiNameEn)
val PlanetPosition.nakshatraLocal: String get() = pick(nakshatraHi, nakshatraEn)
val PlanetPosition.planetLocal: String get() = pick(planetNameHi, planetNameEn)

val KundaliChartData.ascendantLocal: String get() = pick(ascendantRashiHi, ascendantRashiEn)
val KundaliChartData.moonRashiLocal: String get() = pick(moonRashiHi, moonRashiEn)
val KundaliChartData.moonNakshatraLocal: String get() = pick(moonNakshatraHi, moonNakshatraEn)
val KundaliChartData.mahadashaLocal: String get() = pick(currentMahadashaHi, currentMahadashaEn)
val KundaliChartData.antardashaLocal: String get() = pick(currentAntardashaHi, currentAntardashaEn)

val DashaPeriod.planetLocal: String get() = pick(planetHi, planetEn)
val AntardashaPeriod.planetLocal: String get() = pick(planetHi, planetEn)

/**
 * The headline date. PanchangData stores only the English form
 * ("Sunday, 30 August 2026"); in Hindi mode that sat above a Hindi vara and
 * paksha line and read as half-translated. Rebuilt here from the parts the
 * data already carries, so nothing new has to be cached.
 */
val PanchangData.dateLocal: String
    get() {
        if (!LanguageManager.isHindi) return dateString
        val parts = dateString.split(" ")
        if (parts.size < 4) return dateString
        val day = parts[1]
        val monthIdx = MONTH_INDEX[parts[2]] ?: return dateString
        val year = parts[3]
        return "$dayOfWeekHindi, $day ${com.example.astro.AstroNames.GREGORIAN_MONTH_HI[monthIdx]} $year"
    }

private val MONTH_INDEX = listOf(
    "January", "February", "March", "April", "May", "June",
    "July", "August", "September", "October", "November", "December"
).withIndex().associate { (i, m) -> m to i }
