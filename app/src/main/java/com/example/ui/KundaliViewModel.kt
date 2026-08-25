package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.astro.KundaliCalculator
import com.example.astro.KundaliMatchingCalculator
import com.example.astro.NumerologyCalculator
import com.example.data.model.GunaMatchingResult
import com.example.data.model.KundaliChartData
import com.example.data.model.NumerologyData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * KundaliViewModel — owns Kundali, Guna Matching (Ashtakoot), Numerology, and Transits.
 *
 * Split out of MainViewModel to separate "what" (chart/matching calculations)
 * from "where" (city/date) and "billing" concerns.
 */
class KundaliViewModel(application: Application) : AndroidViewModel(application) {

    // --- Kundali input state --------------------------------------------

    var kundaliName = MutableStateFlow("Rahul Saini")
    var kundaliDob = MutableStateFlow("1996-08-15")
    var kundaliTob = MutableStateFlow("10:30")
    var kundaliPlace = MutableStateFlow("Jaipur, Rajasthan")

    private val _generatedKundali = MutableStateFlow(
        KundaliCalculator.generateKundali("Rahul Saini", "1996-08-15", "10:30", "Jaipur, Rajasthan")
    )
    val generatedKundali: StateFlow<KundaliChartData> = _generatedKundali.asStateFlow()

    // --- Transit state -------------------------------------------------

    private val _transitKundali = MutableStateFlow<KundaliChartData?>(null)
    val transitKundali: StateFlow<KundaliChartData?> = _transitKundali.asStateFlow()

    // --- Guna matching state -------------------------------------------

    var matchBoyName = MutableStateFlow("Rahul")
    var matchBoyDob = MutableStateFlow("1995-05-20")
    var matchBoyTob = MutableStateFlow("08:15")

    var matchGirlName = MutableStateFlow("Priya")
    var matchGirlDob = MutableStateFlow("1997-11-12")
    var matchGirlTob = MutableStateFlow("14:30")

    private val _gunaResult = MutableStateFlow(
        KundaliMatchingCalculator.matchKundali("Rahul", "1995-05-20", "08:15", "Priya", "1997-11-12", "14:30")
    )
    val gunaResult: StateFlow<GunaMatchingResult> = _gunaResult.asStateFlow()

    // --- Numerology state ----------------------------------------------

    var numName = MutableStateFlow("Sunil Saini")
    var numDob = MutableStateFlow("1995-07-22")

    private val _numerologyData = MutableStateFlow(
        NumerologyCalculator.calculateNumerology("Sunil Saini", "1995-07-22")
    )
    val numerologyData: StateFlow<NumerologyData> = _numerologyData.asStateFlow()

    // --- Shared loading flag -------------------------------------------

    private val _isCalculating = MutableStateFlow(false)
    val isCalculating: StateFlow<Boolean> = _isCalculating.asStateFlow()

    // --- Hooks for the parent VM ---------------------------------------
    // For ad-triggers and rate-us counters owned by MainViewModel.

    var onKundaliGenerated: (() -> Unit)? = null
    var onGunaCalculated: (() -> Unit)? = null
    var onNumerologyCalculated: (() -> Unit)? = null

    fun generateKundaliChart(name: String, dob: String, tob: String, place: String) {
        kundaliName.value = name
        kundaliDob.value = dob
        kundaliTob.value = tob
        kundaliPlace.value = place

        viewModelScope.launch(Dispatchers.Default) {
            _isCalculating.value = true
            try {
                val result = KundaliCalculator.generateKundali(name, dob, tob, place)
                _generatedKundali.value = result
                onKundaliGenerated?.invoke()
            } catch (_: Exception) {
                // Error reporting owned by MainViewModel
            } finally {
                _isCalculating.value = false
            }
        }
    }

    fun calculateCurrentTransits(cityName: String, lat: Double, lng: Double) {
        viewModelScope.launch(Dispatchers.Default) {
            val now = java.util.Calendar.getInstance()
            val dob = String.format(
                java.util.Locale.US,
                "%d-%02d-%02d",
                now.get(java.util.Calendar.YEAR),
                now.get(java.util.Calendar.MONTH) + 1,
                now.get(java.util.Calendar.DAY_OF_MONTH)
            )
            val tob = String.format(
                java.util.Locale.US,
                "%02d:%02d",
                now.get(java.util.Calendar.HOUR_OF_DAY),
                now.get(java.util.Calendar.MINUTE)
            )

            val transitData = KundaliCalculator.generateKundali(
                name = "Current Transits",
                dobString = dob,
                tobString = tob,
                placeName = cityName,
                lat = lat,
                lng = lng
            )
            _transitKundali.value = transitData
        }
    }

    fun calculateGunaMatching() {
        viewModelScope.launch(Dispatchers.Default) {
            _isCalculating.value = true
            try {
                val result = KundaliMatchingCalculator.matchKundali(
                    matchBoyName.value, matchBoyDob.value, matchBoyTob.value,
                    matchGirlName.value, matchGirlDob.value, matchGirlTob.value
                )
                _gunaResult.value = result
                onGunaCalculated?.invoke()
            } catch (_: Exception) {
            } finally {
                _isCalculating.value = false
            }
        }
    }

    fun calculateNumerology() {
        try {
            _numerologyData.value = NumerologyCalculator.calculateNumerology(numName.value, numDob.value)
            onNumerologyCalculated?.invoke()
        } catch (_: Exception) {
        }
    }
}