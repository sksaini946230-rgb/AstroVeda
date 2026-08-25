package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.astro.ChoghadiyaCalculator
import com.example.astro.MuhuratCalculator
import com.example.data.model.ChoghadiyaSlot
import com.example.data.model.MuhuratItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Date

/**
 * MuhuratViewModel — owns Choghadiya day/night selection and upcoming muhurats.
 *
 * Split out of MainViewModel because these calculations depend on a date and city
 * that come from PanchangViewModel — they belong to a separate concern.
 */
class MuhuratViewModel(application: Application) : AndroidViewModel(application) {

    // --- Choghadiya ----------------------------------------------------

    private val _choghadiyaDaytime = MutableStateFlow(true)
    val choghadiyaDaytime: StateFlow<Boolean> = _choghadiyaDaytime.asStateFlow()

    fun toggleChoghadiyaDayNight(isDay: Boolean) {
        _choghadiyaDaytime.value = isDay
    }

    /**
     * Compute Choghadiya slots. Caller passes the date, city, and time format
     * from PanchangViewModel so this ViewModel stays loosely coupled.
     */
    fun choghadiyaSlots(
        date: Date,
        isDaytime: Boolean,
        lat: Double,
        lon: Double,
        use24Hour: Boolean
    ): List<ChoghadiyaSlot> = ChoghadiyaCalculator.getChoghadiyaSlots(date, isDaytime, lat, lon, use24Hour)

    // --- Upcoming muhurats --------------------------------------------

    val upcomingMuhurats: List<MuhuratItem> = MuhuratCalculator.getUpcomingMuhurats()
}