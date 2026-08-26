package com.example.data.local

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class RecentSearchRepository(
    private val recentSearchDao: RecentSearchDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    val recentSearches: Flow<List<RecentSearchEntity>> = recentSearchDao.getRecentSearches().flowOn(ioDispatcher)

    suspend fun insertSearch(search: RecentSearchEntity) = withContext(ioDispatcher) {
        recentSearchDao.insertSearch(search)
    }

    suspend fun clearAllSearches() = withContext(ioDispatcher) {
        recentSearchDao.clearAllSearches()
    }
}
