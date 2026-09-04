package com.example.astro

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Twelve rashis must not be shown the same horoscope.
 *
 * This exists because they were. The rating, the lucky colour and the lucky
 * stone were each a modular hash of `rashiIdx + house`, and since
 *
 *     house = ((planetRashiIdx - rashiIdx + 12) % 12) + 1
 *
 * the rashiIdx cancels: the sum reduces to the transiting planet's own sign and
 * is therefore identical for every reader. Every sign saw five stars out of five
 * on the daily view, four on the weekly, and the lucky colour took two distinct
 * values across the entire zodiac. The generated readings were fine, which is
 * why it survived — the text looked varied, so nobody checked the numbers.
 *
 * The trap is that any *single* rashi looks perfectly correct in isolation. Only
 * comparing all twelve shows it, so that is what these do.
 */
class RashifalVariesByRashiTest {

    private val periods = listOf("TODAY", "WEEK", "MONTH")

    @Test
    fun `every period returns all twelve rashis`() {
        periods.forEach { period ->
            assertEquals(period, 12, RashifalProvider.getHoroscope(period).size)
        }
    }

    @Test
    fun `the rating is not the same for every rashi`() {
        periods.forEach { period ->
            val ratings = RashifalProvider.getHoroscope(period).map { it.ratingStars }
            assertTrue(
                "$period gave all twelve rashis the same rating ($ratings) — the " +
                    "rashi has cancelled out of whatever produces it",
                ratings.distinct().size > 1
            )
        }
    }

    @Test
    fun `the lucky colour and stone follow the rashi lord, so most signs differ`() {
        periods.forEach { period ->
            val all = RashifalProvider.getHoroscope(period)
            // Seven grahas rule the twelve signs, so seven is the correct count:
            // the pairs that share a lord (Aries/Scorpio, Taurus/Libra,
            // Gemini/Virgo, Sagittarius/Pisces, Capricorn/Aquarius) share a
            // colour and a ratna, and that is tradition rather than a bug.
            assertEquals(
                "$period: lucky colour should take one value per graha",
                7, all.map { it.luckyColorHi }.distinct().size
            )
            assertEquals(
                "$period: lucky stone should take one value per graha",
                7, all.map { it.luckyStoneHi }.distinct().size
            )
            assertEquals(
                "$period: the English colour must vary with the Hindi one",
                7, all.map { it.luckyColorEn }.distinct().size
            )
        }
    }

    @Test
    fun `each rashi gets its own reading in both languages`() {
        periods.forEach { period ->
            val all = RashifalProvider.getHoroscope(period)
            listOf(
                "general" to all.map { it.generalReadingHi },
                "generalEn" to all.map { it.generalReadingEn },
                "career" to all.map { it.careerReadingHi },
                "love" to all.map { it.loveReadingHi },
                "health" to all.map { it.healthReadingHi },
                "finance" to all.map { it.financeReadingHi }
            ).forEach { (name, texts) ->
                assertEquals(
                    "$period: two rashis share the same $name reading",
                    12, texts.distinct().size
                )
            }
        }
    }

    @Test
    fun `the gochar rating follows the classical shubha houses`() {
        // Chandra gochar is read as favourable from the 1st, 3rd, 6th, 7th,
        // 10th and 11th; Surya gochar from the 3rd, 6th, 10th and 11th.
        listOf(1, 3, 6, 7, 10, 11).forEach {
            assertEquals("Moon in house $it", 5, RashifalProvider.gocharRating("Moon", it))
        }
        listOf(3, 6, 10, 11).forEach {
            assertEquals("Sun in house $it", 5, RashifalProvider.gocharRating("Sun", it))
        }
        listOf(4, 5, 8, 9, 12).forEach {
            assertEquals("Moon in house $it", 3, RashifalProvider.gocharRating("Moon", it))
        }
        // The Sun is not read as favourable from the 1st or the 7th, where the
        // Moon is; this is the difference between the two tables.
        assertEquals(3, RashifalProvider.gocharRating("Sun", 1))
        assertEquals(3, RashifalProvider.gocharRating("Sun", 7))
    }
}
