package com.example.astro

import com.example.data.model.ChoghadiyaType
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar

/**
 * The order of the Choghadiya, not merely how many there are.
 *
 * The only test that touched Choghadiya asserted that eight slots came back.
 * Eight always came back, so it always passed — while Sunday's daytime sequence
 * was wrong. On a Sunday the app called 07:34-09:09 **Amrit**, the most
 * auspicious slot there is, when the cycle makes it Char, merely neutral; and
 * called 09:09-10:43 **Rog**, inauspicious, when it is Labh. This is the screen
 * people read to decide when to start something.
 *
 * The rule is not a table to be memorised, it is derivable, which is what makes
 * it testable. Each Choghadiya belongs to a graha:
 *
 *     Udveg = Sun, Char = Venus, Labh = Mercury, Amrit = Moon,
 *     Kaal = Saturn, Shubh = Jupiter, Rog = Mars
 *
 * and they run in the Chaldean order of those grahas. So the daytime sequence
 * for a weekday is that one cycle, rotated to begin with the Choghadiya of the
 * weekday's own lord — Sunday from Udveg because Sunday is the Sun's, Monday
 * from Amrit because Monday is the Moon's, and so on. The night sequence is the
 * same shape, beginning instead with the lord of the fifth weekday from this
 * one, which is the classical rule.
 *
 * Six of the seven daytime rows obeyed this. Sunday stepped through the cycle
 * three at a time instead of one, which is what a copying slip looks like.
 */
class ChoghadiyaSequenceTest {

    /** Chaldean order, written in Choghadiya names. */
    private val cycle = listOf(
        ChoghadiyaType.UDVEG,  // Sun
        ChoghadiyaType.CHAR,   // Venus
        ChoghadiyaType.LABH,   // Mercury
        ChoghadiyaType.AMRIT,  // Moon
        ChoghadiyaType.KAAL,   // Saturn
        ChoghadiyaType.SHUBH,  // Jupiter
        ChoghadiyaType.ROG     // Mars
    )

    /** The Choghadiya each weekday's lord owns. */
    private val weekdayLord = mapOf(
        Calendar.SUNDAY to ChoghadiyaType.UDVEG,
        Calendar.MONDAY to ChoghadiyaType.AMRIT,
        Calendar.TUESDAY to ChoghadiyaType.ROG,
        Calendar.WEDNESDAY to ChoghadiyaType.LABH,
        Calendar.THURSDAY to ChoghadiyaType.SHUBH,
        Calendar.FRIDAY to ChoghadiyaType.CHAR,
        Calendar.SATURDAY to ChoghadiyaType.KAAL
    )

    private val weekdayName = mapOf(
        Calendar.SUNDAY to "Sunday", Calendar.MONDAY to "Monday",
        Calendar.TUESDAY to "Tuesday", Calendar.WEDNESDAY to "Wednesday",
        Calendar.THURSDAY to "Thursday", Calendar.FRIDAY to "Friday",
        Calendar.SATURDAY to "Saturday"
    )

    /** Eight slots: the seven of the cycle, then the first one again. */
    private fun expectedFrom(first: ChoghadiyaType): List<ChoghadiyaType> {
        val i = cycle.indexOf(first)
        return (0..7).map { cycle[(i + it) % 7] }
    }

    /** A date that falls on the given weekday, in a week with no DST anywhere near it. */
    private fun dateOn(weekday: Int): java.util.Date {
        val cal = Calendar.getInstance().apply {
            set(2026, Calendar.MARCH, 1, 12, 0, 0)   // 1 Mar 2026 is a Sunday
            set(Calendar.MILLISECOND, 0)
        }
        while (cal.get(Calendar.DAY_OF_WEEK) != weekday) cal.add(Calendar.DAY_OF_MONTH, 1)
        return cal.time
    }

    @Test
    fun `each day's Choghadiya runs the Chaldean cycle from its own lord`() {
        val wrong = mutableListOf<String>()

        for ((weekday, lord) in weekdayLord) {
            val slots = ChoghadiyaCalculator.getChoghadiyaSlots(
                date = dateOn(weekday), isDaytime = true
            )
            val actual = slots.map { it.type }
            val expected = expectedFrom(lord)
            if (actual != expected) {
                wrong += buildString {
                    append("${weekdayName[weekday]} daytime\n")
                    append("    expected: ${expected.joinToString(" ") { it.nameEn }}\n")
                    append("    actual:   ${actual.joinToString(" ") { it.nameEn }}")
                }
            }
        }

        assertEquals(
            "these weekdays do not follow the cycle:\n" + wrong.joinToString("\n"),
            emptyList<String>(), wrong
        )
    }

    @Test
    fun `the night sequence starts with the lord of the fifth weekday`() {
        val wrong = mutableListOf<String>()

        for ((weekday, _) in weekdayLord) {
            // Fifth weekday counting this one as the first: +4 days.
            val fifth = ((weekday - 1 + 4) % 7) + 1
            val expectedFirst = weekdayLord.getValue(fifth)

            val first = ChoghadiyaCalculator.getChoghadiyaSlots(
                date = dateOn(weekday), isDaytime = false
            ).first().type

            if (first != expectedFirst) {
                wrong += "${weekdayName[weekday]} night starts at ${first.nameEn}, " +
                    "expected ${expectedFirst.nameEn} (lord of ${weekdayName[fifth]})"
            }
        }

        assertEquals(
            "night sequences start on the wrong Choghadiya:\n" + wrong.joinToString("\n"),
            emptyList<String>(), wrong
        )
    }

    @Test
    fun `every weekday still returns eight day and eight night slots`() {
        for (weekday in weekdayLord.keys) {
            val d = dateOn(weekday)
            assertEquals(
                "${weekdayName[weekday]} daytime", 8,
                ChoghadiyaCalculator.getChoghadiyaSlots(d, isDaytime = true).size
            )
            assertEquals(
                "${weekdayName[weekday]} night", 8,
                ChoghadiyaCalculator.getChoghadiyaSlots(d, isDaytime = false).size
            )
        }
    }
}
