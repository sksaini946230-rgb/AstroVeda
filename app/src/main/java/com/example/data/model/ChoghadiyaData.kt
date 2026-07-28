package com.example.data.model

enum class ChoghadiyaType(val nameHi: String, val nameEn: String, val isAuspicious: Boolean, val natureHi: String) {
    AMRIT("अमृत", "Amrit", true, "सर्वोत्तम (Best)"),
    SHUBH("शुभ", "Shubh", true, "उत्तम (Good)"),
    LABH("लाभ", "Labh", true, "लाभकारी (Gainful)"),
    CHAR("चर", "Char", true, "सामान्य/चलायमान (Neutral)"),
    ROG("रोग", "Rog", false, "अशुभ (Bad/Illness)"),
    KAAL("काल", "Kaal", false, "अशुभ (Bad/Loss)"),
    UDVEG("द्वेग", "Udveg", false, "अशुभ (Anxiety)")
}

data class ChoghadiyaSlot(
    val timeSlotString: String,
    val startTime: String,
    val endTime: String,
    val type: ChoghadiyaType,
    val rulerPlanetHi: String,
    val isDay: Boolean
)
