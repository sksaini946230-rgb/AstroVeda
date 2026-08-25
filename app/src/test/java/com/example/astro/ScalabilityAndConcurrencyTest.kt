package com.example.astro

import com.example.data.model.CityLocation
import com.example.util.SecurityUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.system.measureTimeMillis

/**
 * ScalabilityAndConcurrencyTest — Automated high-concurrency benchmark and chaos failure simulation.
 * Verifies:
 * 1. 1,000 concurrent Kundali generation requests on edge client (Sub-millisecond execution)
 * 2. 500 concurrent Ashtakoot 36-Guna matching computations
 * 3. 500 concurrent Panchang calculations across diverse dates and geographic coordinates
 * 4. Zero-Crash Chaos Failure Handling: Corrupted inputs, negative degrees, extreme boundary dates
 */
class ScalabilityAndConcurrencyTest {

    // --- 1. Concurrent Kundali Generation Load Test (1,000 Concurrent Computations) ---

    @Test
    fun testConcurrent1000KundaliCalculations_throughputAndZeroErrors() = runBlocking(Dispatchers.Default) {
        val totalUsers = 1000
        val cities = listOf(
            CityLocation("New Delhi", "नई दिल्ली", "Delhi", 28.6139, 77.2090),
            CityLocation("Mumbai", "मुंबई", "Maharashtra", 19.0760, 72.8777),
            CityLocation("Jaipur", "जयपुर", "Rajasthan", 26.9124, 75.7873),
            CityLocation("Varanasi", "वाराणसी", "Uttar Pradesh", 25.3176, 82.9739),
            CityLocation("Bengaluru", "बेंगलुरु", "Karnataka", 12.9716, 77.5946)
        )

        val durationMs = measureTimeMillis {
            val deferredList = (1..totalUsers).map { userIndex ->
                async {
                    val city = cities[userIndex % cities.size]
                    val day = (userIndex % 28) + 1
                    val dayStr = if (day < 10) "0$day" else "$day"
                    val dob = "199${userIndex % 10}-06-$dayStr"
                    val hour = userIndex % 24
                    val hourStr = if (hour < 10) "0$hour" else "$hour"
                    val tob = "$hourStr:30"

                    val chart = KundaliCalculator.generateKundali(
                        name = "User_$userIndex",
                        dobString = dob,
                        tobString = tob,
                        placeName = city.cityName,
                        lat = city.latitude,
                        lng = city.longitude
                    )
                    assertNotNull(chart)
                    assertEquals(9, chart.planets.size)
                    chart
                }
            }
            val results = deferredList.awaitAll()
            assertEquals("All 1,000 concurrent Kundali calculations must complete successfully", 1000, results.size)
        }

        val avgTimePerKundali = durationMs.toDouble() / totalUsers
        println("⚡ Scalability Benchmark: 1,000 Concurrent Kundalis calculated in ${durationMs}ms (Avg: ${"%.3f".format(avgTimePerKundali)}ms per chart)")
        assertTrue("1,000 Kundalis must execute within 3,000ms total on device", durationMs < 3000)
    }

    // --- 2. Concurrent 36-Guna Matching Load Test (500 Concurrent Computations) ---

    @Test
    fun testConcurrent500KundaliMatching_throughputAndZeroErrors() = runBlocking(Dispatchers.Default) {
        val totalMatches = 500

        val durationMs = measureTimeMillis {
            val deferredList = (1..totalMatches).map { matchIndex ->
                async {
                    val boyDob = "199${matchIndex % 10}-04-15"
                    val girlDob = "199${(matchIndex + 2) % 10}-08-20"

                    val matchResult = KundaliMatchingCalculator.matchKundali(
                        boyName = "Boy_$matchIndex",
                        boyDob = boyDob,
                        boyTob = "08:15",
                        girlName = "Girl_$matchIndex",
                        girlDob = girlDob,
                        girlTob = "14:45"
                    )
                    assertNotNull(matchResult)
                    assertTrue(matchResult.totalObtainedGuna in 0.0..36.0)
                    matchResult
                }
            }
            val results = deferredList.awaitAll()
            assertEquals(500, results.size)
        }

        println("⚡ Scalability Benchmark: 500 Concurrent 36-Guna Matches calculated in ${durationMs}ms")
        assertTrue("500 Matches must execute within 2,000ms", durationMs < 2000)
    }

    // --- 3. Concurrent Panchang Calculations (500 Dates & Coordinates) ---

    @Test
    fun testConcurrent500PanchangCalculations() = runBlocking(Dispatchers.Default) {
        val totalPanchang = 500
        val baseDate = Date()
        val jaipur = CityLocation("Jaipur", "जयपुर", "Rajasthan", 26.9124, 75.7873)

        val durationMs = measureTimeMillis {
            val deferredList = (1..totalPanchang).map { index ->
                async {
                    val dateOffset = Date(baseDate.time + (index * 86400000L))
                    val p = PanchangCalculator.calculatePanchang(dateOffset, jaipur)
                    assertNotNull(p)
                    assertFalse(p.tithiHindi.isBlank())
                    p
                }
            }
            val results = deferredList.awaitAll()
            assertEquals(500, results.size)
        }

        println("⚡ Scalability Benchmark: 500 Concurrent Panchang calculations in ${durationMs}ms")
        assertTrue("500 Panchang calculations must execute within 2,000ms", durationMs < 2000)
    }

    // --- 4. Chaos Failure & Recovery Testing ---

    @Test
    fun testChaosCorruptedInputs_noCrashAndSafeRecovery() {
        val corruptedInputs = listOf(
            "",
            "   ",
            "null",
            "<script>alert(1)</script>",
            "undefined-NaN-undefined",
            "9999-99-99",
            "-1995--05--20",
            "2026/08/25",
            "A".repeat(500)
        )

        for (corrupted in corruptedInputs) {
            // Sanitizer check
            val sanitized = SecurityUtils.sanitizeTextInput(corrupted)
            assertNotNull(sanitized)

            // Kundali Calculator must never crash on malformed dates/times
            val safeChart = KundaliCalculator.generateKundali(
                name = corrupted,
                dobString = corrupted,
                tobString = corrupted,
                placeName = corrupted
            )
            assertNotNull("Kundali generator must return safe fallback chart on corrupted input", safeChart)
            assertEquals(9, safeChart.planets.size)
        }
    }

    @Test
    fun testChaosExtremeGeographicCoordinates() {
        val extremeLocations = listOf(
            CityLocation("North Pole", "उत्तरी ध्रुव", "Arctic", 89.9, 0.0),
            CityLocation("South Pole", "दक्षिणी ध्रुव", "Antarctic", -89.9, 0.0),
            CityLocation("Equator", "भूमध्य रेखा", "Pacific", 0.0, 0.0),
            CityLocation("International Date Line", "तिथि रेखा", "Fiji", -17.7134, 178.0650)
        )

        val date = Date()
        for (loc in extremeLocations) {
            val p = PanchangCalculator.calculatePanchang(date, loc)
            assertNotNull(p)
            assertFalse(p.dateString.isBlank())
        }
    }
}
