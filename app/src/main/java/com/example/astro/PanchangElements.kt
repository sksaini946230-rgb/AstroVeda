package com.example.astro

/**
 * The five limbs of the Panchang, solved from the ephemeris.
 *
 * Three things were wrong here before, and all three were visible to every user
 * on every screen:
 *
 *  1. Tithi and Nakshatra end times were the hardcoded strings
 *     "अगले दिन 04:15 AM तक" and "रात्रि 11:30 PM तक" — the same two lines every
 *     day, for every city, on the Panchang screen, the daily card AND the home
 *     screen widget. They are now solved by bisection.
 *  2. Karana was `(elongation / 6) % 11`, which cycles all eleven names evenly.
 *     The real sequence has seven movable Karanas repeating eight times between
 *     four fixed ones across the sixty half-tithis of a lunar month.
 *  3. The lunar month was the Gregorian month index — August came out as Kartika.
 *     It now comes from the Sun's sidereal sign at the new moon that began the month.
 */
object PanchangElements {

    const val TITHI_SPAN = 12.0
    const val NAKSHATRA_SPAN = 360.0 / 27.0
    const val YOGA_SPAN = 360.0 / 27.0
    const val KARANA_SPAN = 6.0

    /** Movable Karanas, repeating eight times through the month. */
    private val MOVABLE_KARANAS = listOf(
        "बव (Bava)", "बालव (Balava)", "कौलव (Kaulava)", "तैतिल (Taitila)",
        "गर (Gara)", "वणिज (Vanija)", "विष्टि/भद्रा (Vishti/Bhadra)"
    )
    private const val KIMSTUGHNA = "किंस्तुघ्न (Kimstughna)"
    private const val SHAKUNI = "शकुनि (Shakuni)"
    private const val CHATUSHPADA = "चतुष्पाद (Chatushpada)"
    private const val NAGA = "नाग (Naga)"

    val MASA_NAMES_HI = listOf(
        "चैत्र (Chaitra)", "वैशाख (Vaisakha)", "ज्येष्ठ (Jyeshtha)", "आषाढ़ (Ashadha)",
        "श्रावण (Shravana)", "भाद्रपद (Bhadrapada)", "आश्विन (Ashvin)", "कार्तिक (Kartika)",
        "मार्गशीर्ष (Margashirsha)", "पौष (Pausha)", "माघ (Magha)", "फाल्गुन (Phalguna)"
    )
    val MASA_NAMES_EN = listOf(
        "Chaitra", "Vaisakha", "Jyeshtha", "Ashadha", "Shravana", "Bhadrapada",
        "Ashvin", "Kartika", "Margashirsha", "Pausha", "Magha", "Phalguna"
    )

    // ------------------------------------------------------------------
    // Instantaneous values
    // ------------------------------------------------------------------

    /** Tithi number 1..30 (1..15 Shukla, 16..30 Krishna). */
    fun tithiNumber(jd: Double): Int =
        (AstroMath.elongation(jd) / TITHI_SPAN).toInt().coerceIn(0, 29) + 1

    /** Nakshatra index 0..26 from the Moon. */
    fun nakshatraIndex(jd: Double): Int =
        (AstroMath.moonSidereal(jd) / NAKSHATRA_SPAN).toInt().coerceIn(0, 26)

    /** Pada 1..4 within the current Nakshatra. */
    fun nakshatraPada(jd: Double): Int {
        val moon = AstroMath.moonSidereal(jd)
        val within = moon - (moon / NAKSHATRA_SPAN).toInt() * NAKSHATRA_SPAN
        return (within / (NAKSHATRA_SPAN / 4.0)).toInt().coerceIn(0, 3) + 1
    }

    /** Yoga index 0..26 from (Sun + Moon). */
    fun yogaIndex(jd: Double): Int =
        (AstroTime.norm360(AstroMath.sunSidereal(jd) + AstroMath.moonSidereal(jd)) / YOGA_SPAN)
            .toInt().coerceIn(0, 26)

    /** Karana index 0..59 across the lunar month. */
    fun karanaIndex(jd: Double): Int =
        (AstroMath.elongation(jd) / KARANA_SPAN).toInt().coerceIn(0, 59)

    /** Karana name for a 0..59 index, honouring the four fixed Karanas. */
    fun karanaName(index: Int): String = when (index) {
        0 -> KIMSTUGHNA
        57 -> SHAKUNI
        58 -> CHATUSHPADA
        59 -> NAGA
        else -> MOVABLE_KARANAS[(index - 1) % 7]
    }

    // ------------------------------------------------------------------
    // End times — when the current value gives way to the next
    // ------------------------------------------------------------------

    /**
     * Julian Day at which the quantity produced by [valueOf] next crosses a
     * multiple of [span], searching forward up to [maxDays].
     *
     * Returns null if no crossing is found, which for these quantities means
     * something is wrong rather than something is slow.
     */
    private fun nextBoundary(
        jd: Double,
        span: Double,
        maxDays: Double,
        valueOf: (Double) -> Double
    ): Double? {
        val startIndex = Math.floor(valueOf(jd) / span)
        val step = 0.02   // ~29 minutes; every one of these quantities is monotonic at this scale
        var prev = jd
        var t = jd + step
        while (t <= jd + maxDays) {
            val idx = Math.floor(valueOf(t) / span)
            if (idx != startIndex) {
                var lo = prev
                var hi = t
                repeat(40) {
                    val mid = (lo + hi) / 2.0
                    if (Math.floor(valueOf(mid) / span) == startIndex) lo = mid else hi = mid
                }
                return (lo + hi) / 2.0
            }
            prev = t
            t += step
        }
        return null
    }

    /** When the current Tithi ends. A Tithi runs 19-26 hours, so two days is ample. */
    fun tithiEndJd(jd: Double): Double? =
        nextBoundary(jd, TITHI_SPAN, 2.0) { AstroMath.elongation(it) }

    /** When the current Nakshatra ends. */
    fun nakshatraEndJd(jd: Double): Double? =
        nextBoundary(jd, NAKSHATRA_SPAN, 2.0) { AstroMath.moonSidereal(it) }

    /** When the current Yoga ends. */
    fun yogaEndJd(jd: Double): Double? =
        nextBoundary(jd, YOGA_SPAN, 2.0) {
            AstroTime.norm360(AstroMath.sunSidereal(it) + AstroMath.moonSidereal(it))
        }

    /** When the current Karana ends — half a Tithi, so at most about 13 hours. */
    fun karanaEndJd(jd: Double): Double? =
        nextBoundary(jd, KARANA_SPAN, 1.5) { AstroMath.elongation(it) }

    // ------------------------------------------------------------------
    // Lunar month and era
    // ------------------------------------------------------------------

    /**
     * Julian Day of the new moon (elongation = 0) most recently before [jd].
     * Found by walking back to the wrap in elongation, then bisecting.
     */
    fun lastNewMoonJd(jd: Double): Double {
        val step = 0.5
        var t = jd
        var value = AstroMath.elongation(t)
        var back = 0.0
        while (back < 32.0) {
            val prevT = t - step
            val prevValue = AstroMath.elongation(prevT)
            if (prevValue > value) {
                // The wrap from 360 back to 0 lies in (prevT, t).
                var lo = prevT
                var hi = t
                repeat(45) {
                    val mid = (lo + hi) / 2.0
                    if (AstroMath.elongation(mid) > 180.0) lo = mid else hi = mid
                }
                return (lo + hi) / 2.0
            }
            t = prevT
            value = prevValue
            back += step
        }
        return jd - 29.53
    }

    /**
     * Index 0..11 of the lunar month (Chaitra = 0), Amanta reckoning.
     *
     * A lunar month is named for the solar sign the Sun enters during it: the
     * month running from the new moon while the Sun is in Meena is Chaitra.
     */
    fun masaIndex(jd: Double): Int {
        val newMoon = lastNewMoonJd(jd)
        val sunSign = (AstroMath.sunSidereal(newMoon) / 30.0).toInt().coerceIn(0, 11)
        return (sunSign + 1) % 12
    }

    /** True while the Moon is waxing (Shukla Paksha). */
    fun isShuklaPaksha(jd: Double): Boolean = tithiNumber(jd) <= 15

    /**
     * Julian Day of Chaitra Shukla Pratipada — the lunar new year — for the
     * Gregorian year containing [jd]. This is the instant Vikram Samvat and the
     * Saka era both roll over, and it lands somewhere in March or April.
     */
    fun lunarNewYearJd(gregorianYear: Int): Double {
        // Scan the new moons from mid-February to early May and take the first
        // one that opens Chaitra.
        var probe = AstroTime.julianDay(gregorianYear, 2, 15, 0.0)
        val limit = AstroTime.julianDay(gregorianYear, 5, 10, 0.0)
        while (probe <= limit) {
            val newMoon = lastNewMoonJd(probe + 29.53)
            if (newMoon in probe..limit) {
                val sunSign = (AstroMath.sunSidereal(newMoon) / 30.0).toInt().coerceIn(0, 11)
                if ((sunSign + 1) % 12 == 0) return newMoon
            }
            probe += 15.0
        }
        // Should not happen; fall back to a date inside the usual window.
        return AstroTime.julianDay(gregorianYear, 3, 25, 0.0)
    }

    /**
     * Vikram Samvat for [jd].
     *
     * It used to be `gregorianYear + 57` unconditionally, which is wrong for the
     * roughly three months between January and the lunar new year — 1 January 2026
     * was reported as 2083 when it was still 2082.
     */
    fun vikramSamvat(jd: Double, gregorianYear: Int): Int =
        if (jd >= lunarNewYearJd(gregorianYear)) gregorianYear + 57 else gregorianYear + 56

    /** Saka Samvat, which rolls over on the same day. */
    fun sakaSamvat(jd: Double, gregorianYear: Int): Int =
        if (jd >= lunarNewYearJd(gregorianYear)) gregorianYear - 78 else gregorianYear - 79
}
