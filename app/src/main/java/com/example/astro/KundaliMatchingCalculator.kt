package com.example.astro

import com.example.data.model.GunaKootDetail
import com.example.data.model.GunaMatchingResult

object KundaliMatchingCalculator {

    fun matchKundali(
        boyName: String,
        boyDob: String,
        boyTob: String,
        girlName: String,
        girlDob: String,
        girlTob: String
    ): GunaMatchingResult {
        // Generate actual charts to get planetary positions
        val boyChart = KundaliCalculator.generateKundali(boyName, boyDob, boyTob, "Default")
        val girlChart = KundaliCalculator.generateKundali(girlName, girlDob, girlTob, "Default")

        val boyMoonRashiIdx = boyChart.moonRashiHi.let { KundaliCalculator.RASHI_SHORT_HI.indexOf(it) }.coerceAtLeast(0)
        val girlMoonRashiIdx = girlChart.moonRashiHi.let { KundaliCalculator.RASHI_SHORT_HI.indexOf(it) }.coerceAtLeast(0)
        
        val boyNakshatraIdx = boyChart.moonNakshatraHi.let { KundaliCalculator.NAKSHATRAS.indexOf(it) }.coerceAtLeast(0)
        val girlNakshatraIdx = girlChart.moonNakshatraHi.let { KundaliCalculator.NAKSHATRAS.indexOf(it) }.coerceAtLeast(0)

        // 1. Varna (1 pt)
        val varnaPoints = calculateVarna(boyMoonRashiIdx, girlMoonRashiIdx)
        // 2. Vashya (2 pts)
        val vashyaPoints = calculateVashya(boyMoonRashiIdx, girlMoonRashiIdx)
        // 3. Tara (3 pts)
        val taraPoints = calculateTara(boyNakshatraIdx, girlNakshatraIdx)
        // 4. Yoni (4 pts)
        val yoniPoints = calculateYoni(boyNakshatraIdx, girlNakshatraIdx)
        // 5. Graha Maitri (5 pts)
        val grahaMaitriPoints = calculateGrahaMaitri(boyMoonRashiIdx, girlMoonRashiIdx)
        // 6. Gana (6 pts)
        val ganaPoints = calculateGana(boyNakshatraIdx, girlNakshatraIdx)
        // 7. Bhakoot (7 pts)
        val bhakootPoints = calculateBhakoot(boyMoonRashiIdx, girlMoonRashiIdx)
        // 8. Nadi (8 pts)
        val nadiPoints = calculateNadi(boyNakshatraIdx, girlNakshatraIdx)

        val totalGuna = varnaPoints + vashyaPoints + taraPoints + yoniPoints + grahaMaitriPoints + ganaPoints + bhakootPoints + nadiPoints

        // Manglik Dosha
        val boyMarsHouse = boyChart.planets.find { it.planetNameEn == "Mars" }?.houseNumber ?: 0
        val girlMarsHouse = girlChart.planets.find { it.planetNameEn == "Mars" }?.houseNumber ?: 0
        val manglikHouses = listOf(1, 4, 7, 8, 12)
        val isBoyManglik = boyMarsHouse in manglikHouses
        val isGirlManglik = girlMarsHouse in manglikHouses

        val mangalStatus = when {
            isBoyManglik && isGirlManglik -> "दोनों मांगलिक हैं (मंगल दोष निरस्त/मांगलिक सामंजस्य)"
            isBoyManglik -> "वर मांगलिक हैं, कन्या मांगलिक नहीं हैं"
            isGirlManglik -> "कन्या मांगलिक हैं, वर मांगलिक नहीं हैं"
            else -> "दोनों अंश-मांगलिक नहीं हैं (कोई मंगल दोष नहीं)"
        }

        val mangalStatusEn = when {
            isBoyManglik && isGirlManglik -> "Both are Manglik (Mangal Dosha canceled / Manglik compatibility)"
            isBoyManglik -> "Boy is Manglik, Girl is not Manglik"
            isGirlManglik -> "Girl is Manglik, Boy is not Manglik"
            else -> "Both are non-Manglik (No Mangal Dosha)"
        }

        val kootDetails = listOf(
            GunaKootDetail("वर्ण (Varna)", "Varna", 1.0, varnaPoints, "आध्यात्मिक एवं मानसिक दृष्टिकोण का मिलान।"),
            GunaKootDetail("वश्य (Vashya)", "Vashya", 2.0, vashyaPoints, "पारस्परिक आकर्षण एवं अधिकार क्षेत्र।"),
            GunaKootDetail("तारा (Tara)", "Tara", 3.0, taraPoints, "भाग्य, दीर्घायु एवं स्वास्थ्य अनुकूलता।"),
            GunaKootDetail("योनि (Yoni)", "Yoni", 4.0, yoniPoints, "शारीरिक एवं दाम्पत्य सामंजस्य।"),
            GunaKootDetail("ग्रह मैत्री (Graha Maitri)", "Graha Maitri", 5.0, grahaMaitriPoints, "मानसिक विचार एवं बौद्धिक मित्रता।"),
            GunaKootDetail("गण (Gana)", "Gana", 6.0, ganaPoints, "स्वभाव, व्यवहार एवं चरित्र सामंजस्य।"),
            GunaKootDetail("भकूट (Bhakoot)", "Bhakoot", 7.0, bhakootPoints, "पारिवारिक समृद्धि एवं वंश वृद्धि।"),
            GunaKootDetail("नाडी (Nadi)", "Nadi", 8.0, nadiPoints, "आनुवंशिक स्वास्थ्य एवं संतान सुख।")
        )

        val verdictHi: String
        val verdictEn: String
        val summaryHi: String
        val summaryEn: String

        when {
            totalGuna >= 28.0 -> {
                verdictHi = "अति उत्तम मिलान (Excellent Match)"
                verdictEn = "Excellent Match"
                summaryHi = "$boyName एवं $girlName की कुंडली में $totalGuna / 36 गुण प्राप्त हुए हैं। यह विवाह अत्यंत शुभ एवं सुखद वैवाहिक जीवन का संकेत देता है।"
                summaryEn = "Guna score of $totalGuna / 36 obtained between $boyName and $girlName. This indicates an exceptionally auspicious and happy married life."
            }
            totalGuna >= 18.0 -> {
                verdictHi = "शुभ एवं अनुकूल मिलान (Good Match)"
                verdictEn = "Good Match"
                summaryHi = "$boyName एवं $girlName की कुंडली में $totalGuna / 36 गुण मिल रहे हैं। सामान्य पूजा-अनुष्ठान के उपरांत विवाह सम्पन्न किया जा सकता है।"
                summaryEn = "$totalGuna / 36 gunas are matching. The wedding can be safely performed after simple standard rituals."
            }
            else -> {
                verdictHi = "औसत मिलान (Average Match - Remedy Required)"
                verdictEn = "Average Match"
                summaryHi = "$boyName एवं $girlName की कुंडली में $totalGuna / 36 गुण प्राप्त हुए हैं। विवाह पूर्व नाडी अथवा भकूट दोष निवारण उपाय परामर्श योग्य हैं।"
                summaryEn = "Only $totalGuna / 36 gunas match. Prior remedies/prayers for Nadi or Bhakoot Dosha are strongly recommended before marriage."
            }
        }

        return GunaMatchingResult(
            boyName = boyName,
            girlName = girlName,
            totalObtainedGuna = totalGuna,
            maxGuna = 36.0,
            isManglikBoy = isBoyManglik,
            isManglikGirl = isGirlManglik,
            mangalDoshaStatusHi = mangalStatus,
            mangalDoshaStatusEn = mangalStatusEn,
            kootDetails = kootDetails,
            compatibilityVerdictHi = verdictHi,
            compatibilityVerdictEn = verdictEn,
            summaryReadingHi = summaryHi,
            summaryReadingEn = summaryEn
        )
    }

    private fun calculateVarna(b: Int, g: Int): Double {
        val v = listOf(3, 2, 1, 0, 3, 2, 1, 0, 3, 2, 1, 0)
        return if (v[b] >= v[g]) 1.0 else 0.0
    }
    private fun calculateVashya(b: Int, g: Int): Double = if (b == g) 2.0 else 1.0
    private fun calculateTara(b: Int, g: Int): Double {
        val tbg = (g - b + 27) % 27
        val tgb = (b - g + 27) % 27
        val r1 = (tbg % 9)
        val r2 = (tgb % 9)
        val score1 = if (r1 in listOf(3, 5, 7)) 0.0 else 1.5
        val score2 = if (r2 in listOf(3, 5, 7)) 0.0 else 1.5
        return score1 + score2
    }
    private fun calculateYoni(b: Int, g: Int): Double {
        val yoniMap = listOf(
            0, 1, 2, 3, 3, 4, 5, 2, 5, 6, 6, 7, 8, 9, 8, 9, 10, 10, 4, 11, 12, 11, 13, 0, 13, 7, 1
        ) // 0:Horse, 1:Elephant, 2:Sheep, 3:Serpent, 4:Dog, 5:Cat, 6:Rat, 7:Cow, 8:Buffalo, 9:Tiger, 10:Deer, 11:Monkey, 12:Mongoose, 13:Lion
        
        val bYoni = yoniMap[b]
        val gYoni = yoniMap[g]
        
        if (bYoni == gYoni) return 4.0
        
        val swornEnemies = setOf(
            Pair(0, 8), Pair(8, 0), // Horse-Buffalo
            Pair(1, 13), Pair(13, 1), // Elephant-Lion
            Pair(2, 11), Pair(11, 2), // Sheep-Monkey
            Pair(3, 12), Pair(12, 3), // Serpent-Mongoose
            Pair(4, 10), Pair(10, 4), // Dog-Deer
            Pair(5, 6), Pair(6, 5), // Cat-Rat
            Pair(7, 9), Pair(9, 7)  // Cow-Tiger
        )
        
        if (swornEnemies.contains(Pair(bYoni, gYoni))) return 0.0
        return 2.0 // Simplified: 2.0 for neutral/friendly
    }

    private fun calculateGrahaMaitri(b: Int, g: Int): Double {
        val lords = listOf(2, 5, 3, 1, 0, 3, 5, 2, 4, 6, 6, 4) // 0:Sun, 1:Moon, 2:Mars, 3:Mercury, 4:Jupiter, 5:Venus, 6:Saturn
        val bLord = lords[b]
        val gLord = lords[g]
        
        if (bLord == gLord) return 5.0
        
        fun getRelation(p1: Int, p2: Int): Int { // 2:Friend, 1:Neutral, 0:Enemy
            return when (p1) {
                0 -> if (p2 in listOf(1, 2, 4)) 2 else if (p2 in listOf(5, 6)) 0 else 1
                1 -> if (p2 in listOf(0, 3)) 2 else 1
                2 -> if (p2 in listOf(0, 1, 4)) 2 else if (p2 == 3) 0 else 1
                3 -> if (p2 in listOf(0, 5)) 2 else if (p2 == 1) 0 else 1
                4 -> if (p2 in listOf(0, 1, 2)) 2 else if (p2 in listOf(3, 5)) 0 else 1
                5 -> if (p2 in listOf(3, 6)) 2 else if (p2 in listOf(0, 1)) 0 else 1
                6 -> if (p2 in listOf(3, 5)) 2 else if (p2 in listOf(0, 1, 2)) 0 else 1
                else -> 1
            }
        }
        
        val r1 = getRelation(bLord, gLord)
        val r2 = getRelation(gLord, bLord)
        val sum = r1 + r2
        return when (sum) {
            4 -> 5.0 // Friend-Friend
            3 -> 4.0 // Friend-Neutral
            2 -> if (r1 == 1 && r2 == 1) 3.0 else 1.0 // Neutral-Neutral or Friend-Enemy
            1 -> 0.5 // Neutral-Enemy
            else -> 0.0 // Enemy-Enemy
        }
    }

    private fun calculateGana(b: Int, g: Int): Double {
        val ganaMap = listOf(
            0, 1, 2, 1, 0, 1, 0, 0, 2, 2, 1, 1, 0, 2, 0, 2, 0, 2, 2, 1, 1, 0, 2, 2, 1, 1, 0
        ) // 0:Deva, 1:Manushya, 2:Rakshasa
        val bGana = ganaMap[b]
        val gGana = ganaMap[g]
        
        if (bGana == gGana) return 6.0
        if (bGana == 0 && gGana == 1) return 5.0
        if (bGana == 1 && gGana == 0) return 6.0
        if (bGana == 2 && gGana == 0) return 1.0
        if (bGana == 0 && gGana == 2) return 0.0
        if (bGana == 1 && gGana == 2) return 0.0
        if (bGana == 2 && gGana == 1) return 0.0
        return 0.0
    }

    private fun calculateBhakoot(b: Int, g: Int): Double {
        val dist = (g - b + 12) % 12
        return if (dist in listOf(1, 5, 7)) 0.0 else 7.0
    }

    private fun calculateNadi(b: Int, g: Int): Double {
        val nadiMap = listOf(
            0, 1, 2, 2, 1, 0, 0, 1, 2, 2, 1, 0, 0, 1, 2, 2, 1, 0, 0, 1, 2, 2, 1, 0, 0, 1, 2
        ) // 0:Adi, 1:Madhya, 2:Antya
        val bNadi = nadiMap[b]
        val gNadi = nadiMap[g]
        return if (bNadi != gNadi) 8.0 else 0.0
    }
}
