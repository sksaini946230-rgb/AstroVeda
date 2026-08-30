package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

/**
 * Utility to share rashifal/horoscope content as text via WhatsApp and other apps.
 * Per FTL RASHI-010 spec.
 */
object ShareUtils {

    /**
     * Share rashifal reading as formatted text.
     * This is more reliable across all devices than bitmap sharing.
     */
    fun shareRashifalText(
        context: Context,
        rashiName: String,
        rashiNameHi: String,
        period: String,
        general: String,
        career: String = "",
        health: String = "",
        love: String = "",
        finance: String = "",
        luckyNumber: String = "",
        luckyColor: String = "",
        luckyTime: String = "",
        score: Int = 0
    ) {
        val stars = "⭐".repeat(score)
        val periodLabel = when (period.uppercase()) {
            "TODAY" -> LanguageManager.getString("आज का", "today's")
            "WEEK" -> LanguageManager.getString("इस हफ्ते का", "this week's")
            "MONTH" -> LanguageManager.getString("इस महीने का", "this month's")
            else -> LanguageManager.getString("आज का", "today's")
        }

        val sb = StringBuilder()
        sb.appendLine(LanguageManager.getString("🔮 *$rashiNameHi — $periodLabel राशिफल*", "🔮 *$rashiName — $periodLabel horoscope*"))
        sb.appendLine()
        if (stars.isNotEmpty()) sb.appendLine("$stars ($score/5)")
        sb.appendLine()
        if (general.isNotBlank()) {
            sb.appendLine(LanguageManager.getString("📋 *सामान्य फलादेश:* $general", "📋 *Overall:* $general"))
            sb.appendLine()
        }
        if (career.isNotBlank()) {
            sb.appendLine(LanguageManager.getString("💼 *करियर व व्यवसाय:* $career", "💼 *Work & business:* $career"))
            sb.appendLine()
        }
        if (finance.isNotBlank()) {
            sb.appendLine(LanguageManager.getString("💰 *वित्त व धन लाभ:* $finance", "💰 *Money:* $finance"))
            sb.appendLine()
        }
        if (love.isNotBlank()) {
            sb.appendLine(LanguageManager.getString("💑 *प्रेम व संबंध:* $love", "💑 *Love & relationships:* $love"))
            sb.appendLine()
        }
        if (health.isNotBlank()) {
            sb.appendLine(LanguageManager.getString("🌿 *स्वास्थ्य एवं ऊर्जा:* $health", "🌿 *Health & energy:* $health"))
            sb.appendLine()
        }
        if (luckyNumber.isNotBlank()) sb.appendLine(LanguageManager.getString("🔢 शुभ अंक: $luckyNumber", "🔢 Lucky number: $luckyNumber"))
        if (luckyColor.isNotBlank()) sb.appendLine(LanguageManager.getString("🎨 शुभ रंग: $luckyColor", "🎨 Lucky colour: $luckyColor"))
        if (luckyTime.isNotBlank()) sb.appendLine(LanguageManager.getString("⏰ शुभ समय: $luckyTime", "⏰ Lucky time: $luckyTime"))
        sb.appendLine()
        sb.appendLine(LanguageManager.getString("— AstroVeda ज्योतिष ऐप से", "— from the AstroVeda app"))

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, sb.toString())
        }
        context.startActivity(
            Intent.createChooser(shareIntent, LanguageManager.getString("राशिफल शेयर करें", "Share horoscope"))
        )
    }
}
