package com.example.data.ai

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiAstroService {

    private const val TAG = "GeminiAstroService"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun getAiAstrologyInsight(
        userQuestion: String,
        personDetails: String = ""
    ): String = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            Log.d(TAG, "API key is empty or default; utilizing fallback offline Vedic response.")
            return@withContext getOfflineVedicResponse(userQuestion)
        }

        try {
            val systemPrompt = """
                You are AstroVeda AI - an expert, compassionate Vedic Astrologer (ज्योतिषाचार्य).
                Provide accurate, insightful, and uplifting Vedic astrology guidance in clear Hindi (with English technical terms in brackets).
                Focus on planetary remedies (उपाय), gemstones (रत्न), and practical wisdom based on Parashara Jyotish principles.
                IMPORTANT: Respond in plain text only. Do NOT use Markdown formatting - no headers (###), no bold (**text**), no bullet symbols (*), no horizontal rules (---). Use plain sentences and paragraphs, with line breaks between sections instead of Markdown headers.
            """.trimIndent()

            val fullPrompt = if (personDetails.isNotBlank()) {
                "जात विवरण (Kundali Details): $personDetails\n\nप्रश्न (Question): $userQuestion"
            } else {
                "प्रश्न (Question): $userQuestion"
            }

            val jsonBody = JSONObject().apply {
                put("system_instruction", JSONObject().apply {
                    put("parts", JSONObject().apply {
                        put("text", systemPrompt)
                    })
                })
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", fullPrompt)
                            })
                        })
                    })
                })
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = jsonBody.toString().toRequestBody(mediaType)

            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-latest:generateContent?key=$apiKey"

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = try {
                client.newCall(request).execute()
            } catch (e: Exception) {
                Log.e(TAG, "Network failure calling Gemini API: ${e.message}", e)
                return@withContext getOfflineVedicResponse(userQuestion)
            }

            val responseString = response.body?.string() ?: ""

            if (response.isSuccessful && responseString.isNotBlank()) {
                try {
                    val jsonRes = JSONObject(responseString)
                    val candidates = jsonRes.optJSONArray("candidates")
                    if (candidates != null && candidates.length() > 0) {
                        val firstCandidate = candidates.getJSONObject(0)
                        val content = firstCandidate.optJSONObject("content")
                        val parts = content?.optJSONArray("parts")
                        if (parts != null && parts.length() > 0) {
                            return@withContext parts.getJSONObject(0).optString("text", "शुभं करोति कल्याणम्।")
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to parse Gemini response: ${e.message}", e)
                }
            } else {
                Log.e(TAG, "Gemini API error response code: ${response.code}, message: $responseString")
            }
            return@withContext getOfflineVedicResponse(userQuestion)
        } catch (e: Exception) {
            Log.e(TAG, "Unhandled exception in getAiAstrologyInsight: ${e.message}", e)
            return@withContext getOfflineVedicResponse(userQuestion)
        }
    }

    fun getOfflineVedicResponse(question: String): String {
        val qLower = question.lowercase()
        return when {
            qLower.contains("career") || qLower.contains("नौकरी") || qLower.contains("व्यापार") || qLower.contains("job") ->
                "वैदिक ज्योतिष शास्त्र के अनुसार दशम भाव (10th House) एवं कर्मेश ग्रह का अध्ययन आवश्यक है। सूर्य एवं गुरु ग्रह की स्थिति अनुकूल होने पर नौकरी में शीघ्र पदोन्नति एवं व्यापार में लाभ होता है। प्रतिदिन प्रातःकाल सूर्य देव को तांबे के पात्र से जल अर्पित करें (ॐ घृणिः सूर्याय नमः)।"
            qLower.contains("marriage") || qLower.contains("विवाह") || qLower.contains("शादी") || qLower.contains("love") ->
                "सप्तम भाव (7th House) एवं शुक्र/गुरु ग्रह की शुभ दृष्टि वैवाहिक सुख का आधार है। यदि विवाह में विलम्ब हो रहा हो तो प्रत्येक गुरुवार को बेसन के लड्डू अथवा पीली वस्तु का दान करें तथा शिव-पार्वती जी का पूजन करें।"
            qLower.contains("health") || qLower.contains("स्वास्थ्य") || qLower.contains("रोग") ->
                "प्रथम भाव (लग्न) एवं लग्नेश ग्रह का बलवान होना निरोगी काया हेतु अनिवार्य है। महामृत्युंजय मन्त्र अथवा आदित्य हृदय स्तोत्र का पाठ करने से शारीरिक एवं मानसिक ऊर्जा में वृद्धि होती है।"
            else ->
                "ज्योतिष शास्त्र जीवन का मार्गदर्शन करता है। नवग्रह शांति हेतु प्रतिदिन प्रातः स्नानोपरांत 'ॐ नमो भगवते वासुदेवाय' मन्त्र का जप करें तथा अपने कुलदेवता व माता-पिता का आशीर्वाद प्राप्त करें। जीवन में सर्वत्र समृद्धि प्राप्त होगी।"
        }
    }

    suspend fun fetchAstroNewsWithSearchGrounding(): String = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            Log.d(TAG, "API key is empty or default; utilizing fallback offline Astro News.")
            return@withContext getOfflineAstroNews()
        }

        try {
            val systemPrompt = "You are a real-time Astro-News curator. Using Google Search grounding, search for current 2026 Vedic astrological planetary transits (गोचर), eclipses (ग्रहण), and astronomical events. Provide 3 crisp, fascinating news highlights in Hindi with clear headings."

            val jsonBody = JSONObject().apply {
                put("system_instruction", JSONObject().apply {
                    put("parts", JSONObject().apply {
                        put("text", systemPrompt)
                    })
                })
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", "Search latest 2026 Vedic astrology planetary transits and space astronomical news.")
                            })
                        })
                    })
                })
                put("tools", JSONArray().apply {
                    put(JSONObject().apply {
                        put("googleSearch", JSONObject())
                    })
                })
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = jsonBody.toString().toRequestBody(mediaType)

            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-latest:generateContent?key=$apiKey"

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = try {
                client.newCall(request).execute()
            } catch (e: Exception) {
                Log.e(TAG, "Network failure calling Gemini API (AstroNews): ${e.message}", e)
                return@withContext getOfflineAstroNews()
            }

            val responseString = response.body?.string() ?: ""

            if (response.isSuccessful && responseString.isNotBlank()) {
                try {
                    val jsonRes = JSONObject(responseString)
                    val candidates = jsonRes.optJSONArray("candidates")
                    if (candidates != null && candidates.length() > 0) {
                        val firstCandidate = candidates.getJSONObject(0)
                        val content = firstCandidate.optJSONObject("content")
                        val parts = content?.optJSONArray("parts")
                        if (parts != null && parts.length() > 0) {
                            return@withContext parts.getJSONObject(0).optString("text", getOfflineAstroNews())
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to parse Gemini response (AstroNews): ${e.message}", e)
                }
            } else {
                Log.e(TAG, "Gemini API error response code (AstroNews): ${response.code}, message: $responseString")
            }
            return@withContext getOfflineAstroNews()
        } catch (e: Exception) {
            Log.e(TAG, "Unhandled exception in fetchAstroNewsWithSearchGrounding: ${e.message}", e)
            return@withContext getOfflineAstroNews()
        }
    }

    fun getOfflineAstroNews(): String {
        return """
            • 🪐 गुरु ग्रह का अतिचारी गोचर (Jupiter Transit):
              देवगुरु बृहस्पति इस माह मिथुन राशि से कर्क राशि में प्रवेश करेंगे। उच्च के गुरु से हंस महापुरुष योग निर्मित होगा, जिससे ज्ञान व शिक्षा क्षेत्र में उन्नति होगी।

            • 🌘 सूर्य ग्रहण एवं खगोलीय स्थिति (Solar Eclipse Info):
              वर्ष 2026 का आगामी कंकणाकृति सूर्य ग्रहण अत्यंत दुर्लभ होगा। खगोलशास्त्रियों एवं ज्योतिषियों के अनुसार इस दौरान आकाश में 'रिंग ऑफ फायर' का भव्य नज़ारा देखने को मिलेगा।

            • 🌌 नासा जेम्स वेब टेलीस्कोप की खोज (Space & Astronomy News):
              हाल ही में खगोलशास्त्रियों ने दीप अंतरिक्ष में नवजात नक्षत्र मंडल की खोज की है, जो वैदिक ब्रह्मांड विज्ञान के 'हिरण्यगर्भ' सिद्धांत की पुष्टि करता है।
        """.trimIndent()
    }
}
