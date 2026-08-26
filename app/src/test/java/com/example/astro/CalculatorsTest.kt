package com.example.astro

import com.example.data.model.CityLocation
import org.junit.Assert.*
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.abs

class CalculatorsTest {

    @Test
    fun testNumerologyCalculator() {
        val result = NumerologyCalculator.calculateNumerology("Rahul", "1995-05-20")
        assertNotNull(result)
        // Check if Psychic number and Destiny number are calculated correctly
        // DOB = 20 -> 2+0 = 2 (Psychic / Moolank)
        // 20+05+1995 -> 2+0+0+5+1+9+9+5 = 31 -> 3+1 = 4 (Destiny / Bhagyank)
        assertEquals(2, result.moolank)
        assertEquals(4, result.bhagyank)
    }

    @Test
    fun testKundaliCalculatorAccuracy() {
        // Sample 1: Jaipur, 1995-05-20, 08:15 AM
        val name1 = "Sample Native 1"
        val dob1 = "1995-05-20"
        val tob1 = "08:15"
        val place1 = "Jaipur"
        val lat1 = 26.9124
        val lng1 = 75.7873

        val kundali1 = KundaliCalculator.generateKundali(name1, dob1, tob1, place1, lat1, lng1)
        assertNotNull(kundali1)
        assertEquals(name1, kundali1.personName)
        assertEquals(dob1, kundali1.dateOfBirth)
        assertEquals(tob1, kundali1.timeOfBirth)
        assertEquals(9, kundali1.planets.size)
        assertTrue(kundali1.ascendantRashiNumber in 1..12)

        for (planet in kundali1.planets) {
            assertTrue(
                "Planet ${planet.planetNameEn} degree ${planet.degree} must be within [0.0, 30.0)",
                planet.degree in 0.0..30.0
            )
            assertTrue(
                "Planet ${planet.planetNameEn} rashiNumber ${planet.rashiNumber} must be in range [1, 12]",
                planet.rashiNumber in 1..12
            )
            assertTrue(
                "Planet ${planet.planetNameEn} houseNumber ${planet.houseNumber} must be in range [1, 12]",
                planet.houseNumber in 1..12
            )
        }

        // Verify Rahu and Ketu are exactly 180 degrees apart in longitude
        val rahu1 = kundali1.planets.first { it.planetNameEn == "Rahu" }
        val ketu1 = kundali1.planets.first { it.planetNameEn == "Ketu" }
        val rahuAbsLon1 = (rahu1.rashiNumber - 1) * 30.0 + rahu1.degree
        val ketuAbsLon1 = (ketu1.rashiNumber - 1) * 30.0 + ketu1.degree
        val longitudeDiff1 = abs(rahuAbsLon1 - ketuAbsLon1)
        val modDiff1 = (longitudeDiff1 % 360.0 + 360.0) % 360.0
        assertEquals(180.0, modDiff1, 0.01)
        assertTrue("Rahu must be flagged retrograde", rahu1.isRetrograde)
        assertTrue("Ketu must be flagged retrograde", ketu1.isRetrograde)

        // Verify Vimshottari Mahadasha timeline has 9 periods
        assertEquals(9, kundali1.dashaTimeline.size)
        assertTrue(kundali1.currentMahadashaHi.isNotBlank())

        // Sample 2: New Delhi, 1990-01-15, 06:00 AM
        val kundali2 = KundaliCalculator.generateKundali("Sample Native 2", "1990-01-15", "06:00", "New Delhi", 28.6139, 77.2090)
        assertNotNull(kundali2)
        assertEquals(9, kundali2.planets.size)
        val sun2 = kundali2.planets.first { it.planetNameEn == "Sun" }
        assertEquals("मकर", sun2.rashiNameHi) // Sun in Capricorn in mid-January

        // Sample 3: Mumbai, 2000-08-25, 12:00 PM
        val kundali3 = KundaliCalculator.generateKundali("Sample Native 3", "2000-08-25", "12:00", "Mumbai", 19.0760, 72.8777)
        assertNotNull(kundali3)
        assertEquals(9, kundali3.planets.size)
        val sun3 = kundali3.planets.first { it.planetNameEn == "Sun" }
        assertEquals("सिंह", sun3.rashiNameHi) // Sun in Leo after Simha Sankranti in late August
    }

    @Test
    fun testPanchangCalculatorAccuracy() {
        // Date: 2026-07-22 (July 22, 2026)
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val testDate = sdf.parse("2026-07-22")!!
        val delhi = CityLocation("New Delhi", "नई दिल्ली", "Delhi", 28.6139, 77.2090)

        val panchang = PanchangCalculator.calculatePanchang(testDate, delhi)
        assertNotNull(panchang)

        // 1. Verify Vikram Samvat (Vedic constant offset: ~57 years from Gregorian)
        // For July 2026, Vikram Samvat is 2083
        assertEquals(2083, panchang.vikramSamvat)

        // 2. Verify Saka Samvat (Vedic constant offset: ~78 years from Gregorian)
        // For July 2026, Saka Samvat is 1948
        assertEquals(1948, panchang.sakaSamvat)

        // 3. Verify core Panchang strings are populated and non-empty
        assertFalse(panchang.tithiHindi.isEmpty())
        assertFalse(panchang.nakshatraHindi.isEmpty())
        assertFalse(panchang.yogaHindi.isEmpty())
        assertFalse(panchang.karanHindi.isEmpty())
        assertFalse(panchang.dayOfWeekHindi.isEmpty())

        // 4. Verify Sun/Moon rise and set times are computed
        assertTrue(panchang.sunrise.contains(":"))
        assertTrue(panchang.sunset.contains(":"))
        assertTrue(panchang.moonrise.contains(":"))
        assertTrue(panchang.moonset.contains(":"))
        assertTrue(panchang.rahuKaal.contains("-"))
        assertTrue(panchang.abhijitMuhurat.contains("-"))

        // 5. Verify location sensitivity: Kolkata (East) sunrise must be earlier than Jaipur (West)
        val kolkata = CityLocation("Kolkata", "कोलकाता", "West Bengal", 22.5726, 88.3639)
        val jaipur = CityLocation("Jaipur", "जयपुर", "Rajasthan", 26.9124, 75.7873)
        val pKolkata = PanchangCalculator.calculatePanchang(testDate, kolkata, use24Hour = true)
        val pJaipur = PanchangCalculator.calculatePanchang(testDate, jaipur, use24Hour = true)

        val kolkataSunriseMinutes = pKolkata.sunrise.split(":")[0].toInt() * 60 + pKolkata.sunrise.split(":")[1].toInt()
        val jaipurSunriseMinutes = pJaipur.sunrise.split(":")[0].toInt() * 60 + pJaipur.sunrise.split(":")[1].toInt()
        assertTrue("Kolkata sunrise ($kolkataSunriseMinutes mins) must be earlier than Jaipur sunrise ($jaipurSunriseMinutes mins)", kolkataSunriseMinutes < jaipurSunriseMinutes)
    }

    @Test
    fun testPanchangTimeFormat12hAnd24h() {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val testDate = sdf.parse("2026-07-22")!!
        val delhi = CityLocation("New Delhi", "नई दिल्ली", "Delhi", 28.6139, 77.2090)

        val panchang12 = PanchangCalculator.calculatePanchang(testDate, delhi, use24Hour = false)
        val panchang24 = PanchangCalculator.calculatePanchang(testDate, delhi, use24Hour = true)

        assertNotNull(panchang12)
        assertNotNull(panchang24)

        // 12-hour format contains AM or PM
        assertTrue(panchang12.sunrise.contains("AM") || panchang12.sunrise.contains("PM"))
        assertTrue(panchang12.sunset.contains("AM") || panchang12.sunset.contains("PM"))

        // 24-hour format does NOT contain AM or PM and matches HH:mm format
        assertFalse(panchang24.sunrise.contains("AM") || panchang24.sunrise.contains("PM"))
        assertFalse(panchang24.sunset.contains("AM") || panchang24.sunset.contains("PM"))
        assertTrue(panchang24.sunrise.matches(Regex("\\d{2}:\\d{2}")))
        assertTrue(panchang24.sunset.matches(Regex("\\d{2}:\\d{2}")))
    }

    @Test
    fun testKundaliMatchingCalculatorGunaScores() {
        val boyName = "Rahul"
        val boyDob = "1995-05-20"
        val boyTob = "08:15"
        
        val girlName = "Anjali"
        val girlDob = "1997-11-12"
        val girlTob = "14:30"

        val matchingResult = KundaliMatchingCalculator.matchKundali(
            boyName, boyDob, boyTob,
            girlName, girlDob, girlTob
        )

        assertNotNull(matchingResult)
        assertEquals(boyName, matchingResult.boyName)
        assertEquals(girlName, matchingResult.girlName)

        // 1. Verify that total obtained Guna score is between 0 and 36 (Vedic Constant limits)
        assertTrue(
            "Obtained Guna score ${matchingResult.totalObtainedGuna} must be between 0.0 and 36.0",
            matchingResult.totalObtainedGuna in 0.0..36.0
        )

        // 2. Verify Ashtakoota max points constant: sum of all Koota weights must equal 36.0
        val sumOfMaxPoints = matchingResult.kootDetails.sumOf { it.maxPoints }
        assertEquals(36.0, sumOfMaxPoints, 0.001)

        // 3. Verify individual Koota weight constants:
        // Varna (1), Vashya (2), Tara (3), Yoni (4), Graha Maitri (5), Gana (6), Bhakoot (7), Nadi (8)
        val varna = matchingResult.kootDetails.first { it.kootNameEn == "Varna" }
        val vashya = matchingResult.kootDetails.first { it.kootNameEn == "Vashya" }
        val tara = matchingResult.kootDetails.first { it.kootNameEn == "Tara" }
        val yoni = matchingResult.kootDetails.first { it.kootNameEn == "Yoni" }
        val grahaMaitri = matchingResult.kootDetails.first { it.kootNameEn == "Graha Maitri" }
        val gana = matchingResult.kootDetails.first { it.kootNameEn == "Gana" }
        val bhakoot = matchingResult.kootDetails.first { it.kootNameEn == "Bhakoot" }
        val nadi = matchingResult.kootDetails.first { it.kootNameEn == "Nadi" }

        assertEquals(1.0, varna.maxPoints, 0.01)
        assertEquals(2.0, vashya.maxPoints, 0.01)
        assertEquals(3.0, tara.maxPoints, 0.01)
        assertEquals(4.0, yoni.maxPoints, 0.01)
        assertEquals(5.0, grahaMaitri.maxPoints, 0.01)
        assertEquals(6.0, gana.maxPoints, 0.01)
        assertEquals(7.0, bhakoot.maxPoints, 0.01)
        assertEquals(8.0, nadi.maxPoints, 0.01)

        // 4. Verify Manglik status calculation logic matches actual planetary placements (Mars in house 1, 4, 7, 8, 12)
        val boyChart = KundaliCalculator.generateKundali(boyName, boyDob, boyTob, "Default")
        val girlChart = KundaliCalculator.generateKundali(girlName, girlDob, girlTob, "Default")

        val boyMarsHouse = boyChart.planets.find { it.planetNameEn == "Mars" }?.houseNumber ?: 0
        val girlMarsHouse = girlChart.planets.find { it.planetNameEn == "Mars" }?.houseNumber ?: 0

        val manglikHouses = listOf(1, 4, 7, 8, 12)
        val isBoyManglikExpected = boyMarsHouse in manglikHouses
        val isGirlManglikExpected = girlMarsHouse in manglikHouses

        assertEquals(isBoyManglikExpected, matchingResult.isManglikBoy)
        assertEquals(isGirlManglikExpected, matchingResult.isManglikGirl)

        // 5. Verify Nadi & Bhakoot Dosha flags consistency
        val nadiKoot = matchingResult.kootDetails.first { it.kootNameEn == "Nadi" }
        assertEquals(nadiKoot.obtainedPoints == 0.0, matchingResult.hasNadiDosha)

        val bhakootKoot = matchingResult.kootDetails.first { it.kootNameEn == "Bhakoot" }
        assertEquals(bhakootKoot.obtainedPoints == 0.0, matchingResult.hasBhakootDosha)

        // 6. Verify 4-tier score category
        when {
            matchingResult.totalObtainedGuna >= 33.0 -> assertEquals("EXCELLENT", matchingResult.scoreCategory)
            matchingResult.totalObtainedGuna >= 25.0 -> assertEquals("GOOD", matchingResult.scoreCategory)
            matchingResult.totalObtainedGuna >= 18.0 -> assertEquals("AVERAGE", matchingResult.scoreCategory)
            else -> assertEquals("POOR", matchingResult.scoreCategory)
        }
    }

    @Test
    fun testNadiAndBhakootDoshaDetection() {
        // Sample Pair 1: Rohan & Pooja
        val result1 = KundaliMatchingCalculator.matchKundali(
            "Rohan", "1993-04-14", "10:30",
            "Pooja", "1995-08-22", "16:45"
        )
        assertNotNull(result1)
        assertTrue(result1.totalObtainedGuna in 0.0..36.0)
        assertNotNull(result1.nadiDoshaStatusHi)
        assertNotNull(result1.bhakootDoshaStatusHi)
        assertEquals(8, result1.kootDetails.size)

        // Sample Pair 2: Verify same birth details produce identical Nadi and trigger Nadi Dosha
        val resultSame = KundaliMatchingCalculator.matchKundali(
            "Twin Boy", "1995-05-20", "08:15",
            "Twin Girl", "1995-05-20", "08:15"
        )
        assertTrue("Identical birth details must have identical Nadi and thus Nadi Dosha", resultSame.hasNadiDosha)
        assertEquals(0.0, resultSame.kootDetails.first { it.kootNameEn == "Nadi" }.obtainedPoints, 0.01)
    }

    @Test
    fun testVimshottariDashaCalculator() {
        // 1. Verify Vimshottari total cycle duration constant is exactly 120 years
        val totalYears = VimshottariDashaCalculator.VIMSHOTTARI_PLANETS.sumOf { it.durationYears }
        assertEquals(120.0, totalYears, 0.001)

        // 2. Test Nakshatra and Dasha lord mapping
        // Longitude 0.0° -> Ashwini (Index 0) -> Ketu (7 yrs)
        val nakshatraAshwini = VimshottariDashaCalculator.getNakshatraInfo(0.0)
        assertEquals(0, nakshatraAshwini.index)
        assertEquals("अश्विनी", nakshatraAshwini.nameHi)
        assertEquals("Ketu", nakshatraAshwini.lordNameEn)

        // Longitude 20.0° -> Bharani (Index 1) -> Venus (20 yrs)
        val nakshatraBharani = VimshottariDashaCalculator.getNakshatraInfo(20.0)
        assertEquals(1, nakshatraBharani.index)
        assertEquals("भरणी", nakshatraBharani.nameHi)
        assertEquals("Venus", nakshatraBharani.lordNameEn)

        // 3. Test full timeline calculation for DOB 1995-05-20 and Moon at 0.0° (Ashwini start)
        val dashaResult = VimshottariDashaCalculator.calculateVimshottariDasha(
            moonLongitude = 0.0,
            birthDateStr = "1995-05-20"
        )

        assertNotNull(dashaResult)
        assertEquals(9, dashaResult.mahadashas.size)

        // At 0.0°, full 7 years of Ketu Mahadasha remain at birth
        assertEquals("Ketu", dashaResult.mahadashas[0].planetEn)
        assertEquals(7.0, dashaResult.mahadashas[0].durationYears, 0.1)

        // Each Mahadasha must contain 9 Antardashas
        for (mahadasha in dashaResult.mahadashas) {
            assertEquals(9, mahadasha.antardashas.size)
        }

        // Check active Mahadasha for current time
        assertNotNull(dashaResult.currentMahadasha)
    }

    @Test
    fun testChoghadiyaAndMuhuratCalculator() {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val testDate = sdf.parse("2026-07-26")!!
        val city = CityLocation("Jaipur", "जयपुर", "Rajasthan", 26.9124, 75.7873)

        val daySlots = ChoghadiyaCalculator.getChoghadiyaSlots(testDate, isDaytime = true, lat = city.latitude, lon = city.longitude)
        assertNotNull(daySlots)
        assertEquals(8, daySlots.size)

        for (slot in daySlots) {
            assertTrue(slot.timeSlotString.contains("-"))
            assertFalse(slot.rulerPlanetHi.isEmpty())
        }

        val upcomingMuhurats = MuhuratCalculator.getUpcomingMuhurats()
        assertNotNull(upcomingMuhurats)
        assertTrue("Upcoming muhurats list should contain auspicious dates", upcomingMuhurats.isNotEmpty())
    }
}
