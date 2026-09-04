package com.example.astro

import java.util.TimeZone

/**
 * A validated birth moment and place.
 *
 * WHY THIS EXISTS: the Kundali form used to hand raw strings to the calculator,
 * which parsed them with `?: 1995`, `?: 1`, `?: 12` fallbacks. Typing "abcd" as a
 * date produced a complete, confident, entirely fictional chart — and so did
 * "25-08-1994", which is the format most Indian users reach for first. Parsing
 * now happens in one place and fails loudly.
 */
data class BirthData(
    val name: String,
    val year: Int,
    val month: Int,
    val day: Int,
    val hour: Int,
    val minute: Int,
    val placeName: String,
    val latitude: Double,
    val longitude: Double,
    val zone: TimeZone = AstroTime.IST
) {
    /** Julian Day (UT) of the birth moment. */
    val julianDay: Double
        get() = AstroTime.julianDayFromLocal(year, month, day, hour, minute, zone)

    val dateString: String get() = String.format(java.util.Locale.US, "%04d-%02d-%02d", year, month, day)
    val timeString: String get() = String.format(java.util.Locale.US, "%02d:%02d", hour, minute)

    companion object {

        /** The app's fallback location (Jaipur) — used only when a caller genuinely has none. */
        const val FALLBACK_LAT = 26.9124
        const val FALLBACK_LNG = 75.7873

        /**
         * Parses `YYYY-MM-DD` and `HH:MM`.
         *
         * @throws BirthDataException with a bilingual, user-facing message.
         */
        fun parse(
            name: String,
            dobString: String,
            tobString: String,
            placeName: String,
            latitude: Double,
            longitude: Double,
            zone: TimeZone = AstroTime.IST
        ): BirthData {
            // Free text goes on to Room, the Firestore document, the PDF report
            // and the share sheet, and nothing bounded or cleaned it. SecurityUtils
            // has done this since it was written; it simply had no callers.
            val trimmedName = com.example.util.SecurityUtils.sanitizeTextInput(name)
            if (trimmedName.isBlank()) {
                throw BirthDataException(
                    "कृपया नाम दर्ज करें।",
                    "Please enter a name."
                )
            }

            val dateParts = dobString.trim().split("-")
            if (dateParts.size != 3) {
                throw BirthDataException(
                    "जन्म तिथि YYYY-MM-DD प्रारूप में होनी चाहिए (उदा. 1994-08-25)।",
                    "Date of birth must be in YYYY-MM-DD format (e.g. 1994-08-25)."
                )
            }
            val year = dateParts[0].toIntOrNull()
            val month = dateParts[1].toIntOrNull()
            val day = dateParts[2].toIntOrNull()
            if (year == null || month == null || day == null) {
                throw BirthDataException(
                    "जन्म तिथि में केवल अंक होने चाहिए (YYYY-MM-DD)।",
                    "Date of birth must contain only digits (YYYY-MM-DD)."
                )
            }
            if (year < 1800 || year > 2200) {
                throw BirthDataException(
                    "जन्म वर्ष 1800 और 2200 के बीच होना चाहिए। क्या आपने दिन-माह-वर्ष के क्रम में लिखा है?",
                    "Birth year must be between 1800 and 2200. Did you enter day-month-year instead?"
                )
            }
            if (month !in 1..12) {
                throw BirthDataException(
                    "माह 1 से 12 के बीच होना चाहिए।",
                    "Month must be between 1 and 12."
                )
            }
            if (day !in 1..daysInMonth(year, month)) {
                throw BirthDataException(
                    "इस माह में दिन 1 से ${daysInMonth(year, month)} के बीच होना चाहिए।",
                    "Day must be between 1 and ${daysInMonth(year, month)} for this month."
                )
            }

            val timeParts = tobString.trim().split(":")
            if (timeParts.size != 2) {
                throw BirthDataException(
                    "जन्म समय HH:MM (24 घंटे) प्रारूप में होना चाहिए (उदा. 14:15)।",
                    "Time of birth must be in HH:MM 24-hour format (e.g. 14:15)."
                )
            }
            val hour = timeParts[0].toIntOrNull()
            val minute = timeParts[1].toIntOrNull()
            if (hour == null || minute == null) {
                throw BirthDataException(
                    "जन्म समय में केवल अंक होने चाहिए (HH:MM)।",
                    "Time of birth must contain only digits (HH:MM)."
                )
            }
            if (hour !in 0..23) {
                throw BirthDataException(
                    "घंटा 0 से 23 के बीच होना चाहिए (24 घंटे का प्रारूप)।",
                    "Hour must be between 0 and 23 (24-hour format)."
                )
            }
            if (minute !in 0..59) {
                throw BirthDataException(
                    "मिनट 0 से 59 के बीच होना चाहिए।",
                    "Minute must be between 0 and 59."
                )
            }

            if (latitude < -90.0 || latitude > 90.0 || longitude < -180.0 || longitude > 180.0) {
                throw BirthDataException(
                    "जन्म स्थान के निर्देशांक अमान्य हैं। कृपया सूची में से स्थान चुनें।",
                    "Birth place coordinates are invalid. Please pick a place from the list."
                )
            }

            return BirthData(
                name = trimmedName,
                year = year, month = month, day = day,
                hour = hour, minute = minute,
                placeName = com.example.util.SecurityUtils.sanitizeTextInput(placeName).ifBlank { "—" },
                latitude = latitude, longitude = longitude,
                zone = zone
            )
        }

        private fun daysInMonth(year: Int, month: Int): Int = when (month) {
            1, 3, 5, 7, 8, 10, 12 -> 31
            4, 6, 9, 11 -> 30
            2 -> if ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0) 29 else 28
            else -> 31
        }
    }
}

/** Thrown when birth input cannot be trusted. Carries text meant for the user, in both languages. */
class BirthDataException(
    val messageHi: String,
    val messageEn: String
) : IllegalArgumentException(messageEn)
