package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_kundali_profiles")
data class KundaliEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val gender: String, // "MALE" or "FEMALE"
    val dateOfBirth: String, // YYYY-MM-DD
    val timeOfBirth: String, // HH:MM
    val placeOfBirth: String,
    val latitude: Double,
    val longitude: Double,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
