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
        score: Int = 0
    ) {
        val stars = "⭐".repeat(score)
        val periodLabel = when (period.uppercase()) {
            "TODAY" -> "आज का"
            "WEEK" -> "इस हफ्ते का"
            "MONTH" -> "इस महीने का"
            else -> "आज का"
        }

        val sb = StringBuilder()
        sb.appendLine("🔮 *$rashiNameHi ($rashiName) — $periodLabel राशिफल*")
        sb.appendLine()
        if (stars.isNotEmpty()) sb.appendLine("$stars ($score/5)")
        sb.appendLine()
        if (general.isNotBlank()) {
            sb.appendLine("📋 *सामान्य:* $general")
            sb.appendLine()
        }
        if (career.isNotBlank()) {
            sb.appendLine("💼 *करियर:* $career")
            sb.appendLine()
        }
        if (health.isNotBlank()) {
            sb.appendLine("❤\u200D🩹 *स्वास्थ्य:* $health")
            sb.appendLine()
        }
        if (love.isNotBlank()) {
            sb.appendLine("💑 *प्रेम:* $love")
            sb.appendLine()
        }
        if (finance.isNotBlank()) {
            sb.appendLine("💰 *वित्त:* $finance")
            sb.appendLine()
        }
        if (luckyNumber.isNotBlank()) sb.appendLine("🔢 शुभ अंक: $luckyNumber")
        if (luckyColor.isNotBlank()) sb.appendLine("🎨 शुभ रंग: $luckyColor")
        sb.appendLine()
        sb.appendLine("— AstroVeda ज्योतिष ऐप से")

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, sb.toString())
        }
        context.startActivity(
            Intent.createChooser(shareIntent, "राशिफल शेयर करें")
        )
    }
}
