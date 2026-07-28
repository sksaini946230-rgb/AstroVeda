package com.example.data.model

data class FestivalData(
    val id: String,
    val nameEn: String,
    val nameHi: String,
    val dateString: String,
    val dateIso: String,
    val dayNameHi: String,
    val monthNameHi: String,
    val pakshaHi: String,
    val tithiHi: String,
    val isMajor: Boolean = true,
    val regionFilter: String = "ALL", // "ALL", "NORTH", "RAJASTHAN", "SOUTH"
    val significanceEn: String,
    val significanceHi: String,
    val pujaVidhiHi: String,
    val regionalHistoryHi: String = "वैदिक ग्रंथों एवं प्रांतीय परंपराओं के अनुसार इस पर्व का विशेष ऐतिहासिक व आध्यात्मिक महत्व है।"
)
