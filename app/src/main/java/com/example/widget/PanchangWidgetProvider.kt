package com.example.widget

import com.example.data.model.tithiLocal
import com.example.data.model.nakshatraLocal
import com.example.data.model.yogaLocal
import com.example.data.model.karanaLocal
import com.example.data.model.masaLocal
import com.example.data.model.pakshaLocal
import com.example.data.model.varaLocal
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import app.revati.jyotish.R
import com.example.astro.PanchangCalculator
import com.example.data.local.CityPreferences
import com.example.util.LanguageManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PanchangWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == AppWidgetManager.ACTION_APPWIDGET_UPDATE || intent.action == ACTION_REFRESH_WIDGET) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, PanchangWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            onUpdate(context, appWidgetManager, appWidgetIds)
        }
    }

    companion object {
        const val ACTION_REFRESH_WIDGET = "com.example.widget.ACTION_REFRESH_WIDGET"

        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val city = CityPreferences.read(context)
            val use24Hour = CityPreferences.use24Hour(context)
            val today = Date()
            val panchang = PanchangCalculator.calculatePanchang(today, city, use24Hour)

            val dateFormatter = SimpleDateFormat("EEEE, dd MMM", Locale.getDefault())
            val dateStr = dateFormatter.format(today)

            val views = RemoteViews(context.packageName, R.layout.panchang_widget).apply {
                // These five labels had no ids and were never set, so they sat at
                // their layout defaults — "SUNRISE / सूर्योदय", "TITHI / तिथि" and
                // so on, both languages jammed together with a slash, whichever
                // language the user had chosen. The Aug 2026 sweep for hardcoded
                // strings covered Kotlin and never looked at the XML layouts.
                setTextViewText(R.id.widget_label_sunrise, LanguageManager.getString("सूर्योदय", "SUNRISE"))
                setTextViewText(R.id.widget_label_sunset, LanguageManager.getString("सूर्यास्त", "SUNSET"))
                setTextViewText(R.id.widget_label_tithi, LanguageManager.getString("तिथि", "TITHI"))
                setTextViewText(R.id.widget_label_nakshatra, LanguageManager.getString("नक्षत्र", "NAKSHATRA"))
                setTextViewText(R.id.widget_label_moon_phase, LanguageManager.getString("चंद्र कला", "MOON PHASE"))

                setTextViewText(R.id.widget_date, dateStr)
                setTextViewText(R.id.widget_sunrise, panchang.sunrise)
                setTextViewText(R.id.widget_sunset, panchang.sunset)
                setTextViewText(R.id.widget_tithi, panchang.tithiLocal)
                setTextViewText(R.id.widget_nakshatra, panchang.nakshatraLocal)

                val moonPhase = PanchangCalculator.getMoonPhaseInfo(panchang.pakshaHindi, panchang.tithiHindi)
                // nameHindi unconditionally — an English user saw the phase name
                // in Hindi next to English labels.
                setTextViewText(
                    R.id.widget_moon_phase,
                    "${moonPhase.emoji} " +
                        LanguageManager.getString(moonPhase.nameHindi, moonPhase.nameEn) +
                        " • ${moonPhase.illuminationPercent}%"
                )

                setTextViewText(R.id.widget_masa, "${panchang.masaLocal} | ${panchang.pakshaLocal}")
                setTextViewText(R.id.widget_location, "📍 ${panchang.locationName}")

                val mainIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    mainIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                setOnClickPendingIntent(R.id.widget_root, pendingIntent)
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        fun triggerUpdate(context: Context) {
            try {
                val intent = Intent(context, PanchangWidgetProvider::class.java).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                }
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val componentName = ComponentName(context, PanchangWidgetProvider::class.java)
                val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
                if (appWidgetIds.isNotEmpty()) {
                    intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
                    context.sendBroadcast(intent)
                }
            } catch (e: Exception) {
                android.util.Log.e("PanchangWidget", "Widget update broadcast failed", e)
            }
        }
    }
}
