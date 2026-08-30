package com.example.util

import android.os.Build
import android.util.Log
import app.revati.jyotish.BuildConfig
import java.io.File
import java.util.regex.Pattern

/**
 * SecurityUtils — Core security, device integrity, and input sanitization utilities.
 * Complies with OWASP Mobile Top 10 guidelines and DPDP Act 2023.
 */
object SecurityUtils {

    private const val TAG = "SecurityUtils"

    // --- 1. Root & Device Integrity Check ---

    private val KNOWN_ROOT_PATHS = arrayOf(
        "/system/app/Superuser.apk",
        "/sbin/su",
        "/system/bin/su",
        "/system/xbin/su",
        "/data/local/xbin/su",
        "/data/local/bin/su",
        "/system/sd/xbin/su",
        "/system/bin/failsafe/su",
        "/data/local/su",
        "/su/bin/su"
    )

    /**
     * Checks if the host Android device shows indications of root/tampering.
     * Returns true if root signatures or su binaries are detected.
     */
    fun isDeviceRooted(): Boolean {
        return checkBuildTags() || checkRootBinaries() || checkSuCommand()
    }

    private fun checkBuildTags(): Boolean {
        val buildTags = Build.TAGS
        return buildTags != null && buildTags.contains("test-keys")
    }

    private fun checkRootBinaries(): Boolean {
        return try {
            KNOWN_ROOT_PATHS.any { path -> File(path).exists() }
        } catch (_: Throwable) {
            false
        }
    }

    private fun checkSuCommand(): Boolean {
        var process: Process? = null
        return try {
            process = Runtime.getRuntime().exec(arrayOf("/system/xbin/which", "su"))
            val reader = process.inputStream.bufferedReader()
            reader.readLine() != null
        } catch (_: Throwable) {
            false
        } finally {
            process?.destroy()
        }
    }

    // --- 2. Input Sanitization & Attack Prevention ---

    private val HTML_TAG_PATTERN = Pattern.compile("<[^>]*>")
    private val SCRIPT_PATTERN = Pattern.compile("(?i)<script.*?>.*?</script.*?>")
    private val SQL_INJECTION_PATTERN = Pattern.compile("(?i)(\b(SELECT|INSERT|UPDATE|DELETE|DROP|ALTER|UNION|OR|AND)\b|--|;)")

    /**
     * Sanitizes general text input (names, places, search queries) by removing
     * script tags, HTML markup, control characters, and excess whitespace.
     */
    fun sanitizeTextInput(input: String?): String {
        if (input.isNullOrBlank()) return ""
        var clean = input.trim()
        clean = SCRIPT_PATTERN.matcher(clean).replaceAll("")
        clean = HTML_TAG_PATTERN.matcher(clean).replaceAll("")
        // Strip non-printable ASCII control characters except standard whitespace
        clean = clean.replace(Regex("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F\\x7F]"), "")
        // Prevent buffer overflows by capping reasonable length
        return clean.take(255)
    }

    /**
     * Validates date string in YYYY-MM-DD format with realistic calendar bounds.
     */
    fun isValidDate(dateStr: String): Boolean {
        val parts = dateStr.trim().split("-")
        if (parts.size != 3) return false
        val year = parts[0].toIntOrNull() ?: return false
        val month = parts[1].toIntOrNull() ?: return false
        val day = parts[2].toIntOrNull() ?: return false

        if (year !in 1900..2100) return false
        if (month !in 1..12) return false
        val maxDays = when (month) {
            4, 6, 9, 11 -> 30
            2 -> if ((year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)) 29 else 28
            else -> 31
        }
        return day in 1..maxDays
    }

    /**
     * Validates time string in HH:MM format (24-hour).
     */
    fun isValidTime(timeStr: String): Boolean {
        val parts = timeStr.trim().split(":")
        if (parts.size != 2) return false
        val hour = parts[0].toIntOrNull() ?: return false
        val minute = parts[1].toIntOrNull() ?: return false
        return hour in 0..23 && minute in 0..59
    }

    // --- 3. Privacy Masking & Safe Logging ---

    /**
     * Masks an email address for privacy-compliant UI display (e.g. j***e@domain.com).
     */
    fun maskEmail(email: String?): String {
        if (email.isNullOrBlank() || !email.contains("@")) return "—"
        val parts = email.split("@")
        val name = parts[0]
        val domain = parts[1]
        val maskedName = when {
            name.length <= 2 -> name.first() + "***"
            else -> name.first() + "***" + name.last()
        }
        return "$maskedName@$domain"
    }

    /**
     * Masks a phone number for privacy display (e.g. +91 ****** 4321).
     */
    fun maskPhone(phone: String?): String {
        if (phone.isNullOrBlank()) return "—"
        val clean = phone.filter { it.isDigit() || it == '+' }
        if (clean.length < 6) return "***"
        val visibleSuffix = clean.takeLast(4)
        return "******$visibleSuffix"
    }

    /**
     * Secure debug logger that is automatically suppressed in Release builds to avoid PII leaks.
     */
    fun logSecure(tag: String, message: String) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, message)
        }
    }
}
