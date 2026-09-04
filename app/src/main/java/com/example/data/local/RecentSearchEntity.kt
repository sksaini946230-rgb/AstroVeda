package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "recent_searches", indices = [Index(value = ["createdAt"])])
data class RecentSearchEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String, // "KUNDALI" or "MATCHING"
    val data: String, // JSON representation of the search
    val createdAt: Long = System.currentTimeMillis()
)
