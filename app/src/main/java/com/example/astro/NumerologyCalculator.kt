package com.example.astro

import com.example.data.model.NumerologyData

object NumerologyCalculator {

    private val PLANETS_MAP = mapOf(
        1 to "सूर्य (Sun)", 2 to "चन्द्र (Moon)", 3 to "गुरु (Jupiter)",
        4 to "राहु (Rahu)", 5 to "बुध (Mercury)", 6 to "शुक्र (Venus)",
        7 to "केतु (Ketu)", 8 to "शनि (Saturn)", 9 to "मंगल (Mars)"
    )

    private val READINGS = mapOf(
        1 to Pair("मूलांक 1 वाले व्यक्ति साहसी, नेतृत्व क्षमता से परिपूर्ण एवं स्वाभिमानी होते हैं। सूर्य देव की कृपा से प्रशासनिक एवं प्रबंधन कार्यों में उच्च सफलता मिलती है।", "भाग्यशाली दिन: रविवार एवं सोमवार। शुभ रंग: पीला एवं केसरिया।"),
        2 to Pair("मूलांक 2 वाले व्यक्ति कल्पनाशील, कलात्मक एवं भावुक होते हैं। चन्द्र देव की कृपा से रचनात्मक क्षेत्रों, काव्य, संगीत एवं जनसेवा में विशेष सफलता प्राप्त होती है।", "भाग्यशाली दिन: सोमवार एवं रविवार। शुभ रंग: सफेद एवं हल्का हरा।"),
        3 to Pair("मूलांक 3 के स्वामी देवगुरु बृहस्पति हैं। आप ज्ञानी, न्यायप्रिय, आध्यात्मिक एवं शिक्षण कार्य में निपुण होते हैं। समाज में मान-सम्मान प्राप्त होता है।", "भाग्यशाली दिन: गुरुवार एवं शुक्रवार। शुभ रंग: पीला एवं सुनहरा।"),
        4 to Pair("मूलांक 4 के स्वामी राहु हैं। आप व्यावहारिक, विश्लेषणात्मक एवं लीक से हटकर सोचने वाले होते हैं। तकनीकी, अनुसंधान एवं मीडिया क्षेत्र में सफलता मिलती है।", "भाग्यशाली दिन: रविवार एवं शनिवार। शुभ रंग: नीला एवं सलेटी।"),
        5 to Pair("मूलांक 5 के स्वामी बुध देव हैं। आप तीव्र बुद्धि, चतुर वाकपटुता एवं व्यापारिक कुशलता के धनी होते हैं। शेयर बाज़ार, कंसल्टेंसी एवं आईटी में प्रगति करते हैं।", "भाग्यशाली दिन: बुधवार एवं शुक्रवार। शुभ रंग: हरा एवं हल्का पीला।"),
        6 to Pair("मूलांक 6 के स्वामी दैत्यगुरु शुक्र हैं। आप सौंदर्यप्रेमी, कलात्मक, आकर्षक व्यक्तित्व एवं ऐश्वर्यप्रिय होते हैं। फैशन, डिजाइनिंग एवं फिल्म क्षेत्र में ख्याति मिलती है।", "भाग्यशाली दिन: शुक्रवार एवं मंगलवार। शुभ रंग: गुलाबी एवं सफेद।"),
        7 to Pair("मूलांक 7 के स्वामी केतु हैं। आप गूढ़ विद्या, शोध, दर्शनशास्त्र एवं योग-अध्यात्म में गहरा रुझान रखते हैं। स्वतंत्र विचारक एवं दूरदर्शी होते हैं।", "भाग्यशाली दिन: रविवार एवं गुरुवार। शुभ रंग: हल्का पीला एवं सफेद।"),
        8 to Pair("मूलांक 8 के स्वामी न्यायप्रिय शनिदेव हैं। आप कर्मठ, धैर्यवान, संघर्षशील एवं दूरदर्शी होते हैं। जीवन के उत्तरार्ध में विशाल संपत्ति व प्रतिष्ठा अर्जित करते हैं।", "भाग्यशाली दिन: शनिवार एवं शुक्रवार। शुभ रंग: गहरा नीला एवं काला।"),
        9 to Pair("मूलांक 9 के स्वामी पराक्रमी मंगल देव हैं। आप ऊर्जावान, निर्भीक, देशभक्त एवं रक्षक स्वभाव के होते हैं। सेना, पुलिस, खेल एवं रियल एस्टेट में नाम कमाते हैं।", "भाग्यशाली दिन: मंगलवार एवं रविवार। शुभ रंग: लाल एवं नारंगी।")
    )

    fun calculateNumerology(name: String, dobString: String): NumerologyData {
        val digits = dobString.filter { it.isDigit() }

        // Moolank = Single digit sum of birth DAY
        val dayPart = dobString.split("-").getOrNull(2)?.filter { it.isDigit() } ?: "1"
        var moolankSum = dayPart.sumOf { it.toString().toInt() }
        while (moolankSum > 9) {
            moolankSum = moolankSum.toString().sumOf { it.toString().toInt() }
        }
        val moolank = if (moolankSum == 0) 1 else moolankSum

        // Bhagyank = Single digit sum of FULL DOB
        var bhagyankSum = digits.sumOf { it.toString().toInt() }
        while (bhagyankSum > 9) {
            bhagyankSum = bhagyankSum.toString().sumOf { it.toString().toInt() }
        }
        val bhagyank = if (bhagyankSum == 0) 1 else bhagyankSum

        // Name number
        val nameDigits = name.uppercase().map { char ->
            when (char) {
                'A', 'I', 'J', 'Q', 'Y' -> 1
                'B', 'K', 'R' -> 2
                'C', 'G', 'L', 'S' -> 3
                'D', 'M', 'T' -> 4
                'E', 'H', 'N', 'X' -> 5
                'U', 'V', 'W' -> 6
                'O', 'Z' -> 7
                'F', 'P' -> 8
                else -> 0
            }
        }.sum()
        var nameNum = nameDigits
        while (nameNum > 9) {
            nameNum = nameNum.toString().sumOf { it.toString().toInt() }
        }
        if (nameNum == 0) nameNum = 1

        val rulingPlanet = PLANETS_MAP[moolank] ?: "सूर्य"
        val readingPair = READINGS[moolank] ?: READINGS[1]!!

        val friendly = when (moolank) {
            1 -> listOf(1, 2, 3, 5, 9)
            2 -> listOf(1, 2, 3, 5)
            3 -> listOf(1, 2, 3, 9)
            4 -> listOf(1, 5, 6, 7)
            5 -> listOf(1, 5, 6)
            6 -> listOf(5, 6, 8)
            7 -> listOf(1, 4, 5, 7)
            8 -> listOf(5, 6)
            else -> listOf(1, 3, 9)
        }

        val enemy = when (moolank) {
            1 -> listOf(8)
            2 -> listOf(8)
            3 -> listOf(6)
            4 -> listOf(2, 8)
            5 -> listOf(2)
            6 -> listOf(3)
            7 -> listOf(2)
            8 -> listOf(1, 2, 9)
            else -> listOf(8)
        }

        return NumerologyData(
            personName = name,
            dateOfBirth = dobString,
            moolank = moolank,
            bhagyank = bhagyank,
            nameNumber = nameNum,
            rulingPlanetHi = rulingPlanet,
            luckyDaysHi = readingPair.second,
            luckyColorsHi = "पीला, लाल, सफेद एवं सुनहरा",
            friendlyNumbers = friendly,
            enemyNumbers = enemy,
            moolankReadingHi = readingPair.first,
            bhagyankReadingHi = "भाग्यांक $bhagyank आपके जीवन पथ को दर्शाती है। व्यवसाय एवं व्यक्तिगत जीवन में मूलांक $moolank के स्वामी $rulingPlanet की दशा शुभ फल देगी।"
        )
    }
}
