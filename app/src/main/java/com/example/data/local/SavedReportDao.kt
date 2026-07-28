package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedReportDao {
    @Query("SELECT * FROM saved_astrology_reports ORDER BY createdAt DESC")
    fun getAllSavedReports(): Flow<List<SavedReportEntity>>

    @Query("SELECT * FROM saved_astrology_reports WHERE id = :id LIMIT 1")
    fun getReportById(id: Long): Flow<SavedReportEntity?>

    @Query("SELECT * FROM saved_astrology_reports WHERE reportType = :type ORDER BY createdAt DESC")
    fun getSavedReportsByType(type: String): Flow<List<SavedReportEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: SavedReportEntity): Long

    @Update
    suspend fun updateReport(report: SavedReportEntity)

    @Delete
    suspend fun deleteReport(report: SavedReportEntity)

    @Query("DELETE FROM saved_astrology_reports WHERE id = :id")
    suspend fun deleteReportById(id: Long)

    @Query("DELETE FROM saved_astrology_reports")
    suspend fun deleteAllReports()
}
