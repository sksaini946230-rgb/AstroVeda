package com.example.astro

import java.util.Calendar
import java.util.GregorianCalendar

/**
 * Festival dates, computed from the tithi rules rather than typed in per year.
 *
 * The festival table used to carry a hardcoded date per entry, for one year
 * only. That is a cliff — the list ran out in November 2026 — and it was also
 * wrong: three of the twelve 2026 dates in the shipped release disagreed with
 * published panchang (Govardhan Puja, Chhath and Gangaur were each a day early).
 *
 * Every one of these festivals is defined by a rule, and this app already owns
 * an ephemeris good enough to apply it.
 *
 * **Purnimanta, not Amanta.** The month names in the festival table follow
 * North Indian usage, where a lunar month ends at the full moon. Janmashtami is
 * "Bhadrapada Krishna Ashtami" there; the same day is Shravana Krishna Ashtami
 * in the Amanta reckoning [PanchangElements.masaIndex] returns. The two agree in
 * Shukla Paksha and differ by one month in Krishna Paksha, so a Krishna-paksha
 * rule is shifted back a month before it is matched. Getting this backwards
 * moves Diwali and Janmashtami by a whole lunar month.
 *
 * **A festival is not always the sunrise tithi.** Most are: the day is the one
 * whose sunrise falls inside the wanted tithi. But several have their own
 * window, fixed by tradition and not by us — Ganesh Chaturthi is decided at
 * madhyahna, Dussehra at aparahna, Dhanteras and Diwali at pradosh. Where a
 * tithi touches the window on two days, the day it pervades more of it wins.
 * See [Observance].
 *
 * Sunrise is taken at Ujjain, the classical reference meridian for Indian
 * panchang, so every user sees one national calendar rather than one that
 * shifts with the city they happen to have picked.
 *
 * Verified against published panchang for 2025-2028 in `FestivalCalculatorTest`
 * — 48 dates, all four years. If you touch this file, run that test.
 */
object FestivalCalculator {

    /** Ujjain — the classical prime meridian of Indian astronomy. */
    private const val REFERENCE_LAT = 23.1765
    private const val REFERENCE_LON = 75.7885

    /** Pradosh runs three muhurta — 2h24m — from sunset. */
    private const val PRADOSH_LENGTH_DAYS = 2.4 / 24.0

    /**
     * The window of the day that decides which date a festival falls on.
     *
     * The daytime, sunrise to sunset, divides into five equal parts: pratahkala,
     * sangava, madhyahna, aparahna, sayahna. A festival that is "madhyahna
     * vyapini" is held on the day whose third part the tithi pervades.
     */
    enum class Observance {
        /** Tithi current at sunrise. The ordinary rule. */
        SUNRISE,
        /** Third fifth of the daytime. Ganesh Chaturthi. */
        MADHYAHNA,
        /** Fourth fifth of the daytime. Dussehra / Vijayadashami. */
        APARAHNA,
        /** Sunset to 2h24m after. Dhanteras, Lakshmi Puja on Diwali. */
        PRADOSH,
        /** The middle of the night that follows the day. */
        NISHITA,
        /** Moonrise. Karwa Chauth, whose fast is broken when the Moon is seen. */
        CHANDRODAYA,
        /**
         * Sunrise through the end of pradosh, counting only the time Bhadra
         * leaves free. Raksha Bandhan: no rakhi is tied during Bhadra, and
         * Bhadra sits on the first half of Purnima, so Purnima reaching into a
         * day is not enough — the day that wins is the one with the most usable
         * time in it. In 2026 that put the festival on 28 August, whose Purnima
         * ran only to 9:48 AM, over the 27th, whose whole afternoon was Purnima
         * but entirely Bhadra.
         */
        RAKSHA_BANDHAN
    }

    /** Tithi 1..30: 1..15 Shukla (15 = Purnima), 16..30 Krishna (30 = Amavasya). */
    fun tithiNumberFor(pakshaHi: String, tithiHi: String): Int? {
        if (tithiHi.contains(AstroNames.AMAVASYA_HI)) return 30
        if (tithiHi.contains("पूर्णिमा")) return 15

        val index = AstroNames.TITHI_HI.indexOfFirst { it == tithiHi }
        if (index < 0) return null
        return if (pakshaHi.contains(AstroNames.SHUKLA_HI)) index + 1 else index + 16
    }

    /** Index 0..11 of a Hindi month name, Chaitra = 0. */
    fun masaIndexFor(monthNameHi: String): Int? =
        AstroNames.MASA_HI.indexOfFirst { it == monthNameHi }.takeIf { it >= 0 }

    /**
     * The Gregorian date in [year] on which the rule falls, or null if it does
     * not fall in that year — a Chaitra festival can land either side of a year
     * boundary, and an Adhika month can push one out entirely.
     *
     * [purnimantaMasaIndex] is the month as the festival table names it.
     */
    fun dateFor(
        purnimantaMasaIndex: Int,
        tithiNumber: Int,
        year: Int,
        observance: Observance = Observance.SUNRISE
    ): Calendar? {
        val isKrishna = tithiNumber >= 16
        // Purnimanta Krishna Paksha belongs to the previous Amanta month.
        val amantaMasa =
            if (isKrishna) (purnimantaMasaIndex - 1 + 12) % 12 else purnimantaMasaIndex

        // The tithi that precedes the wanted one, so a day where it begins after
        // sunrise is still considered — it may still pervade a later window.
        val previousTithi = if (tithiNumber == 1) 30 else tithiNumber - 1

        var best: Calendar? = null
        var bestUsable = 0.0
        var bestWindow = 0.0
        var bestDaylight = 0.0

        val cal = GregorianCalendar(AstroTime.IST).apply { clear(); set(year, Calendar.JANUARY, 1) }
        val end = GregorianCalendar(AstroTime.IST).apply { clear(); set(year, Calendar.DECEMBER, 31) }

        while (!cal.after(end)) {
            val midnightJd = AstroTime.julianDayFromLocal(
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1,
                cal.get(Calendar.DAY_OF_MONTH), 0, 0, AstroTime.IST
            )
            val times = RiseSetCalculator.sunRiseSet(midnightJd, REFERENCE_LAT, REFERENCE_LON)
            val sunrise = times.riseJd ?: (midnightJd + 0.25)
            val sunset = times.setJd ?: (midnightJd + 0.75)

            // Only days where the tithi is already running at sunrise, or begins
            // during the day, can hold the festival. This keeps the window
            // sampling to two or three days a year instead of 365.
            val sunriseTithi = PanchangElements.tithiNumber(sunrise)
            if ((sunriseTithi == tithiNumber || sunriseTithi == previousTithi) &&
                !isAdhikaMonth(sunrise)
            ) {
                val moonrise = RiseSetCalculator
                    .moonRiseSet(midnightJd, REFERENCE_LAT, REFERENCE_LON).riseJd
                val (from, to) = window(observance, sunrise, sunset, moonrise)
                val windowOverlap = pervasion(from, to, tithiNumber, amantaMasa)
                val daylightOverlap = pervasion(sunrise, sunset, tithiNumber, amantaMasa)

                // Bhadra-free pervasion ranks above pervasion that Bhadra eats,
                // so a later usable afternoon beats an earlier forbidden one.
                val usable = if (observance == Observance.RAKSHA_BANDHAN) {
                    pervasion(from, to, tithiNumber, amantaMasa, excludeBhadra = true)
                } else {
                    windowOverlap
                }

                // The ritual window decides it. A kshaya tithi can fail to touch
                // any day's window — it is short enough to fall between two
                // sunrises entirely — and then the day it spends most of itself
                // in is the day it is kept. Without this, Govardhan Puja 2028
                // and Sharad Navratri 2027 have no date at all.
                // Raksha Bandhan is kept on the first day that offers any
                // Bhadra-free Purnima at all, however little: in 2031 that was a
                // nine-minute window in the pradosh of 2 August, chosen over the
                // hour and a half the next morning held.
                if (observance == Observance.RAKSHA_BANDHAN && bestUsable > 0.0) {
                    cal.add(Calendar.DAY_OF_MONTH, 1)
                    continue
                }

                val better = usable > bestUsable ||
                    (usable == bestUsable && windowOverlap > bestWindow) ||
                    (usable == bestUsable && windowOverlap == bestWindow &&
                        daylightOverlap > bestDaylight)
                if (better && (windowOverlap > 0.0 || daylightOverlap > 0.0)) {
                    bestUsable = usable
                    bestWindow = windowOverlap
                    bestDaylight = daylightOverlap
                    best = cal.clone() as Calendar
                }
            }
            cal.add(Calendar.DAY_OF_MONTH, 1)
        }
        return best
    }

    private fun window(
        observance: Observance,
        sunrise: Double,
        sunset: Double,
        moonrise: Double?
    ): Pair<Double, Double> {
        val fifth = (sunset - sunrise) / 5.0
        return when (observance) {
            Observance.SUNRISE -> sunrise to sunrise
            Observance.MADHYAHNA -> (sunrise + 2 * fifth) to (sunrise + 3 * fifth)
            Observance.APARAHNA -> (sunrise + 3 * fifth) to (sunrise + 4 * fifth)
            Observance.RAKSHA_BANDHAN -> sunrise to (sunset + PRADOSH_LENGTH_DAYS)
            Observance.PRADOSH -> sunset to (sunset + PRADOSH_LENGTH_DAYS)
            // Nishita is the eighth of fifteen muhurta of the night, which is
            // the middle of it. The night belongs to the day it follows, so this
            // window can run past midnight into the next calendar date.
            // The fast ends when the Moon is sighted, so the tithi at moonrise
            // is what fixes the day — not the tithi at sunrise, which in 2027
            // would have moved Karwa Chauth a day late.
            Observance.CHANDRODAYA -> {
                val instant = moonrise ?: (sunset + 2.0 / 24.0)
                instant to instant
            }
            Observance.NISHITA -> {
                val nightLength = 1.0 - (sunset - sunrise)
                val middle = sunset + nightLength / 2.0
                val muhurta = nightLength / 15.0
                (middle - muhurta / 2.0) to (middle + muhurta / 2.0)
            }
        }
    }

    /**
     * How much of [from]..[to] the wanted tithi covers, 0..1. A zero-length
     * window (the sunrise rule) is a plain test of that instant.
     *
     * Sampled rather than solved: a tithi boundary inside the window is what
     * decides the day, and a minute's resolution over a window of a couple of
     * hours settles that far more sharply than the difference between any two
     * published panchangs.
     */
    private fun pervasion(
        from: Double,
        to: Double,
        tithiNumber: Int,
        amantaMasa: Int,
        excludeBhadra: Boolean = false
    ): Double {
        // Masa is settled once at the edges rather than at every sample: it can
        // only turn at a new moon, and finding one is far more work than a
        // tithi. That is what makes minute-resolution sampling affordable.
        val masaOk = PanchangElements.masaIndex(from) == amantaMasa ||
            PanchangElements.masaIndex(to) == amantaMasa
        if (!masaOk) return 0.0

        fun holds(jd: Double): Boolean {
            if (PanchangElements.tithiNumber(jd) != tithiNumber) return false
            if (excludeBhadra && isBhadra(jd)) return false
            return true
        }

        if (to <= from) return if (holds(from)) 1.0 else 0.0

        // A minute apiece. Raksha Bandhan 2031 turns on a usable window nine
        // minutes wide, and a coarser sweep walks straight past it.
        val minutes = ((to - from) * 24.0 * 60.0).toInt()
        val samples = minutes.coerceIn(60, 2000)
        var inside = 0
        for (i in 0..samples) {
            if (holds(from + (to - from) * i / samples)) inside++
        }
        return inside.toDouble() / (samples + 1)
    }

    /**
     * True if the lunar month containing [jd] is Adhika — the intercalary month
     * that keeps the lunar year with the solar one.
     *
     * A lunar month is Adhika when the Sun changes no sign during it. Festivals
     * are not kept in it; they wait for the Nija month of the same name, which
     * follows. Without this, Gangaur 2029 lands in the Adhika Chaitra of March
     * instead of the real one in April.
     */
    private fun isAdhikaMonth(jd: Double): Boolean {
        val start = PanchangElements.lastNewMoonJd(jd)
        val nextStart = PanchangElements.lastNewMoonJd(start + 31.0)
        val signAtStart = (AstroMath.sunSidereal(start) / 30.0).toInt().coerceIn(0, 11)
        val signAtEnd = (AstroMath.sunSidereal(nextStart - 0.01) / 30.0).toInt().coerceIn(0, 11)
        return signAtStart == signAtEnd
    }

    /** Bhadra is the Vishti karana. */
    private fun isBhadra(jd: Double): Boolean =
        PanchangElements.karanaName(PanchangElements.karanaIndex(jd)).contains("भद्रा")
}
