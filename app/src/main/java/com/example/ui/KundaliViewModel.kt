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

    var kundaliName = MutableStateFlow("")
    var kundaliDob = MutableStateFlow("")
    var kundaliTob = MutableStateFlow("")
    var kundaliPlace = MutableStateFlow("")

    private val _generatedKundali = MutableStateFlow<KundaliChartData?>(null)
    val generatedKundali: StateFlow<KundaliChartData?> = _generatedKundali.asStateFlow()

    // --- Transit state -------------------------------------------------

    private val _transitKundali = MutableStateFlow<KundaliChartData?>(null)
    val transitKundali: StateFlow<KundaliChartData?> = _transitKundali.asStateFlow()

    // --- Guna matching state -------------------------------------------

    var matchBoyName = MutableStateFlow("")
    var matchBoyDob = MutableStateFlow("")
    var matchBoyTob = MutableStateFlow("12:00")

    var matchGirlName = MutableStateFlow("")
    var matchGirlDob = MutableStateFlow("")
    var matchGirlTob = MutableStateFlow("12:00")

    private val _gunaResult = MutableStateFlow<GunaMatchingResult?>(null)
    val gunaResult: StateFlow<GunaMatchingResult?> = _gunaResult.asStateFlow()

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
        val bName = matchBoyName.value.trim()
        val bDob = matchBoyDob.value.trim()
        val bTob = matchBoyTob.value.trim().ifBlank { "12:00" }
        val gName = matchGirlName.value.trim()
        val gDob = matchGirlDob.value.trim()
        val gTob = matchGirlTob.value.trim().ifBlank { "12:00" }

        viewModelScope.launch(Dispatchers.Default) {
            _isCalculating.value = true
            try {
                val result = KundaliMatchingCalculator.matchKundali(
                    bName, bDob, bTob,
                    gName, gDob, gTob
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