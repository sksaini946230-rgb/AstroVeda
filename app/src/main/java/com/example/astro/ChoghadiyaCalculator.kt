package com.example.astro

import com.example.data.model.ChoghadiyaSlot
import com.example.data.model.ChoghadiyaType
import java.util.Calendar
import java.util.Date

object ChoghadiyaCalculator {

    // Day Choghadiya sequence by day of week (starting from Sunrise)
    // 1: Sun, 2: Mon, 3: Tue, 4: Wed, 5: Thu, 6: Fri, 7: Sat
    private val DAY_SEQUENCES = mapOf(
        Calendar.SUNDAY to listOf(ChoghadiyaType.UDVEG, ChoghadiyaType.AMRIT, ChoghadiyaType.ROG, ChoghadiyaType.LABH, ChoghadiyaType.SHUBH, ChoghadiyaType.CHAR, ChoghadiyaType.KAAL, ChoghadiyaType.UDVEG),
        Calendar.MONDAY to listOf(ChoghadiyaType.AMRIT, ChoghadiyaType.KAAL, ChoghadiyaType.SHUBH, ChoghadiyaType.ROG, ChoghadiyaType.UDVEG, ChoghadiyaType.CHAR, ChoghadiyaType.LABH, ChoghadiyaType.AMRIT),
        Calendar.TUESDAY to listOf(ChoghadiyaType.ROG, ChoghadiyaType.UDVEG, ChoghadiyaType.CHAR, ChoghadiyaType.LABH, ChoghadiyaType.AMRIT, ChoghadiyaType.KAAL, ChoghadiyaType.SHUBH, ChoghadiyaType.ROG),
        Calendar.WEDNESDAY to listOf(ChoghadiyaType.LABH, ChoghadiyaType.AMRIT, ChoghadiyaType.KAAL, ChoghadiyaType.SHUBH, ChoghadiyaType.ROG, ChoghadiyaType.UDVEG, ChoghadiyaType.CHAR, ChoghadiyaType.LABH),
        Calendar.THURSDAY to listOf(ChoghadiyaType.SHUBH, ChoghadiyaType.ROG, ChoghadiyaType.UDVEG, ChoghadiyaType.CHAR, ChoghadiyaType.LABH, ChoghadiyaType.AMRIT, ChoghadiyaType.KAAL, ChoghadiyaType.SHUBH),
        Calendar.FRIDAY to listOf(ChoghadiyaType.CHAR, ChoghadiyaType.LABH, ChoghadiyaType.AMRIT, ChoghadiyaType.KAAL, ChoghadiyaType.SHUBH, ChoghadiyaType.ROG, ChoghadiyaType.UDVEG, ChoghadiyaType.CHAR),
        Calendar.SATURDAY to listOf(ChoghadiyaType.KAAL, ChoghadiyaType.SHUBH, ChoghadiyaType.ROG, ChoghadiyaType.UDVEG, ChoghadiyaType.CHAR, ChoghadiyaType.LABH, ChoghadiyaType.AMRIT, ChoghadiyaType.KAAL)
    )

    private val NIGHT_SEQUENCES = mapOf(
        Calendar.SUNDAY to listOf(ChoghadiyaType.SHUBH, ChoghadiyaType.AMRIT, ChoghadiyaType.CHAR, ChoghadiyaType.ROG, ChoghadiyaType.KAAL, ChoghadiyaType.LABH, ChoghadiyaType.UDVEG, ChoghadiyaType.SHUBH),
        Calendar.MONDAY to listOf(ChoghadiyaType.CHAR, ChoghadiyaType.ROG, ChoghadiyaType.KAAL, ChoghadiyaType.LABH, ChoghadiyaType.UDVEG, ChoghadiyaType.SHUBH, ChoghadiyaType.AMRIT, ChoghadiyaType.CHAR),
        Calendar.TUESDAY to listOf(ChoghadiyaType.KAAL, ChoghadiyaType.LABH, ChoghadiyaType.UDVEG, ChoghadiyaType.SHUBH, ChoghadiyaType.AMRIT, ChoghadiyaType.CHAR, ChoghadiyaType.ROG, ChoghadiyaType.KAAL),
        Calendar.WEDNESDAY to listOf(ChoghadiyaType.UDVEG, ChoghadiyaType.SHUBH, ChoghadiyaType.AMRIT, ChoghadiyaType.CHAR, ChoghadiyaType.ROG, ChoghadiyaType.KAAL, ChoghadiyaType.LABH, ChoghadiyaType.UDVEG),
        Calendar.THURSDAY to listOf(ChoghadiyaType.AMRIT, ChoghadiyaType.CHAR, ChoghadiyaType.ROG, ChoghadiyaType.KAAL, ChoghadiyaType.LABH, ChoghadiyaType.UDVEG, ChoghadiyaType.SHUBH, ChoghadiyaType.AMRIT),
        Calendar.FRIDAY to listOf(ChoghadiyaType.ROG, ChoghadiyaType.KAAL, ChoghadiyaType.LABH, ChoghadiyaType.UDVEG, ChoghadiyaType.SHUBH, ChoghadiyaType.AMRIT, ChoghadiyaType.CHAR, ChoghadiyaType.ROG),
        Calendar.SATURDAY to listOf(ChoghadiyaType.LABH, ChoghadiyaType.UDVEG, ChoghadiyaType.SHUBH, ChoghadiyaType.AMRIT, ChoghadiyaType.CHAR, ChoghadiyaType.ROG, ChoghadiyaType.KAAL, ChoghadiyaType.LABH)
    )

    private val RULERS = mapOf(
        ChoghadiyaType.AMRIT to "चन्द्र (Moon)",
        ChoghadiyaType.SHUBH to "गुरु (Jupiter)",
        ChoghadiyaType.LABH to "बुध (Mercury)",
        ChoghadiyaType.CHAR to "शुक्र (Venus)",
        ChoghadiyaType.ROG to "मंगल (Mars)",
        ChoghadiyaType.KAAL to "शनि (Saturn)",
        ChoghadiyaType.UDVEG to "सूर्य (Sun)"
    )

    fun getChoghadiyaSlots(
        date: Date,
        isDaytime: Boolean = true,
        lat: Double = 26.9124,
        lon: Double = 75.7873
    ): List<ChoghadiyaSlot> {
        val cal = Calendar.getInstance().apply { time = date }
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        val dayOfYear = cal.get(Calendar.DAY_OF_YEAR)

        val sequence = if (isDaytime) {
            DAY_SEQUENCES[dayOfWeek] ?: DAY_SEQUENCES[Calendar.SUNDAY]!!
        } else {
            NIGHT_SEQUENCES[dayOfWeek] ?: NIGHT_SEQUENCES[Calendar.SUNDAY]!!
        }

        // Solar calculations
        val latRad = Math.toRadians(lat)
        val N = dayOfYear.toDouble()
        val radM = Math.toRadians((357.528 + 0.9856003 * N) % 360.0)
        val radL = Math.toRadians((280.460 + 0.9856474 * N) % 360.0)
        val trueSunLonRad = radL + Math.toRadians(1.915 * Math.sin(radM) + 0.020 * Math.sin(2 * radM))
        val sinDec = Math.sin(Math.toRadians(23.439)) * Math.sin(trueSunLonRad)
        val decRad = Math.asin(sinDec)
        val cosH = (Math.sin(Math.toRadians(-0.8333)) - Math.sin(latRad) * Math.sin(decRad)) / (Math.cos(latRad) * Math.cos(decRad))
        val hDeg = Math.toDegrees(Math.acos(cosH.coerceIn(-1.0, 1.0)))
        val eotMins = 4.0 * Math.toDegrees(trueSunLonRad - radL)
        val solarNoonMin = 720.0 - eotMins + (82.5 - lon) * 4.0

        val sunriseMin = (solarNoonMin - hDeg * 4.0).toInt()
        val sunsetMin = (solarNoonMin + hDeg * 4.0).toInt()

        val baseStartMin = if (isDaytime) sunriseMin else sunsetMin
        val totalDurationMin = if (isDaytime) (sunsetMin - sunriseMin) else (1440 - (sunsetMin - sunriseMin))
        val slotLen = totalDurationMin / 8.0

        return sequence.mapIndexed { idx, type ->
            val startMins = ((baseStartMin + idx * slotLen).toInt()) % 1440
            val endMins = ((baseStartMin + (idx + 1) * slotLen).toInt()) % 1440

            val startStr = formatMins(startMins)
            val endStr = formatMins(endMins)

            ChoghadiyaSlot(
                timeSlotString = "$startStr - $endStr",
                startTime = startStr,
                endTime = endStr,
                type = type,
                rulerPlanetHi = RULERS[type] ?: "सूर्य",
                isDay = isDaytime
            )
        }
    }

    private fun formatMins(mins: Int): String {
        val hrs = mins / 60
        val m = mins % 60
        val ampm = if (hrs >= 12) "PM" else "AM"
        val displayHrs = if (hrs == 0) 12 else if (hrs > 12) hrs - 12 else hrs
        return String.format(java.util.Locale.US, "%02d:%02d %s", displayHrs, m, ampm)
    }
}
