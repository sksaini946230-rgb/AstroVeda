package com.example.astro

import com.example.data.model.MuhuratItem
import com.example.data.model.CityLocation
import java.util.Calendar
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale

object MuhuratCalculator {

    fun getUpcomingMuhurats(): List<MuhuratItem> {
        val muhurats = mutableListOf<MuhuratItem>()
        val cal = Calendar.getInstance()
        val sdf = SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH)
        val defaultCity = CityLocation("Jaipur", "जयपुर", "Rajasthan", 26.9124, 75.7873)

        // Find upcoming good days by scanning next 60 days
        var foundWedding = false
        var foundGriha = false
        var foundBusiness = false
        var foundVehicle = false
        var foundTravel = false

        for (i in 1..60) {
            cal.add(Calendar.DAY_OF_YEAR, 1)
            val dateStr = sdf.format(cal.time)
            val panchang = PanchangCalculator.calculatePanchang(cal.time, defaultCity)

            // Rules for Muhurat selection based on Panchang data
            val isAuspiciousTithi = !panchang.tithiHindi.contains("अष्टमी") && !panchang.tithiHindi.contains("नवमी") && !panchang.tithiHindi.contains("चतुर्दशी") && !panchang.tithiHindi.contains("अमावस्या")
            
            if (!foundWedding && isAuspiciousTithi && (panchang.nakshatraHindi == "रोहिणी" || panchang.nakshatraHindi == "उत्तराफाल्गुनी" || panchang.nakshatraHindi == "स्वाती" || panchang.nakshatraHindi == "अनुराधा" || panchang.nakshatraHindi == "पुष्य")) {
                muhurats.add(
                    MuhuratItem(
                        id = "m1",
                        categoryHi = "विवाह मुहूर्त", categoryEn = "Wedding Muhurat",
                        dateString = dateStr,
                        dayOfWeekHi = panchang.dayOfWeekHindi, dayOfWeekEn = panchang.dayOfWeek,
                        startTime = panchang.sunrise, endTime = panchang.sunset,
                        tithiHi = panchang.tithiHindi, tithiEn = panchang.tithi,
                        nakshatraHi = panchang.nakshatraHindi, nakshatraEn = panchang.nakshatra,
                        qualityHi = "अति शुभ", qualityEn = "Highly auspicious",
                        descriptionHi = "रोहिणी/उत्तराफाल्गुनी नक्षत्र में उत्तम विवाह लगन।",
                        descriptionEn = "A strong wedding window under Rohini or Uttara Phalguni Nakshatra."
                    )
                )
                foundWedding = true
            }

            if (!foundGriha && isAuspiciousTithi && (panchang.nakshatraHindi == "रोहिणी" || panchang.nakshatraHindi == "पुष्य" || panchang.nakshatraHindi == "चित्रा" || panchang.nakshatraHindi == "स्वाती")) {
                muhurats.add(
                    MuhuratItem(
                        id = "m2",
                        categoryHi = "गृह प्रवेश", categoryEn = "Housewarming",
                        dateString = dateStr,
                        dayOfWeekHi = panchang.dayOfWeekHindi, dayOfWeekEn = panchang.dayOfWeek,
                        startTime = panchang.sunrise, endTime = panchang.sunset,
                        tithiHi = panchang.tithiHindi, tithiEn = panchang.tithi,
                        nakshatraHi = panchang.nakshatraHindi, nakshatraEn = panchang.nakshatra,
                        qualityHi = "शुभ", qualityEn = "Auspicious",
                        descriptionHi = "पुष्य/रोहिणी नक्षत्र में नया गृह प्रवेश फलदायी।",
                        descriptionEn = "Entering a new home under Pushya or Rohini Nakshatra is considered fruitful."
                    )
                )
                foundGriha = true
            }

            if (!foundBusiness && isAuspiciousTithi && (panchang.nakshatraHindi == "अश्विनी" || panchang.nakshatraHindi == "हस्त" || panchang.nakshatraHindi == "पुष्य" || panchang.nakshatraHindi == "श्रवण")) {
                muhurats.add(
                    MuhuratItem(
                        id = "m3",
                        categoryHi = "व्यापार शुभारम्भ", categoryEn = "Business Launch",
                        dateString = dateStr,
                        dayOfWeekHi = panchang.dayOfWeekHindi, dayOfWeekEn = panchang.dayOfWeek,
                        startTime = panchang.sunrise, endTime = panchang.sunset,
                        tithiHi = panchang.tithiHindi, tithiEn = panchang.tithi,
                        nakshatraHi = panchang.nakshatraHindi, nakshatraEn = panchang.nakshatra,
                        qualityHi = "अति शुभ", qualityEn = "Highly auspicious",
                        descriptionHi = "हस्त/पुष्य नक्षत्र में व्यापार की शुरुआत।",
                        descriptionEn = "Beginning a venture under Hasta or Pushya Nakshatra."
                    )
                )
                foundBusiness = true
            }

            if (!foundVehicle && (panchang.nakshatraHindi == "श्रवण" || panchang.nakshatraHindi == "धनिष्ठा" || panchang.nakshatraHindi == "शतभिषा" || panchang.nakshatraHindi == "चित्रा" || panchang.nakshatraHindi == "अश्विनी")) {
                muhurats.add(
                    MuhuratItem(
                        id = "m4",
                        categoryHi = "वाहन खरीद", categoryEn = "Vehicle Purchase",
                        dateString = dateStr,
                        dayOfWeekHi = panchang.dayOfWeekHindi, dayOfWeekEn = panchang.dayOfWeek,
                        startTime = panchang.sunrise, endTime = panchang.sunset,
                        tithiHi = panchang.tithiHindi, tithiEn = panchang.tithi,
                        nakshatraHi = panchang.nakshatraHindi, nakshatraEn = panchang.nakshatra,
                        qualityHi = "शुभ", qualityEn = "Auspicious",
                        descriptionHi = "श्रवण/चित्रा नक्षत्र में नया वाहन क्रय मुहूर्त।",
                        descriptionEn = "A favourable window to buy a vehicle, under Shravana or Chitra Nakshatra."
                    )
                )
                foundVehicle = true
            }

            if (!foundTravel && (panchang.nakshatraHindi == "अश्विनी" || panchang.nakshatraHindi == "पुष्य" || panchang.nakshatraHindi == "स्वाती" || panchang.nakshatraHindi == "अनुराधा" || panchang.nakshatraHindi == "रोहिणी")) {
                muhurats.add(
                    MuhuratItem(
                        id = "m5",
                        categoryHi = "शुभ यात्रा", categoryEn = "Travel Muhurat",
                        dateString = dateStr,
                        dayOfWeekHi = panchang.dayOfWeekHindi, dayOfWeekEn = panchang.dayOfWeek,
                        startTime = panchang.sunrise, endTime = panchang.sunset,
                        tithiHi = panchang.tithiHindi, tithiEn = panchang.tithi,
                        nakshatraHi = panchang.nakshatraHindi, nakshatraEn = panchang.nakshatra,
                        qualityHi = "शुभ", qualityEn = "Auspicious",
                        descriptionHi = "शुभ नक्षत्र में यात्रा हेतु उत्तम समय।",
                        descriptionEn = "A good time to set out, under a favourable Nakshatra."
                    )
                )
                foundTravel = true
            }
        }

        // Fallback: If no strict match found, provide general auspicious dates
        if (muhurats.isEmpty()) {
            val fallbackCal = Calendar.getInstance()
            fallbackCal.add(Calendar.DAY_OF_YEAR, 2)
            val dateStr = sdf.format(fallbackCal.time)
            val panchang = PanchangCalculator.calculatePanchang(fallbackCal.time, defaultCity)
            muhurats.add(
                MuhuratItem(
                    id = "m1",
                    categoryHi = "शुभ कार्य मुहूर्त", categoryEn = "General Muhurat",
                    dateString = dateStr,
                    dayOfWeekHi = panchang.dayOfWeekHindi, dayOfWeekEn = panchang.dayOfWeek,
                    startTime = panchang.sunrise, endTime = panchang.sunset,
                    tithiHi = panchang.tithiHindi, tithiEn = panchang.tithi,
                    nakshatraHi = panchang.nakshatraHindi, nakshatraEn = panchang.nakshatra,
                    qualityHi = "शुभ", qualityEn = "Auspicious",
                    descriptionHi = "सर्वार्थ सिद्धि व अभिजित मुहूर्त योग।",
                    descriptionEn = "Sarvartha Siddhi and the Abhijit Muhurta window."
                )
            )
        }
        return muhurats
    }
}
