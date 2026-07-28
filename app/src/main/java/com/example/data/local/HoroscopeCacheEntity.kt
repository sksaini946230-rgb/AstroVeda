package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "horoscope_cache")
data class HoroscopeCacheEntity(
    @PrimaryKey val cacheKey: String,
    val rashiId: Int,
    val rashiNameEn: String,
    val rashiNameHi: String,
    val symbol: String,
    val elementHi: String,
    val rulerHi: String,
    val ratingStars: Int,
    val luckyNumber: Int,
    val luckyColorEn: String,
    val luckyColorHi: String,
    val luckyStoneHi: String,
    val generalReadingHi: String,
    val generalReadingEn: String,
    val careerReadingHi: String,
    val careerReadingEn: String,
    val healthReadingHi: String,
    val healthReadingEn: String,
    val loveReadingHi: String,
    val loveReadingEn: String,
    val financeReadingHi: String,
    val financeReadingEn: String,
    val cachedAtTimestamp: Long = System.currentTimeMillis()
)
