package com.example.data.model

data class MuhuratItem(
    val id: String,
    val categoryHi: String, // विवाह, गृह प्रवेश, व्यापार प्रारम्भ, वाहन खरीद, यात्रा
    val categoryEn: String,
    val dateString: String,
    val dayOfWeekHi: String,
    val dayOfWeekEn: String = "",
    val startTime: String,
    val endTime: String,
    val tithiHi: String,
    val tithiEn: String = "",
    val nakshatraHi: String,
    val nakshatraEn: String = "",
    val qualityHi: String, // अति शुभ, शुभ
    val qualityEn: String = "",
    val descriptionHi: String,
    val descriptionEn: String = ""
)
