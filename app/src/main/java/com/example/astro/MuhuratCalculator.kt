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
                    MuhuratItem("m1", "विवाह मुहूर्त (Wedding)", "Wedding Muhurat", dateStr, panchang.dayOfWeekHindi, panchang.sunrise, panchang.sunset, panchang.tithiHindi, panchang.nakshatraHindi, "अति शुभ (Best)", "रोहिणी/उत्तराफाल्गुनी नक्षत्र में उत्तम विवाह लगन।")
                )
                foundWedding = true
            }

            if (!foundGriha && isAuspiciousTithi && (panchang.nakshatraHindi == "रोहिणी" || panchang.nakshatraHindi == "पुष्य" || panchang.nakshatraHindi == "चित्रा" || panchang.nakshatraHindi == "स्वाती")) {
                muhurats.add(
                    MuhuratItem("m2", "गृह प्रवेश (Griha Pravesh)", "Housewarming", dateStr, panchang.dayOfWeekHindi, panchang.sunrise, panchang.sunset, panchang.tithiHindi, panchang.nakshatraHindi, "शुभ (Good)", "पुष्य/रोहिणी नक्षत्र में नया गृह प्रवेश फलदायी।")
                )
                foundGriha = true
            }

            if (!foundBusiness && isAuspiciousTithi && (panchang.nakshatraHindi == "अश्विनी" || panchang.nakshatraHindi == "हस्त" || panchang.nakshatraHindi == "पुष्य" || panchang.nakshatraHindi == "श्रवण")) {
                muhurats.add(
                    MuhuratItem("m3", "व्यापार शुभारम्भ (Business Launch)", "Business Launch", dateStr, panchang.dayOfWeekHindi, panchang.sunrise, panchang.sunset, panchang.tithiHindi, panchang.nakshatraHindi, "अति शुभ (Best)", "हस्त/पुष्य नक्षत्र में व्यापार की शुरुआत।")
                )
                foundBusiness = true
            }

            if (!foundVehicle && (panchang.nakshatraHindi == "श्रवण" || panchang.nakshatraHindi == "धनिष्ठा" || panchang.nakshatraHindi == "शतभिषा" || panchang.nakshatraHindi == "चित्रा" || panchang.nakshatraHindi == "अश्विनी")) {
                muhurats.add(
                    MuhuratItem("m4", "वाहन खरीद (Vehicle Purchase)", "Vehicle Purchase", dateStr, panchang.dayOfWeekHindi, panchang.sunrise, panchang.sunset, panchang.tithiHindi, panchang.nakshatraHindi, "शुभ (Good)", "श्रवण/चित्रा नक्षत्र में नया वाहन क्रय मुहूर्त।")
                )
                foundVehicle = true
            }

            if (!foundTravel && (panchang.nakshatraHindi == "अश्विनी" || panchang.nakshatraHindi == "पुष्य" || panchang.nakshatraHindi == "स्वाती" || panchang.nakshatraHindi == "अनुराधा" || panchang.nakshatraHindi == "रोहिणी")) {
                muhurats.add(
                    MuhuratItem("m5", "शुभ यात्रा (Auspicious Travel)", "Travel Muhurat", dateStr, panchang.dayOfWeekHindi, panchang.sunrise, panchang.sunset, panchang.tithiHindi, panchang.nakshatraHindi, "शुभ (Good)", "शुभ नक्षत्र में यात्रा हेतु उत्तम समय।")
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
                MuhuratItem("m1", "शुभ कार्य मुहूर्त (Auspicious Work)", "General Muhurat", dateStr, panchang.dayOfWeekHindi, panchang.sunrise, panchang.sunset, panchang.tithiHindi, panchang.nakshatraHindi, "शुभ (Good)", "सर्वार्थ सिद्धि व अभिजित मुहूर्त योग।")
            )
        }
        return muhurats
    }
}
