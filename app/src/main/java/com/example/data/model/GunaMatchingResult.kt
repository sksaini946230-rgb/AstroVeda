package com.example.data.model

data class GunaKootDetail(
    val kootNameHi: String, // वर्ण, वश्य, तारा, योनि, ग्रह मैत्री, गण, भकूट, नाडी
    val kootNameEn: String,
    val maxPoints: Double,
    val obtainedPoints: Double,
    val descriptionHi: String,
    val descriptionEn: String = "",
    val isFavorable: Boolean = true
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
    val summaryReadingEn: String,
    val boyMoonRashi: String = "",
    val girlMoonRashi: String = "",
    val boyNakshatra: String = "",
    val girlNakshatra: String = "",
    val boyNadi: String = "",
    val girlNadi: String = "",
    val boyGana: String = "",
    val girlGana: String = "",
    val boyYoni: String = "",
    val girlYoni: String = "",
    val boyVarna: String = "",
    val girlVarna: String = "",
    val boyVashya: String = "",
    val girlVashya: String = "",
    val hasNadiDosha: Boolean = false,
    val nadiDoshaStatusHi: String = "",
    val nadiDoshaStatusEn: String = "",
    val hasBhakootDosha: Boolean = false,
    val bhakootDoshaStatusHi: String = "",
    val bhakootDoshaStatusEn: String = "",
    val scoreCategory: String = "AVERAGE"
)
