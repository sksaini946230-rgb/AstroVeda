package com.example.data.ai

import org.json.JSONArray
import org.json.JSONObject

/**
 * Builds the JSON payload for Gemini `generateContent`.
 *
 * Keeps prompt logic separate from HTTP and parsing — makes it
 * easy to test prompt structure without making network calls.
 */
object GeminiPromptBuilder {

    private val SYSTEM_PROMPT_VEDIC = """
You are AstroVeda AI - an expert, compassionate Vedic Astrologer (ज्योतिषाचार्य).
Provide accurate, insightful, and uplifting Vedic astrology guidance in clear Hindi (with English technical terms in brackets).
Focus on planetary remedies (उपाय), gemstones (रत्न), and practical wisdom based on Parashara Jyotish principles.
IMPORTANT: Respond in plain text only. Do NOT use Markdown formatting - no headers (###), no bold (**text**), no bullet symbols (*), no horizontal rules (---). Use plain sentences and paragraphs, with line breaks between sections instead of Markdown headers.
""".trimIndent()

    private const val SYSTEM_PROMPT_NEWS = "You are a real-time Astro-News curator. Using Google Search grounding, search for current 2026 Vedic astrological planetary transits (गोचर), eclipses (ग्रहण), and astronomical events. Provide 3 crisp, fascinating news highlights in Hindi with clear headings."

    fun buildInsightPayload(userQuestion: String, personDetails: String = ""): JSONObject {
        val fullPrompt = if (personDetails.isNotBlank()) {
            "जात विवरण (Kundali Details): $personDetails\n\nप्रश्न (Question): $userQuestion"
        } else {
            "प्रश्न (Question): $userQuestion"
        }
        return buildPayload(SYSTEM_PROMPT_VEDIC, fullPrompt, includeSearch = false)
    }

    fun buildNewsPayload(): JSONObject = buildPayload(
        SYSTEM_PROMPT_NEWS,
        "Search latest 2026 Vedic astrology planetary transits and space astronomical news.",
        includeSearch = true
    )

    private fun buildPayload(systemText: String, userText: String, includeSearch: Boolean): JSONObject {
        return JSONObject().apply {
            put("system_instruction", JSONObject().apply {
                put("parts", JSONObject().apply {
                    put("text", systemText)
                })
            })
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", userText) })
                    })
                })
            })
            if (includeSearch) {
                put("tools", JSONArray().apply {
                    put(JSONObject().apply {
                        put("googleSearch", JSONObject())
                    })
                })
            }
        }
    }
}