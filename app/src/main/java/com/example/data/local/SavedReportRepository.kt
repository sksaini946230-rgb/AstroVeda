package com.example.data.local

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class SavedReportRepository(
    private val savedReportDao: SavedReportDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    val allReports: Flow<List<SavedReportEntity>> = savedReportDao.getAllSavedReports().flowOn(ioDispatcher)

    fun getReportById(id: Long): Flow<SavedReportEntity?> {
        return savedReportDao.getReportById(id).flowOn(ioDispatcher)
    }

    fun getReportsByType(type: String): Flow<List<SavedReportEntity>> {
        return savedReportDao.getSavedReportsByType(type).flowOn(ioDispatcher)
    }

    suspend fun saveReport(report: SavedReportEntity): Long = withContext(ioDispatcher) {
        savedReportDao.insertReport(report)
    }

    suspend fun updateReport(report: SavedReportEntity) = withContext(ioDispatcher) {
        savedReportDao.updateReport(report)
    }

    suspend fun deleteReport(report: SavedReportEntity) = withContext(ioDispatcher) {
        savedReportDao.deleteReport(report)
    }

    suspend fun deleteReportById(id: Long) = withContext(ioDispatcher) {
        savedReportDao.deleteReportById(id)
    }

    suspend fun deleteAllReports() = withContext(ioDispatcher) {
        savedReportDao.deleteAllReports()
    }
}
