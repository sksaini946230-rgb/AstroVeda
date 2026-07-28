package com.example.astro

import com.example.data.model.RashifalData
import java.util.Calendar
import kotlin.random.Random

object RashifalProvider {

    fun getDailyHoroscope(): List<RashifalData> {
        val cal = Calendar.getInstance()
        val seed = (cal.get(Calendar.YEAR) * 1000 + cal.get(Calendar.DAY_OF_YEAR)).toLong()
        val rnd = Random(seed)

        return listOf(
            generateRashiData(1, "Aries", "मेष (Aries)", "♈", "अग्नि (Fire)", "मंगल (Mars)", rnd),
            generateRashiData(2, "Taurus", "वृषभ (Taurus)", "♉", "पृथ्वी (Earth)", "शुक्र (Venus)", rnd),
            generateRashiData(3, "Gemini", "मिथुन (Gemini)", "♊", "वायु (Air)", "बुध (Mercury)", rnd),
            generateRashiData(4, "Cancer", "कर्क (Cancer)", "♋", "जल (Water)", "चन्द्र (Moon)", rnd),
            generateRashiData(5, "Leo", "सिंह (Leo)", "♌", "अग्नि (Fire)", "सूर्य (Sun)", rnd),
            generateRashiData(6, "Virgo", "कन्या (Virgo)", "♍", "पृथ्वी (Earth)", "बुध (Mercury)", rnd),
            generateRashiData(7, "Libra", "तुला (Libra)", "♎", "वायु (Air)", "शुक्र (Venus)", rnd),
            generateRashiData(8, "Scorpio", "वृश्चिक (Scorpio)", "♏", "जल (Water)", "मंगल (Mars)", rnd),
            generateRashiData(9, "Sagittarius", "धनु (Sagittarius)", "♐", "अग्नि (Fire)", "गुरु (Jupiter)", rnd),
            generateRashiData(10, "Capricorn", "मकर (Capricorn)", "♑", "पृथ्वी (Earth)", "शनि (Saturn)", rnd),
            generateRashiData(11, "Aquarius", "कुंभ (Aquarius)", "♒", "वायु (Air)", "शनि (Saturn)", rnd),
            generateRashiData(12, "Pisces", "मीन (Pisces)", "♓", "जल (Water)", "गुरु (Jupiter)", rnd)
        )
    }

    private fun generateRashiData(
        id: Int, en: String, hi: String, sym: String, elem: String, ruler: String, rnd: Random
    ): RashifalData {
        val stones = listOf("नीलम (Blue Sapphire)", "पुखराज (Yellow Sapphire)", "पन्ना (Emerald)", "मूंगा (Red Coral)", "मोती (Pearl)", "माणिक्य (Ruby)", "हीरा (Diamond)")
        val colorsHi = listOf("लाल व गुलाबी", "सफेद व सिल्वर", "हरा व फिरोजी", "पीला व केसरिया", "नीला व काला")
        val colorsEn = listOf("Red & Pink", "White & Silver", "Green & Turquoise", "Yellow & Saffron", "Blue & Black")
        
        val colorIdx = rnd.nextInt(colorsHi.size)
        val stoneIdx = rnd.nextInt(stones.size)
        
        val genHi = listOf("आज का दिन आपके लिए ऊर्जावान रहेगा।", "आज नए अवसरों की प्राप्ति होगी।", "आज मन में संतोष और शांति रहेगी।", "आज पारिवारिक सहयोग मिलेगा।", "आज रचनात्मक कार्यों में मन लगेगा।")
        val genEn = listOf("An energetic day ahead.", "New opportunities await you.", "Inner peace and satisfaction today.", "Family support brings joy.", "Creative pursuits will be highly rewarding.")
        
        val carHi = listOf("कार्यक्षेत्र में नई जिम्मेदारी मिल सकती है।", "सहकर्मियों के साथ तालमेल अच्छा रहेगा।", "व्यापार में विस्तार की योजना बनेगी।", "नौकरी में पदोन्नति के योग हैं।")
        val carEn = listOf("New responsibilities at work.", "Excellent harmony with colleagues.", "Business expansion plans will succeed.", "Chances of promotion in job.")
        
        val hlthHi = listOf("स्वास्थ्य उत्तम रहेगा।", "थकान महसूस हो सकती है, विश्राम करें।", "खान-पान पर ध्यान दें।", "योग और ध्यान से मानसिक शांति मिलेगी।")
        val hlthEn = listOf("Health remains excellent.", "May feel tired, get some rest.", "Pay attention to your diet.", "Yoga and meditation will bring peace.")
        
        val loveHi = listOf("जीवनसाथी के साथ संबंध मधुर होंगे।", "प्रेमी से उपहार मिल सकता है।", "आज का दिन रोमांस से भरा रहेगा।", "पारिवारिक रिश्तों में घनिष्ठता बढ़ेगी।")
        val loveEn = listOf("Sweet relations with spouse.", "May receive a gift from loved one.", "A day filled with romance.", "Family bonds will grow stronger.")
        
        val finHi = listOf("आर्थिक स्थिति मजबूत होगी।", "अचानक धन लाभ के योग हैं।", "निवेश के लिए अच्छा समय है।", "व्यय पर नियंत्रण रखें।")
        val finEn = listOf("Financial condition will be strong.", "Sudden monetary gains indicated.", "Good time for new investments.", "Control unnecessary expenses.")

        return RashifalData(
            rashiId = id,
            rashiNameEn = en,
            rashiNameHi = hi,
            symbol = sym,
            elementHi = elem,
            rulerHi = ruler,
            ratingStars = rnd.nextInt(3, 6),
            luckyNumber = rnd.nextInt(1, 10),
            luckyColorEn = colorsEn[colorIdx],
            luckyColorHi = colorsHi[colorIdx],
            luckyStoneHi = stones[stoneIdx],
            generalReadingHi = genHi[rnd.nextInt(genHi.size)],
            generalReadingEn = genEn[rnd.nextInt(genEn.size)],
            careerReadingHi = carHi[rnd.nextInt(carHi.size)],
            careerReadingEn = carEn[rnd.nextInt(carEn.size)],
            healthReadingHi = hlthHi[rnd.nextInt(hlthHi.size)],
            healthReadingEn = hlthEn[rnd.nextInt(hlthEn.size)],
            loveReadingHi = loveHi[rnd.nextInt(loveHi.size)],
            loveReadingEn = loveEn[rnd.nextInt(loveEn.size)],
            financeReadingHi = finHi[rnd.nextInt(finHi.size)],
            financeReadingEn = finEn[rnd.nextInt(finEn.size)]
        )
    }
}
