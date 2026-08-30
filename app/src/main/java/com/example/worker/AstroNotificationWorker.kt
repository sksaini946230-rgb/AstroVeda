package com.example.worker

import com.example.data.model.dateLocal
import com.example.util.LanguageManager
import com.example.data.model.tithiLocal
import com.example.data.model.nakshatraLocal
import com.example.data.model.yogaLocal
import com.example.data.model.karanaLocal
import com.example.data.model.masaLocal
import com.example.data.model.pakshaLocal
import com.example.data.model.varaLocal
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.MainActivity
import com.example.astro.PanchangCalculator
import com.example.astro.RashifalProvider
import com.example.data.model.CityLocation
import java.util.Date
import java.util.concurrent.TimeUnit

class AstroNotificationWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    companion object {
        const val WORK_NAME = "astro_daily_notification_work"
        const val CHANNEL_ID = "astro_premium_panchang"

        fun scheduleDailyNotification(context: Context) {
            val sharedPrefs = context.getSharedPreferences("astroveda_prefs", Context.MODE_PRIVATE)
            val isEnabled = sharedPrefs.getBoolean("daily_notification_enabled", true)
            
            if (!isEnabled) {
                WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
                return
            }

            val hour = sharedPrefs.getInt("notification_hour", 6) // Default to 6 AM for Panchang
            val minute = sharedPrefs.getInt("notification_minute", 30)

            val calendar = java.util.Calendar.getInstance()
            val now = calendar.timeInMillis

            calendar.set(java.util.Calendar.HOUR_OF_DAY, hour)
            calendar.set(java.util.Calendar.MINUTE, minute)
            calendar.set(java.util.Calendar.SECOND, 0)
            calendar.set(java.util.Calendar.MILLISECOND, 0)

            if (calendar.timeInMillis <= now) {
                calendar.add(java.util.Calendar.DAY_OF_YEAR, 1)
            }

            val initialDelay = calendar.timeInMillis - now

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .build()

            val dailyWorkRequest = PeriodicWorkRequestBuilder<AstroNotificationWorker>(24, TimeUnit.HOURS)
                .setConstraints(constraints)
                .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                .addTag("DAILY_PANCHANG")
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                dailyWorkRequest
            )
        }
    }

    override suspend fun doWork(): Result {
        return try {
            val context = applicationContext
            
            // Get location from preferences
            val sharedPrefs = context.getSharedPreferences("astroveda_prefs", Context.MODE_PRIVATE)
            val lat = sharedPrefs.getFloat("city_lat", 26.9124f).toDouble()
            val lon = sharedPrefs.getFloat("city_lon", 75.7873f).toDouble()
            val name = sharedPrefs.getString("city_name", "Jaipur") ?: "Jaipur"
            val nameHi = sharedPrefs.getString("city_name_hi", "जयपुर") ?: "जयपुर"
            val state = sharedPrefs.getString("city_state", "Rajasthan") ?: "Rajasthan"
            val userCity = CityLocation(name, nameHi, state, lat, lon)

            val use24Hour = sharedPrefs.getBoolean("use_24_hour_format", false)
            val panchang = PanchangCalculator.calculatePanchang(Date(), userCity, use24Hour)
            val moonPhase = PanchangCalculator.getMoonPhaseInfo(panchang.pakshaHindi, panchang.tithiHindi)

            val horoscopes = RashifalProvider.getDailyHoroscope()
            val userRashiId = sharedPrefs.getInt("user_rashi_id", 1)
            val horoscope = horoscopes.find { it.rashiId == userRashiId } ?: horoscopes.first()

            val title = LanguageManager.getString(
                "✨ दैनिक पंचांग एवं राशिफल (${userCity.cityNameHindi})",
                "✨ Today's Panchang & horoscope (${userCity.cityName})"
            )
            val content = "🌅 ${panchang.sunrise} | 🌇 ${panchang.sunset} • 📜 ${panchang.tithiLocal}"
            val bigText = LanguageManager.getString(
                "🕉️ Revati दैनिक पंचांग एवं राशिफल (${panchang.dateLocal})\n\n" +
                    "🌅 सूर्योदय: ${panchang.sunrise} | 🌇 सूर्यास्त: ${panchang.sunset}\n" +
                    "📜 तिथि: ${panchang.tithiLocal}\n" +
                    "✨ नक्षत्र: ${panchang.nakshatraLocal}\n" +
                    "🌔 चंद्र कला: ${moonPhase.emoji} ${moonPhase.nameHindi}\n" +
                    "🌟 अभिजित मुहूर्त: ${panchang.abhijitMuhurat}\n" +
                    "⚠️ राहुकाल: ${panchang.rahuKaal}\n\n" +
                    "🔮 दैनिक राशिफल - ${horoscope.rashiNameHi}:\n" +
                    "\"${horoscope.generalReadingHi}\"",
                "🕉️ Revati — today's Panchang & horoscope (${panchang.dateString})\n\n" +
                    "🌅 Sunrise: ${panchang.sunrise} | 🌇 Sunset: ${panchang.sunset}\n" +
                    "📜 Tithi: ${panchang.tithiLocal}\n" +
                    "✨ Nakshatra: ${panchang.nakshatraLocal}\n" +
                    "🌔 Moon: ${moonPhase.emoji} ${moonPhase.nameEn}\n" +
                    "🌟 Abhijit Muhurat: ${panchang.abhijitMuhurat}\n" +
                    "⚠️ Rahu Kaal: ${panchang.rahuKaal}\n\n" +
                    "🔮 Horoscope — ${horoscope.rashiNameEn}:\n" +
                    "\"${horoscope.generalReadingEn}\""
            )

            showNotification(title, content, bigText)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun showNotification(title: String, content: String, bigText: String) {
        val context = applicationContext
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = context.getString(app.revati.jyotish.R.string.notif_channel_panchang_name)
            val channelDesc = context.getString(app.revati.jyotish.R.string.notif_channel_panchang_desc)
            val channel = NotificationChannel(
                CHANNEL_ID,
                channelName,
                NotificationManager.IMPORTANCE_HIGH // High importance for "premium" feel
            ).apply {
                description = channelDesc
                enableLights(true)
                lightColor = android.graphics.Color.parseColor("#D4A84B") // Premium Gold
                enableVibration(true)
                setShowBadge(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            1001,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(app.revati.jyotish.R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setColor(android.graphics.Color.parseColor("#D4A84B")) // Premium Gold
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()

        notificationManager.notify(1001, notification)
    }
}
