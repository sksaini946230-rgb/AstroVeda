package com.example.astro

data class NumerologyValidationResult(
    val isValid: Boolean,
    val nameError: String? = null,
    val dobError: String? = null
)

object NumerologyValidator {

    fun validateName(name: String): String? {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) {
            return "नाम दर्ज करना अनिवार्य है (Name is required)"
        }
        if (trimmed.length < 2) {
            return "नाम में कम से कम 2 अक्षर होने चाहिए (Min 2 characters)"
        }
        if (!trimmed.any { it.isLetter() }) {
            return "नाम में वैध अक्षर होने चाहिए (Name must contain letters)"
        }
        return null
    }

    fun validateDob(dob: String): String? {
        val trimmed = dob.trim()
        if (trimmed.isEmpty()) {
            return "जन्म तिथि अनिवार्य है (DOB is required)"
        }
        
        // Regex for YYYY-MM-DD
        val regex = Regex("""^(\d{4})-(0[1-9]|1[0-2])-(0[1-9]|[12]\d|3[01])$""")
        val match = regex.matchEntire(trimmed)
        if (match == null) {
            return "प्रारूप YYYY-MM-DD होना चाहिए (Format: YYYY-MM-DD)"
        }

        val (yearStr, monthStr, dayStr) = match.destructured
        val year = yearStr.toIntOrNull() ?: 0
        val month = monthStr.toIntOrNull() ?: 0
        val day = dayStr.toIntOrNull() ?: 0

        if (year < 1900 || year > 2026) {
            return "वर्ष 1900 से 2026 के बीच होना चाहिए (Year between 1900-2026)"
        }

        // Days in month check
        val maxDays = when (month) {
            2 -> if ((year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)) 29 else 28
            4, 6, 9, 11 -> 30
            else -> 31
        }

        if (day > maxDays) {
            return "माह $month में अधिकतम $maxDays दिन होते हैं (Invalid day for month)"
        }

        return null
    }

    fun validateInput(name: String, dob: String): NumerologyValidationResult {
        val nameErr = validateName(name)
        val dobErr = validateDob(dob)
        val valid = (nameErr == null && dobErr == null)
        return NumerologyValidationResult(
            isValid = valid,
            nameError = nameErr,
            dobError = dobErr
        )
    }
}
