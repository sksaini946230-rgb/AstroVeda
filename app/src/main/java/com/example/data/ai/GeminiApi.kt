package com.example.data.ai

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Pure HTTP adapter for the Gemini API.
 *
 * Contains no business logic — just builds OkHttp requests from JSON
 * payloads and parses JSON responses back to String. This makes it
 * trivial to mock in unit tests (inject a fake OkHttp [client] or
 * stub with MockWebServer).
 */
class GeminiApi(private val client: OkHttpClient = defaultClient()) {

    fun postGenerateContent(url: String, jsonBody: JSONObject): String? {
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = jsonBody.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return null
            return response.body?.string()
        }
    }

    companion object {
        fun defaultClient(): OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(GeminiApiConfig.CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(GeminiApiConfig.READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(GeminiApiConfig.WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()
    }
}