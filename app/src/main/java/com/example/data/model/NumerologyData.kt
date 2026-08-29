package com.example.data.model

data class NumerologyData(
    val personName: String,
    val dateOfBirth: String,
    val moolank: Int, // Psychic Number (1-9)
    val bhagyank: Int, // Destiny Number (1-9)
    val nameNumber: Int, // Name Number
    val rulingPlanetHi: String,
    val rulingPlanetEn: String = "",
    val luckyDaysHi: String,
    val luckyDaysEn: String = "",
    val luckyColorsHi: String,
    val luckyColorsEn: String = "",
    val friendlyNumbers: List<Int>,
    val enemyNumbers: List<Int>,
    val moolankReadingHi: String,
    val moolankReadingEn: String = "",
    val bhagyankReadingHi: String,
    val bhagyankReadingEn: String = ""
)
