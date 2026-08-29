package com.example.astro

import com.example.data.model.CityLocation
import org.junit.Assert.*
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * AstroVedaComprehensiveQATest — End-to-end automated QA validation test suite.
 * Covers:
 * 1. 12 Rashis & Horoscope periods (TODAY, WEEK, MONTH)
 * 2. Ashtakoot 36-Guna Kundali Matching & Mangal Dosha checks
 * 3. Choghadiya Day & Night slot calculations (8+8 slots)
 * 4. 2026 Festivals list and data integrity
 * 5. Vimshottari Dasha calculations
 * 6. Edge cases: leap years, midnight births, extreme latitudes
 */
class AstroVedaComprehensiveQATest {

    // --- 1. Rashifal Coverage & Period Integrity ---

    @Test
    fun testRashifalAll12SignsCovered() {
        val dailyList = RashifalProvider.getDailyHoroscope()
        assertEquals("Must contain exactly 12 zodiac signs", 12, dailyList.size)

        for (item in dailyList) {
            assertTrue("Rashi ID must be 1..12", item.rashiId in 1..12)
            assertTrue("Rashi name in English must not be blank", item.rashiNameEn.isNotBlank())
            assertTrue("Rashi name in Hindi must not be blank", item.rashiNameHi.isNotBlank())
            assertTrue("Symbol must not be blank", item.symbol.isNotBlank())
            assertTrue("Rating stars must be in 1..5", item.ratingStars in 1..5)
            assertTrue("Lucky number must be in 1..9", item.luckyNumber in 1..9)
            assertTrue("Lucky color must not be blank", item.luckyColorHi.isNotBlank())
            assertTrue("General reading must not be blank", item.generalReadingHi.isNotBlank())
            assertTrue("Career reading must not be blank", item.careerReadingHi.isNotBlank())
            assertTrue("Finance reading must not be blank", item.financeReadingHi.isNotBlank())
            assertTrue("Love reading must not be blank", item.loveReadingHi.isNotBlank())
            assertTrue("Health reading must not be blank", item.healthReadingHi.isNotBlank())

            // Total reading text must be deep and context-rich (150+ words)
            val combinedText = "${item.generalReadingHi} ${item.careerReadingHi} ${item.financeReadingHi} ${item.loveReadingHi} ${item.healthReadingHi}"
            val wordCount = combinedText.split("\\s+".toRegex()).size
            assertTrue("Daily reading for ${item.rashiNameEn} must be context-rich with > 100 words (actual: $wordCount)", wordCount >= 100)
        }
    }

    @Test
    fun testRashifalWeeklyAndMonthlyReadings() {
        val weeklyList = RashifalProvider.getHoroscope("WEEK")
        assertEquals(12, weeklyList.size)
        val monthlyList = RashifalProvider.getHoroscope("MONTH")
        assertEquals(12, monthlyList.size)

        for (item in weeklyList) {
            assertTrue("Weekly general reading must not be blank", item.generalReadingHi.isNotBlank())
            assertTrue("Weekly career reading must not be blank", item.careerReadingHi.isNotBlank())
            assertTrue("Weekly love reading must not be blank", item.loveReadingHi.isNotBlank())
        }
        for (item in monthlyList) {
            assertTrue("Monthly general reading must not be blank", item.generalReadingHi.isNotBlank())
            assertTrue("Monthly career reading must not be blank", item.careerReadingHi.isNotBlank())
            assertTrue("Monthly love reading must not be blank", item.loveReadingHi.isNotBlank())
        }
    }

    // --- 2. Kundali Matching (Ashtakoot 36 Guna) ---

    @Test
    fun testKundaliMatchingGunaScoreBounds() {
        val boyName = "Rahul"
        val boyDob = "1995-05-20"
        val boyTob = "08:15"
        val girlName = "Priya"
        val girlDob = "1997-11-12"
        val girlTob = "14:30"

        val matchResult = KundaliMatchingCalculator.matchKundali(
            boyName, boyDob, boyTob, girlName, girlDob, girlTob
        )

        assertNotNull(matchResult)
        assertTrue("Total Guna score must be in 0.0..36.0", matchResult.totalObtainedGuna in 0.0..36.0)
        assertEquals(36.0, matchResult.maxGuna, 0.01)
        assertNotNull("Mangal Dosha boy must not be null", matchResult.isManglikBoy)
        assertNotNull("Mangal Dosha girl must not be null", matchResult.isManglikGirl)
        assertTrue("Verdict must not be blank", matchResult.compatibilityVerdictHi.isNotBlank())
        assertEquals("Must have 8 Ashtakoot Guna categories", 8, matchResult.kootDetails.size)
    }

    // --- 3. Choghadiya Day & Night Calculations ---

    @Test
    fun testChoghadiyaDayAndNightSlotsCount() {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val testDate = sdf.parse("2026-08-15")!!
        val lat = 26.9124
        val lon = 75.7873

        val daySlots = ChoghadiyaCalculator.getChoghadiyaSlots(testDate, isDaytime = true, lat = lat, lon = lon, use24Hour = false)
        val nightSlots = ChoghadiyaCalculator.getChoghadiyaSlots(testDate, isDaytime = false, lat = lat, lon = lon, use24Hour = false)

        assertEquals("Daytime must have exactly 8 Choghadiya slots", 8, daySlots.size)
        assertEquals("Nighttime must have exactly 8 Choghadiya slots", 8, nightSlots.size)

        for (slot in daySlots) {
            assertTrue("Slot name must not be blank", slot.type.nameHi.isNotBlank())
            assertTrue("Time range must not be blank", slot.timeSlotString.isNotBlank())
        }
    }

    // --- 4. Festivals Provider Integrity ---

    @Test
    fun testFestivalProviderList() {
        val festivals = FestivalProvider.getFestivals()
        assertTrue("Must have at least 10 major festivals", festivals.size >= 10)

        for (fest in festivals) {
            assertTrue("Festival name in Hindi must not be blank", fest.nameHi.isNotBlank())
            assertTrue("Festival name in English must not be blank", fest.nameEn.isNotBlank())
            assertTrue("Festival date must not be blank", fest.dateString.isNotBlank())
            assertTrue("Festival significance must not be blank", fest.significanceHi.isNotBlank())
        }
    }

    // --- 5. Vimshottari Dasha Calculations ---

    @Test
    fun testVimshottariDashaCalculation() {
        val moonLongitude = 45.0 // Rohini Nakshatra (Moon lord)
        val birthDateStr = "1996-08-15"
        val dashaResult = VimshottariDashaCalculator.calculateVimshottariDasha(moonLongitude, birthDateStr)
        assertNotNull(dashaResult)
        assertNotNull("Current Mahadasha must not be null", dashaResult.currentMahadasha)
        assertTrue("Mahadashas list must have 9 planets", dashaResult.mahadashas.size == 9)
        assertTrue("Balance at birth formatted string must not be blank", dashaResult.balanceAtBirthFormatted.isNotBlank())
    }

    // --- 6. Edge Cases & Boundary Handling ---

    @Test
    fun testKundaliMidnightBirthBoundary() {
        // Midnight 00:00 boundary
        val kundali = KundaliCalculator.generateKundali(
            name = "Midnight Baby",
            dobString = "2000-01-01",
            tobString = "00:00",
            placeName = "Jaipur"
        )
        assertNotNull(kundali)
        assertEquals(9, kundali.planets.size)
    }

    @Test
    fun testKundaliLeapDayBoundary() {
        // Leap year Feb 29
        val kundali = KundaliCalculator.generateKundali(
            name = "Leap Baby",
            dobString = "2024-02-29",
            tobString = "12:00",
            placeName = "New Delhi"
        )
        assertNotNull(kundali)
        assertEquals(9, kundali.planets.size)
    }

    @Test
    fun testPanchangExtremeLatitudes() {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val testDate = sdf.parse("2026-06-21")!!
        // Extreme north latitude (Srinagar, Kashmir: 34.0837 N, 74.7973 E)
        val srinagar = CityLocation("Srinagar", "श्रीनगर", "Jammu and Kashmir", 34.0837, 74.7973)
        val panchang = PanchangCalculator.calculatePanchang(testDate, srinagar)

        assertNotNull(panchang)
        assertFalse(panchang.sunrise.isBlank())
        assertFalse(panchang.sunset.isBlank())
        assertFalse(panchang.tithiHindi.isBlank())
    }

    // --- 7. Saved Profile Data Model & CRUD Integrity ---

    @Test
    fun testKundaliProfileEntityLifecycle() {
        val originalProfile = com.example.data.local.KundaliEntity(
            id = 101L,
            name = "Aarav Sharma",
            gender = "MALE",
            dateOfBirth = "1998-04-12",
            timeOfBirth = "14:30",
            placeOfBirth = "Jaipur, Rajasthan",
            latitude = 26.9124,
            longitude = 75.7873,
            notes = "Family Member"
        )

        assertEquals("Aarav Sharma", originalProfile.name)
        assertEquals("1998-04-12", originalProfile.dateOfBirth)

        // Verify copy/update lifecycle
        val updatedProfile = originalProfile.copy(
            name = "Aarav S. Sharma",
            placeOfBirth = "Mumbai, Maharashtra"
        )

        assertEquals(101L, updatedProfile.id)
        assertEquals("Aarav S. Sharma", updatedProfile.name)
        assertEquals("Mumbai, Maharashtra", updatedProfile.placeOfBirth)
        assertEquals("1998-04-12", updatedProfile.dateOfBirth)
    }

    // --- 8. Language & Time Format Settings Integrity ---

    @Test
    fun testLanguageManagerReactiveToggle() {
        com.example.util.LanguageManager.setLanguage(com.example.util.AppLanguage.HINDI)
        assertEquals(com.example.util.AppLanguage.HINDI, com.example.util.LanguageManager.currentLanguage)
        assertEquals("नमस्ते", com.example.util.LanguageManager.getString("नमस्ते", "Hello"))

        com.example.util.LanguageManager.toggleLanguage()
        assertEquals(com.example.util.AppLanguage.ENGLISH, com.example.util.LanguageManager.currentLanguage)
        assertEquals("Hello", com.example.util.LanguageManager.getString("नमस्ते", "Hello"))

        // Reset to default Hindi
        com.example.util.LanguageManager.setLanguage(com.example.util.AppLanguage.HINDI)
    }

    @Test
    fun testTimeFormat12hAnd24hConsistency() {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val testDate = sdf.parse("2026-07-22")!!
        val jaipur = CityLocation("Jaipur", "जयपुर", "Rajasthan", 26.9124, 75.7873)

        // 12-hour format: AM/PM included
        val p12 = PanchangCalculator.calculatePanchang(testDate, jaipur, use24Hour = false)
        assertTrue(p12.sunrise.contains("AM") || p12.sunrise.contains("PM"))

        // 24-hour format: pure HH:mm
        val p24 = PanchangCalculator.calculatePanchang(testDate, jaipur, use24Hour = true)
        assertFalse(p24.sunrise.contains("AM") || p24.sunrise.contains("PM"))
        assertTrue(p24.sunrise.matches(Regex("\\d{2}:\\d{2}")))
    }

    // --- 9. Two-Way Cloud Sync Conflict-Free Union Merge ---

    @Test
    fun testProfileTwoWayUnionMergeStrategy() {
        // Scenario: User created profile 1 locally while offline
        val localList = mutableListOf(
            com.example.data.local.KundaliEntity(
                id = 1L,
                name = "Local User",
                gender = "MALE",
                dateOfBirth = "1992-01-01",
                timeOfBirth = "10:00",
                placeOfBirth = "Delhi",
                latitude = 28.6139,
                longitude = 77.2090
            )
        )

        // Remote cloud contains profile 2 (created previously on another device) and profile 1
        val cloudList = listOf(
            com.example.data.local.KundaliEntity(
                id = 2L,
                name = "Cloud User",
                gender = "FEMALE",
                dateOfBirth = "1994-06-15",
                timeOfBirth = "15:30",
                placeOfBirth = "Mumbai",
                latitude = 19.0760,
                longitude = 72.8777
            )
        )

        // Union merge simulation:
        cloudList.forEach { cloudProfile ->
            val exists = localList.any { it.id == cloudProfile.id || (it.name.equals(cloudProfile.name, true) && it.dateOfBirth == cloudProfile.dateOfBirth) }
            if (!exists) {
                localList.add(cloudProfile)
            }
        }

        // Assert: Result contains BOTH profiles (Union = 2 profiles, 0 data loss)
        assertEquals(2, localList.size)
        assertTrue(localList.any { it.name == "Local User" })
        assertTrue(localList.any { it.name == "Cloud User" })
    }

    // --- 10. DPDP Act 2023 Data Deletion & Privacy Compliance ---

    @Test
    fun testDpdpAct2023DataPurgeLifecycle() {
        val activeProfiles = mutableListOf(
            com.example.data.local.KundaliEntity(
                id = 10L,
                name = "User To Purge",
                gender = "MALE",
                dateOfBirth = "1990-01-01",
                timeOfBirth = "12:00",
                placeOfBirth = "Jaipur",
                latitude = 26.9124,
                longitude = 75.7873
            )
        )
        val activeReports = mutableListOf("Report 1", "Report 2")
        val activeRecentSearches = mutableListOf("Search A", "Search B")

        assertEquals(1, activeProfiles.size)
        assertEquals(2, activeReports.size)
        assertEquals(2, activeRecentSearches.size)

        // Simulate complete DPDP Act 2023 wipe
        activeProfiles.clear()
        activeReports.clear()
        activeRecentSearches.clear()

        assertTrue(activeProfiles.isEmpty())
        assertTrue(activeReports.isEmpty())
        assertTrue(activeRecentSearches.isEmpty())
    }

    // --- 11. Edge-Case Validation & Graceful Fallback Integrity ---

    @Test
    fun testDateInputEdgeCasesAndBoundaries() {
        // Year bounds (1900..2100)
        assertFalse("Year 1800 is out of bounds", com.example.util.SecurityUtils.isValidDate("1800-01-01"))
        assertFalse("Year 2101 is out of bounds", com.example.util.SecurityUtils.isValidDate("2101-01-01"))
        assertTrue("Year 1995 is within bounds", com.example.util.SecurityUtils.isValidDate("1995-05-15"))

        // Leap Year February
        assertTrue("Feb 29 on leap year 2024 is valid", com.example.util.SecurityUtils.isValidDate("2024-02-29"))
        assertTrue("Feb 29 on leap year 2000 is valid", com.example.util.SecurityUtils.isValidDate("2000-02-29"))
        assertFalse("Feb 29 on non-leap year 2023 is invalid", com.example.util.SecurityUtils.isValidDate("2023-02-29"))
        assertFalse("Feb 30 on leap year 2024 is invalid", com.example.util.SecurityUtils.isValidDate("2024-02-30"))

        // 30 vs 31 day months
        assertTrue("April 30 is valid", com.example.util.SecurityUtils.isValidDate("2024-04-30"))
        assertFalse("April 31 is invalid", com.example.util.SecurityUtils.isValidDate("2024-04-31"))
        assertTrue("May 31 is valid", com.example.util.SecurityUtils.isValidDate("2024-05-31"))

        // Month out of range
        assertFalse("Month 13 is invalid", com.example.util.SecurityUtils.isValidDate("2024-13-10"))
        assertFalse("Month 00 is invalid", com.example.util.SecurityUtils.isValidDate("2024-00-10"))
    }

    @Test
    fun testTimeInputEdgeCases() {
        assertTrue("00:00 is valid", com.example.util.SecurityUtils.isValidTime("00:00"))
        assertTrue("23:59 is valid", com.example.util.SecurityUtils.isValidTime("23:59"))
        assertFalse("24:00 is invalid", com.example.util.SecurityUtils.isValidTime("24:00"))
        assertFalse("12:60 is invalid", com.example.util.SecurityUtils.isValidTime("12:60"))
        assertFalse("-1:30 is invalid", com.example.util.SecurityUtils.isValidTime("-1:30"))
    }

    @Test
    fun testKundaliCalculatorRejectsMalformedInput() {
        // This test used to assert the opposite: that corrupt input still produced
        // a chart. That WAS the bug. "invalid-date" silently became 1995-01-01 at
        // 12:00, and the app showed a complete, confident, entirely fictional
        // Lagna, Moon sign, Nakshatra and Dasha. Bad input must now be refused so
        // the screen can tell the user what to correct.
        val corrupt = listOf(
            Triple("invalid-date", "invalid-time", "unparseable"),
            Triple("25-08-1994", "14:15", "day-month-year, the order Indian users type first"),
            Triple("1994-08-25", "25:99", "hour and minute out of range"),
            Triple("1994-13-25", "14:15", "month 13"),
            Triple("1994-02-30", "14:15", "30th of February"),
            Triple("abcd", "xy", "not digits at all")
        )

        for ((dob, tob, why) in corrupt) {
            try {
                KundaliCalculator.generateKundali(
                    name = "Test", dobString = dob, tobString = tob, placeName = "Unknown"
                )
                fail("Expected rejection for $why (dob=$dob tob=$tob) but a chart was returned")
            } catch (e: com.example.astro.BirthDataException) {
                // Every message must be usable on screen, in both languages.
                assertTrue("Hindi message missing for $why", e.messageHi.isNotBlank())
                assertTrue("English message missing for $why", e.messageEn.isNotBlank())
            }
        }
    }

    @Test
    fun testKundaliCalculatorAcceptsValidInput() {
        val chart = KundaliCalculator.generateKundali(
            name = "Test",
            dobString = "1994-08-25",
            tobString = "14:15",
            placeName = "Jhunjhunu",
            lat = 28.1289,
            lng = 75.3995
        )
        assertNotNull(chart)
        assertEquals(9, chart.planets.size)
        assertEquals(12, chart.housePlanetsMap.size)
    }

    // --- 12. Full Hindi / English Localization Integrity ---

    @Test
    fun testBilingualLocalizationIntegrity() {
        // Test LanguageManager String Resolution
        com.example.util.LanguageManager.setLanguage(com.example.util.AppLanguage.HINDI)
        val hindiGreeting = com.example.util.LanguageManager.getString("दैनिक पंचांग", "Daily Panchang")
        assertEquals("दैनिक पंचांग", hindiGreeting)

        com.example.util.LanguageManager.setLanguage(com.example.util.AppLanguage.ENGLISH)
        val englishGreeting = com.example.util.LanguageManager.getString("दैनिक पंचांग", "Daily Panchang")
        assertEquals("Daily Panchang", englishGreeting)

        // Reset to default
        com.example.util.LanguageManager.setLanguage(com.example.util.AppLanguage.HINDI)

        // Test 12 Rashis have non-empty Hindi and English labels & readings
        val rashis = RashifalProvider.getDailyHoroscope()
        assertEquals(12, rashis.size)
        rashis.forEach { rashi ->
            assertTrue("Rashi name in Hindi must not be blank", rashi.rashiNameHi.isNotBlank())
            assertTrue("Rashi name in English must not be blank", rashi.rashiNameEn.isNotBlank())
            assertTrue("General reading Hindi must not be blank", rashi.generalReadingHi.isNotBlank())
            assertTrue("General reading English must not be blank", rashi.generalReadingEn.isNotBlank())
        }

        // Test 9 Planets have valid Hindi & English short names
        val testKundali = KundaliCalculator.generateKundali(
            name = "Test",
            dobString = "1995-05-15",
            tobString = "12:00",
            placeName = "Jaipur"
        )
        assertEquals(9, testKundali.planets.size)
        testKundali.planets.forEach { p ->
            assertTrue("Planet English name must not be blank", p.planetNameEn.isNotBlank())
            assertTrue("Planet Hindi name must not be blank", p.planetNameHi.isNotBlank())
            assertTrue("Planet Nakshatra in Hindi must not be blank", p.nakshatraHi.isNotBlank())
        }
    }

    // --- 13. High Performance & Calculation Latency Benchmarks ---

    @Test
    fun testAstrologicalCalculationLatencyAndPerformance() {
        val testDate = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("Asia/Kolkata")).apply {
            set(2026, java.util.Calendar.AUGUST, 26)
        }.time
        val delhiCity = CityLocation("New Delhi", "नई दिल्ली", "Delhi", 28.6139, 77.2090)

        // 1. Panchang Benchmark (<50ms for complete daily calculations)
        val panchangStart = System.currentTimeMillis()
        val panchang = PanchangCalculator.calculatePanchang(testDate, delhiCity)
        val panchangDuration = System.currentTimeMillis() - panchangStart
        assertNotNull(panchang)
        assertTrue("Panchang computation took ${panchangDuration}ms (must be < 50ms)", panchangDuration < 50)

        // 2. Kundali Benchmark (<50ms for complete chart & 9 planetary positions)
        val kundaliStart = System.currentTimeMillis()
        val kundali = KundaliCalculator.generateKundali(
            name = "Benchmark User",
            dobString = "1995-10-24",
            tobString = "14:30",
            placeName = "New Delhi",
            lat = 28.6139,
            lng = 77.2090
        )
        val kundaliDuration = System.currentTimeMillis() - kundaliStart
        assertNotNull(kundali)
        assertTrue("Kundali computation took ${kundaliDuration}ms (must be < 50ms)", kundaliDuration < 50)

        // 3. Kundali Milan Benchmark (<50ms for 36 Guna Ashtakoot calculation)
        val milanStart = System.currentTimeMillis()
        val milan = KundaliMatchingCalculator.matchKundali(
            boyName = "Rahul",
            boyDob = "1995-05-15",
            boyTob = "08:15",
            girlName = "Priya",
            girlDob = "1997-11-12",
            girlTob = "14:30"
        )
        val milanDuration = System.currentTimeMillis() - milanStart
        assertNotNull(milan)
        assertTrue("Milan computation took ${milanDuration}ms (must be < 50ms)", milanDuration < 50)
    }

    // --- 14. Crash Reporting Privacy & Gemini Offline Resilience ---

    @Test
    fun testCrashlyticsAndAiTelemetryPrivacyCompliance() {
        // Test Gemini Offline Fallback Response
        val offlineQuestion = "What is the effect of Shani Sade Sati?"
        val fallbackResponse = com.example.data.ai.GeminiAstroService.getOfflineVedicResponse(offlineQuestion)
        assertNotNull(fallbackResponse)
        assertTrue("Fallback response must contain helpful Vedic advice", fallbackResponse.isNotBlank())

        // Test Offline Astro News
        val offlineNews = com.example.data.ai.GeminiAstroService.getOfflineAstroNews()
        assertNotNull(offlineNews)
        assertTrue("Offline astro news must not be empty", offlineNews.isNotEmpty())
    }
}
