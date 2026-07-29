package com.example.astro

import com.example.data.model.RashifalData
import java.util.Date

object RashifalProvider {

    // House-theme templates keyed by house number (1-12). {planet} is replaced with the transiting planet's Hindi/English name.
    private val GENERAL_HI = listOf(
        "{planet} आज आपकी ही राशि (प्रथम भाव) में गोचर कर रहा है, जिससे आत्मविश्वास और ऊर्जा बढ़ेगी।",
        "{planet} आपके द्वितीय भाव (धन-परिवार) में स्थित है, वित्तीय मामलों पर ध्यान केंद्रित रहेगा।",
        "{planet} आपके तृतीय भाव (साहस-संचार) में है, साहस और संवाद-कौशल प्रबल रहेंगे।",
        "{planet} आपके चतुर्थ भाव (गृह-मातृ) में गोचर कर रहा है, घर-परिवार व संपत्ति संबंधी विषय महत्वपूर्ण रहेंगे।",
        "{planet} आपके पंचम भाव (रचनात्मकता-प्रेम) में स्थित है, रचनात्मक कार्यों में मन लगेगा।",
        "{planet} आपके षष्ठ भाव (स्वास्थ्य-सेवा) में है, दैनिक कार्यों व स्वास्थ्य पर ध्यान देना उचित रहेगा।",
        "{planet} आपके सप्तम भाव (साझेदारी-विवाह) में गोचर कर रहा है, रिश्तों पर प्रभाव पड़ेगा।",
        "{planet} आपके अष्टम भाव (रूपांतरण) में स्थित है, अप्रत्याशित बदलावों के लिए तैयार रहें।",
        "{planet} आपके नवम भाव (भाग्य-यात्रा) में है, भाग्य का साथ मिलेगा।",
        "{planet} आपके दशम भाव (करियर) में गोचर कर रहा है, पेशेवर जीवन में घटनाक्रम संभव हैं।",
        "{planet} आपके एकादश भाव (लाभ-मित्र) में स्थित है, आर्थिक लाभ की संभावना है।",
        "{planet} आपके द्वादश भाव (व्यय-आध्यात्म) में है, आत्मचिंतन का समय है।"
    )
    private val GENERAL_EN = listOf(
        "{planet} transits your own sign (1st house) today, boosting confidence and energy.",
        "{planet} sits in your 2nd house (wealth-family) — financial matters take focus.",
        "{planet} is in your 3rd house (courage-communication) — courage and expression are strong.",
        "{planet} transits your 4th house (home-mother) — home and property matters gain importance.",
        "{planet} sits in your 5th house (creativity-romance) — creative pursuits feel rewarding.",
        "{planet} is in your 6th house (health-service) — daily routine and health need attention.",
        "{planet} transits your 7th house (partnership-marriage) — relationships feel the influence.",
        "{planet} sits in your 8th house (transformation) — stay ready for the unexpected.",
        "{planet} is in your 9th house (fortune-travel) — luck may favor you.",
        "{planet} transits your 10th house (career) — professional developments are likely.",
        "{planet} sits in your 11th house (gains-friends) — financial gains are indicated.",
        "{planet} is in your 12th house (expense-spirituality) — a time for reflection."
    )

    private val CAREER_HI = listOf(
        "व्यक्तिगत पहल से आज कार्यक्षेत्र में सराहना मिल सकती है।",
        "धन-संबंधी कार्यों में आज सावधानी से आगे बढ़ें।",
        "सहकर्मियों से संवाद आज करियर में सहायक रहेगा।",
        "घर से जुड़े कार्य आज ध्यान माँग सकते हैं, कार्यालय में संतुलन बनाए रखें।",
        "रचनात्मक विचार आज पेशेवर जीवन में नई दिशा दे सकते हैं।",
        "कार्यभार आज अधिक रह सकता है, प्राथमिकताएँ तय करें।",
        "व्यावसायिक साझेदारी में आज कोई महत्वपूर्ण चर्चा हो सकती है।",
        "अचानक कार्य-परिवर्तन या जिम्मेदारी का सामना आज हो सकता है।",
        "उच्च अधिकारियों से आज अनुकूल समर्थन मिल सकता है।",
        "करियर में आज कोई महत्वपूर्ण निर्णय या घोषणा संभव है।",
        "टीम व सहयोगियों से आज लाभदायक सहयोग मिलेगा।",
        "आज पर्दे के पीछे का कार्य अधिक फलदायी रहेगा, प्रचार से बचें।"
    )
    private val CAREER_EN = listOf(
        "Personal initiative today could bring recognition at work.",
        "Proceed carefully with money-related work matters today.",
        "Communication with colleagues aids your career today.",
        "Home matters may demand attention — balance with office work.",
        "Creative ideas could redirect your professional path today.",
        "Workload may rise today — set clear priorities.",
        "An important discussion in a business partnership is possible today.",
        "A sudden change in role or responsibility may arise today.",
        "Support from seniors is likely today.",
        "An important career decision or announcement is possible today.",
        "Team and peer cooperation proves beneficial today.",
        "Behind-the-scenes work pays off more than public efforts today."
    )

    private val HEALTH_HI = listOf(
        "आज ऊर्जा स्तर उच्च रहेगा, नई गतिविधियाँ शुरू करने के लिए अच्छा समय है।",
        "भारी भोजन से आज बचें, पाचन पर ध्यान दें।",
        "आज बेचैनी महसूस हो सकती है, गहरी साँस और विश्राम सहायक रहेंगे।",
        "पारिवारिक तनाव का असर स्वास्थ्य पर पड़ सकता है, शांत रहें।",
        "आज मानसिक ऊर्जा अच्छी रहेगी, हल्का व्यायाम लाभकारी रहेगा।",
        "आज स्वास्थ्य पर विशेष ध्यान देने की आवश्यकता है, नियमित जांच कराएँ।",
        "रिश्तों से जुड़ा तनाव आज स्वास्थ्य को प्रभावित कर सकता है।",
        "आज अचानक थकान या ऊर्जा में कमी महसूस हो सकती है, आराम करें।",
        "आज समग्र स्वास्थ्य अच्छा रहेगा, यात्रा में सावधानी बरतें।",
        "कार्यभार के कारण आज तनाव संभव है, ब्रेक लेना न भूलें।",
        "आज ऊर्जा व उत्साह बना रहेगा, सामाजिक गतिविधियाँ लाभकारी रहेंगी।",
        "आज नींद व विश्राम को प्राथमिकता दें, अत्यधिक सोच से बचें।"
    )
    private val HEALTH_EN = listOf(
        "Energy is high today — a good time to start new activities.",
        "Avoid heavy meals today; watch your digestion.",
        "Some restlessness is possible — deep breathing and rest help.",
        "Family tension could affect health — stay calm.",
        "Mental energy is good today; light exercise helps.",
        "Health needs special attention today — consider a check-up.",
        "Relationship-related stress may affect health today.",
        "Sudden fatigue or low energy is possible — rest well.",
        "Overall health stays good today; be cautious while traveling.",
        "Workload may cause stress today — remember to take breaks.",
        "Energy and enthusiasm stay high; social activities help.",
        "Prioritize sleep and rest today; avoid overthinking."
    )

    private val LOVE_HI = listOf(
        "आज आत्म-प्रेम और आत्मविश्वास रिश्तों में सकारात्मकता लाएगा।",
        "परिवार के साथ समय बिताना आज रिश्तों को मज़बूत करेगा।",
        "आज संवाद से किसी गलतफहमी को सुलझाने का अच्छा समय है।",
        "घर पर भावनात्मक जुड़ाव आज गहरा होगा।",
        "आज रोमांस और आत्मीयता के प्रबल योग हैं।",
        "छोटी नाराज़गी आज सेवा-भाव से सुलझ सकती है।",
        "आज जीवनसाथी या साथी के साथ महत्वपूर्ण बातचीत हो सकती है।",
        "रिश्तों में आज अप्रत्याशित मोड़ आ सकता है, धैर्य रखें।",
        "आज साथी के साथ यात्रा या नई योजना बन सकती है।",
        "करियर व्यस्तता का असर आज रिश्तों पर पड़ सकता है, समय निकालें।",
        "मित्रों व सामाजिक दायरे से आज खुशी मिलेगी।",
        "आज अकेले समय बिताना आत्म-चिंतन में सहायक रहेगा।"
    )
    private val LOVE_EN = listOf(
        "Self-love and confidence bring positivity to relationships today.",
        "Time with family strengthens bonds today.",
        "Good day to resolve a misunderstanding through conversation.",
        "Emotional connection at home deepens today.",
        "Strong chances of romance and closeness today.",
        "A small disagreement may resolve through a caring gesture.",
        "An important conversation with your partner is possible today.",
        "Relationships may take an unexpected turn — be patient.",
        "A trip or new plan with your partner may take shape today.",
        "Career busyness may affect relationships — make time.",
        "Friends and social circle bring joy today.",
        "Alone time today aids self-reflection."
    )

    private val FINANCE_HI = listOf(
        "आज आत्मविश्वास के साथ वित्तीय निर्णय लेना लाभकारी रहेगा।",
        "आज धन-लाभ या पारिवारिक संपत्ति संबंधी विषय महत्वपूर्ण रहेंगे।",
        "छोटे निवेश या नई योजना पर आज विचार कर सकते हैं।",
        "संपत्ति या घर से जुड़े वित्तीय मामलों पर आज ध्यान दें।",
        "रचनात्मक परियोजनाओं से आज अतिरिक्त आय संभव है।",
        "आज अनावश्यक खर्चों पर नियंत्रण आवश्यक रहेगा।",
        "साझेदारी में वित्तीय निर्णय आज सोच-समझकर लें।",
        "आज अचानक व्यय की संभावना है, बजट पर ध्यान दें।",
        "आज दीर्घकालिक निवेश के लिए शुभ समय है।",
        "करियर से जुड़ा वित्तीय लाभ आज संभव है।",
        "आज धन-लाभ व मित्रों से सहयोग के योग हैं।",
        "आज व्यय पर विशेष नियंत्रण रखें, अनावश्यक जोखिम से बचें।"
    )
    private val FINANCE_EN = listOf(
        "Financial decisions made with confidence pay off today.",
        "Monetary gain or family property matters are significant today.",
        "Consider a small investment or new financial plan today.",
        "Pay attention to property or home-related finances today.",
        "Creative projects may bring extra income today.",
        "Control unnecessary expenses today.",
        "Decide financial matters in partnerships thoughtfully today.",
        "Sudden expenses are possible — watch your budget.",
        "A favorable day for long-term investment.",
        "Career-related financial gain is possible today.",
        "Chances of monetary gain and support from friends today.",
        "Keep tight control on spending; avoid unnecessary risk today."
    )

    fun getHoroscope(period: String): List<RashifalData> {
        val today = Date()
        return listOf(
            generateRashiData(1, "Aries", "मेष (Aries)", "♈", "अग्नि (Fire)", "मंगल (Mars)", period, today),
            generateRashiData(2, "Taurus", "वृषभ (Taurus)", "♉", "पृथ्वी (Earth)", "शुक्र (Venus)", period, today),
            generateRashiData(3, "Gemini", "मिथुन (Gemini)", "♊", "वायु (Air)", "बुध (Mercury)", period, today),
            generateRashiData(4, "Cancer", "कर्क (Cancer)", "♋", "जल (Water)", "चन्द्र (Moon)", period, today),
            generateRashiData(5, "Leo", "सिंह (Leo)", "♌", "अग्नि (Fire)", "सूर्य (Sun)", period, today),
            generateRashiData(6, "Virgo", "कन्या (Virgo)", "♍", "पृथ्वी (Earth)", "बुध (Mercury)", period, today),
            generateRashiData(7, "Libra", "तुला (Libra)", "♎", "वायु (Air)", "शुक्र (Venus)", period, today),
            generateRashiData(8, "Scorpio", "वृश्चिक (Scorpio)", "♏", "जल (Water)", "मंगल (Mars)", period, today),
            generateRashiData(9, "Sagittarius", "धनु (Sagittarius)", "♐", "अग्नि (Fire)", "गुरु (Jupiter)", period, today),
            generateRashiData(10, "Capricorn", "मकर (Capricorn)", "♑", "पृथ्वी (Earth)", "शनि (Saturn)", period, today),
            generateRashiData(11, "Aquarius", "कुंभ (Aquarius)", "♒", "वायु (Air)", "शनि (Saturn)", period, today),
            generateRashiData(12, "Pisces", "मीन (Pisces)", "♓", "जल (Water)", "गुरु (Jupiter)", period, today)
        )
    }

    /** Backward-compatible default (Today) — used by MainViewModel's initial state. */
    fun getDailyHoroscope(): List<RashifalData> = getHoroscope("TODAY")

    private fun generateRashiData(
        id: Int, en: String, hi: String, sym: String, elem: String, ruler: String, period: String, today: Date
    ): RashifalData {
        val stones = listOf("नीलम (Blue Sapphire)", "पुखराज (Yellow Sapphire)", "पन्ना (Emerald)", "मूंगा (Red Coral)", "मोती (Pearl)", "माणिक्य (Ruby)", "हीरा (Diamond)")
        val colorsHi = listOf("लाल व गुलाबी", "सफेद व सिल्वर", "हरा व फिरोजी", "पीला व केसरिया", "नीला व काला")
        val colorsEn = listOf("Red & Pink", "White & Silver", "Green & Turquoise", "Yellow & Saffron", "Blue & Black")

        val rashiIdx = id - 1 // 0-based
        val driverPlanet = TransitCalculator.driverPlanetForPeriod(period)
        val house = TransitCalculator.getDriverHouse(rashiIdx, period, today) // 1-12, real calculated
        val planetHi = TransitCalculator.planetNameHi(driverPlanet)
        val houseIdx = house - 1

        // Deterministic (not random) picks based on house + rashi, so lucky attributes are still varied per rashi
        val colorIdx = (rashiIdx + house) % colorsHi.size
        val stoneIdx = (rashiIdx * 3 + house) % stones.size
        val rating = 3 + ((rashiIdx + house) % 3) // 3..5
        val luckyNum = 1 + ((rashiIdx * 7 + house * 3) % 9)

        return RashifalData(
            rashiId = id,
            rashiNameEn = en,
            rashiNameHi = hi,
            symbol = sym,
            elementHi = elem,
            rulerHi = ruler,
            ratingStars = rating,
            luckyNumber = luckyNum,
            luckyColorEn = colorsEn[colorIdx],
            luckyColorHi = colorsHi[colorIdx],
            luckyStoneHi = stones[stoneIdx],
            generalReadingHi = GENERAL_HI[houseIdx].replace("{planet}", planetHi),
            generalReadingEn = GENERAL_EN[houseIdx].replace("{planet}", driverPlanet),
            careerReadingHi = CAREER_HI[houseIdx],
            careerReadingEn = CAREER_EN[houseIdx],
            healthReadingHi = HEALTH_HI[houseIdx],
            healthReadingEn = HEALTH_EN[houseIdx],
            loveReadingHi = LOVE_HI[houseIdx],
            loveReadingEn = LOVE_EN[houseIdx],
            financeReadingHi = FINANCE_HI[houseIdx],
            financeReadingEn = FINANCE_EN[houseIdx],
            period = period
        )
    }
}
