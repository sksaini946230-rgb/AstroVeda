package com.example.data.model

import com.example.util.LanguageManager

enum class ChoghadiyaType(
    val nameHi: String,
    val nameEn: String,
    val isAuspicious: Boolean,
    val natureHi: String,
    val natureEn: String
) {
    AMRIT("अमृत", "Amrit", true, "सर्वोत्तम", "Best"),
    SHUBH("शुभ", "Shubh", true, "उत्तम", "Good"),
    LABH("लाभ", "Labh", true, "लाभकारी", "Gainful"),
    CHAR("चर", "Char", true, "सामान्य", "Neutral"),
    ROG("रोग", "Rog", false, "अशुभ — रोग", "Avoid — illness"),
    KAAL("काल", "Kaal", false, "अशुभ — हानि", "Avoid — loss"),
    // The Hindi name was misspelt "द्वेग"; the word is उद्वेग.
    UDVEG("उद्वेग", "Udveg", false, "अशुभ — बेचैनी", "Avoid — unease");

    val nameLocal: String get() = LanguageManager.getString(nameHi, nameEn)
    val natureLocal: String get() = LanguageManager.getString(natureHi, natureEn)
}

data class ChoghadiyaSlot(
    val timeSlotString: String,
    val startTime: String,
    val endTime: String,
    val type: ChoghadiyaType,
    val rulerPlanetHi: String,
    val isDay: Boolean
)
