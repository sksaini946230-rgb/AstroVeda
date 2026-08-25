package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.astro.FestivalProvider
import com.example.astro.RashifalProvider
import com.example.data.ai.GeminiAstroService
import com.example.data.local.AstroCacheRepository
import com.example.data.local.DatabaseProvider
import com.example.data.model.FestivalData
import com.example.data.model.RashifalData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * RashifalViewModel — owns horoscope, festivals, selected rashi, and AI insights.
 *
 * Split out of MainViewModel to consolidate Rashifal-specific concerns
 * (12 rashi lists, period selection, personalized insights, festival list).
 */
class RashifalViewModel(application: Application) : AndroidViewModel(application) {

    private val cacheRepository: AstroCacheRepository =
        DatabaseProvider.getAstroCacheRepository(application)

    // --- Horoscope state -----------------------------------------------

    private val _dailyHoroscopes = MutableStateFlow(RashifalProvider.getDailyHoroscope())
    val dailyHoroscopesState: StateFlow<List<RashifalData>> = _dailyHoroscopes.asStateFlow()
    val dailyHoroscopes: List<RashifalData> get() = _dailyHoroscopes.value

    private val _isHoroscopeLoading = MutableStateFlow(false)
    val isHoroscopeLoading: StateFlow<Boolean> = _isHoroscopeLoading.asStateFlow()

    fun loadHoroscopesWithCache(period: String = "TODAY", forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _isHoroscopeLoading.value = true
            try {
                _dailyHoroscopes.value = cacheRepository.getHoroscopesWith7DayCache(period = period, forceRefresh = forceRefresh)
                onHoroscopesLoaded?.invoke()
            } catch (_: Exception) {
            } finally {
                _isHoroscopeLoading.value = false
            }
        }
    }

    fun refreshHoroscopes(period: String = "TODAY") {
        loadHoroscopesWithCache(period = period, forceRefresh = true)
    }

    private val _selectedRashiId = MutableStateFlow(1) // Mesh
    val selectedRashiId: StateFlow<Int> = _selectedRashiId.asStateFlow()

    fun selectRashi(id: Int) {
        _selectedRashiId.value = id
    }

    // --- Festivals ----------------------------------------------------

    val festivals: List<FestivalData> = FestivalProvider.getFestivals()

    // --- AI personalized insight ---------------------------------------

    private val _aiRashifalInsight = MutableStateFlow("")
    val aiRashifalInsight: StateFlow<String> = _aiRashifalInsight.asStateFlow()

    private val _isRashifalAiLoading = MutableStateFlow(false)
    val isRashifalAiLoading: StateFlow<Boolean> = _isRashifalAiLoading.asStateFlow()

    fun fetchPersonalizedInsight(rashiName: String) {
        viewModelScope.launch {
            _isRashifalAiLoading.value = true
            try {
                val question = "Provide a personalized daily horoscope insight for $rashiName."
                _aiRashifalInsight.value = GeminiAstroService.getAiAstrologyInsight(question, "Rashi: $rashiName")
            } catch (_: Exception) {
                _aiRashifalInsight.value = "Unable to fetch personalized insight at the moment."
            } finally {
                _isRashifalAiLoading.value = false
            }
        }
    }

    // --- Hooks --------------------------------------------------------

    var onHoroscopesLoaded: (() -> Unit)? = null
}