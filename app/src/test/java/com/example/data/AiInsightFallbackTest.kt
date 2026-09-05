package com.example.data

import com.example.data.ai.GeminiAstroService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Why the offline fallback must never be shown as a personalised insight.
 *
 * The Rashifal screen offers an "AI Personalized Insight" per sign. When the
 * model cannot be reached, getAiAstrologyInsight returns getOfflineVedicResponse
 * for the question it was given — and that function picks its text by keyword:
 * "career", "marriage", "health". The insight question is
 *
 *     "Provide a personalized daily horoscope insight for <sign>."
 *
 * which contains none of them, so all twelve signs fall into the same else
 * branch and receive one identical paragraph. Printed under a heading promising
 * a personalised reading, that is not a cosmetic problem — it tells the user
 * something untrue about what they are looking at.
 *
 * MainViewModel.fetchPersonalizedInsight therefore treats a result equal to the
 * fallback as a failure and offers a retry. This test pins the premise: if the
 * fallback ever does become sign-aware, this fails and that decision can be
 * revisited deliberately rather than by accident.
 */
class AiInsightFallbackTest {

    private val signs = listOf(
        "Aries", "Taurus", "Gemini", "Cancer", "Leo", "Virgo",
        "Libra", "Scorpio", "Sagittarius", "Capricorn", "Aquarius", "Pisces"
    )

    private fun insightQuestion(sign: String) =
        "Provide a personalized daily horoscope insight for $sign."

    @Test
    fun `the offline fallback cannot tell the twelve signs apart`() {
        val answers = signs.map { GeminiAstroService.getOfflineVedicResponse(insightQuestion(it)) }

        assertEquals(
            "the offline fallback returns one text for all twelve signs, which is " +
                "why MainViewModel treats it as a failure rather than an insight",
            1, answers.distinct().size
        )
    }

    @Test
    fun `the fallback is still keyword aware for questions that carry one`() {
        val career = GeminiAstroService.getOfflineVedicResponse("What about my career?")
        val marriage = GeminiAstroService.getOfflineVedicResponse("When is my marriage?")
        val generic = GeminiAstroService.getOfflineVedicResponse(insightQuestion("Aries"))

        assertEquals(3, listOf(career, marriage, generic).distinct().size)
    }

    /**
     * The app's own suggested questions must not all get the same answer.
     *
     * They did. "When will I get a promotion at work?" carried neither "career"
     * nor "job"; the money chip had no branch to land in at all; so three of the
     * four English chips returned the generic paragraph. Worse, the Hindi twin of
     * the first one matched on नौकरी and the English one did not, so the same tap
     * gave a different-quality answer depending on the reader's language.
     *
     * These are the exact strings from NumerologyScreen.quickQuestions. If a chip
     * is reworded, this fails, which is the point.
     */
    private val quickQuestionsEn = listOf(
        "When will I get a promotion at work?",
        "Is marriage likely for me in 2026?",
        "Which remedy helps with money?",
        "Simple remedies to calm a Rahu dasha?"
    )

    private val quickQuestionsHi = listOf(
        "मेरी नौकरी में पदोन्नति कब होगी?",
        "क्या मेरा विवाह 2026 में संभव है?",
        "धन लाभ हेतु कौन सा उपाय करें?",
        "राहु दशा शांति के सरल उपाय क्या हैं?"
    )

    @Test
    fun `each suggested question offline gets its own topical answer`() {
        val answers = quickQuestionsEn.map { GeminiAstroService.getOfflineVedicResponse(it) }
        assertEquals(
            "the four chips the app puts in front of the user must not collapse " +
                "into one answer — got:\n" + answers.joinToString("\n") { it.take(60) },
            4, answers.distinct().size
        )
    }

    @Test
    fun `the Hindi chips reach the same topics as the English ones`() {
        quickQuestionsEn.zip(quickQuestionsHi).forEach { (en, hi) ->
            assertEquals(
                "\"$en\" and its Hindi twin must land in the same branch",
                GeminiAstroService.getOfflineVedicResponse(en),
                GeminiAstroService.getOfflineVedicResponse(hi)
            )
        }
    }

    @Test
    fun `none of the suggested questions falls through to the generic answer`() {
        val generic = GeminiAstroService.getOfflineVedicResponse("zzz nothing matches this")
        (quickQuestionsEn + quickQuestionsHi).forEach { q ->
            assertTrue(
                "\"$q\" fell through to the catch-all",
                GeminiAstroService.getOfflineVedicResponse(q) != generic
            )
        }
    }
}
