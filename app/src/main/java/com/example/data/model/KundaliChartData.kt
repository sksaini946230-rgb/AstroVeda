package com.example.data.model

data class PlanetPosition(
    val planetNameEn: String, // Sun, Moon, Mars, Mercury, Jupiter, Venus, Saturn, Rahu, Ketu
    val planetNameHi: String, // सूर्य, चन्द्र, मंगल, बुध, गुरु, शुक्र, शनि, राहु, केतु
    val rashiNumber: Int, // 1 to 12
    val rashiNameHi: String, // मेष, वृषभ...
    val degree: Double, // 0.0 to 29.9
    val houseNumber: Int, // 1 to 12
    val isRetrograde: Boolean = false,
    val nakshatraHi: String
)

data class KundaliChartData(
    val personName: String,
    val dateOfBirth: String,
    val timeOfBirth: String,
    val placeOfBirth: String,
    val ascendantRashiNumber: Int, // Lagna Rashi (1 to 12)
    val ascendantRashiHi: String,
    val moonRashiHi: String,
    val moonNakshatraHi: String,
    val planets: List<PlanetPosition>,
    val housePlanetsMap: Map<Int, List<String>>, // House 1..12 -> List of planet short names in Hindi
    val currentMahadashaHi: String,
    val currentAntardashaHi: String,
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
