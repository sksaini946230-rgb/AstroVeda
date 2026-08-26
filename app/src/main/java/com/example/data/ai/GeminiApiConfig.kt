package com.example.data.ai

import com.example.BuildConfig
import java.util.concurrent.TimeUnit

/**
 * Gemini API configuration: endpoint URL, timeouts, and API key resolution.
 *
 * Keeping this in its own file means the HTTP layer can be unit-tested
 * (e.g. verifying the key is read from BuildConfig) without constructing
 * a real OkHttpClient.
 */
object GeminiApiConfig {

    const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/"
    const val DEFAULT_MODEL = "gemini-flash-latest"
    const val GENERATE_CONTENT_ENDPOINT = ":generateContent"

    const val DEFAULT_API_KEY = "MY_GEMINI_API_KEY"

    /** Connect/read/write timeouts in seconds for the underlying OkHttp client. */
    const val CONNECT_TIMEOUT_SECONDS = 60L
    const val READ_TIMEOUT_SECONDS = 60L
    const val WRITE_TIMEOUT_SECONDS = 60L

    /** Returns the configured API key, or [DEFAULT_API_KEY] when BuildConfig is unavailable. */
    fun getApiKey(): String = try {
        BuildConfig.GEMINI_API_KEY
    } catch (e: Exception) {
        DEFAULT_API_KEY
    }

    /** True when no real API key is configured (offline mode). */
    fun isOffline(): Boolean {
        val key = getApiKey()
        return key.isBlank() || key == DEFAULT_API_KEY
    }

    /** Builds the full generateContent URL with the API key appended. */
    fun generateContentUrl(model: String = DEFAULT_MODEL, apiKey: String): String =
        "$BASE_URL${model}$GENERATE_CONTENT_ENDPOINT?key=$apiKey"
}