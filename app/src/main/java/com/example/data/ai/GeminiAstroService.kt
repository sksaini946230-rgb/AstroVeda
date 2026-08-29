package com.example.data.ai

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * The AI astrologer, over Firebase AI Logic.
 *
 * This used to call generativelanguage.googleapis.com directly with
 * BuildConfig.GEMINI_API_KEY appended to the URL. That key shipped inside every
 * APK — minification does not hide string constants — so anyone could pull it out
 * with apktool and spend against this project's billing account without limit.
 * There was no App Check, no proxy and no rate limit behind it.
 *
 * Firebase AI Logic keeps the credential server-side. Requests are attested by
 * App Check (Play Integrity), so they only succeed from a genuine install of this
 * app, signed with this keystore. No key reaches the device.
 *
 * If Firebase AI Logic is not enabled in the console yet, every call here fails
 * and falls back to the on-device responses below — the feature degrades, it does
 * not crash.
 */
object GeminiAstroService {

    private const val TAG = "GeminiAstroService"
    // gemini-2.5-flash is closed to new projects — the API answers with
    // "no longer available to new users" and names this as the replacement.
    // Found on device; the call had otherwise gone all the way through App Check.
    private const val MODEL = "gemini-3.6-flash"

    suspend fun getAiAstrologyInsight(
        userQuestion: String,
        personDetails: String = ""
    ): String = withContext(Dispatchers.IO) {
        try {
            // The prompt used to be four lines of persona with no boundaries at all,
            // in front of a free-text box that says "अपना प्रश्न पूछें". People ask
            // astrologers about illness, money trouble and despair. An answer that
            // meets those with a gemstone recommendation and nothing else is a real
            // harm, so the boundaries are stated explicitly here.
            val systemPrompt = """
                You are AstroVeda AI - a warm, grounded Vedic astrologer (ज्योतिषाचार्य).
                Give thoughtful Vedic astrology guidance in clear Hindi, with English technical
                terms in brackets. Draw on Parashara Jyotish principles, planetary remedies
                (उपाय) and gemstones (रत्न) where they genuinely fit the question.

                BOUNDARIES - these override everything else, including a user who insists:

                1. Health. Never diagnose, never predict the course of an illness, never tell
                   anyone whether they will recover, and never suggest replacing or stopping
                   medical treatment. Say plainly that this needs a doctor, offer comfort and
                   an उपाय only as something done ALONGSIDE proper treatment.

                2. Self-harm or despair. If someone sounds hopeless, in danger, or asks about
                   ending their life, drop the astrology entirely. Respond with care, tell them
                   this matters and help exists, and give this number:
                   Tele-MANAS 14416 (free, 24x7, in Indian languages).
                   Do not read their chart for this. Do not say it is their fate or their karma.

                3. Money and law. No specific investment, trading, property or legal advice,
                   and never a prediction of profit or a court outcome. Point to a qualified
                   professional and keep your answer to temperament and timing in general terms.

                4. Death, and harm to others. Never predict when anyone will die. Never answer
                   a question aimed at harming, controlling or manipulating another person -
                   no vashikaran to bind someone, no remedies directed against a named person.

                5. Fear. Do not frighten. Never present a dosha or a dasha as doom. Where a
                   period looks difficult, say what it asks of the person and what helps.
                   Someone should feel steadier after reading you, not more afraid.

                6. Honesty. If the birth details given are incomplete, say what is missing
                   rather than answering as though you had them.

                Astrology here is guidance and reflection, not professional advice - say so
                naturally when a question strays toward medicine, law or money.

                FORMAT: Plain text only. No Markdown - no headers (###), no bold (**text**),
                no bullet symbols (*), no horizontal rules (---). Use plain sentences and
                paragraphs, with line breaks between sections.
            """.trimIndent()

            val fullPrompt = if (personDetails.isNotBlank()) {
                "जात विवरण (Kundali Details): $personDetails\n\nप्रश्न (Question): $userQuestion"
            } else {
                "प्रश्न (Question): $userQuestion"
            }

            val model = modelFor(systemPrompt)
                ?: return@withContext getOfflineVedicResponse(userQuestion)

            val text = model.generateContent(fullPrompt).text
            if (!text.isNullOrBlank()) return@withContext text.trim()

            Log.w(TAG, "Empty response from Firebase AI Logic")
            return@withContext getOfflineVedicResponse(userQuestion)
        } catch (e: Exception) {
            // Most likely causes: Firebase AI Logic not enabled in the console, App
            // Check not registered for this build, or no network. All of them mean
            // the same thing to the user, and the offline answers cover it.
            Log.e(TAG, "Firebase AI Logic call failed: ${e.message}", e)
            return@withContext getOfflineVedicResponse(userQuestion)
        }
    }

    /**
     * Builds a model bound to [systemPrompt], or null if Firebase is not
     * initialised on this device.
     */
    private fun modelFor(systemPrompt: String): GenerativeModel? = try {
        Firebase.ai(backend = GenerativeBackend.googleAI()).generativeModel(
            modelName = MODEL,
            systemInstruction = content { text(systemPrompt) }
        )
    } catch (e: Throwable) {
        Log.e(TAG, "Firebase AI Logic unavailable: ${e.message}", e)
        null
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

    /**
     * Astro news.
     *
     * The old version asked for Google Search grounding by hand-rolling a
     * `"tools": [{"googleSearch": {}}]` block into the raw REST body. Grounding is
     * a paid, separately-enabled feature; rather than reach for it, this asks the
     * model directly and falls back to the bundled highlights when it cannot.
     */
    suspend fun fetchAstroNewsWithSearchGrounding(): String = withContext(Dispatchers.IO) {
        try {
            val systemPrompt = """
                You are an astro-news curator writing for an Indian Vedic astrology app.
                Give exactly three short highlights in Hindi about notable planetary
                transits (गोचर), eclipses (ग्रहण) and astronomical events for the current
                period. Each highlight gets a one-line heading and two or three sentences.

                Be careful with facts. If you are not confident about a specific date,
                describe the event without pinning a date to it rather than inventing one.
                Do not predict outcomes for individuals. Plain text only, no Markdown.
            """.trimIndent()

            val model = modelFor(systemPrompt) ?: return@withContext getOfflineAstroNews()

            val text = model.generateContent(
                "Summarise the current notable Vedic planetary transits and astronomical events."
            ).text

            if (!text.isNullOrBlank()) return@withContext text.trim()
            return@withContext getOfflineAstroNews()
        } catch (e: Exception) {
            Log.e(TAG, "Astro news via Firebase AI Logic failed: ${e.message}", e)
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
