package com.example.util

/**
 * The hour the daily alerts go out, in one place.
 *
 * It has drifted twice. Once the worker and the ViewModel read the same
 * preference key with different fallbacks, so Settings said 7:00 AM and the
 * notification arrived at 6:30. That was fixed by making both read 7 — and then
 * the onboarding screen was found still promising "प्रातः 06:00 बजे", a
 * hardcoded string nobody had updated, so the app told a new user one time and
 * delivered at another.
 *
 * Four places need this number: MainViewModel's default, the two workers'
 * fallbacks, and the sentence onboarding shows. A constant is the only way they
 * stay equal.
 */
object NotificationDefaults {

    /** Daily Panchang and Rahu Kaal alert, 24-hour clock. */
    const val DAILY_HOUR = 7

    /** Muhurat and Choghadiya alert. Same hour, its own key and its own switch. */
    const val MUHURAT_HOUR = 7

    /** "07:00 AM" — for a sentence, not for scheduling. */
    fun formatHour(hour: Int): String {
        val suffix = if (hour < 12) "AM" else "PM"
        val h = when {
            hour == 0 -> 12
            hour > 12 -> hour - 12
            else -> hour
        }
        return "%02d:00 %s".format(h, suffix)
    }
}
