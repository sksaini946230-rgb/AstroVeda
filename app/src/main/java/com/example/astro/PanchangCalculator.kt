package com.example.astro

import com.example.data.model.CityLocation
import com.example.data.model.PanchangData
import com.example.data.model.PlanetPosition
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.floor

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
        CityLocation("Varanasi", "वाराणसी", "Uttar Pradesh", 25.3176, 82.9739)
    )

    private val TITHIS_HI = listOf(
        "प्रतिपदा (Pratipada)", "द्वितीया (Dwitiya)", "तृतीया (Tritiya)", "चतुर्थी (Chaturthi)",
        "पंचमी (Panchami)", "षष्ठी (Shasthi)", "सप्तमी (Saptami)", "अष्टमी (Ashtami)",
        "नवमी (Navami)", "दशमी (Dashami)", "एकादशी (Ekadashi)", "द्वादशी (Dwadashi)",
        "त्रयोदशी (Trayodashi)", "चतुर्दशी (Chaturdashi)", "पूर्णिमा / अमावस्या"
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
        "व्याघात (Vyaghata)", "हर्पण (Harshana)", "वज्र (Vajra)", "सिद्धि (Siddhi)",
        "व्यतीपात (Vyatipata)", "वरीयान (Variyan)", "परिघ (Parigha)", "शिव (Shiva)",
        "सिद्ध (Siddha)", "साध्य (Sadhya)", "शुभ (Shubha)", "शुक्ल (Shukla)",
        "ब्रह्म (Brahma)", "ऐन्द्र (Aindra)", "वैधृति (Vaidhriti)"
    )

    private val KARANS_HI = listOf(
        "बव (Bava)", "बालव (Balava)", "कौलव (Kaulava)", "तैतिल (Taitila)",
        "गर (Gara)", "वणिज (Vanija)", "विष्टि/भद्रा (Vishti/Bhadra)", "शकुनि (Shakuni)",
        "चतुष्पाद (Chatushpada)", "नाग (Naga)", "किंस्तुघ्न (Kinstughna)"
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

    fun calculatePanchang(date: Date, city: CityLocation, use24Hour: Boolean = false): PanchangData {
        val cal = Calendar.getInstance().apply { time = date }
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        val varInfo = VARS_HI[dayOfWeek] ?: Pair("रविवार", "Sunday")

        val dayOfYear = cal.get(Calendar.DAY_OF_YEAR)
        val year = cal.get(Calendar.YEAR)

        // Vikram Samvat calculations (typically Year + 57)
        val vikramSamvat = year + 57
        val sakaSamvat = year - 78

        // Month calculation
        val monthIdx = cal.get(Calendar.MONTH)
        val masaNames = listOf(
            Pair("चैत्र (Chaitra)", "Chaitra"),
            Pair("वैशाख (Vaisakha)", "Vaisakha"),
            Pair("ज्येष्ठ (Jyeshtha)", "Jyeshtha"),
            Pair("आषाढ़ (Ashadha)", "Ashadha"),
            Pair("श्रावण (Shravana)", "Shravana"),
            Pair("भाद्रपद (Bhadrapada)", "Bhadrapada"),
            Pair("आश्विन (Ashvin)", "Ashvin"),
            Pair("कार्तिक (Kartika)", "Kartika"),
            Pair("मार्गशीर्ष (Margashirsha)", "Margashirsha"),
            Pair("पौष (Pausha)", "Pausha"),
            Pair("माघ (Magha)", "Magha"),
            Pair("फाल्गुन (Phalguna)", "Phalguna")
        )
        val masa = masaNames[monthIdx % 12]

        val minute = cal.get(Calendar.MINUTE)
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val month = cal.get(Calendar.MONTH) + 1
        val day = cal.get(Calendar.DAY_OF_MONTH)

        val hourDecimal = hour + minute / 60.0
        val planetDegrees = AstroMath.calculatePlanets(year, month, day, hourDecimal)

        val sunDeg = planetDegrees["Sun"] ?: 0.0
        val moonDeg = planetDegrees["Moon"] ?: 0.0

        val diffDeg = (moonDeg - sunDeg + 360.0) % 360.0
        val tithiNum = (diffDeg / 12.0).toInt() + 1 // 1 to 30

        val isShukla = tithiNum <= 15
        val pakshaHi = if (isShukla) "शुक्ल पक्ष (Shukla Paksha)" else "कृष्ण पक्ष (Krishna Paksha)"
        val tithiIndex = (if (tithiNum <= 15) tithiNum - 1 else tithiNum - 16).coerceIn(0, 14)
        val tithiName = TITHIS_HI[tithiIndex]
        val tithiEndProgress = ((diffDeg % 12.0) / 12.0 * 100).toFloat()

        // Nakshatra calculation (13°20' per nakshatra)
        val nakshatraIdx = (moonDeg / 13.3333).toInt().coerceIn(0, 26)
        val nakshatraName = NAKSHATRAS_HI[nakshatraIdx]
        val nakshatraPada = ((moonDeg % 13.3333) / 3.3333).toInt() + 1

        // Yoga calculation ((Sun + Moon) / 13°20')
        val yogaIdx = ((sunDeg + moonDeg) % 360.0 / 13.3333).toInt().coerceIn(0, 26)
        val yogaName = YOGAS_HI[yogaIdx]

        // Karan calculation (half of Tithi)
        val karanIdx = (diffDeg / 6.0).toInt() % 11
        val karanName = KARANS_HI[karanIdx]

        // Exact Solar Astronomical Calculations for City Latitude & Longitude
        val latRad = Math.toRadians(city.latitude)
        val lonDeg = city.longitude
        
        // Solar mean anomaly M & mean longitude L
        val N = dayOfYear.toDouble()
        val radM = Math.toRadians((357.528 + 0.9856003 * N) % 360.0)
        val radL = Math.toRadians((280.460 + 0.9856474 * N) % 360.0)
        val trueSunLonRad = radL + Math.toRadians(1.915 * Math.sin(radM) + 0.020 * Math.sin(2 * radM))
        
        // Declination
        val sinDec = Math.sin(Math.toRadians(23.439)) * Math.sin(trueSunLonRad)
        val decRad = Math.asin(sinDec)
        
        // Hour angle for atmospheric refraction + semi-diameter (-0.8333 degrees)
        val cosH = (Math.sin(Math.toRadians(-0.8333)) - Math.sin(latRad) * Math.sin(decRad)) / (Math.cos(latRad) * Math.cos(decRad))
        val hDeg = Math.toDegrees(Math.acos(cosH.coerceIn(-1.0, 1.0)))
        
        // Equation of Time in minutes
        val eotMins = 4.0 * Math.toDegrees(trueSunLonRad - radL)
        
        // Solar Noon in minutes from midnight IST (UTC+5.5)
        // IST meridian is 82.5 degrees East
        val solarNoonMin = 720.0 - eotMins + (82.5 - lonDeg) * 4.0
        val baseSunriseMin = (solarNoonMin - hDeg * 4.0).toInt()
        val baseSunsetMin = (solarNoonMin + hDeg * 4.0).toInt()

        val sunriseStr = formatMinutesToTime(baseSunriseMin, use24Hour)
        val sunsetStr = formatMinutesToTime(baseSunsetMin, use24Hour)
        val moonriseStr = formatMinutesToTime((baseSunsetMin - 60) % 1440, use24Hour)
        val moonsetStr = formatMinutesToTime((baseSunriseMin + 600) % 1440, use24Hour)

        // Rahu Kaal calculation (1/8th of day duration)
        val dayDurationMin = baseSunsetMin - baseSunriseMin
        val slotLen = dayDurationMin / 8

        // Rahu Kaal order by weekday: Sun=8, Mon=2, Tue=7, Wed=5, Thu=6, Fri=4, Sat=3
        val rahuSlots = mapOf(
            Calendar.SUNDAY to 7,
            Calendar.MONDAY to 1,
            Calendar.TUESDAY to 6,
            Calendar.WEDNESDAY to 4,
            Calendar.THURSDAY to 5,
            Calendar.FRIDAY to 3,
            Calendar.SATURDAY to 2
        )
        val rSlot = rahuSlots[dayOfWeek] ?: 1
        val rahuStartMin = baseSunriseMin + rSlot * slotLen
        val rahuEndMin = rahuStartMin + slotLen
        val rahuKaalStr = "${formatMinutesToTime(rahuStartMin, use24Hour)} - ${formatMinutesToTime(rahuEndMin, use24Hour)}"

        // Gulika Kaal
        val gulikaSlots = mapOf(
            Calendar.SUNDAY to 6, Calendar.MONDAY to 5, Calendar.TUESDAY to 4,
            Calendar.WEDNESDAY to 3, Calendar.THURSDAY to 2, Calendar.FRIDAY to 1, Calendar.SATURDAY to 0
        )
        val gSlot = gulikaSlots[dayOfWeek] ?: 0
        val gulikaStartMin = baseSunriseMin + gSlot * slotLen
        val gulikaKaalStr = "${formatMinutesToTime(gulikaStartMin, use24Hour)} - ${formatMinutesToTime(gulikaStartMin + slotLen, use24Hour)}"

        // Yamaganda
        val yamaSlots = mapOf(
            Calendar.SUNDAY to 4, Calendar.MONDAY to 3, Calendar.TUESDAY to 2,
            Calendar.WEDNESDAY to 1, Calendar.THURSDAY to 0, Calendar.FRIDAY to 6, Calendar.SATURDAY to 5
        )
        val ySlot = yamaSlots[dayOfWeek] ?: 0
        val yamaStartMin = baseSunriseMin + ySlot * slotLen
        val yamaKaalStr = "${formatMinutesToTime(yamaStartMin, use24Hour)} - ${formatMinutesToTime(yamaStartMin + slotLen, use24Hour)}"

        // Abhijit Muhurat (midday 48 minutes)
        val midDay = baseSunriseMin + dayDurationMin / 2
        val abhijitStr = "${formatMinutesToTime(midDay - 24, use24Hour)} - ${formatMinutesToTime(midDay + 24, use24Hour)}"
        val brahmaStr = "${formatMinutesToTime(baseSunriseMin - 96, use24Hour)} - ${formatMinutesToTime(baseSunriseMin - 48, use24Hour)}"

        val rashiNamesHi = listOf("मेष", "वृषभ", "मिथुन", "कर्क", "सिंह", "कन्या", "तुला", "वृश्चिक", "धनु", "मकर", "कुंभ", "मीन")
        val sunSignIdx = (sunDeg / 30.0).toInt().coerceIn(0, 11)
        val moonSignIdx = (moonDeg / 30.0).toInt().coerceIn(0, 11)

        val PLANETS_INFO = listOf(
            Pair("Sun", "सूर्य (Su)"), Pair("Moon", "चन्द्र (Mo)"), Pair("Mars", "मंगल (Ma)"),
            Pair("Mercury", "बुध (Me)"), Pair("Jupiter", "गुरु (Ju)"), Pair("Venus", "शुक्र (Ve)"),
            Pair("Saturn", "शनि (Sa)"), Pair("Rahu", "राहु (Ra)"), Pair("Ketu", "केतु (Ke)")
        )
        val NAKSHATRAS = listOf(
            "अश्विनी", "भरणी", "कृत्तिका", "रोहिणी", "मृगशिरा", "आर्द्रा", "पुनर्वसु", "पुष्य", "अश्लेषा",
            "मघा", "पूर्वाफाल्गुनी", "उत्तराफाल्गुनी", "हस्त", "चित्रा", "स्वाती", "विशाखा", "अनुराधा", "ज्येष्ठा",
            "मूल", "पूर्वाषाढा", "उत्तराषाढा", "श्रवण", "धनिष्ठा", "शतभिषा", "पूर्वाभाद्रपद", "उत्तराभाद्रपद", "रेवती"
        )
        
        // Sidereal Ascendant (Lagna) with Lahiri Ayanamsa
        val y = if (month <= 2) year - 1 else year
        val m = if (month <= 2) month + 12 else month
        val aVal = y / 100
        val bVal = 2 - aVal + aVal / 4
        val jdVal = floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + hourDecimal / 24.0 + bVal - 1524.5

        val t1900Val = (jdVal - 2415020.0) / 36525.0
        val ayanamsaVal = 22.460148 + 1.396042 * t1900Val + 0.000308 * t1900Val * t1900Val

        val gmst = (18.697374558 + 24.06570982441908 * (jdVal - 2451545.0)) % 24.0
        val gmstDeg = if (gmst < 0) (gmst + 24.0) * 15.0 else gmst * 15.0
        val lstDeg = (gmstDeg + city.longitude) % 360.0
        val lst = if (lstDeg < 0) lstDeg + 360.0 else lstDeg

        val t2000Val = (jdVal - 2451545.0) / 36525.0
        val obliquity = 23.4392911 - (46.8150 * t2000Val) / 3600.0

        val lstRad = Math.toRadians(lst)
        val epsRad = Math.toRadians(obliquity)
        val latRadCalculated = Math.toRadians(city.latitude)

        val num = kotlin.math.cos(lstRad)
        val den = -kotlin.math.sin(lstRad) * kotlin.math.cos(epsRad) - kotlin.math.tan(latRadCalculated) * kotlin.math.sin(epsRad)
        var ascendantDeg = Math.toDegrees(kotlin.math.atan2(num, den))
        if (ascendantDeg < 0) ascendantDeg += 360.0

        var siderealAscendant = (ascendantDeg - ayanamsaVal) % 360.0
        if (siderealAscendant < 0) siderealAscendant += 360.0

        val ascendantRashiIdx = (siderealAscendant / 30.0).toInt().coerceIn(0, 11)

        val calTomorrow = Calendar.getInstance().apply {
            time = date
            add(Calendar.DAY_OF_YEAR, 1)
        }
        val tYear = calTomorrow.get(Calendar.YEAR)
        val tMonth = calTomorrow.get(Calendar.MONTH) + 1
        val tDay = calTomorrow.get(Calendar.DAY_OF_MONTH)
        val planetDegreesTomorrow = AstroMath.calculatePlanets(tYear, tMonth, tDay, hourDecimal)

        val planetPositions = mutableListOf<PlanetPosition>()
        PLANETS_INFO.forEach { (en, hi) ->
            val deg = planetDegrees[en] ?: 0.0
            val degTomorrow = planetDegreesTomorrow[en] ?: 0.0
            var diff = degTomorrow - deg
            while (diff > 180.0) diff -= 360.0
            while (diff < -180.0) diff += 360.0

            val isRetro = when (en) {
                "Rahu", "Ketu" -> true
                "Sun", "Moon" -> false
                else -> diff < -0.0001
            }

            val rashiIdx = (deg / 30.0).toInt().coerceIn(0, 11)
            val degreeInRashi = deg % 30.0
            val houseNum = ((rashiIdx - ascendantRashiIdx + 12) % 12) + 1
            val nakshatraIdxP = (deg / 13.333333).toInt().coerceIn(0, 26)
            
            planetPositions.add(PlanetPosition(
                planetNameEn = en,
                planetNameHi = hi,
                rashiNumber = rashiIdx + 1,
                rashiNameHi = rashiNamesHi[rashiIdx],
                degree = String.format(Locale.US, "%.2f", degreeInRashi).toDouble(),
                houseNumber = houseNum,
                isRetrograde = isRetro,
                nakshatraHi = NAKSHATRAS[nakshatraIdxP]
            ))
        }

        val dateFmt = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.ENGLISH)

        return PanchangData(
            dateString = dateFmt.format(date),
            dayOfWeek = varInfo.second,
            dayOfWeekHindi = varInfo.first,
            vikramSamvat = vikramSamvat,
            sakaSamvat = sakaSamvat,
            masaName = masa.second,
            masaNameHindi = masa.first,
            paksha = if (isShukla) "Shukla Paksha" else "Krishna Paksha",
            pakshaHindi = pakshaHi,
            tithi = tithiName,
            tithiHindi = tithiName,
            tithiEndTime = "अगले दिन 04:15 AM तक",
            tithiProgressPercent = tithiEndProgress,
            nakshatra = nakshatraName,
            nakshatraHindi = nakshatraName,
            nakshatraEndTime = "रात्रि 11:30 PM तक",
            nakshatraPada = nakshatraPada,
            yoga = yogaName,
            yogaHindi = yogaName,
            karan = karanName,
            karanHindi = karanName,
            sunrise = sunriseStr,
            sunset = sunsetStr,
            moonrise = moonriseStr,
            moonset = moonsetStr,
            rahuKaal = rahuKaalStr,
            gulikaKaal = gulikaKaalStr,
            yamaganda = yamaKaalStr,
            abhijitMuhurat = abhijitStr,
            brahmaMuhurat = brahmaStr,
            sunSign = rashiNamesHi[sunSignIdx],
            moonSign = rashiNamesHi[moonSignIdx],
            locationName = city.cityNameHindi,
            latitude = city.latitude,
            longitude = city.longitude,
            planets = planetPositions
        )
    }

    private fun formatMinutesToTime(minutes: Int, use24Hour: Boolean = false): String {
        val m = (minutes + 1440) % 1440
        val hrs = m / 60
        val mins = m % 60
        if (use24Hour) {
            return String.format(Locale.getDefault(), "%02d:%02d", hrs, mins)
        }
        val ampm = if (hrs >= 12) "PM" else "AM"
        val displayHrs = if (hrs == 0) 12 else if (hrs > 12) hrs - 12 else hrs
        return String.format(Locale.getDefault(), "%02d:%02d %s", displayHrs, mins, ampm)
    }

    fun getMoonPhaseInfo(pakshaHindi: String, tithiHindi: String): MoonPhaseInfo {
        val isShukla = pakshaHindi.contains("शुक्ल", ignoreCase = true) || pakshaHindi.contains("Shukla", ignoreCase = true)
        val isPurnima = tithiHindi.contains("पूर्णिमा", ignoreCase = true) || tithiHindi.contains("Purnima", ignoreCase = true)
        val isAmavasya = tithiHindi.contains("अमावस्या", ignoreCase = true) || tithiHindi.contains("Amavasya", ignoreCase = true)

        if (isPurnima) {
            return MoonPhaseInfo(
                emoji = "🌕",
                nameHindi = "पूर्णिमा (Full Moon)",
                nameEn = "Full Moon",
                illuminationPercent = 100,
                isWaxing = true
            )
        }

        if (isAmavasya) {
            return MoonPhaseInfo(
                emoji = "🌑",
                nameHindi = "अमावस्या (New Moon)",
                nameEn = "New Moon",
                illuminationPercent = 0,
                isWaxing = false
            )
        }

        val tithiNum = when {
            tithiHindi.contains("प्रतिपदा") || tithiHindi.contains("Pratipada") -> 1
            tithiHindi.contains("द्वितीया") || tithiHindi.contains("Dwitiya") -> 2
            tithiHindi.contains("तृतीया") || tithiHindi.contains("Tritiya") -> 3
            tithiHindi.contains("चतुर्थी") || tithiHindi.contains("Chaturthi") -> 4
            tithiHindi.contains("पंचमी") || tithiHindi.contains("Panchami") -> 5
            tithiHindi.contains("षष्ठी") || tithiHindi.contains("Shasthi") -> 6
            tithiHindi.contains("सप्तमी") || tithiHindi.contains("Saptami") -> 7
            tithiHindi.contains("अष्टमी") || tithiHindi.contains("Ashtami") -> 8
            tithiHindi.contains("नवमी") || tithiHindi.contains("Navami") -> 9
            tithiHindi.contains("दशमी") || tithiHindi.contains("Dashami") -> 10
            tithiHindi.contains("एकादशी") || tithiHindi.contains("Ekadashi") -> 11
            tithiHindi.contains("द्वादशी") || tithiHindi.contains("Dwadashi") -> 12
            tithiHindi.contains("त्रयोदशी") || tithiHindi.contains("Trayodashi") -> 13
            tithiHindi.contains("चतुर्दशी") || tithiHindi.contains("Chaturdashi") -> 14
            else -> 7
        }

        return if (isShukla) {
            val approxIllumination = ((tithiNum / 15.0) * 100).toInt().coerceIn(5, 95)
            when (tithiNum) {
                in 1..3 -> MoonPhaseInfo("🌒", "शुक्ल पक्ष (Waxing Crescent)", "Waxing Crescent", approxIllumination, true)
                in 4..7 -> MoonPhaseInfo("🌓", "शुक्ल पक्ष (First Quarter)", "First Quarter", approxIllumination, true)
                in 8..11 -> MoonPhaseInfo("🌔", "शुक्ल पक्ष (Waxing Gibbous)", "Waxing Gibbous", approxIllumination, true)
                else -> MoonPhaseInfo("🌔", "शुक्ल पक्ष (Waxing Gibbous)", "Waxing Gibbous", approxIllumination, true)
            }
        } else {
            val approxIllumination = (((15 - tithiNum) / 15.0) * 100).toInt().coerceIn(5, 95)
            when (tithiNum) {
                in 1..3 -> MoonPhaseInfo("🌖", "कृष्ण पक्ष (Waning Gibbous)", "Waning Gibbous", approxIllumination, false)
                in 4..7 -> MoonPhaseInfo("🌗", "कृष्ण पक्ष (Third Quarter)", "Third Quarter", approxIllumination, false)
                in 8..11 -> MoonPhaseInfo("🌘", "कृष्ण पक्ष (Waning Crescent)", "Waning Crescent", approxIllumination, false)
                else -> MoonPhaseInfo("🌘", "कृष्ण पक्ष (Waning Crescent)", "Waning Crescent", approxIllumination, false)
            }
        }
    }
}

data class MoonPhaseInfo(
    val emoji: String,
    val nameHindi: String,
    val nameEn: String,
    val illuminationPercent: Int,
    val isWaxing: Boolean
)
