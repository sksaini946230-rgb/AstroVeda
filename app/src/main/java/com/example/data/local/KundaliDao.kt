package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface KundaliDao {
    @Query("SELECT * FROM saved_kundali_profiles ORDER BY createdAt DESC")
    fun getAllSavedProfiles(): Flow<List<KundaliEntity>>

    @Query("SELECT * FROM saved_kundali_profiles ORDER BY createdAt DESC")
    suspend fun getSavedProfilesList(): List<KundaliEntity>

    @Query("SELECT * FROM saved_kundali_profiles WHERE id = :id LIMIT 1")
    fun getProfileById(id: Long): Flow<KundaliEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: KundaliEntity): Long

    @Update
    suspend fun updateProfile(profile: KundaliEntity)

    @Delete
    suspend fun deleteProfile(profile: KundaliEntity)

    @Query("DELETE FROM saved_kundali_profiles WHERE id = :id")
    suspend fun deleteProfileById(id: Long)

    @Query("DELETE FROM saved_kundali_profiles")
    suspend fun deleteAllProfiles()
}
