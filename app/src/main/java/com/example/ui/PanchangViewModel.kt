package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.astro.PanchangCalculator
import com.example.data.local.AstroCacheRepository
import com.example.data.local.DatabaseProvider
import com.example.data.model.CityLocation
import com.example.data.model.PanchangData
import com.example.util.LanguageManager
import com.example.widget.PanchangWidgetProvider
import com.example.widget.TithiNakshatraWidgetProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date

/**
 * PanchangViewModel — owns all Panchang, city, date, and notification state.
 *
 * Split out of MainViewModel to reduce its responsibility surface from 1051 lines
 * down to a focused, testable concern. Exposes:
 *  - panchangState: daily PanchangData (cached via AstroCacheRepository)
 *  - selectedCity: GPS / user-selected CityLocation
 *  - selectedDate: the date the user is viewing
 *  - notification toggles + time format
 */
class PanchangViewModel(application: Application) : AndroidViewModel(application) {

    private val cacheRepository: AstroCacheRepository =
        DatabaseProvider.getAstroCacheRepository(application)

    private val sharedPrefs = applicationSharedPreferences(application)

    // --- Panchang state -------------------------------------------------

    private val _panchangState = MutableStateFlow(
        PanchangCalculator.calculatePanchang(Date(), PanchangCalculator.popularCities[0])
    )
    val panchangState: StateFlow<PanchangData> = _panchangState.asStateFlow()

    private val _isPanchangLoading = MutableStateFlow(false)
    val isPanchangLoading: StateFlow<Boolean> = _isPanchangLoading.asStateFlow()

    // --- City state ------------------------------------------------------

    private val _selectedCity = MutableStateFlow(loadCityFromPrefs())
    val selectedCity: StateFlow<CityLocation> = _selectedCity.asStateFlow()

    fun hasUserSetCity(): Boolean = sharedPrefs.contains("city_name")

    private fun loadCityFromPrefs(): CityLocation {
        val lat = sharedPrefs.getFloat("city_lat", 26.9124f).toDouble()
        val lon = sharedPrefs.getFloat("city_lon", 75.7873f).toDouble()
        val name = sharedPrefs.getString("city_name", "Jaipur") ?: "Jaipur"
        val nameHi = sharedPrefs.getString("city_name_hi", "जयपुर") ?: "जयपुर"
        val state = sharedPrefs.getString("city_state", "Rajasthan") ?: "Rajasthan"
        return CityLocation(name, nameHi, state, lat, lon)
    }

    fun setCity(city: CityLocation) {
        _selectedCity.value = city
        sharedPrefs.edit()
            .putFloat("city_lat", city.latitude.toFloat())
            .putFloat("city_lon", city.longitude.toFloat())
            .putString("city_name", city.cityName)
            .putString("city_name_hi", city.cityNameHindi)
            .putString("city_state", city.state)
            .apply()
        recalculatePanchang()
        PanchangWidgetProvider.triggerUpdate(getApplication())
        TithiNakshatraWidgetProvider.triggerUpdate(getApplication())
    }

    // --- Date state -----------------------------------------------------

    private val _selectedDate = MutableStateFlow(Date())
    val selectedDate: StateFlow<Date> = _selectedDate.asStateFlow()

    fun setDate(date: Date) {
        _selectedDate.value = date
        recalculatePanchang()
    }

    // --- Time format ----------------------------------------------------

    private val _use24HourFormat = MutableStateFlow(sharedPrefs.getBoolean("use_24_hour_format", false))
    val use24HourFormat: StateFlow<Boolean> = _use24HourFormat.asStateFlow()

    fun toggle24HourFormat() {
        val newValue = !_use24HourFormat.value
        _use24HourFormat.value = newValue
        sharedPrefs.edit().putBoolean("use_24_hour_format", newValue).apply()
        recalculatePanchang(forceRefresh = true)
        PanchangWidgetProvider.triggerUpdate(getApplication())
        TithiNakshatraWidgetProvider.triggerUpdate(getApplication())
    }

    // --- Notification toggles -------------------------------------------

    val dailyRahuKaalAlert = MutableStateFlow(sharedPrefs.getBoolean("daily_notification_enabled", true))
    val festivalRemindersAlert = MutableStateFlow(sharedPrefs.getBoolean("festival_notification_enabled", true))
    val muhuratAlertsEnabled = MutableStateFlow(sharedPrefs.getBoolean("muhurat_notification_enabled", true))

    private val _notificationHour = MutableStateFlow(sharedPrefs.getInt("notification_hour", 7))
    val notificationHour: StateFlow<Int> = _notificationHour.asStateFlow()

    private val _notificationMinute = MutableStateFlow(sharedPrefs.getInt("notification_minute", 0))
    val notificationMinute: StateFlow<Int> = _notificationMinute.asStateFlow()

    fun toggleRahuKaalAlert() {
        dailyRahuKaalAlert.value = !dailyRahuKaalAlert.value
        sharedPrefs.edit().putBoolean("daily_notification_enabled", dailyRahuKaalAlert.value).apply()
        com.example.worker.AstroNotificationWorker.scheduleDailyNotification(getApplication())
    }

    fun setNotificationTime(hour: Int, minute: Int) {
        _notificationHour.value = hour
        _notificationMinute.value = minute
        sharedPrefs.edit()
            .putInt("notification_hour", hour)
            .putInt("notification_minute", minute)
            .apply()
        com.example.worker.AstroNotificationWorker.scheduleDailyNotification(getApplication())
    }

    fun toggleFestivalAlert() {
        festivalRemindersAlert.value = !festivalRemindersAlert.value
        sharedPrefs.edit().putBoolean("festival_notification_enabled", festivalRemindersAlert.value).apply()
        com.example.worker.FestivalNotificationWorker.scheduleFestivalNotification(getApplication())
    }

    fun toggleMuhuratAlert() {
        muhuratAlertsEnabled.value = !muhuratAlertsEnabled.value
        sharedPrefs.edit().putBoolean("muhurat_notification_enabled", muhuratAlertsEnabled.value).apply()
        com.example.worker.MuhuratNotificationWorker.scheduleMuhuratNotification(getApplication())
    }

    // --- Language -------------------------------------------------------

    fun toggleLanguage() {
        LanguageManager.toggleLanguage()
    }

    // --- Panchang calculation ------------------------------------------

    // Hook for rate-us counter (owned by MainViewModel). Override in tests if needed.
    var onPanchangRecalculated: (() -> Unit)? = null

    fun recalculatePanchang(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _isPanchangLoading.value = true
            try {
                _panchangState.value = cacheRepository.getPanchangWith7DayCache(
                    _selectedDate.value,
                    _selectedCity.value,
                    use24Hour = _use24HourFormat.value,
                    forceRefresh = forceRefresh
                )
                onPanchangRecalculated?.invoke()
            } catch (e: Exception) {
                // Error reporting is owned by the parent MainViewModel's global error stream.
            } finally {
                _isPanchangLoading.value = false
            }
        }
    }

    private fun recalculatePanchang() {
        recalculatePanchang(forceRefresh = false)
    }

    fun refreshPanchang() {
        recalculatePanchang(forceRefresh = true)
    }

    private fun applicationSharedPreferences(application: Application) =
        application.getSharedPreferences("astroveda_prefs", android.content.Context.MODE_PRIVATE)
}