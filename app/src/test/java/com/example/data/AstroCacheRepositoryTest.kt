package com.example.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.AstroCacheRepository
import com.example.data.local.HoroscopeCacheEntity
import com.example.data.model.CityLocation
import com.example.util.AppLanguage
import com.example.util.LanguageManager
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar

/**
 * What the seven-day caches are allowed to hand back.
 *
 * Both of these were wrong in the same direction — the cache answering a
 * question slightly wider than the one being asked — and neither is visible
 * from the screen, because a stale Rashifal and a fresh one look exactly alike.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AstroCacheRepositoryTest {

    private lateinit var db: AppDatabase
    private lateinit var repo: AstroCacheRepository

    private val jaipur = CityLocation("Jaipur", "जयपुर", "Rajasthan", 26.9124, 75.7873)

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        @Suppress("OPT_IN_USAGE")
        repo = AstroCacheRepository(
            db.panchangCacheDao(),
            db.horoscopeCacheDao(),
            UnconfinedTestDispatcher()
        )
        LanguageManager.setLanguage(AppLanguage.HINDI)
    }

    @After
    fun tearDown() {
        db.close()
        LanguageManager.setLanguage(AppLanguage.HINDI)
    }

    private fun today() = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(Date())

    private fun yesterday(): String {
        val cal = GregorianCalendar().apply { add(Calendar.DAY_OF_MONTH, -1) }
        return java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(cal.time)
    }

    private fun row(rashiId: Int, periodKey: String, marker: String) = HoroscopeCacheEntity(
        cacheKey = "${rashiId}_TODAY_$periodKey",
        rashiId = rashiId,
        rashiNameEn = "Sign $rashiId",
        rashiNameHi = "राशि $rashiId",
        symbol = "♈",
        elementHi = "अग्नि (Fire)",
        rulerHi = "मंगल (Mars)",
        ratingStars = 3,
        luckyNumber = 1,
        luckyColorEn = "Red",
        luckyColorHi = "लाल",
        luckyStoneHi = "मूंगा",
        generalReadingHi = marker,
        generalReadingEn = marker,
        careerReadingHi = marker,
        careerReadingEn = marker,
        healthReadingHi = marker,
        healthReadingEn = marker,
        loveReadingHi = marker,
        loveReadingEn = marker,
        financeReadingHi = marker,
        financeReadingEn = marker,
        period = "TODAY",
        // Written yesterday, so still inside the seven-day window.
        cachedAtTimestamp = System.currentTimeMillis() - 20 * 60 * 60 * 1000L
    )

    /**
     * "आज का राशिफल" was served from whenever it was first computed. The lookup
     * matched on the period and a seven-day window, so yesterday's twelve rows
     * were still valid this morning and were handed back as today's — the daily
     * horoscope actually changed once a week.
     */
    @Test
    fun `yesterday's daily horoscope is not served as today's`() = runTest {
        (1..12).forEach { db.horoscopeCacheDao().insertHoroscopeCache(row(it, yesterday(), "STALE")) }

        val result = repo.getHoroscopesWith7DayCache("TODAY")

        assertEquals(12, result.size)
        assertTrue(
            "a row from yesterday came back as today's horoscope",
            result.none { it.generalReadingHi == "STALE" }
        )
    }

    /**
     * A forced refresh writes a second set without displacing the first, and the
     * old lookup then returned both. Twenty-four rows with each rashiId twice is
     * a duplicate key in the LazyColumn that draws them, which throws.
     */
    @Test
    fun `two days of rows in the table still give twelve distinct rashis`() = runTest {
        (1..12).forEach { db.horoscopeCacheDao().insertHoroscopeCache(row(it, yesterday(), "STALE")) }
        (1..12).forEach { db.horoscopeCacheDao().insertHoroscopeCache(row(it, today(), "FRESH")) }

        val result = repo.getHoroscopesWith7DayCache("TODAY")

        assertEquals(12, result.size)
        assertEquals("each rashi must appear once", 12, result.map { it.rashiId }.distinct().size)
        assertTrue(result.all { it.generalReadingHi == "FRESH" })
    }

    /** Today's own rows are still a hit — the fix must not disable the cache. */
    @Test
    fun `today's rows are still served from the cache`() = runTest {
        (1..12).forEach { db.horoscopeCacheDao().insertHoroscopeCache(row(it, today(), "FRESH")) }

        val result = repo.getHoroscopesWith7DayCache("TODAY")

        assertEquals(12, result.size)
        assertTrue(result.all { it.generalReadingHi == "FRESH" })
    }

    /**
     * Some of what the Panchang row holds is already localised text — the Tithi
     * and Nakshatra end times ("09:57 AM तक" against "until 09:57 AM") and the
     * Sun and Moon signs, which are stored in one language only. A language
     * switch forces today to be recomputed, but every other date the user had
     * looked at came back out of the cache in the language it was written in.
     */
    @Test
    fun `a panchang cached in one language is not served in the other`() = runTest {
        val date = GregorianCalendar().apply { clear(); set(2026, 8, 20, 12, 0) }.time

        LanguageManager.setLanguage(AppLanguage.HINDI)
        val hindi = repo.getPanchangWith7DayCache(date, jaipur)

        LanguageManager.setLanguage(AppLanguage.ENGLISH)
        val english = repo.getPanchangWith7DayCache(date, jaipur)

        assertNotEquals(
            "the cached Hindi row came back for an English reader",
            hindi.sunSign, english.sunSign
        )
        assertTrue("end time still in Hindi", english.tithiEndTime.none { it in 'ऀ'..'ॿ' })
        // And the astronomy itself is the same day either way.
        assertEquals(hindi.sunrise, english.sunrise)
        assertEquals(hindi.nakshatraPada, english.nakshatraPada)
    }
}
