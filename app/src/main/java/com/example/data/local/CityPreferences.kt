package com.example.data.local

import android.content.Context
import com.example.data.model.CityLocation

/**
 * The selected city, read from the one place it is stored.
 *
 * These six lines were copy-pasted into six files — MainViewModel,
 * the since-deleted PanchangViewModel, both widget providers and two workers —
 * each repeating the same keys and the same Jaipur fallback. Six chances for
 * one of them to drift.
 */
object CityPreferences {

    const val PREFS = "astroveda_prefs"

    private const val DEFAULT_LAT = 26.9124f
    private const val DEFAULT_LON = 75.7873f

    fun read(context: Context): CityLocation {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return CityLocation(
            cityName = prefs.getString("city_name", "Jaipur") ?: "Jaipur",
            cityNameHindi = prefs.getString("city_name_hi", "जयपुर") ?: "जयपुर",
            state = prefs.getString("city_state", "Rajasthan") ?: "Rajasthan",
            latitude = prefs.getFloat("city_lat", DEFAULT_LAT).toDouble(),
            longitude = prefs.getFloat("city_lon", DEFAULT_LON).toDouble()
        )
    }

    fun use24Hour(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean("use_24_hour_format", false)
}
