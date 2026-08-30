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
import com.example.data.model.CityLocation
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
            val sharedPrefs = context.getSharedPreferences("astroveda_prefs", Context.MODE_PRIVATE)
            val lat = sharedPrefs.getFloat("city_lat", 26.9124f).toDouble()
            val lon = sharedPrefs.getFloat("city_lon", 75.7873f).toDouble()
            val name = sharedPrefs.getString("city_name", "Jaipur") ?: "Jaipur"
            val nameHi = sharedPrefs.getString("city_name_hi", "जयपुर") ?: "जयपुर"
            val state = sharedPrefs.getString("city_state", "Rajasthan") ?: "Rajasthan"
            val city = CityLocation(name, nameHi, state, lat, lon)

            val use24Hour = sharedPrefs.getBoolean("use_24_hour_format", false)
            val today = Date()
            val panchang = PanchangCalculator.calculatePanchang(today, city, use24Hour)

            val dateFormatter = SimpleDateFormat("EEEE, dd MMM", Locale.getDefault())
            val dateStr = dateFormatter.format(today)

            val views = RemoteViews(context.packageName, R.layout.panchang_widget).apply {
                setTextViewText(R.id.widget_date, dateStr)
                setTextViewText(R.id.widget_sunrise, panchang.sunrise)
                setTextViewText(R.id.widget_sunset, panchang.sunset)
                setTextViewText(R.id.widget_tithi, panchang.tithiLocal)
                setTextViewText(R.id.widget_nakshatra, panchang.nakshatraLocal)

                val moonPhase = PanchangCalculator.getMoonPhaseInfo(panchang.pakshaHindi, panchang.tithiHindi)
                setTextViewText(R.id.widget_moon_phase, "${moonPhase.emoji} ${moonPhase.nameHindi} • ${moonPhase.illuminationPercent}%")

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
                e.printStackTrace()
            }
        }
    }
}
