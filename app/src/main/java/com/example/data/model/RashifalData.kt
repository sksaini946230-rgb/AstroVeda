package com.example.data.model

data class RashifalData(
    val rashiId: Int, // 1 to 12
    val rashiNameEn: String, // Aries, Taurus...
    val rashiNameHi: String, // मेष, वृषभ...
    val symbol: String, // ♈, ♉...
    val elementHi: String, // अग्नि, पृथ्वी...
    val elementEn: String = "", // Fire, Earth...
    val rulerHi: String, // मंगल, शुक्र...
    val rulerEn: String = "", // Mars, Venus...
    val ratingStars: Int, // 1 to 5
    val luckyNumber: Int,
    val luckyColorEn: String,
    val luckyColorHi: String,
    val luckyStoneHi: String = "मूंगा",
    val luckyTimeHi: String = "प्रातः 09:15 - 10:45",
    val luckyTimeEn: String = "09:15 AM - 10:45 AM",
    val generalReadingHi: String,
    val generalReadingEn: String,
    val careerReadingHi: String,
    val careerReadingEn: String,
    val healthReadingHi: String,
    val healthReadingEn: String,
    val loveReadingHi: String,
    val loveReadingEn: String,
    val financeReadingHi: String,
    val financeReadingEn: String,
    val period: String = "TODAY" // "TODAY", "WEEK", "MONTH"
)
