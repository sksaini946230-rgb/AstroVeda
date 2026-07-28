package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "panchang_cache")
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
    val cachedAtTimestamp: Long = System.currentTimeMillis()
)
