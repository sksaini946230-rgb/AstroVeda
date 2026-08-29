package com.example.astro

import com.example.util.LanguageManager

/**
 * Every Vedic term the app displays, in Hindi and in English, kept apart.
 *
 * The calculators used to emit one combined string — "प्रतिपदा (Pratipada)",
 * "श्रावण (Shravana)" — and write it into BOTH the `tithi` and `tithiHindi`
 * fields of the model. So an English user got Hindi with a parenthetical, and the
 * English half of every data class was dead weight. Splitting the names here lets
 * each field carry what it says it carries.
 *
 * The lists are index-aligned with the astronomical order: tithi 0..14 within a
 * paksha, nakshatra 0..26 from Ashwini, yoga 0..26 from Vishkumbha, karana by the
 * 0..59 half-tithi index, masa 0..11 from Chaitra, rashi 0..11 from Mesha.
 */
object AstroNames {

    // ---------------------------------------------------------------- Tithi
    val TITHI_HI = listOf(
        "प्रतिपदा", "द्वितीया", "तृतीया", "चतुर्थी", "पंचमी",
        "षष्ठी", "सप्तमी", "अष्टमी", "नवमी", "दशमी",
        "एकादशी", "द्वादशी", "त्रयोदशी", "चतुर्दशी", "पूर्णिमा"
    )
    val TITHI_EN = listOf(
        "Pratipada", "Dwitiya", "Tritiya", "Chaturthi", "Panchami",
        "Shashthi", "Saptami", "Ashtami", "Navami", "Dashami",
        "Ekadashi", "Dwadashi", "Trayodashi", "Chaturdashi", "Purnima"
    )
    const val AMAVASYA_HI = "अमावस्या"
    const val AMAVASYA_EN = "Amavasya"

    // ------------------------------------------------------------ Nakshatra
    val NAKSHATRA_HI = listOf(
        "अश्विनी", "भरणी", "कृत्तिका", "रोहिणी", "मृगशिरा", "आर्द्रा", "पुनर्वसु",
        "पुष्य", "अश्लेषा", "मघा", "पूर्वाफाल्गुनी", "उत्तराफाल्गुनी", "हस्त",
        "चित्रा", "स्वाती", "विशाखा", "अनुराधा", "ज्येष्ठा", "मूल", "पूर्वाषाढा",
        "उत्तराषाढा", "श्रवण", "धनिष्ठा", "शतभिषा", "पूर्वाभाद्रपद",
        "उत्तराभाद्रपद", "रेवती"
    )
    val NAKSHATRA_EN = listOf(
        "Ashwini", "Bharani", "Krittika", "Rohini", "Mrigashira", "Ardra", "Punarvasu",
        "Pushya", "Ashlesha", "Magha", "Purva Phalguni", "Uttara Phalguni", "Hasta",
        "Chitra", "Swati", "Vishakha", "Anuradha", "Jyeshtha", "Moola", "Purva Ashadha",
        "Uttara Ashadha", "Shravana", "Dhanishta", "Shatabhisha", "Purva Bhadrapada",
        "Uttara Bhadrapada", "Revati"
    )

    // ----------------------------------------------------------------- Yoga
    val YOGA_HI = listOf(
        "विष्कुम्भ", "प्रीति", "आयुष्मान", "सौभाग्य", "शोभन", "अतिगण्ड", "सुकर्मा",
        "धृति", "शूल", "गण्ड", "वृद्धि", "ध्रुव", "व्याघात", "हर्षण", "वज्र",
        "सिद्धि", "व्यतीपात", "वरीयान", "परिघ", "शिव", "सिद्ध", "साध्य",
        "शुभ", "शुक्ल", "ब्रह्म", "ऐन्द्र", "वैधृति"
    )
    val YOGA_EN = listOf(
        "Vishkumbha", "Priti", "Ayushman", "Saubhagya", "Shobhana", "Atiganda", "Sukarma",
        "Dhriti", "Shoola", "Ganda", "Vriddhi", "Dhruva", "Vyaghata", "Harshana", "Vajra",
        "Siddhi", "Vyatipata", "Variyan", "Parigha", "Shiva", "Siddha", "Sadhya",
        "Shubha", "Shukla", "Brahma", "Aindra", "Vaidhriti"
    )

    // --------------------------------------------------------------- Karana
    private val MOVABLE_KARANA_HI = listOf(
        "बव", "बालव", "कौलव", "तैतिल", "गर", "वणिज", "विष्टि (भद्रा)"
    )
    private val MOVABLE_KARANA_EN = listOf(
        "Bava", "Balava", "Kaulava", "Taitila", "Gara", "Vanija", "Vishti (Bhadra)"
    )
    private val FIXED_KARANA_HI = mapOf(
        0 to "किंस्तुघ्न", 57 to "शकुनि", 58 to "चतुष्पाद", 59 to "नाग"
    )
    private val FIXED_KARANA_EN = mapOf(
        0 to "Kimstughna", 57 to "Shakuni", 58 to "Chatushpada", 59 to "Naga"
    )

    /** Karana name for a 0..59 half-tithi index. */
    fun karanaHi(index: Int): String =
        FIXED_KARANA_HI[index] ?: MOVABLE_KARANA_HI[(index - 1).mod(7)]

    fun karanaEn(index: Int): String =
        FIXED_KARANA_EN[index] ?: MOVABLE_KARANA_EN[(index - 1).mod(7)]

    // ----------------------------------------------------------------- Masa
    val MASA_HI = listOf(
        "चैत्र", "वैशाख", "ज्येष्ठ", "आषाढ़", "श्रावण", "भाद्रपद",
        "आश्विन", "कार्तिक", "मार्गशीर्ष", "पौष", "माघ", "फाल्गुन"
    )
    val MASA_EN = listOf(
        "Chaitra", "Vaisakha", "Jyeshtha", "Ashadha", "Shravana", "Bhadrapada",
        "Ashvin", "Kartika", "Margashirsha", "Pausha", "Magha", "Phalguna"
    )

    // ---------------------------------------------------------------- Rashi
    val RASHI_HI = listOf(
        "मेष", "वृषभ", "मिथुन", "कर्क", "सिंह", "कन्या",
        "तुला", "वृश्चिक", "धनु", "मकर", "कुंभ", "मीन"
    )
    val RASHI_EN = listOf(
        "Aries", "Taurus", "Gemini", "Cancer", "Leo", "Virgo",
        "Libra", "Scorpio", "Sagittarius", "Capricorn", "Aquarius", "Pisces"
    )
    /** Sanskrit names, for readers who want the Vedic term in English text. */
    val RASHI_SANSKRIT = listOf(
        "Mesha", "Vrishabha", "Mithuna", "Karka", "Simha", "Kanya",
        "Tula", "Vrishchika", "Dhanu", "Makara", "Kumbha", "Meena"
    )

    // --------------------------------------------------------------- Planet
    val PLANET_HI = mapOf(
        "Sun" to "सूर्य", "Moon" to "चन्द्र", "Mars" to "मंगल", "Mercury" to "बुध",
        "Jupiter" to "गुरु", "Venus" to "शुक्र", "Saturn" to "शनि",
        "Rahu" to "राहु", "Ketu" to "केतु"
    )
    val PLANET_SHORT = mapOf(
        "Sun" to "Su", "Moon" to "Mo", "Mars" to "Ma", "Mercury" to "Me",
        "Jupiter" to "Ju", "Venus" to "Ve", "Saturn" to "Sa",
        "Rahu" to "Ra", "Ketu" to "Ke"
    )

    // ------------------------------------------------------------- Paksha
    const val SHUKLA_HI = "शुक्ल पक्ष"
    const val SHUKLA_EN = "Shukla Paksha"
    const val KRISHNA_HI = "कृष्ण पक्ष"
    const val KRISHNA_EN = "Krishna Paksha"

    // ----------------------------------------------------------------- Vara
    val VARA_HI = listOf(
        "रविवार", "सोमवार", "मंगलवार", "बुधवार", "गुरुवार", "शुक्रवार", "शनिवार"
    )
    val VARA_EN = listOf(
        "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"
    )

    // ------------------------------------------------- Hindi -> English lookup
    /**
     * Reverse lookups for values that were persisted in Hindi only.
     *
     * The horoscope cache table stores `rulerHi` and `elementHi` but has no
     * English columns, and adding them is not free here: this Room database is
     * configured with fallbackToDestructiveMigration(dropAllTables = true), so a
     * version bump would drop every saved birth profile along with the cache.
     * Deriving the English on the way out of the cache costs nothing and touches
     * no user data.
     */
    private val PLANET_EN_BY_HI = PLANET_HI.entries.associate { (en, hi) -> hi to en }

    private val ELEMENT_EN_BY_HI = mapOf(
        "अग्नि" to "Fire", "पृथ्वी" to "Earth", "वायु" to "Air", "जल" to "Water"
    )

    /** English planet name for a Hindi one; returns the input if unrecognised. */
    fun planetEnFromHi(hi: String): String =
        PLANET_EN_BY_HI[hi.substringBefore(" (").trim()] ?: hi

    /** English element name for a Hindi one; returns the input if unrecognised. */
    fun elementEnFromHi(hi: String): String =
        ELEMENT_EN_BY_HI[hi.substringBefore(" (").trim()] ?: hi

    // --------------------------------------------------------------- Helper
    /** Picks the Hindi or English form for the language in force. */
    fun pick(hi: String, en: String): String = LanguageManager.getString(hi, en)
}
