package com.example.service

import com.example.util.LanguageManager
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.data.model.GunaMatchingResult
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object MatchingPdfReportService {

    fun generatePdfReport(context: Context, result: GunaMatchingResult): File? {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // Standard A4 Size (595 x 842 pt)
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val goldBorderPaint = Paint().apply {
            color = Color.parseColor("#DAA520")
            style = Paint.Style.STROKE
            strokeWidth = 3f
            isAntiAlias = true
        }

        val fillHeaderPaint = Paint().apply {
            color = Color.parseColor("#22120A") // Deep Sacred Maroon Background
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        val textHeaderPaint = Paint().apply {
            color = Color.parseColor("#FFD700") // Sacred Gold
            textSize = 20f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }

        val textSubHeaderPaint = Paint().apply {
            color = Color.parseColor("#EEEEEE")
            textSize = 12f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }

        val titlePaint = Paint().apply {
            color = Color.parseColor("#8B0000") // Deep Red/Maroon
            textSize = 15f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            isAntiAlias = true
        }

        val bodyPaint = Paint().apply {
            color = Color.parseColor("#222222")
            textSize = 12f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            isAntiAlias = true
        }

        val boldBodyPaint = Paint().apply {
            color = Color.parseColor("#111111")
            textSize = 12f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            isAntiAlias = true
        }

        // 1. Draw Outer Decorative Frame
        val margin = 20f
        canvas.drawRect(margin, margin, 595f - margin, 842f - margin, goldBorderPaint)
        canvas.drawRect(margin + 4f, margin + 4f, 595f - margin - 4f, 842f - margin - 4f, goldBorderPaint)

        // 2. Top Header Banner
        val headerRect = RectF(margin + 5f, margin + 5f, 595f - margin - 5f, 100f)
        canvas.drawRect(headerRect, fillHeaderPaint)

        // 3. Header Texts
        canvas.drawText(LanguageManager.getString(" वैदिक अष्टकूट कुण्डली गुण मिलान ", " Vedic Ashtakoot Kundali Matching "), 595f / 2f, 55f, textHeaderPaint)
        canvas.drawText("Revati 36-Guna Kundali Matching Certified Report", 595f / 2f, 78f, textSubHeaderPaint)

        var yPos = 130f

        // 4. Couple Names Banner
        val couplePaint = Paint().apply {
            color = Color.parseColor("#8B0000")
            textSize = 18f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(LanguageManager.getString("वर: ${result.boyName}    कन्या: ${result.girlName}", "Groom: ${result.boyName}    Bride: ${result.girlName}"), 595f / 2f, yPos, couplePaint)
        yPos += 30f

        // 5. Total Guna Score Card
        val scoreCardBg = RectF(40f, yPos, 555f, yPos + 65f)
        val isAuspicious = result.totalObtainedGuna >= 18.0
        val scoreBgPaint = Paint().apply {
            color = if (isAuspicious) Color.parseColor("#E8F5E9") else Color.parseColor("#FFEBEE")
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val scoreBorderPaint = Paint().apply {
            color = if (isAuspicious) Color.parseColor("#2E7D32") else Color.parseColor("#C62828")
            style = Paint.Style.STROKE
            strokeWidth = 2f
            isAntiAlias = true
        }
        canvas.drawRoundRect(scoreCardBg, 12f, 12f, scoreBgPaint)
        canvas.drawRoundRect(scoreCardBg, 12f, 12f, scoreBorderPaint)

        val scoreTextPaint = Paint().apply {
            color = if (isAuspicious) Color.parseColor("#1B5E20") else Color.parseColor("#B71C1C")
            textSize = 21f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(LanguageManager.getString("प्राप्त कुल गुण: ${result.totalObtainedGuna} / 36.0", "Total gunas: ${result.totalObtainedGuna} / 36.0"), 595f / 2f, yPos + 28f, scoreTextPaint)

        val verdictTextPaint = Paint().apply {
            color = Color.parseColor("#333333")
            textSize = 13f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(LanguageManager.getString("निष्कर्ष: ${result.compatibilityVerdictHi}", "Verdict: ${result.compatibilityVerdictEn}"), 595f / 2f, yPos + 52f, verdictTextPaint)

        yPos += 85f

        // 6. Dosha Analysis (Mangal, Nadi, Bhakoot)
        canvas.drawText(LanguageManager.getString("1. प्रमुख दोष विचार:", "1. Dosha analysis:"), 40f, yPos, titlePaint)
        yPos += 18f
        canvas.drawText(LanguageManager.getString("• मंगल दोष: ${result.mangalDoshaStatusHi}", "• Mangal dosha: ${result.mangalDoshaStatusEn}"), 50f, yPos, bodyPaint)
        yPos += 16f
        val nadiColor = if (result.hasNadiDosha) Color.parseColor("#C62828") else Color.parseColor("#2E7D32")
        val nadiPaint = Paint(bodyPaint).apply { color = nadiColor }
        canvas.drawText(LanguageManager.getString(
            "• नाड़ी दोष: ${if (result.hasNadiDosha) "दोष उपस्थित (समान नाड़ी)" else "दोष मुक्त"}",
            "• Nadi dosha: ${if (result.hasNadiDosha) "present (same nadi)" else "none"}"
        ), 50f, yPos, nadiPaint)
        yPos += 16f
        val bhakootColor = if (result.hasBhakootDosha) Color.parseColor("#C62828") else Color.parseColor("#2E7D32")
        val bhakootPaint = Paint(bodyPaint).apply { color = bhakootColor }
        canvas.drawText(LanguageManager.getString(
            "• भकूट दोष: ${if (result.hasBhakootDosha) "दोष उपस्थित" else "दोष मुक्त"}",
            "• Bhakoot dosha: ${if (result.hasBhakootDosha) "present" else "none"}"
        ), 50f, yPos, bhakootPaint)

        yPos += 28f

        // 7. Ashtakoot Breakdown Table Header
        canvas.drawText(LanguageManager.getString("2. अष्टकूट 36-गुण विवरण तालिका:", "2. Ashtakoot score table:"), 40f, yPos, titlePaint)
        yPos += 15f

        // Table Header Row Background
        val tableHeaderBg = RectF(40f, yPos, 555f, yPos + 25f)
        val thPaint = Paint().apply {
            color = Color.parseColor("#F5F0E6")
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawRect(tableHeaderBg, thPaint)

        val thTextPaint = Paint().apply {
            color = Color.parseColor("#5D4037")
            textSize = 11f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            isAntiAlias = true
        }

        canvas.drawText(LanguageManager.getString("अष्टकूट नाम", "Koota"), 50f, yPos + 17f, thTextPaint)
        canvas.drawText(LanguageManager.getString("अधिकतम गुण", "Max"), 230f, yPos + 17f, thTextPaint)
        canvas.drawText(LanguageManager.getString("प्राप्त गुण", "Scored"), 330f, yPos + 17f, thTextPaint)
        canvas.drawText(LanguageManager.getString("व्याख्या व प्रभाव", "What it means"), 420f, yPos + 17f, thTextPaint)

        yPos += 25f

        val tableLinePaint = Paint().apply {
            color = Color.parseColor("#E0E0E0")
            strokeWidth = 1f
        }

        result.kootDetails.forEach { koot ->
            canvas.drawText(koot.kootNameHi, 50f, yPos + 18f, boldBodyPaint)
            canvas.drawText("${koot.maxPoints}", 240f, yPos + 18f, bodyPaint)

            val ptsPaint = Paint(boldBodyPaint).apply {
                color = if (koot.obtainedPoints > 0) Color.parseColor("#2E7D32") else Color.parseColor("#C62828")
            }
            canvas.drawText("${koot.obtainedPoints}", 340f, yPos + 18f, ptsPaint)

            val desc = if (koot.descriptionHi.length > 22) koot.descriptionHi.take(20) + ".." else koot.descriptionHi
            canvas.drawText(desc, 420f, yPos + 18f, bodyPaint)

            yPos += 24f
            canvas.drawLine(40f, yPos, 555f, yPos, tableLinePaint)
        }

        yPos += 25f

        // 8. Summary Reading
        canvas.drawText(LanguageManager.getString("3. विवाह निष्कर्ष एवं परामर्श:", "3. Summary and guidance:"), 40f, yPos, titlePaint)
        yPos += 22f

        // Text Wrapping for Summary Reading
        val summaryWords = result.summaryReadingHi.split(" ")
        var line = ""
        val maxLineWidth = 500f

        summaryWords.forEach { word ->
            val testLine = if (line.isEmpty()) word else "$line $word"
            if (bodyPaint.measureText(testLine) <= maxLineWidth) {
                line = testLine
            } else {
                canvas.drawText(line, 40f, yPos, bodyPaint)
                yPos += 18f
                line = word
            }
        }
        if (line.isNotEmpty()) {
            canvas.drawText(line, 40f, yPos, bodyPaint)
            yPos += 18f
        }

        yPos += 30f

        // Footer Date and AstroVeda Stamp
        val sdf = SimpleDateFormat("dd MMMM yyyy, hh:mm a", Locale.getDefault())
        val dateStr = LanguageManager.getString("रिपोर्ट तिथि: ${sdf.format(Date())}", "Report date: ${sdf.format(Date())}")
        val footerPaint = Paint().apply {
            color = Color.parseColor("#777777")
            textSize = 10f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.ITALIC)
            isAntiAlias = true
        }
        canvas.drawText(dateStr, 40f, 810f, footerPaint)
        canvas.drawText("© Revati Vedic Kundali Matching Service", 320f, 810f, footerPaint)

        pdfDocument.finishPage(page)

        return try {
            val file = File(context.cacheDir, "Kundali_Matching_Report_${System.currentTimeMillis()}.pdf")
            val outputStream = FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            pdfDocument.close()
            outputStream.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            pdfDocument.close()
            null
        }
    }

    fun sharePdfReport(context: Context, pdfFile: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            pdfFile
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(
            Intent.EXTRA_SUBJECT,
            LanguageManager.getString(
                "Revati कुण्डली मिलान रिपोर्ट (PDF)",
                "Revati Kundali Matching PDF Report"
            )
        )
            putExtra(Intent.EXTRA_TEXT, "Revati Vedic 36-Guna Kundali Matching PDF Report")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(
            Intent.createChooser(
                shareIntent,
                LanguageManager.getString(
                    "कुण्डली मिलान रिपोर्ट साझा करें",
                    "Share Kundali Matching PDF Report"
                )
            )
        )
    }
}
