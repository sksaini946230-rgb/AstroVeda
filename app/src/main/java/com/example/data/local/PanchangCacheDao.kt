package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PanchangCacheDao {
    @Query("SELECT * FROM panchang_cache WHERE cacheKey = :key LIMIT 1")
    suspend fun getCachedPanchang(key: String): PanchangCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPanchangCache(entity: PanchangCacheEntity)

    @Query("DELETE FROM panchang_cache WHERE cachedAtTimestamp < :expiredBefore")
    suspend fun deleteExpiredCache(expiredBefore: Long)
}
