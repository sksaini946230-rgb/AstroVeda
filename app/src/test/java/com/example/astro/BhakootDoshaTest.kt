package com.example.astro

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Bhakoot, checked across the whole zodiac rather than on one couple.
 *
 * The bug this pins was invisible from a single match: 5/9 (Nav-Pancham) is one
 * of the three Bhakoot doshas and it was being scored the full 7 points, with
 * the screen saying "no Bhakoot dosha" underneath. Any one couple looked
 * plausible; only laying all 144 rashi pairs out side by side shows that the
 * zero appears in four columns where it should appear in six.
 *
 * Seven points of thirty-six is not a rounding difference — it is enough to
 * carry a match over the 18-point line the conclusion text treats as a verdict.
 *
 * The table itself is asserted from the classical rule rather than restated as
 * a literal, so a test that agrees with a wrong implementation cannot pass.
 */
class BhakootDoshaTest {

    /** Distances that carry a dosha, from the shastra: 2/12, 5/9, 6/8. */
    private fun isDoshaDistance(d: Int) = d in setOf(2, 12, 5, 9, 6, 8)

    // Reached by reflection until the koots were made `internal` for
    // ClassicalTablesTest. Reflection by name is the wrong tool here anyway: an
    // internal function's JVM name carries a module suffix, so the lookup broke
    // silently on a visibility change rather than on a behaviour change.
    private fun bhakoot(boyRashi: Int, girlRashi: Int): Double =
        KundaliMatchingCalculator.calculateBhakoot(boyRashi, girlRashi)

    @Test
    fun `every rashi pair scores bhakoot by the classical rule`() {
        val wrong = mutableListOf<String>()
        for (b in 0..11) {
            for (g in 0..11) {
                val dist = ((g - b + 12) % 12) + 1
                val expected = if (isDoshaDistance(dist)) 0.0 else 7.0
                val actual = bhakoot(b, g)
                if (actual != expected) {
                    wrong += "boy=$b girl=$g dist=$dist expected=$expected actual=$actual"
                }
            }
        }
        assertTrue("Bhakoot disagrees with the classical rule:\n" + wrong.joinToString("\n"), wrong.isEmpty())
    }

    @Test
    fun `nav pancham is a dosha in both directions`() {
        // Sagittarius (8) and Leo (4) are 5/9 from each other. This is the pair
        // that shipped as a full 7 with "no Bhakoot dosha" printed beneath it.
        assertEquals(0.0, bhakoot(8, 4), 0.001)
        assertEquals(0.0, bhakoot(4, 8), 0.001)
    }

    @Test
    fun `a third of the zodiac carries a dosha, not a fifth`() {
        // Six of the twelve one-way distances are doshas, so exactly half the
        // pairs from any given sign score zero. Counting the whole grid catches
        // a missing pair even if the direct assertions above are edited away.
        val zeros = (0..11).sumOf { b -> (0..11).count { g -> bhakoot(b, g) == 0.0 } }
        assertEquals(72, zeros)
    }

    @Test
    fun `the dosha label names nav pancham rather than mislabelling it`() {
        // 15 Jun 1995 puts the Moon in Sagittarius; 22 Nov 1997 puts it in Leo.
        // Before the fix this pair produced "no Bhakoot dosha"; a naive fix that
        // only changed the score would have called it Shadashtaka.
        val result = KundaliMatchingCalculator.matchKundali(
            "Boy", "1995-06-15", "12:00",
            "Girl", "1997-11-22", "12:00"
        )
        assertTrue("expected a Bhakoot dosha for a 5/9 pair", result.hasBhakootDosha)
        assertTrue(
            "Hindi label should name Nav-Pancham, was: ${result.bhakootDoshaStatusHi}",
            result.bhakootDoshaStatusHi.contains("नवपंचम")
        )
        assertTrue(
            "English label should name Nav-Pancham, was: ${result.bhakootDoshaStatusEn}",
            result.bhakootDoshaStatusEn.contains("Nav-Pancham")
        )
    }
}
