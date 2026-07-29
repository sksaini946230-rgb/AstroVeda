package com.example.astro

import org.junit.Test
import java.util.Calendar
import java.util.Date

class TransitCalculatorTest {

    private fun dateFor(year: Int, month: Int, day: Int): Date {
        val cal = Calendar.getInstance()
        cal.set(year, month - 1, day, 12, 0, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.time
    }

    @Test
    fun printReadingsAcrossPeriods() {
        val date = dateFor(2026, 7, 29)
        val data = RashifalProvider.getHoroscope("TODAY").first()
        val dataWeek = RashifalProvider.getHoroscope("WEEK").first()
        val dataMonth = RashifalProvider.getHoroscope("MONTH").first()
        println("TODAY career EN -> ${data.careerReadingEn}")
        println("WEEK  career EN -> ${dataWeek.careerReadingEn}")
        println("MONTH career EN -> ${dataMonth.careerReadingEn}")
        println("TODAY general HI -> ${data.generalReadingHi}")
        println("WEEK  general HI -> ${dataWeek.generalReadingHi}")
        println("MONTH general HI -> ${dataMonth.generalReadingHi}")
    }
}
