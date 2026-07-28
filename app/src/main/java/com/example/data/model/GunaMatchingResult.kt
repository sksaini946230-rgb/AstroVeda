package com.example.data.model

data class GunaKootDetail(
    val kootNameHi: String, // वर्ण, वश्य, तारा, योनि, ग्रह मैत्री, गण, भकूट, नाडी
    val kootNameEn: String,
    val maxPoints: Double,
    val obtainedPoints: Double,
    val descriptionHi: String
)

data class GunaMatchingResult(
    val boyName: String,
    val girlName: String,
    val totalObtainedGuna: Double, // Out of 36
    val maxGuna: Double = 36.0,
    val isManglikBoy: Boolean,
    val isManglikGirl: Boolean,
    val mangalDoshaStatusHi: String,
    val mangalDoshaStatusEn: String,
    val kootDetails: List<GunaKootDetail>,
    val compatibilityVerdictHi: String,
    val compatibilityVerdictEn: String,
    val summaryReadingHi: String,
    val summaryReadingEn: String
)
