package com.example.util

import java.util.regex.Pattern

/**
 * Input sanitisation and privacy masking.
 *
 * WHY THIS FILE SHRANK: it used to be 154 lines advertising itself as OWASP
 * Mobile Top 10 and DPDP Act 2023 compliance, with seven passing unit tests — and
 * **not one call site anywhere in the app**. The tests passed, the security
 * document cited it, and none of it ran. What actually protects this app is
 * BirthData.parse, which validates properly; this file now supports that instead
 * of pretending to replace it.
 *
 * Removed, deliberately:
 *
 *  - `isDeviceRooted` and its three helpers. Root detection that nothing consults
 *    is pure liability — one of them shelled out through Runtime.exec for a result
 *    no caller read.
 *  - `SQL_INJECTION_PATTERN`. It had never worked: in Kotlin `"\b"` is the
 *    backspace character, not a regex word boundary, so the pattern only matched
 *    SQL keywords wrapped in literal backspaces. It was also unnecessary — every
 *    Room query in this app is parameterised, with no @RawQuery or execSQL
 *    anywhere.
 *  - `logSecure`. Nothing called it, and no logging in this app carries PII.
 */
object SecurityUtils {

    private val HTML_TAG_PATTERN = Pattern.compile("<[^>]*>")
    private val SCRIPT_PATTERN = Pattern.compile("(?i)<script.*?>.*?</script.*?>")

    /** Longest free-text value accepted for a name, place or note. */
    const val MAX_TEXT_LENGTH = 255

    /**
     * Cleans free text — names, places, notes — by removing script and HTML
     * markup and control characters, and capping the length.
     *
     * The cap matters: `name` and `placeOfBirth` flow into Room, into the
     * Firestore document, into the PDF report and into the share sheet, and
     * nothing bounded them.
     */
    fun sanitizeTextInput(input: String?): String {
        if (input.isNullOrBlank()) return ""
        var clean = input.trim()
        clean = SCRIPT_PATTERN.matcher(clean).replaceAll("")
        clean = HTML_TAG_PATTERN.matcher(clean).replaceAll("")
        // Strip non-printable ASCII control characters except standard whitespace
        clean = clean.replace(Regex("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F\\x7F]"), "")
        return clean.take(MAX_TEXT_LENGTH)
    }

    /**
     * Validates a date string in YYYY-MM-DD format with realistic calendar bounds.
     *
     * BirthData.parse is the authority for birth input and does this itself with
     * bilingual errors; this is for callers that only need a yes or no.
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

    /** Validates a time string in HH:MM format (24-hour). */
    fun isValidTime(timeStr: String): Boolean {
        val parts = timeStr.trim().split(":")
        if (parts.size != 2) return false
        val hour = parts[0].toIntOrNull() ?: return false
        val minute = parts[1].toIntOrNull() ?: return false
        return hour in 0..23 && minute in 0..59
    }

    /** Masks an email address for privacy-compliant UI display (e.g. j***e@domain.com). */
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

    /** Masks a phone number for privacy display (e.g. ******4321). */
    fun maskPhone(phone: String?): String {
        if (phone.isNullOrBlank()) return "—"
        val clean = phone.filter { it.isDigit() || it == '+' }
        if (clean.length < 6) return "***"
        val visibleSuffix = clean.takeLast(4)
        return "******$visibleSuffix"
    }
}
