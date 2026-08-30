package com.example.data.model

data class PlanetPosition(
    val planetNameEn: String, // Sun, Moon, Mars, Mercury, Jupiter, Venus, Saturn, Rahu, Ketu
    val planetNameHi: String, // सूर्य, चन्द्र, मंगल, बुध, गुरु, शुक्र, शनि, राहु, केतु
    val rashiNumber: Int, // 1 to 12
    val rashiNameHi: String, // मेष, वृषभ...
    val rashiNameEn: String = "", // Aries, Taurus...
    val degree: Double, // 0.0 to 29.9
    val houseNumber: Int, // 1 to 12
    val isRetrograde: Boolean = false,
    val nakshatraHi: String,
    val nakshatraEn: String = ""
)

data class KundaliChartData(
    val personName: String,
    val dateOfBirth: String,
    val timeOfBirth: String,
    val placeOfBirth: String,
    val ascendantRashiNumber: Int, // Lagna Rashi (1 to 12)
    val ascendantRashiHi: String,
    val ascendantRashiEn: String = "",
    val moonRashiHi: String,
    val moonRashiEn: String = "",
    val moonNakshatraHi: String,
    val moonNakshatraEn: String = "",
    val planets: List<PlanetPosition>,
    // House 1..12 -> language-neutral planet tokens ("Sun", "Mars|R").
    // Render them with AstroNames.houseGlyph(), which picks the reader's script;
    // storing the Hindi here froze a chart into whatever language drew it.
    val housePlanetsMap: Map<Int, List<String>>,
    val currentMahadashaHi: String,
    val currentMahadashaEn: String = "",
    val currentAntardashaHi: String,
    val currentAntardashaEn: String = "",
    val dashaTimeline: List<DashaPeriod>
)

data class AntardashaPeriod(
    val planetHi: String,
    val planetEn: String,
    val startDate: String,
    val endDate: String,
    val durationMonths: Double,
    val isCurrent: Boolean = false
)

data class DashaPeriod(
    val planetHi: String,
    val planetEn: String,
    val startDate: String,
    val endDate: String,
    val durationYears: Double,
    val isCurrent: Boolean = false,
    val antardashas: List<AntardashaPeriod> = emptyList()
)
