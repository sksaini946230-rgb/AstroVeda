package com.example.astro

import com.example.data.model.CityLocation
import com.example.data.model.PanchangData
import com.example.data.model.PlanetPosition
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar
import java.util.Locale
import java.util.TimeZone

object PanchangCalculator {

    val popularCities = listOf(
        CityLocation("Jaipur", "जयपुर", "Rajasthan", 26.9124, 75.7873),
        CityLocation("New Delhi", "नई दिल्ली", "Delhi", 28.6139, 77.2090),
        CityLocation("Jodhpur", "जोधपुर", "Rajasthan", 26.2389, 73.0243),
        CityLocation("Udaipur", "उदयपुर", "Rajasthan", 24.5854, 73.7125),
        CityLocation("Kota", "कोटा", "Rajasthan", 25.2138, 75.8648),
        CityLocation("Bikaner", "बीकानेर", "Rajasthan", 28.0229, 73.3119),
        CityLocation("Mumbai", "मुंबई", "Maharashtra", 19.0760, 72.8777),
        CityLocation("Ahmedabad", "अहमदाबाद", "Gujarat", 23.0225, 72.5714),
        CityLocation("Lucknow", "लखनऊ", "Uttar Pradesh", 26.8467, 80.9462),
        CityLocation("Kolkata", "कोलकाता", "West Bengal", 22.5726, 88.3639),
        CityLocation("Bengaluru", "बेंगलुरु", "Karnataka", 12.9716, 77.5946),
        CityLocation("Varanasi", "वाराणसी", "Uttar Pradesh", 25.3176, 82.9739),
        CityLocation("Chennai", "चेन्नई", "Tamil Nadu", 13.0827, 80.2707),
        CityLocation("Hyderabad", "हैदराबाद", "Telangana", 17.3850, 78.4867),
        CityLocation("Pune", "पुणे", "Maharashtra", 18.5204, 73.8567),
        CityLocation("Patna", "पटना", "Bihar", 25.5941, 85.1376),
        CityLocation("Bhopal", "भोपाल", "Madhya Pradesh", 23.2599, 77.4126),
        CityLocation("Chandigarh", "चंडीगढ़", "Chandigarh", 30.7333, 76.7794),
        CityLocation("Guwahati", "गुवाहाटी", "Assam", 26.1445, 91.7362),
        CityLocation("Bhubaneswar", "भुवनेश्वर", "Odisha", 20.2961, 85.8245),
        CityLocation("Kochi", "कोच्चि", "Kerala", 9.9312, 76.2673),
        CityLocation("Nagpur", "नागपुर", "Maharashtra", 21.1458, 79.0882),
        CityLocation("Indore", "इंदौर", "Madhya Pradesh", 22.7196, 75.8577),
        CityLocation("Surat", "सूरत", "Gujarat", 21.1702, 72.8311),
        CityLocation("Amritsar", "अमृतसर", "Punjab", 31.6340, 74.8723),
        CityLocation("Dehradun", "देहरादून", "Uttarakhand", 30.3165, 78.0322),
        CityLocation("Raipur", "रायपुर", "Chhattisgarh", 21.2514, 81.6296),
        CityLocation("Ranchi", "रांची", "Jharkhand", 23.3441, 85.3096),
        CityLocation("Srinagar", "श्रीनगर", "Jammu & Kashmir", 34.0837, 74.7973),
        CityLocation("Thiruvananthapuram", "तिरुवनन्तपुरम", "Kerala", 8.5241, 76.9366)
    )

    private val TITHIS_HI = listOf(
        "प्रतिपदा (Pratipada)", "द्वितीया (Dwitiya)", "तृतीया (Tritiya)", "चतुर्थी (Chaturthi)",
        "पंचमी (Panchami)", "षष्ठी (Shasthi)", "सप्तमी (Saptami)", "अष्टमी (Ashtami)",
        "नवमी (Navami)", "दशमी (Dashami)", "एकादशी (Ekadashi)", "द्वादशी (Dwadashi)",
        "त्रयोदशी (Trayodashi)", "चतुर्दशी (Chaturdashi)", "पूर्णिमा (Purnima)"
    )

    private val NAKSHATRAS_HI = listOf(
        "अश्विनी (Ashwini)", "भरणी (Bharani)", "कृत्तिका (Krittika)", "रोहिणी (Rohini)",
        "मृगशिरा (Mrigashira)", "आर्द्रा (Ardra)", "पुनर्वसु (Punarvasu)", "पुष्य (Pushya)",
        "अश्लेषा (Ashlesha)", "मघा (Magha)", "पूर्वाफाल्गुनी (Purva Phalguni)", "उत्तराफाल्गुनी (Uttara Phalguni)",
        "हस्त (Hasta)", "चित्रा (Chitra)", "स्वाती (Swati)", "विशाखा (Vishakha)",
        "अनुराधा (Anuradha)", "ज्येष्ठा (Jyeshtha)", "मूल (Moola)", "पूर्वाषाढा (Purva Ashadha)",
        "उत्तराषाढा (Uttara Ashadha)", "श्रवण (Shravana)", "धनिष्ठा (Dhanishta)", "शतभिषा (Shatabhisha)",
        "पूर्वाभाद्रपद (Purva Bhadrapada)", "उत्तराभाद्रपद (Uttara Bhadrapada)", "रेवती (Revati)"
    )

    private val YOGAS_HI = listOf(
        "विष्कुम्भ (Vishkumbha)", "प्रीति (Priti)", "आयुष्मान (Ayushman)", "सौभाग्य (Saubhagya)",
        "शोभन (Shobhana)", "अतिगण्ड (Atiganda)", "सुकर्मा (Sukarma)", "धृति (Dhriti)",
        "शूल (Shoola)", "गण्ड (Ganda)", "वृद्धि (Vriddhi)", "ध्रुव (Dhruva)",
        "व्याघात (Vyaghata)", "हर्षण (Harshana)", "वज्र (Vajra)", "सिद्धि (Siddhi)",
        "व्यतीपात (Vyatipata)", "वरीयान (Variyan)", "परिघ (Parigha)", "शिव (Shiva)",
        "सिद्ध (Siddha)", "साध्य (Sadhya)", "शुभ (Shubha)", "शुक्ल (Shukla)",
        "ब्रह्म (Brahma)", "ऐन्द्र (Aindra)", "वैधृति (Vaidhriti)"
    )

    private val VARS_HI = mapOf(
        Calendar.SUNDAY to Pair("रविवार (Ravivar)", "Sunday"),
        Calendar.MONDAY to Pair("सोमवार (Somvar)", "Monday"),
        Calendar.TUESDAY to Pair("मंगलवार (Mangalvar)", "Tuesday"),
        Calendar.WEDNESDAY to Pair("बुधवार (Budhvar)", "Wednesday"),
        Calendar.THURSDAY to Pair("गुरुवार (Guruvar)", "Thursday"),
        Calendar.FRIDAY to Pair("शुक्रवार (Shukravar)", "Friday"),
        Calendar.SATURDAY to Pair("शनिवार (Shanivar)", "Saturday")
    )

    private val RASHI_NAMES_HI = listOf(
        "मेष", "वृषभ", "मिथुन", "कर्क", "सिंह", "कन्या",
        "तुला", "वृश्चिक", "धनु", "मकर", "कुंभ", "मीन"
    )

    private val PLANETS_INFO = listOf(
        Pair("Sun", "सूर्य (Su)"), Pair("Moon", "चन्द्र (Mo)"), Pair("Mars", "मंगल (Ma)"),
        Pair("Mercury", "बुध (Me)"), Pair("Jupiter", "गुरु (Ju)"), Pair("Venus", "शुक्र (Ve)"),
        Pair("Saturn", "शनि (Sa)"), Pair("Rahu", "राहु (Ra)"), Pair("Ketu", "केतु (Ke)")
    )

    private val NAKSHATRAS_SHORT = listOf(
        "अश्विनी", "भरणी", "कृत्तिका", "रोहिणी", "मृगशिरा", "आर्द्रा", "पुनर्वसु", "पुष्य", "अश्लेषा",
        "मघा", "पूर्वाफाल्गुनी", "उत्तराफाल्गुनी", "हस्त", "चित्रा", "स्वाती", "विशाखा", "अनुराधा", "ज्येष्ठा",
        "मूल", "पूर्वाषाढा", "उत्तराषाढा", "श्रवण", "धनिष्ठा", "शतभिषा", "पूर्वाभाद्रपद", "उत्तराभाद्रपद", "रेवती"
    )

    /**
     * The Panchang for a calendar day at a place.
     *
     * IMPORTANT — the five limbs are evaluated **at sunrise**, not at the moment
     * the screen happens to be open. That is the Vedic convention and the only
     * way this output can agree with a printed panchang: previously the headline
     * Tithi changed as the day went on, so the same day could read "Purnima,
     * Shukla" in the morning and "Pratipada, Krishna" in the evening.
     */
    fun calculatePanchang(date: Date, city: CityLocation, use24Hour: Boolean = false): PanchangData {
        val zone = AstroTime.IST
        val cal = GregorianCalendar(zone).apply { time = date }
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        val varInfo = VARS_HI[dayOfWeek] ?: Pair("रविवार (Ravivar)", "Sunday")

        val midnightJd = AstroTime.julianDayFromLocal(year, month, day, 0, 0, zone)

        val sunTimes = RiseSetCalculator.sunRiseSet(midnightJd, city.latitude, city.longitude)
        val moonTimes = RiseSetCalculator.moonRiseSet(midnightJd, city.latitude, city.longitude)

        // Fall back to 6am local only when the Sun genuinely does not rise (polar).
        val sunriseJd = sunTimes.riseJd ?: (midnightJd + 0.25)
        val sunsetJd = sunTimes.setJd ?: (midnightJd + 0.75)

        // ---- the five limbs, at sunrise ----
        val tithiNum = PanchangElements.tithiNumber(sunriseJd)
        val isShukla = tithiNum <= 15
        val tithiIndex = (if (isShukla) tithiNum - 1 else tithiNum - 16).coerceIn(0, 14)
        val tithiName = if (tithiNum == 30) "अमावस्या (Amavasya)" else TITHIS_HI[tithiIndex]

        val nakIdx = PanchangElements.nakshatraIndex(sunriseJd)
        val yogaIdx = PanchangElements.yogaIndex(sunriseJd)
        val karanaIdx = PanchangElements.karanaIndex(sunriseJd)

        // Progress through the current Tithi, for the UI ring.
        val elongation = AstroMath.elongation(sunriseJd)
        val tithiProgress = ((elongation % PanchangElements.TITHI_SPAN) /
            PanchangElements.TITHI_SPAN * 100.0).toFloat()

        val tithiEnd = PanchangElements.tithiEndJd(sunriseJd)
        val nakEnd = PanchangElements.nakshatraEndJd(sunriseJd)

        // ---- day divisions, all measured from real sunrise/sunset ----
        val sunriseMin = jdToLocalMinutes(sunriseJd, zone)
        val sunsetMin = jdToLocalMinutes(sunsetJd, zone)
        val dayDurationMin = if (sunsetMin > sunriseMin) sunsetMin - sunriseMin else 720
        val slotLen = dayDurationMin / 8

        // Weekday slot tables (verified against standard practice — do not reorder).
        val rahuSlots = mapOf(
            Calendar.SUNDAY to 7, Calendar.MONDAY to 1, Calendar.TUESDAY to 6,
            Calendar.WEDNESDAY to 4, Calendar.THURSDAY to 5, Calendar.FRIDAY to 3,
            Calendar.SATURDAY to 2
        )
        val gulikaSlots = mapOf(
            Calendar.SUNDAY to 6, Calendar.MONDAY to 5, Calendar.TUESDAY to 4,
            Calendar.WEDNESDAY to 3, Calendar.THURSDAY to 2, Calendar.FRIDAY to 1,
            Calendar.SATURDAY to 0
        )
        val yamaSlots = mapOf(
            Calendar.SUNDAY to 4, Calendar.MONDAY to 3, Calendar.TUESDAY to 2,
            Calendar.WEDNESDAY to 1, Calendar.THURSDAY to 0, Calendar.FRIDAY to 6,
            Calendar.SATURDAY to 5
        )

        fun slotRange(slot: Int): String {
            val start = sunriseMin + slot * slotLen
            return "${formatMinutesToTime(start, use24Hour)} - ${formatMinutesToTime(start + slotLen, use24Hour)}"
        }

        val rahuKaalStr = slotRange(rahuSlots[dayOfWeek] ?: 1)
        val gulikaKaalStr = slotRange(gulikaSlots[dayOfWeek] ?: 0)
        val yamaKaalStr = slotRange(yamaSlots[dayOfWeek] ?: 0)

        val midDay = sunriseMin + dayDurationMin / 2
        val abhijitStr = "${formatMinutesToTime(midDay - 24, use24Hour)} - ${formatMinutesToTime(midDay + 24, use24Hour)}"
        val brahmaStr = "${formatMinutesToTime(sunriseMin - 96, use24Hour)} - ${formatMinutesToTime(sunriseMin - 48, use24Hour)}"

        // ---- planets, at sunrise ----
        val ascendant = KundaliCalculator.ascendantDegrees(sunriseJd, city.latitude, city.longitude)
        val ascendantRashiIdx = (ascendant / 30.0).toInt().coerceIn(0, 11)

        val planetDegrees = AstroMath.calculatePlanets(sunriseJd)
        val before = AstroMath.calculatePlanets(sunriseJd - 0.5)
        val after = AstroMath.calculatePlanets(sunriseJd + 0.5)

        val planetPositions = PLANETS_INFO.map { (en, hi) ->
            val deg = planetDegrees[en] ?: 0.0
            val motion = AstroTime.wrap180((after[en] ?: 0.0) - (before[en] ?: 0.0))
            val rashiIdx = (deg / 30.0).toInt().coerceIn(0, 11)
            PlanetPosition(
                planetNameEn = en,
                planetNameHi = hi,
                rashiNumber = rashiIdx + 1,
                rashiNameHi = RASHI_NAMES_HI[rashiIdx],
                degree = String.format(Locale.US, "%.2f", deg % 30.0).toDouble(),
                houseNumber = ((rashiIdx - ascendantRashiIdx + 12) % 12) + 1,
                isRetrograde = when (en) {
                    "Rahu", "Ketu" -> true
                    "Sun", "Moon" -> false
                    else -> motion < 0.0
                },
                nakshatraHi = NAKSHATRAS_SHORT[(deg / PanchangElements.NAKSHATRA_SPAN).toInt().coerceIn(0, 26)]
            )
        }

        val masaIdx = PanchangElements.masaIndex(sunriseJd)
        val sunDeg = planetDegrees["Sun"] ?: 0.0
        val moonDeg = planetDegrees["Moon"] ?: 0.0

        val dateFmt = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.ENGLISH).apply { timeZone = zone }

        return PanchangData(
            dateString = dateFmt.format(date),
            dayOfWeek = varInfo.second,
            dayOfWeekHindi = varInfo.first,
            vikramSamvat = PanchangElements.vikramSamvat(sunriseJd, year),
            sakaSamvat = PanchangElements.sakaSamvat(sunriseJd, year),
            masaName = PanchangElements.MASA_NAMES_EN[masaIdx],
            masaNameHindi = PanchangElements.MASA_NAMES_HI[masaIdx],
            paksha = if (isShukla) "Shukla Paksha" else "Krishna Paksha",
            pakshaHindi = if (isShukla) "शुक्ल पक्ष (Shukla Paksha)" else "कृष्ण पक्ष (Krishna Paksha)",
            tithi = tithiName,
            tithiHindi = tithiName,
            tithiEndTime = formatEndTime(tithiEnd, midnightJd, zone, use24Hour),
            tithiProgressPercent = tithiProgress,
            nakshatra = NAKSHATRAS_HI[nakIdx],
            nakshatraHindi = NAKSHATRAS_HI[nakIdx],
            nakshatraEndTime = formatEndTime(nakEnd, midnightJd, zone, use24Hour),
            nakshatraPada = PanchangElements.nakshatraPada(sunriseJd),
            yoga = YOGAS_HI[yogaIdx],
            yogaHindi = YOGAS_HI[yogaIdx],
            karan = PanchangElements.karanaName(karanaIdx),
            karanHindi = PanchangElements.karanaName(karanaIdx),
            sunrise = formatJdTime(sunriseJd, zone, use24Hour),
            sunset = formatJdTime(sunsetJd, zone, use24Hour),
            // The Moon skips a rise or a set about once a month — say so rather
            // than inventing a time, which is what this field used to do.
            moonrise = moonTimes.riseJd?.let { formatJdTime(it, zone, use24Hour) } ?: "—",
            moonset = moonTimes.setJd?.let { formatJdTime(it, zone, use24Hour) } ?: "—",
            rahuKaal = rahuKaalStr,
            gulikaKaal = gulikaKaalStr,
            yamaganda = yamaKaalStr,
            abhijitMuhurat = abhijitStr,
            brahmaMuhurat = brahmaStr,
            sunSign = RASHI_NAMES_HI[(sunDeg / 30.0).toInt().coerceIn(0, 11)],
            moonSign = RASHI_NAMES_HI[(moonDeg / 30.0).toInt().coerceIn(0, 11)],
            locationName = city.cityNameHindi,
            latitude = city.latitude,
            longitude = city.longitude,
            planets = planetPositions
        )
    }

    // ------------------------------------------------------------------
    // Formatting
    // ------------------------------------------------------------------

    /** Minutes since local midnight for a Julian Day. */
    private fun jdToLocalMinutes(jd: Double, zone: TimeZone): Int {
        val cal = GregorianCalendar(zone).apply { timeInMillis = AstroTime.millisFromJulianDay(jd) }
        return cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
    }

    private fun formatJdTime(jd: Double, zone: TimeZone, use24Hour: Boolean): String =
        formatMinutesToTime(jdToLocalMinutes(jd, zone), use24Hour)

    /**
     * "07:42 PM तक" or "अगले दिन 04:15 AM तक" — and now these are the real
     * crossing times, computed per day and per place.
     */
    private fun formatEndTime(
        endJd: Double?,
        midnightJd: Double,
        zone: TimeZone,
        use24Hour: Boolean
    ): String {
        if (endJd == null) return "—"
        val time = formatJdTime(endJd, zone, use24Hour)
        val daysAhead = Math.floor(endJd - midnightJd).toInt()
        return when {
            daysAhead <= 0 -> "$time तक"
            daysAhead == 1 -> "अगले दिन $time तक"
            else -> "$daysAhead दिन बाद $time तक"
        }
    }

    fun formatMinutesToTime(minutes: Int, use24Hour: Boolean = false): String {
        val m = ((minutes % 1440) + 1440) % 1440
        val hrs = m / 60
        val mins = m % 60
        if (use24Hour) {
            return String.format(Locale.US, "%02d:%02d", hrs, mins)
        }
        val ampm = if (hrs >= 12) "PM" else "AM"
        val displayHrs = if (hrs % 12 == 0) 12 else hrs % 12
        return String.format(Locale.US, "%02d:%02d %s", displayHrs, mins, ampm)
    }

    fun getMoonPhaseInfo(pakshaHindi: String, tithiHindi: String): MoonPhaseInfo {
        val isShukla = pakshaHindi.contains("शुक्ल") || pakshaHindi.contains("Shukla", ignoreCase = true)
        val isPurnima = tithiHindi.contains("पूर्णिमा") || tithiHindi.contains("Purnima", ignoreCase = true)
        val isAmavasya = tithiHindi.contains("अमावस्या") || tithiHindi.contains("Amavasya", ignoreCase = true)

        if (isPurnima) {
            return MoonPhaseInfo("🌕", "पूर्णिमा (Full Moon)", "Full Moon", 100, true)
        }
        if (isAmavasya) {
            return MoonPhaseInfo("🌑", "अमावस्या (New Moon)", "New Moon", 0, false)
        }

        val tithiNum = TITHI_LOOKUP.entries.firstOrNull { tithiHindi.contains(it.key) }?.value ?: 7

        return if (isShukla) {
            val illumination = ((tithiNum / 15.0) * 100).toInt().coerceIn(5, 95)
            when (tithiNum) {
                in 1..3 -> MoonPhaseInfo("🌒", "शुक्ल पक्ष (Waxing Crescent)", "Waxing Crescent", illumination, true)
                in 4..7 -> MoonPhaseInfo("🌓", "शुक्ल पक्ष (First Quarter)", "First Quarter", illumination, true)
                else -> MoonPhaseInfo("🌔", "शुक्ल पक्ष (Waxing Gibbous)", "Waxing Gibbous", illumination, true)
            }
        } else {
            val illumination = (((15 - tithiNum) / 15.0) * 100).toInt().coerceIn(5, 95)
            when (tithiNum) {
                in 1..3 -> MoonPhaseInfo("🌖", "कृष्ण पक्ष (Waning Gibbous)", "Waning Gibbous", illumination, false)
                in 4..7 -> MoonPhaseInfo("🌗", "कृष्ण पक्ष (Third Quarter)", "Third Quarter", illumination, false)
                else -> MoonPhaseInfo("🌘", "कृष्ण पक्ष (Waning Crescent)", "Waning Crescent", illumination, false)
            }
        }
    }

    private val TITHI_LOOKUP = linkedMapOf(
        "प्रतिपदा" to 1, "द्वितीया" to 2, "तृतीया" to 3, "चतुर्थी" to 4, "पंचमी" to 5,
        "षष्ठी" to 6, "सप्तमी" to 7, "अष्टमी" to 8, "नवमी" to 9, "दशमी" to 10,
        "एकादशी" to 11, "द्वादशी" to 12, "त्रयोदशी" to 13, "चतुर्दशी" to 14
    )
}

data class MoonPhaseInfo(
    val emoji: String,
    val nameHindi: String,
    val nameEn: String,
    val illuminationPercent: Int,
    val isWaxing: Boolean
)
