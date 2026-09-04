package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.model.KundaliChartData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object KundaliImageGenerator {

    suspend fun generateAndShareChart(context: Context, chartData: KundaliChartData): Uri? = withContext(Dispatchers.IO) {
        try {
            // High-resolution bitmap: 1080 x 1350 (standard portrait social media aspect ratio)
            val width = 1080
            val height = 1350
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            // 1. Draw premium Vedic background: Deep dark blue / night sky gradient/solid
            val bgPaint = Paint().apply {
                color = Color.parseColor("#0F1016") // Deep midnight blue
                style = Paint.Style.FILL
            }
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

            // Decorative gold outer border
            val borderPaint = Paint().apply {
                color = Color.parseColor("#D4AF37") // Premium Gold
                style = Paint.Style.STROKE
                strokeWidth = 6f
                isAntiAlias = true
            }
            canvas.drawRect(20f, 20f, (width - 20).toFloat(), (height - 20).toFloat(), borderPaint)

            // Inner subtle border
            val borderInnerPaint = Paint().apply {
                color = Color.parseColor("#44D4AF37") // 25% gold opacity
                style = Paint.Style.STROKE
                strokeWidth = 2f
                isAntiAlias = true
            }
            canvas.drawRect(35f, 35f, (width - 35).toFloat(), (height - 35).toFloat(), borderInnerPaint)

            // 2. Draw Header Details
            val titlePaint = Paint().apply {
                color = Color.parseColor("#FFD700") // Golden Yellow
                textSize = 48f
                isFakeBoldText = true
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }
            canvas.drawText("✨ Revati Kundali ✨", (width / 2).toFloat(), 110f, titlePaint)

            val subtitlePaint = Paint().apply {
                color = Color.WHITE
                textSize = 34f
                isFakeBoldText = true
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }
            canvas.drawText(LanguageManager.getString("जन्म कुण्डली", "Birth Chart"), (width / 2).toFloat(), 170f, subtitlePaint)

            // Info Box / Text
            val infoPaint = Paint().apply {
                color = Color.parseColor("#E0E0E0") // Off white
                textSize = 28f
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }
            val infoText1 = LanguageManager.getString(
                "नाम: ${chartData.personName} | लग्न: ${chartData.ascendantRashiHi}",
                "Name: ${chartData.personName} | Lagna: ${chartData.ascendantRashiEn}"
            )
            val infoText2 = LanguageManager.getString(
                "जन्म: ${chartData.dateOfBirth} | ${chartData.timeOfBirth} | ${chartData.placeOfBirth}",
                "Born: ${chartData.dateOfBirth} | ${chartData.timeOfBirth} | ${chartData.placeOfBirth}"
            )
            val infoText3 = LanguageManager.getString(
                "राशि: ${chartData.moonRashiHi} | नक्षत्र: ${chartData.moonNakshatraHi}",
                "Moon sign: ${chartData.moonRashiEn} | Nakshatra: ${chartData.moonNakshatraEn}"
            )

            canvas.drawText(infoText1, (width / 2).toFloat(), 230f, infoPaint)
            canvas.drawText(infoText2, (width / 2).toFloat(), 275f, infoPaint)
            canvas.drawText(infoText3, (width / 2).toFloat(), 320f, infoPaint)

            // 3. Draw North Indian Kundali Grid Chart
            // Let's place the chart in a square of 700x700 centered horizontally.
            val chartSize = 700
            val chartLeft = (width - chartSize) / 2
            val chartTop = 380
            val chartRight = chartLeft + chartSize
            val chartBottom = chartTop + chartSize

            val chartBgPaint = Paint().apply {
                color = Color.parseColor("#171923") // Dark gray/blue for chart area
                style = Paint.Style.FILL
            }
            canvas.drawRect(chartLeft.toFloat(), chartTop.toFloat(), chartRight.toFloat(), chartBottom.toFloat(), chartBgPaint)

            val gridGoldPaint = Paint().apply {
                color = Color.parseColor("#D4AF37") // Premium Gold lines
                style = Paint.Style.STROKE
                strokeWidth = 4f
                isAntiAlias = true
            }

            // Outer chart square
            canvas.drawRect(chartLeft.toFloat(), chartTop.toFloat(), chartRight.toFloat(), chartBottom.toFloat(), gridGoldPaint)

            // Main Diagonals
            canvas.drawLine(chartLeft.toFloat(), chartTop.toFloat(), chartRight.toFloat(), chartBottom.toFloat(), gridGoldPaint)
            canvas.drawLine(chartRight.toFloat(), chartTop.toFloat(), chartLeft.toFloat(), chartBottom.toFloat(), gridGoldPaint)

            // Inner Diamond
            val pTopX = chartLeft + chartSize / 2f
            val pTopY = chartTop.toFloat()
            val pRightX = chartRight.toFloat()
            val pRightY = chartTop + chartSize / 2f
            val pBottomX = chartLeft + chartSize / 2f
            val pBottomY = chartBottom.toFloat()
            val pLeftX = chartLeft.toFloat()
            val pLeftY = chartTop + chartSize / 2f

            val diamondPath = Path().apply {
                moveTo(pTopX, pTopY)
                lineTo(pRightX, pRightY)
                lineTo(pBottomX, pBottomY)
                lineTo(pLeftX, pLeftY)
                close()
            }
            canvas.drawPath(diamondPath, gridGoldPaint)

            // Approximate center positions for 12 Houses (Local within the chart area)
            val w = chartSize.toFloat()
            val h = chartSize.toFloat()
            val housePositions = mapOf(
                1 to Offset(w * 0.5f, h * 0.25f),
                2 to Offset(w * 0.25f, h * 0.12f),
                3 to Offset(w * 0.12f, h * 0.25f),
                4 to Offset(w * 0.25f, h * 0.5f),
                5 to Offset(w * 0.12f, h * 0.75f),
                6 to Offset(w * 0.25f, h * 0.88f),
                7 to Offset(w * 0.5f, h * 0.75f),
                8 to Offset(w * 0.75f, h * 0.88f),
                9 to Offset(w * 0.88f, h * 0.75f),
                10 to Offset(w * 0.75f, h * 0.5f),
                11 to Offset(w * 0.88f, h * 0.25f),
                12 to Offset(w * 0.75f, h * 0.12f)
            )

            val rashiPaint = Paint().apply {
                color = Color.parseColor("#FFD700") // Rich Gold for Rashi number
                textSize = 30f
                isFakeBoldText = true
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }

            val planetPaint = Paint().apply {
                color = Color.parseColor("#FF4500") // Orangey-red for planets
                textSize = 24f
                isFakeBoldText = true
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }

            val lagnaRashi = chartData.ascendantRashiNumber
            fun getRashiForHouse(house: Int): Int {
                return (lagnaRashi + house - 2) % 12 + 1
            }

            for (houseNum in 1..12) {
                val offset = housePositions[houseNum] ?: Offset(0f, 0f)
                val canvasX = chartLeft + offset.x
                val canvasY = chartTop + offset.y

                val rashiNum = getRashiForHouse(houseNum)
                val planets = (chartData.housePlanetsMap[houseNum] ?: emptyList()).map { com.example.astro.AstroNames.houseGlyph(it) }

                // Draw rashi number
                canvas.drawText("$rashiNum", canvasX, canvasY - 10f, rashiPaint)

                // Draw planet short names if any
                if (planets.isNotEmpty()) {
                    val planetsText = planets.joinToString(" ")
                    canvas.drawText(planetsText, canvasX, canvasY + 22f, planetPaint)
                }
            }

            // Draw Chart label inside the center
            val chartLabelPaint = Paint().apply {
                color = Color.parseColor("#D4AF37")
                textSize = 22f
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }
            canvas.drawText(LanguageManager.getString("लग्न कुण्डली", "Lagna chart"), (width / 2).toFloat(), (chartTop + chartSize / 2f + 5f), chartLabelPaint)

            // 4. Footer info / watermark
            val footerPaint = Paint().apply {
                color = Color.parseColor("#888888") // Grey text
                textSize = 24f
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }
            canvas.drawText("Revati - Vedic Astrology App", (width / 2).toFloat(), (height - 80).toFloat(), footerPaint)

            val watermarkPaint = Paint().apply {
                color = Color.parseColor("#D4AF37")
                textSize = 20f
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }
            // Was "www.astroveda.app" — a domain nobody owns, under a brand name
            // this app stopped using. Every shared chart carried it. The Play
            // listing is the only address that actually resolves to Revati.
            canvas.drawText(
                "Revati : Kundli & Panchang — Google Play",
                (width / 2).toFloat(),
                (height - 45).toFloat(),
                watermarkPaint
            )

            // Save the bitmap to a cache file
            val cacheDir = context.cacheDir
            val imagesFolder = File(cacheDir, "shared_images")
            if (!imagesFolder.exists()) {
                imagesFolder.mkdirs()
            }
            val file = File(imagesFolder, "kundali_chart_${System.currentTimeMillis()}.png")
            val out = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            out.flush()
            out.close()

            // Return Uri
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } catch (e: Exception) {
            android.util.Log.e("KundaliImageGenerator", "Chart image share failed", e)
            null
        }
    }

    private data class Offset(val x: Float, val y: Float)
}
