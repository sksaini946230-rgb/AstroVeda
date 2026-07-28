package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface HoroscopeCacheDao {
    @Query("SELECT * FROM horoscope_cache WHERE cacheKey = :key LIMIT 1")
    suspend fun getCachedHoroscope(key: String): HoroscopeCacheEntity?

    @Query("SELECT * FROM horoscope_cache WHERE cachedAtTimestamp >= :validAfter")
    suspend fun getAllValidHoroscopes(validAfter: Long): List<HoroscopeCacheEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHoroscopeCache(entity: HoroscopeCacheEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllHoroscopes(entities: List<HoroscopeCacheEntity>)

    @Query("DELETE FROM horoscope_cache WHERE cachedAtTimestamp < :expiredBefore")
    suspend fun deleteExpiredCache(expiredBefore: Long)
}
