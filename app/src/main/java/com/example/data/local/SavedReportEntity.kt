package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "saved_astrology_reports",
    indices = [Index(value = ["reportType"]), Index(value = ["createdAt"])]
)
data class SavedReportEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val reportType: String, // "KUNDALI", "MATCHING", "NUMEROLOGY_AI", "HOROSCOPE"
    val profileName: String,
    val summaryText: String,
    val detailedJsonData: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
