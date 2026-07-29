package com.example.data.local

import com.example.astro.PanchangCalculator
import com.example.astro.RashifalProvider
import com.example.data.model.CityLocation
import com.example.data.model.PanchangData
import com.example.data.model.RashifalData
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AstroCacheRepository(
    private val panchangCacheDao: PanchangCacheDao,
    private val horoscopeCacheDao: HoroscopeCacheDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    companion object {
        const val SEVEN_DAYS_MS = 7 * 24 * 60 * 60 * 1000L
    }

    suspend fun getPanchangWith7DayCache(date: Date, city: CityLocation, use24Hour: Boolean = false, forceRefresh: Boolean = false): PanchangData = withContext(ioDispatcher) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val dateKey = dateFormat.format(date)
        val cacheKey = "${dateKey}_${city.cityName.replace(" ", "_")}_24h_${use24Hour}"
        val now = System.currentTimeMillis()

        if (!forceRefresh) {
            val cached = panchangCacheDao.getCachedPanchang(cacheKey)
            if (cached != null && (now - cached.cachedAtTimestamp) < SEVEN_DAYS_MS) {
                return@withContext PanchangData(
                    dateString = cached.dateString,
                    dayOfWeek = cached.dayOfWeek,
                    dayOfWeekHindi = cached.dayOfWeekHindi,
                    vikramSamvat = cached.vikramSamvat,
                    sakaSamvat = cached.sakaSamvat,
                    masaName = cached.masaName,
                    masaNameHindi = cached.masaNameHindi,
                    paksha = cached.paksha,
                    pakshaHindi = cached.pakshaHindi,
                    tithi = cached.tithi,
                    tithiHindi = cached.tithiHindi,
                    tithiEndTime = cached.tithiEndTime,
                    tithiProgressPercent = cached.tithiProgressPercent,
                    nakshatra = cached.nakshatra,
                    nakshatraHindi = cached.nakshatraHindi,
                    nakshatraEndTime = cached.nakshatraEndTime,
                    nakshatraPada = cached.nakshatraPada,
                    yoga = cached.yoga,
                    yogaHindi = cached.yogaHindi,
                    karan = cached.karan,
                    karanHindi = cached.karanHindi,
                    sunrise = cached.sunrise,
                    sunset = cached.sunset,
                    moonrise = cached.moonrise,
                    moonset = cached.moonset,
                    rahuKaal = cached.rahuKaal,
                    gulikaKaal = cached.gulikaKaal,
                    yamaganda = cached.yamaganda,
                    abhijitMuhurat = cached.abhijitMuhurat,
                    brahmaMuhurat = cached.brahmaMuhurat,
                    sunSign = cached.sunSign,
                    moonSign = cached.moonSign,
                    locationName = cached.locationName,
                    latitude = cached.latitude,
                    longitude = cached.longitude
                )
            }
        }

        // Calculate fresh
        val freshPanchang = PanchangCalculator.calculatePanchang(date, city, use24Hour)

        // Save to Room cache
        val entity = PanchangCacheEntity(
            cacheKey = cacheKey,
            dateString = freshPanchang.dateString,
            dayOfWeek = freshPanchang.dayOfWeek,
            dayOfWeekHindi = freshPanchang.dayOfWeekHindi,
            vikramSamvat = freshPanchang.vikramSamvat,
            sakaSamvat = freshPanchang.sakaSamvat,
            masaName = freshPanchang.masaName,
            masaNameHindi = freshPanchang.masaNameHindi,
            paksha = freshPanchang.paksha,
            pakshaHindi = freshPanchang.pakshaHindi,
            tithi = freshPanchang.tithi,
            tithiHindi = freshPanchang.tithiHindi,
            tithiEndTime = freshPanchang.tithiEndTime,
            tithiProgressPercent = freshPanchang.tithiProgressPercent,
            nakshatra = freshPanchang.nakshatra,
            nakshatraHindi = freshPanchang.nakshatraHindi,
            nakshatraEndTime = freshPanchang.nakshatraEndTime,
            nakshatraPada = freshPanchang.nakshatraPada,
            yoga = freshPanchang.yoga,
            yogaHindi = freshPanchang.yogaHindi,
            karan = freshPanchang.karan,
            karanHindi = freshPanchang.karanHindi,
            sunrise = freshPanchang.sunrise,
            sunset = freshPanchang.sunset,
            moonrise = freshPanchang.moonrise,
            moonset = freshPanchang.moonset,
            rahuKaal = freshPanchang.rahuKaal,
            gulikaKaal = freshPanchang.gulikaKaal,
            yamaganda = freshPanchang.yamaganda,
            abhijitMuhurat = freshPanchang.abhijitMuhurat,
            brahmaMuhurat = freshPanchang.brahmaMuhurat,
            sunSign = freshPanchang.sunSign,
            moonSign = freshPanchang.moonSign,
            locationName = freshPanchang.locationName,
            latitude = freshPanchang.latitude,
            longitude = freshPanchang.longitude,
            cachedAtTimestamp = now
        )
        panchangCacheDao.insertPanchangCache(entity)

        return@withContext freshPanchang
    }

    suspend fun getHoroscopesWith7DayCache(period: String = "TODAY", forceRefresh: Boolean = false): List<RashifalData> = withContext(ioDispatcher) {
        val now = System.currentTimeMillis()
        val validAfter = now - SEVEN_DAYS_MS

        if (!forceRefresh) {
            val cachedEntities = horoscopeCacheDao.getAllValidHoroscopes(period, validAfter)
            if (cachedEntities.size >= 12) {
                return@withContext cachedEntities.map { entity ->
                    RashifalData(
                        rashiId = entity.rashiId,
                        rashiNameEn = entity.rashiNameEn,
                        rashiNameHi = entity.rashiNameHi,
                        symbol = entity.symbol,
                        elementHi = entity.elementHi,
                        rulerHi = entity.rulerHi,
                        ratingStars = entity.ratingStars,
                        luckyNumber = entity.luckyNumber,
                        luckyColorEn = entity.luckyColorEn,
                        luckyColorHi = entity.luckyColorHi,
                        luckyStoneHi = entity.luckyStoneHi,
                        generalReadingHi = entity.generalReadingHi,
                        generalReadingEn = entity.generalReadingEn,
                        careerReadingHi = entity.careerReadingHi,
                        careerReadingEn = entity.careerReadingEn,
                        healthReadingHi = entity.healthReadingHi,
                        healthReadingEn = entity.healthReadingEn,
                        loveReadingHi = entity.loveReadingHi,
                        loveReadingEn = entity.loveReadingEn,
                        financeReadingHi = entity.financeReadingHi,
                        financeReadingEn = entity.financeReadingEn,
                        period = entity.period
                    )
                }
            }
        }

        // Fresh computation
        val freshHoroscopes = RashifalProvider.getHoroscope(period)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val periodKey = when (period) {
            "WEEK" -> {
                val cal = java.util.Calendar.getInstance()
                "${cal.get(java.util.Calendar.YEAR)}_W${cal.get(java.util.Calendar.WEEK_OF_YEAR)}"
            }
            "MONTH" -> {
                val cal = java.util.Calendar.getInstance()
                "${cal.get(java.util.Calendar.YEAR)}_M${cal.get(java.util.Calendar.MONTH)}"
            }
            else -> dateFormat.format(Date())
        }

        val entities = freshHoroscopes.map { item ->
            HoroscopeCacheEntity(
                cacheKey = "${item.rashiId}_${period}_$periodKey",
                rashiId = item.rashiId,
                rashiNameEn = item.rashiNameEn,
                rashiNameHi = item.rashiNameHi,
                symbol = item.symbol,
                elementHi = item.elementHi,
                rulerHi = item.rulerHi,
                ratingStars = item.ratingStars,
                luckyNumber = item.luckyNumber,
                luckyColorEn = item.luckyColorEn,
                luckyColorHi = item.luckyColorHi,
                luckyStoneHi = item.luckyStoneHi,
                generalReadingHi = item.generalReadingHi,
                generalReadingEn = item.generalReadingEn,
                careerReadingHi = item.careerReadingHi,
                careerReadingEn = entityFieldOr(item.careerReadingEn),
                healthReadingHi = item.healthReadingHi,
                healthReadingEn = entityFieldOr(item.healthReadingEn),
                loveReadingHi = item.loveReadingHi,
                loveReadingEn = entityFieldOr(item.loveReadingEn),
                financeReadingHi = item.financeReadingHi,
                financeReadingEn = entityFieldOr(item.financeReadingEn),
                period = item.period,
                cachedAtTimestamp = now
            )
        }

        horoscopeCacheDao.insertAllHoroscopes(entities)
        return@withContext freshHoroscopes
    }

    private fun entityFieldOr(value: String?): String = value ?: ""
}
