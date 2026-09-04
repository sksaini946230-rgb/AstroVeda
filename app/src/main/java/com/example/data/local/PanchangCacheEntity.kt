package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "panchang_cache", indices = [Index(value = ["cachedAtTimestamp"])])
data class PanchangCacheEntity(
    @PrimaryKey val cacheKey: String,
    val dateString: String,
    val dayOfWeek: String,
    val dayOfWeekHindi: String,
    val vikramSamvat: Int,
    val sakaSamvat: Int,
    val masaName: String,
    val masaNameHindi: String,
    val paksha: String,
    val pakshaHindi: String,
    val tithi: String,
    val tithiHindi: String,
    val tithiEndTime: String,
    val tithiProgressPercent: Float,
    val nakshatra: String,
    val nakshatraHindi: String,
    val nakshatraEndTime: String,
    val nakshatraPada: Int,
    val yoga: String,
    val yogaHindi: String,
    val karan: String,
    val karanHindi: String,
    val sunrise: String,
    val sunset: String,
    val moonrise: String,
    val moonset: String,
    val rahuKaal: String,
    val gulikaKaal: String,
    val yamaganda: String,
    val abhijitMuhurat: String,
    val brahmaMuhurat: String,
    val sunSign: String,
    val moonSign: String,
    val locationName: String,
    val latitude: Double,
    val longitude: Double,
    /**
     * The planet list, serialised.
     *
     * This column did not exist, so a cache hit had no planets to return and
     * AstroCacheRepository re-ran the whole ephemeris just to fetch them — every
     * hit cost a database read *plus* a full recompute, which made the cache
     * slower than no cache at all. The comment there blamed
     * fallbackToDestructiveMigration for making a column impossible; that stopped
     * being true when real migrations landed, and nobody came back to it.
     */
    val planetsJson: String = "",
    val cachedAtTimestamp: Long = System.currentTimeMillis()
)
