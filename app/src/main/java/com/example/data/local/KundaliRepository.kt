package com.example.data.local

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class KundaliRepository(
    private val kundaliDao: KundaliDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    val allProfiles: Flow<List<KundaliEntity>> = kundaliDao.getAllSavedProfiles().flowOn(ioDispatcher)

    fun getProfileById(id: Long): Flow<KundaliEntity?> {
        return kundaliDao.getProfileById(id).flowOn(ioDispatcher)
    }

    suspend fun getSavedProfilesList(): List<KundaliEntity> = withContext(ioDispatcher) {
        kundaliDao.getSavedProfilesList()
    }

    suspend fun saveProfile(profile: KundaliEntity): Long = withContext(ioDispatcher) {
        kundaliDao.insertProfile(profile)
    }

    suspend fun updateProfile(profile: KundaliEntity) = withContext(ioDispatcher) {
        kundaliDao.updateProfile(profile)
    }

    suspend fun deleteProfile(profile: KundaliEntity) = withContext(ioDispatcher) {
        kundaliDao.deleteProfile(profile)
    }

    suspend fun deleteProfileById(id: Long) = withContext(ioDispatcher) {
        kundaliDao.deleteProfileById(id)
    }

    suspend fun deleteAllProfiles() = withContext(ioDispatcher) {
        kundaliDao.deleteAllProfiles()
    }
}
