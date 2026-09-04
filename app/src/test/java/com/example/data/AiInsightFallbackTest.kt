package com.example.data

import com.example.data.ai.GeminiAstroService
import org.junit.Assert.assertEquals
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
}
