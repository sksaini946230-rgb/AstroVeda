package com.example.data.model

data class PanchangData(
    val dateString: String,
    val dayOfWeek: String, // Var
    val dayOfWeekHindi: String,
    val vikramSamvat: Int,
    val sakaSamvat: Int,
    val masaName: String, // e.g. Shravana, Bhadrapada
    val masaNameHindi: String,
    val paksha: String, // Shukla Paksha / Krishna Paksha
    val pakshaHindi: String,
    val tithi: String,
    val tithiHindi: String,
    val tithiEndTime: String,
    val tithiProgressPercent: Float,
    val nakshatra: String,
    val nakshatraHindi: String,
    val nakshatraEndTime: String,
    val nakshatraPada: Int,
    val yoga: String,
    val yogaHindi: String,
    val karan: String,
    val karanHindi: String,
    val sunrise: String,
    val sunset: String,
    val moonrise: String,
    val moonset: String,
    val rahuKaal: String,
    val gulikaKaal: String,
    val yamaganda: String,
    val abhijitMuhurat: String,
    val brahmaMuhurat: String,
    val sunSign: String,
    val moonSign: String,
    val locationName: String,
    val latitude: Double,
    val longitude: Double,
    val planets: List<PlanetPosition> = emptyList()
)

data class CityLocation(
    val cityName: String,
    val cityNameHindi: String,
    val state: String,
    val latitude: Double,
    val longitude: Double
)
