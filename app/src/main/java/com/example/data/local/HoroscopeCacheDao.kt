package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface HoroscopeCacheDao {
    @Query("SELECT * FROM horoscope_cache WHERE cacheKey = :key LIMIT 1")
    suspend fun getCachedHoroscope(key: String): HoroscopeCacheEntity?

    /**
     * The twelve rows for one period *and one occurrence of it*.
     *
     * This used to match on `period` and a seven-day window alone, which is not
     * the same question. The rows are written with the day, week or month they
     * were computed for baked into the key, and nothing read it back — so
     * yesterday's "TODAY" set was still inside the window this morning and was
     * served as today's, and the daily Rashifal only actually changed once a
     * week. Worse, a forced refresh (a language switch) wrote a second set
     * without displacing the first, and the next ordinary load returned
     * twenty-four rows with each rashiId twice — which is a duplicate key in the
     * LazyColumn that draws them, and that throws.
     */
    @Query("SELECT * FROM horoscope_cache WHERE period = :period AND cacheKey IN (:keys) AND cachedAtTimestamp >= :validAfter")
    suspend fun getValidHoroscopesForKeys(
        period: String,
        keys: List<String>,
        validAfter: Long
    ): List<HoroscopeCacheEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHoroscopeCache(entity: HoroscopeCacheEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllHoroscopes(entities: List<HoroscopeCacheEntity>)

    @Query("DELETE FROM horoscope_cache WHERE cachedAtTimestamp < :expiredBefore")
    suspend fun deleteExpiredCache(expiredBefore: Long)
}
