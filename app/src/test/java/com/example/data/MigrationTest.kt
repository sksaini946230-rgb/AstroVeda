package com.example.data

import androidx.room.testing.MigrationTestHelper
import androidx.test.platform.app.InstrumentationRegistry
import com.example.data.local.AppDatabase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Verifies each migration against a real database of the previous version.
 *
 * There was no migration test infrastructure at all, which is a gap that only
 * mattered once a migration existed — and this is the first one. It carries user
 * data: birth profiles, which the database used to drop on every schema change
 * before real migrations landed. A migration that silently loses a row here is
 * the same class of defect all over again.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MigrationTest {

    private companion object {
        const val TEST_DB = "migration-test"
    }

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java
    )

    @Test
    fun `migrate 5 to 6 keeps every saved profile and gives each a unique uuid`() {
        helper.createDatabase(TEST_DB, 5).apply {
            execSQL(
                "INSERT INTO saved_kundali_profiles " +
                    "(id, name, gender, dateOfBirth, timeOfBirth, placeOfBirth, latitude, longitude, notes, createdAt) " +
                    "VALUES (1, 'राम', 'MALE', '1994-08-25', '14:15', 'Jaipur', 26.9124, 75.7873, '', 1000)"
            )
            execSQL(
                "INSERT INTO saved_kundali_profiles " +
                    "(id, name, gender, dateOfBirth, timeOfBirth, placeOfBirth, latitude, longitude, notes, createdAt) " +
                    "VALUES (2, 'सीता', 'FEMALE', '1996-02-11', '09:40', 'Udaipur', 24.5854, 73.7125, '', 2000)"
            )
            close()
        }

        val db = helper.runMigrationsAndValidate(
            TEST_DB, 6, true, AppDatabase.MIGRATION_5_6
        )

        db.query("SELECT id, name, uuid FROM saved_kundali_profiles ORDER BY id").use { c ->
            assertEquals("both profiles must survive the migration", 2, c.count)

            val uuids = mutableSetOf<String>()
            val names = mutableListOf<String>()
            while (c.moveToNext()) {
                names.add(c.getString(1))
                val uuid = c.getString(2)
                assertNotNull(uuid)
                assertTrue("backfilled uuid must not be the empty default", uuid.isNotBlank())
                uuids.add(uuid)
            }
            assertEquals(listOf("राम", "सीता"), names)
            assertEquals("each row must get its own uuid", 2, uuids.size)
        }
    }

    @Test
    fun `migrate 5 to 6 adds the planetsJson column with an empty default`() {
        helper.createDatabase(TEST_DB, 5).apply {
            execSQL(
                "INSERT INTO panchang_cache (cacheKey, dateString, dayOfWeek, dayOfWeekHindi, " +
                    "vikramSamvat, sakaSamvat, masaName, masaNameHindi, paksha, pakshaHindi, tithi, " +
                    "tithiHindi, tithiEndTime, tithiProgressPercent, nakshatra, nakshatraHindi, " +
                    "nakshatraEndTime, nakshatraPada, yoga, yogaHindi, karan, karanHindi, sunrise, " +
                    "sunset, moonrise, moonset, rahuKaal, gulikaKaal, yamaganda, abhijitMuhurat, " +
                    "brahmaMuhurat, sunSign, moonSign, locationName, latitude, longitude, cachedAtTimestamp) " +
                    "VALUES ('k', '', '', '', 2081, 1946, '', '', '', '', '', '', '', 0.0, '', '', " +
                    "'', 1, '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', 'Jaipur', " +
                    "26.9124, 75.7873, 1000)"
            )
            close()
        }

        val db = helper.runMigrationsAndValidate(TEST_DB, 6, true, AppDatabase.MIGRATION_5_6)

        db.query("SELECT planetsJson FROM panchang_cache WHERE cacheKey = 'k'").use { c ->
            assertTrue(c.moveToFirst())
            assertEquals("", c.getString(0))
        }
    }

    @Test
    fun `migrate 5 to 6 works on an empty database`() {
        helper.createDatabase(TEST_DB, 5).close()
        val db = helper.runMigrationsAndValidate(TEST_DB, 6, true, AppDatabase.MIGRATION_5_6)
        db.query("SELECT COUNT(*) FROM saved_kundali_profiles").use { c ->
            assertTrue(c.moveToFirst())
            assertEquals(0, c.getInt(0))
        }
    }

    @Test
    fun `the unique uuid index really is unique`() {
        helper.createDatabase(TEST_DB, 5).close()
        val db = helper.runMigrationsAndValidate(TEST_DB, 6, true, AppDatabase.MIGRATION_5_6)

        db.execSQL(
            "INSERT INTO saved_kundali_profiles " +
                "(id, uuid, name, gender, dateOfBirth, timeOfBirth, placeOfBirth, latitude, longitude, notes, createdAt) " +
                "VALUES (1, 'dupe', 'A', 'MALE', '1994-08-25', '14:15', 'Jaipur', 26.9, 75.7, '', 1)"
        )
        var rejected = false
        try {
            db.execSQL(
                "INSERT INTO saved_kundali_profiles " +
                    "(id, uuid, name, gender, dateOfBirth, timeOfBirth, placeOfBirth, latitude, longitude, notes, createdAt) " +
                    "VALUES (2, 'dupe', 'B', 'MALE', '1994-08-25', '14:15', 'Jaipur', 26.9, 75.7, '', 2)"
            )
        } catch (e: Exception) {
            rejected = true
        }
        assertTrue("a duplicate uuid must be refused by the index", rejected)
        assertFalse(false)
    }

    /**
     * 6 → 7 exists only to clear horoscope_cache, and that is easy to write and
     * easy to get wrong in the direction that matters: dropping the wrong table.
     *
     * The stale rows are the reason. RashifalProvider gave every rashi an
     * identical rating until this release; a seven-day cache that survives the
     * upgrade would keep serving that to an updated user for a week. Saved
     * profiles and panchang rows are not stale and must not be touched.
     */
    @Test
    fun `migrate 6 to 7 clears cached horoscopes`() {
        helper.createDatabase(TEST_DB, 6).apply {
            execSQL(
                "INSERT INTO horoscope_cache (cacheKey, rashiId, rashiNameEn, rashiNameHi, symbol, " +
                    "elementHi, rulerHi, ratingStars, luckyNumber, luckyColorEn, luckyColorHi, " +
                    "luckyStoneHi, luckyTimeHi, luckyTimeEn, generalReadingHi, generalReadingEn, " +
                    "careerReadingHi, careerReadingEn, healthReadingHi, healthReadingEn, " +
                    "loveReadingHi, loveReadingEn, financeReadingHi, financeReadingEn, period, " +
                    "cachedAtTimestamp) VALUES ('TODAY_1', 1, 'Aries', 'मेष', '♈', 'अग्नि', 'मंगल', " +
                    "5, 7, 'Green & Turquoise', 'हरा व फिरोजी', 'पन्ना', 'x', 'x', 'a', 'a', 'b', 'b', " +
                    "'c', 'c', 'd', 'd', 'e', 'e', 'TODAY', 1000)"
            )
            close()
        }

        val db = helper.runMigrationsAndValidate(TEST_DB, 7, true, AppDatabase.MIGRATION_6_7)

        db.query("SELECT COUNT(*) FROM horoscope_cache").use {
            it.moveToFirst()
            assertEquals("stale horoscope rows must not survive the upgrade", 0, it.getInt(0))
        }
    }

    @Test
    fun `migrate 6 to 7 leaves saved profiles alone`() {
        helper.createDatabase(TEST_DB, 6).apply {
            execSQL(
                "INSERT INTO saved_kundali_profiles " +
                    "(id, uuid, name, gender, dateOfBirth, timeOfBirth, placeOfBirth, latitude, longitude, notes, createdAt) " +
                    "VALUES (1, 'keep-me', 'सुनील', 'MALE', '1994-08-25', '14:15', 'Jaipur', 26.9, 75.7, 'note', 1)"
            )
            close()
        }

        val db = helper.runMigrationsAndValidate(TEST_DB, 7, true, AppDatabase.MIGRATION_6_7)

        db.query("SELECT uuid, name, notes FROM saved_kundali_profiles").use {
            assertEquals("the profile must survive", 1, it.count)
            it.moveToFirst()
            assertEquals("keep-me", it.getString(0))
            assertEquals("सुनील", it.getString(1))
            assertEquals("note", it.getString(2))
        }
    }

    /** A user on the shipped production build arrives at 5, not 6. */
    @Test
    fun `a version 5 database migrates all the way to 7 with its profiles`() {
        helper.createDatabase(TEST_DB, 5).apply {
            execSQL(
                "INSERT INTO saved_kundali_profiles " +
                    "(id, name, gender, dateOfBirth, timeOfBirth, placeOfBirth, latitude, longitude, notes, createdAt) " +
                    "VALUES (1, 'Ravi', 'MALE', '1988-01-02', '06:40', 'Noida', 28.5, 77.4, 'twin A', 1), " +
                    "(2, 'Ravi', 'MALE', '1988-01-02', '06:45', 'Noida', 28.5, 77.4, 'twin B', 2)"
            )
            close()
        }

        val db = helper.runMigrationsAndValidate(
            TEST_DB, 7, true, AppDatabase.MIGRATION_5_6, AppDatabase.MIGRATION_6_7
        )

        db.query("SELECT uuid FROM saved_kundali_profiles ORDER BY id").use {
            assertEquals("both twins must survive 5 -> 7", 2, it.count)
            it.moveToFirst()
            val first = it.getString(0)
            it.moveToNext()
            val second = it.getString(0)
            assertTrue("each row needs a uuid", first.isNotBlank() && second.isNotBlank())
            assertTrue("twins must not share a uuid", first != second)
        }
    }
}
