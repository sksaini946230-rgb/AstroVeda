package com.example.worker

import com.example.util.LanguageManager
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
import com.example.astro.FestivalProvider
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit

class FestivalNotificationWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    companion object {
        const val WORK_NAME = "astro_festival_notification_work"
        const val CHANNEL_ID = "astro_festival_notifications"

        fun scheduleFestivalNotification(context: Context) {
            val sharedPrefs = context.getSharedPreferences("astroveda_prefs", Context.MODE_PRIVATE)
            val isEnabled = sharedPrefs.getBoolean("festival_notification_enabled", true)
            
            if (!isEnabled) {
                WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
                return
            }

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .build()

            // Schedule to run every day at around 10:00 AM
            val calendar = Calendar.getInstance()
            val now = calendar.timeInMillis

            calendar.set(Calendar.HOUR_OF_DAY, 10)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            if (calendar.timeInMillis <= now) {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }

            val initialDelay = calendar.timeInMillis - now

            val dailyWorkRequest = PeriodicWorkRequestBuilder<FestivalNotificationWorker>(24, TimeUnit.HOURS)
                .setConstraints(constraints)
                .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
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
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, 1) // Tomorrow
            
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH) + 1 // 1-12
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val dateStr = String.format("%04d-%02d-%02d", year, month, day)

            val festivals = FestivalProvider.getFestivals()
            val tomorrowFestivals = festivals.filter { it.dateIso == dateStr }

            if (tomorrowFestivals.isNotEmpty()) {
                val names = tomorrowFestivals.joinToString(", ") { LanguageManager.getString(it.nameHi, it.nameEn) }
                val title = LanguageManager.getString("कल का त्योहार: $names", "Tomorrow: $names")
                val content = LanguageManager.getString(
                    "Revati आपको कल के व्रत/त्योहार की याद दिला रहा है।",
                    "A reminder from Revati about tomorrow's fast or festival."
                )
                showNotification(title, content)
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun showNotification(title: String, content: String) {
        val context = applicationContext
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Festival Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminders for upcoming Hindu festivals and Vrats."
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(1002, notification)
    }
}
