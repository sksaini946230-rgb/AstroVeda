package com.example.data.model

import com.example.util.LanguageManager
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
) {
    /**
     * The name in whichever language the app is showing.
     *
     * Both names are carried, and two screens still reached for the Hindi one
     * directly — so an English user was told "Current City: जयपुर" in
     * onboarding and saw "जयपुर (Rajasthan)" in Settings. Reading the field by
     * name is what makes that easy to do by accident; this is the same shape as
     * ChoghadiyaType.nameLocal, and the thing to use.
     *
     * A city resolved from GPS has no Hindi name — the geocoder does not give
     * one — so both fields hold the same string there and this returns it
     * either way.
     */
    val nameLocal: String get() = LanguageManager.getString(cityNameHindi, cityName)
}
