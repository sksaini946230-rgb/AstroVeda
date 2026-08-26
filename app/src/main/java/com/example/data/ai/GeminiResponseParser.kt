package com.example.data.ai

import org.json.JSONObject

/**
 * Pure parser for Gemini API JSON responses.
 *
 * Extracted from [GeminiAstroService] so it can be unit-tested without
 * touching the network. The expected shape is the standard
 * `generateContent` payload:
 *
 * ```json
 * {
 *   "candidates": [
 *     {
 *       "content": {
 *         "parts": [
 *           { "text": "..." }
 *         ]
 *       }
 *     }
 *   ]
 * }
 * ```
 */
object GeminiResponseParser {

    /**
     * Returns the first `text` part in the response, or `null` if
     * the JSON doesn't match the expected shape.
     */
    fun extractFirstText(responseJson: String): String? {
        return try {
            val root = JSONObject(responseJson)
            val candidates = root.optJSONArray("candidates") ?: return null
            if (candidates.length() == 0) return null

            val firstCandidate = candidates.getJSONObject(0)
            val content = firstCandidate.optJSONObject("content") ?: return null
            val parts = content.optJSONArray("parts") ?: return null
            if (parts.length() == 0) return null

            parts.getJSONObject(0).optString("text").takeIf { it.isNotBlank() }
        } catch (e: Exception) {
            null
        }
    }
}