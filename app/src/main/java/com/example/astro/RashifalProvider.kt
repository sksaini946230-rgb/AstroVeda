package com.example.astro

import com.example.data.model.RashifalData
import java.util.Calendar
import java.util.Date

/**
 * On-device Vedic Astrology Horoscope (Rashifal) Provider.
 * Computes deep, structured, context-rich readings for each of the 12 zodiac signs
 * based on planetary transits (Moon for Today/Week, Sun for Month) across the 12 houses.
 * Features 3 rotating template variations per house to ensure non-repetitive, authentic guidance.
 */
object RashifalProvider {

    // =========================================================================
    // 1. GENERAL / OVERVIEW READINGS (12 Houses x 3 Variations)
    // =========================================================================
    private val GENERAL_HI = listOf(
        // House 1 (Tanu Bhava)
        listOf(
            "{planet} {P} आपकी ही राशि (प्रथम भाव) में गोचर कर रहा है, जिससे आपके आत्मविश्वास, आभा और कार्यक्षमता में उल्लेखनीय वृद्धि होगी। अटके हुए महत्वपूर्ण कार्यों को पुनः गति देने के लिए यह समय अत्यंत अनुकूल है। सकारात्मक दृष्टिकोण बनाए रखें और दिन की शुरुआत शांत चित्त व स्पष्ट संकल्प के साथ करें।",
            "लग्न भाव में {planet} की शुभ स्थिति {P} आपको ऊर्जावान, व्यावहारिक और प्रभावशाली बनाए रखेगी। आप अपनी व्यक्तिगत योजनाओं और नई प्राथमिकताओं को क्रियान्वित करने के लिए स्वतः प्रेरित महसूस करेंगे। अपने निर्णय स्वयं लें और अंतरात्मा की आवाज पर पूरा भरोसा रखें।",
            "{planet} का प्रथम भाव में संचरण {P} आपके विचारों में स्पष्टता, बौद्धिक आकर्षण और सकारात्मकता लाएगा। सामाजिक व पारिवारिक स्तर पर लोग आपकी सलाह को सम्मान देंगे। अनावश्यक चिंताओं को त्यागकर अपने प्रमुख लक्ष्यों पर पूरी निष्ठा से केंद्रित रहें।"
        ),
        // House 2 (Dhana & Kutumba)
        listOf(
            "{planet} आपके द्वितीय भाव (धन व कुटुंब) में स्थित है, जिससे पारिवारिक सौहार्द और आर्थिक योजनाएं केंद्र में रहेंगी। आपकी वाणी में सौम्यता और प्रभाव रहेगा, जिससे लोग आपकी बात ध्यान से सुनेंगे। महत्वपूर्ण चर्चाओं में धैर्य बनाए रखें और परिजनों की सलाह का आदर करें।",
            "द्वितीय भाव में {planet} की स्थिति {P} संचित धन की सुरक्षा और परिवार में सामंजस्य स्थापित करने में सहायक होगी। नए आर्थिक अवसरों का विश्लेषण करने के लिए यह अनुकूल समय है। अपने शब्दों का चयन सोच-समझकर करें ताकि किसी के आत्मसम्मान को ठेस न पहुंचे।",
            "{planet} का प्रभाव {P} आपकी वित्तीय प्राथमिकताओं को व्यवस्थित करने और पारिवारिक दायित्वों को निभाने में सहायक रहेगा। किसी पुराने आर्थिक मसले का सकारात्मक समाधान निकल सकता है। घर के वरिष्ठ सदस्यों से मार्गदर्शन लेना बहुत लाभप्रद रहेगा।"
        ),
        // House 3 (Sahaja & Parakrama)
        listOf(
            "{planet} आपके तृतीय भाव (पराक्रम व संचार) में गोचर कर रहा है, जिससे आपका साहस, निर्णय क्षमता और सामाजिक सक्रियता प्रबल रहेगी। छोटी यात्राओं या नए संपर्कों से लाभ के अवसर मिलेंगे। भाई-बहनों और मित्रों का पूरा सहयोग मिलेगा, जिससे आपके प्रयासों को संबल प्राप्त होगा।",
            "तृतीय भाव में {planet} की उपस्थिति {P} आपको नई चुनौतियों का डटकर सामना करने की शक्ति प्रदान करेगी। संचार माध्यमों, लेखन या तकनीकी कार्यों से जुड़े जातकों के लिए समय बहुत रचनात्मक है। अपने विचारों को निडर होकर व्यक्त करें और नए संपर्कों का स्वागत करें।",
            "{planet} का गोचर {P} आपके पराक्रम में वृद्धि करेगा और नए उद्यम शुरू करने की प्रेरणा देगा। पड़ोसियों व सहकर्मियों के साथ संबंध प्रगाढ़ होंगे। अपनी ऊर्जा को सही दिशा में केंद्रित करके बड़े लक्ष्यों को प्राप्त किया जा सकता है।"
        ),
        // House 4 (Sukha & Matru)
        listOf(
            "{planet} आपके चतुर्थ भाव (सुख, मातृ व गृह) में गोचर कर रहा है, जिससे पारिवारिक शांति और सुख-सुविधाओं में वृद्धि होगी। घर की साज-सज्जा या वाहन संबंधी योजनाओं पर ध्यान रहेगा। माताजी का आशीर्वाद और स्नेह आपको मानसिक संबल व आंतरिक संतुष्टि प्रदान करेगा।",
            "चतुर्थ भाव में {planet} की स्थिति {P} आपको आंतरिक शांति और मानसिक संतुलन प्रदान करेगी। घर-परिवार में सामंजस्य बनाए रखने के आपके प्रयास सफल होंगे। किसी अचल संपत्ति से जुड़े मसले पर सकारात्मक प्रगति होगी और मन प्रसन्न रहेगा।",
            "{planet} का प्रभाव {P} आपके घरेलू जीवन को सुखद और आनंदमय बनाएगा। परिवार के साथ बिताया गया समय आपको नई ऊर्जा और भावनात्मक सुरक्षा देगा। किसी नए वाहन या गृह-उपयोगी वस्तु की खरीदारी की योजना बन सकती है।"
        ),
        // House 5 (Putra & Buddhi)
        listOf(
            "{planet} आपके पंचम भाव (विद्या, बुद्धि व रचनात्मकता) में स्थित है, जिससे आपकी बौद्धिक क्षमता और नवाचार चरम पर रहेंगे। कला, साहित्य, शोध या अध्ययन से जुड़े कार्यों में विशेष सफलता मिलेगी। संतान पक्ष से कोई सुखद समाचार मन को प्रफुल्लित कर सकता है।",
            "पंचम भाव में {planet} का गोचर {P} आपकी रचनात्मक प्रतिभा को निखारेगा और नई योजनाओं को जन्म देगा। निर्णय लेने में आपकी दूरदर्शिता और विवेकशीलता की सराहना होगी। विद्यार्थियों और परीक्षार्थियों के लिए यह समय विशेष एकाग्रता और सफलता का रहेगा।",
            "{planet} की शुभ दृष्टि {P} आपके जीवन में उत्साह, आनंद और नए विचारों का प्रवाह लाएगी। बौद्धिक चर्चाओं और ज्ञानवर्धक गतिविधियों में भाग लेने का अवसर मिलेगा। अपने शौक और रचनात्मक रुचियों के लिए भी कुछ समय अवश्य निकालें।"
        ),
        // House 6 (Ari & Roga)
        listOf(
            "{planet} आपके षष्ठ भाव (शत्रु, ऋण व स्वास्थ्य) में गोचर कर रहा है, जिससे आप विरोधियों पर हावी रहेंगे और दैनिक कार्यों में अनुशासन बढ़ेगा। कार्यक्षेत्र में आने वाली बाधाओं को आप अपनी सूझबूझ से दूर कर लेंगे। स्वास्थ्य और खानपान की आदतों में सजगता बरतें।",
            "षष्ठ भाव में {planet} की स्थिति {P} आपकी कर्तव्यनिष्ठा और सेवा-भाव को प्रबल बनाएगी। कानूनी या प्रशासनिक मामलों में आपके पक्ष में परिणाम आने के शुभ संकेत हैं। अपने दैनिक रूटीन को व्यवस्थित रखें और विवादों से दूर रहकर काम पर ध्यान दें।",
            "{planet} का प्रभाव {P} आपको पुरानी समस्याओं और ऋण संबंधी चिंताओं से मुक्ति दिलाने में सहायक होगा। सहकर्मियों के साथ मिलकर किए गए काम में सफलता मिलेगी। अति-परिश्रम से बचें और अपने विश्राम के लिए भी समय निर्धारित करें।"
        ),
        // House 7 (Kalatra & Yuvati)
        listOf(
            "{planet} आपके सप्तम भाव (साझेदारी, विवाह व जनसंपर्क) में गोचर कर रहा है, जिससे सामाजिक प्रतिष्ठा और साझेदारी के कार्यों में प्रगति होगी। दांपत्य जीवन में मधुरता और परस्पर विश्वास का विस्तार होगा। नए व्यावसायिक प्रस्तावों या साझेदारियों पर गंभीरता से विचार हो सकता है।",
            "सप्तम भाव में {planet} की उपस्थिति {P} आपको दूसरों के साथ प्रभावी संबंध स्थापित करने में सक्षम बनाएगी। जीवनसाथी के सहयोग से कोई रुका हुआ काम पूरा होगा। जनसंपर्क और सार्वजनिक मंचों पर आपकी छवि और अधिक उज्ज्वल होगी।",
            "{planet} का संचरण {P} आपके रिश्तों में नई ऊर्जा और समझदारी लाएगा। व्यापारिक अनुबंधों और आपसी समझौतों में पारदर्शिता रखना अत्यंत लाभकारी रहेगा। किसी महत्वपूर्ण व्यक्ति से मुलाकात आपके भविष्य के लिए शुभ साबित होगी।"
        ),
        // House 8 (Randhra & Ayur)
        listOf(
            "{planet} आपके अष्टम भाव (रूपांतरण, शोध व गूढ़ विद्या) में स्थित है, जिससे आत्म-निरीक्षण और आध्यात्मिक चिंतन में रुचि बढ़ेगी। अप्रत्याशित बदलावों के लिए मानसिक रूप से तैयार रहें और धैर्य बनाए रखें। शोध, ज्योतिष या गहरे विषयों के अध्ययन के लिए यह समय उत्तम है।",
            "अष्टम भाव में {planet} का गोचर {P} आपके जीवन में छिपे हुए पहलुओं और पुरानी समस्याओं को हल करने का अवसर देगा। जोखिम भरे कार्यों और जल्दबाजी में लिए गए निर्णयों से बचें। अपनी आंतरिक शक्ति को पहचानें और ध्यान-साधना से मन को स्थिर रखें।",
            "{planet} का प्रभाव {P} आपको भावनात्मक रूप से परिपक्व और मजबूत बनाएगा। अनपेक्षित स्रोतों से लाभ या पैतृक संपत्ति से जुड़ी चर्चाएं आगे बढ़ सकती हैं। किसी भी दस्तावेज पर हस्ताक्षर करने से पूर्व सभी शर्तों को भली-भांति समझ लें।"
        ),
        // House 9 (Dharma & Bhagya)
        listOf(
            "{planet} आपके नवम भाव (भाग्य, धर्म व उच्च विद्या) में गोचर कर रहा है, जिससे भाग्य का पूरा साथ मिलेगा और अटके कार्य पूरे होंगे। धार्मिक कार्यों, तीर्थ यात्रा या गुरुजनों के सान्निध्य में समय बिताने के सुंदर योग बन रहे हैं। आपकी आस्था और नैतिक मूल्य आपको सही दिशा दिखाएंगे।",
            "नवम भाव में {planet} की उपस्थिति {P} आपके दृष्टिकोण को व्यापक और आशावादी बनाएगी। उच्च शिक्षा, मार्गदर्शन या दूर की यात्राओं से जुड़े प्रयास सफल होंगे। अपने ज्ञान और अनुभवों को दूसरों के साथ साझा करने से मान-सम्मान बढ़ेगा।",
            "{planet} का गोचर {P} आपके जीवन में नई संभावनाएं और सौभाग्य लेकर आएगा। वरिष्ठ मार्गदर्शकों का आशीर्वाद प्राप्त होगा और महत्वपूर्ण निर्णयों में भाग्य अनुकूल रहेगा। धर्म और परोपकार के कार्यों में अपनी सहभागिता बनाए रखें।"
        ),
        // House 10 (Karma & Rajya)
        listOf(
            "{planet} आपके दशम भाव (करियर, मान-प्रतिष्ठा व सत्ता) में गोचर कर रहा है, जिससे कार्यक्षेत्र में आपकी प्रतिष्ठा और प्रभाव में उल्लेखनीय वृद्धि होगी। पदोन्नति, नई जिम्मेदारियों या बड़े व्यावसायिक सौदों के प्रबल योग हैं। अपने दायित्वों का निर्वहन पूरी निष्ठा से करें।",
            "दशम भाव में {planet} की मजबूत स्थिति {P} आपके पेशेवर जीवन को नई ऊंचाइयों पर ले जाने का अवसर प्रदान करेगी। उच्चाधिकारियों और सरकार से जुड़े कार्यों में अनुकूल परिणाम मिलेंगे। अपने नेतृत्व कौशल का सही उपयोग करके लक्ष्यों को प्राप्त करें।",
            "{planet} का संचरण {P} आपके कार्यस्थल पर सम्मान, पहचान और नए अवसर लाएगा। आपकी दीर्घकालिक मेहनत का सुखद फल मिलने का समय आ गया है। व्यावसायिक साझेदारों के साथ मिलकर बनाई गई रणनीतियां सफल सिद्ध होंगी।"
        ),
        // House 11 (Labha & Aya)
        listOf(
            "{planet} आपके एकादश भाव (आय, लाभ व मित्र) में स्थित है, जिससे आर्थिक लाभ और महत्वाकांक्षाओं की पूर्ति के प्रबल योग बन रहे हैं। मित्रों और सामाजिक संपर्कों का बड़ा सहयोग मिलेगा, जिससे नई योजनाओं को बल मिलेगा। आय के अतिरिक्त स्रोतों पर काम शुरू हो सकता है।",
            "एकादश भाव में {planet} का गोचर {P} आपके सामाजिक दायरे का विस्तार करेगा और लाभदायक संबंधों को मजबूत बनाएगा। किसी पुराने प्रयास का आर्थिक प्रतिफल प्राप्त हो सकता है। सामूहिक गतिविधियों और नेटवर्किंग में आपकी सक्रियता फलदायी रहेगी।",
            "{planet} की शुभ स्थिति {P} आपकी इच्छाओं की पूर्ति और आर्थिक समृद्धि के द्वार खोलेगी। शुभचिंतकों की सलाह से महत्वपूर्ण वित्तीय प्रगति संभव है। अपने भविष्य के लक्ष्यों को व्यवस्थित रूप से निर्धारित करें और आगे बढ़ें।"
        ),
        // House 12 (Vyaya & Moksha)
        listOf(
            "{planet} आपके द्वादश भाव (व्यय, विदेश व मोक्ष) में गोचर कर रहा है, जिससे आत्मचिंतन, परोपकार और आध्यात्मिक गतिविधियों में मन लगेगा। दूरस्थ स्थानों या विदेशी संपर्कों से लाभ के अवसर बन सकते हैं। अनावश्यक खर्चों पर नियंत्रण रखें और मानसिक शांति को प्राथमिकता दें।",
            "द्वादश भाव में {planet} की उपस्थिति {P} आपको बाहरी दुनिया के कोलाहल से दूर एकांत में चिंतन करने की प्रेरणा देगी। दान-पुण्य और सेवा कार्यों से मन को असीम संतुष्टि मिलेगी। अपनी नींद और विश्राम की दिनचर्या को नियमित बनाए रखें।",
            "{planet} का प्रभाव {P} आपके आध्यात्मिक विकास और पुराने मानसिक बंधनों से मुक्ति में सहायक होगा। आयात-निर्यात या विदेशी कंपनियों से जुड़े जातकों के लिए दिन शुभ परिणाम दे सकता है। शांति और सकारात्मकता बनाए रखें।"
        )
    )

    private val GENERAL_EN = listOf(
        // House 1
        listOf(
            "{planet} transits your own sign (1st house) {p}, significantly enhancing your self-confidence, aura, and personal effectiveness. Favorable momentum allows you to clear pending tasks and take bold initiatives. Maintain an optimistic outlook and begin your day with mental clarity and calm resolve.",
            "The presence of {planet} in your ascendant house {p} keeps you energized, proactive, and influential across all endeavors. You will feel naturally motivated to implement personal goals and structure your daily priorities. Trust your inner voice when making important decisions.",
            "{planet}'s transit through your 1st house {p} brings clarity of thought, intellectual charm, and positive energy. Peers and family members will value your counsel and leadership. Stay focused on your core objectives and let go of unnecessary worries."
        ),
        // House 2
        listOf(
            "{planet} sits in your 2nd house (wealth and family) {p}, bringing financial planning and domestic harmony to the forefront. Your words carry warmth, authority, and persuasive clarity. Approach important family discussions with patience and respect the wisdom of elders.",
            "With {planet} in your 2nd house {p}, safeguarding accumulated savings and nurturing household unity become key priorities. Favorable conditions support analyzing new financial avenues. Choose your expressions thoughtfully to preserve harmony in all relationships.",
            "{planet}'s influence {p} helps you organize your financial priorities and fulfill family responsibilities with ease. A lingering monetary matter finds a constructive resolution. Seeking counsel from senior family members proves highly beneficial."
        ),
        // House 3
        listOf(
            "{planet} transits your 3rd house (courage and communication) {p}, elevating your initiative, courage, and networking skills. Short trips and new contacts open up rewarding opportunities. Support from siblings and trusted friends strengthens your confidence.",
            "The placement of {planet} in the 3rd house {p} empowers you to tackle fresh challenges with enthusiasm and resilience. Those in communications, writing, or technology enjoy a creative surge. Express your ideas boldly and welcome new connections.",
            "{planet}'s transit {p} fuels your drive and inspires you to launch new endeavors with zeal. Cordial ties with neighbors and peers bring ease to your daily schedule. Channeling your active energy into focused goals yields rewarding progress."
        ),
        // House 4
        listOf(
            "{planet} transits your 4th house (home, mother, and comfort) {p}, enhancing domestic tranquility and emotional contentment. Matters relating to property, home decor, or vehicles gain momentum. Your mother's blessings and affection provide deep solace and strength.",
            "{planet}'s placement in your 4th house {p} brings a deep sense of inner calm and emotional stability. Your efforts to nurture domestic unity bear fruit. Positive developments are likely in real estate or property affairs, keeping your spirits high.",
            "{planet}'s influence {p} makes your home environment warm, cozy, and uplifting. Spending quality time with loved ones replenishes your spirit and deepens bonds. Plans for acquiring household assets or a vehicle proceed smoothly."
        ),
        // House 5
        listOf(
            "{planet} occupies your 5th house (intellect, creativity, and progeny) {p}, elevating your intellectual sharpness and innovative instincts. Exceptional success is indicated in arts, research, and academia. Heartening news regarding children or educational pursuits brings joy.",
            "With {planet} transiting the 5th house {p}, your creative brilliance shines brightly, sparking fresh ideas and projects. Your foresight and discernment in decision-making will be widely appreciated. Students and professionals will experience enhanced focus.",
            "{planet}'s auspicious transit {p} infuses your life with optimism, enthusiasm, and inspiring concepts. Engaging in thoughtful dialogues and creative hobbies will prove deeply satisfying. Dedicate time to activities that ignite your passion."
        ),
        // House 6
        listOf(
            "{planet} transits your 6th house (health, diligence, and service) {p}, empowering you to outshine competitors and maintain discipline. Workplace obstacles will dissolve under your practical problem-solving. Maintain mindful dietary habits and consistent routines.",
            "The placement of {planet} in the 6th house {p} sharpens your work ethic and dedication to service. Favorable indications surround legal, bureaucratic, or administrative proceedings. Keep your schedule organized and steer clear of unnecessary disputes.",
            "{planet}'s transit {p} aids in resolving lingering difficulties and debt-related concerns. Collaborative teamwork delivers solid accomplishments. Avoid overexertion by scheduling periodic breaks throughout the day."
        ),
        // House 7
        listOf(
            "{planet} transits your 7th house (partnerships, marriage, and public relations) {p}, enhancing your social standing and joint ventures. Mutual respect and emotional warmth deepen in marital and romantic life. Promising discussions regarding new alliances or contracts may unfold.",
            "The presence of {planet} in the 7th house {p} equips you to foster rewarding, balanced interpersonal connections. Spousal support assists in accomplishing a key objective. Your reputation in public circles and business environments shines brighter.",
            "{planet}'s transit {p} revitalizes your partnerships with fresh perspective and understanding. Maintaining transparency in commercial agreements and personal pacts brings great advantages. A meeting with an influential person proves auspicious."
        ),
        // House 8
        listOf(
            "{planet} sits in your 8th house (transformation, research, and occult) {p}, encouraging introspection and spiritual reflection. Stay adaptable and composed when navigating unexpected changes. Deep research, investigation, and esoteric studies will yield profound insights.",
            "With {planet} in your 8th house {p}, opportunities emerge to uncover hidden dynamics and resolve deep-seated challenges. Exercise prudent caution with speculative moves and hurried choices. Draw upon your inner resilience and find calm through meditation.",
            "{planet}'s influence {p} fosters emotional maturity and inner strength. Favorable discussions regarding legacy matters or unearned gains may advance. Carefully review all terms and conditions before entering legal commitments."
        ),
        // House 9
        listOf(
            "{planet} transits your 9th house (fortune, dharma, and higher wisdom) {p}, bringing favorable fortune and smooth completion to long-pending tasks. Auspicious alignments support pilgrimages, spiritual practices, and learning from mentors. Your moral clarity guides you well.",
            "The placement of {planet} in the 9th house {p} broadens your horizons with hope and optimism. Endeavors related to higher education, mentorship, or long-distance travel flourish. Sharing your insights and wisdom elevates your honor and standing.",
            "{planet}'s transit {p} opens doors to new opportunities and auspicious turns of events. Blessings from elders and spiritual teachers empower your journey. Engaging in philanthropic and noble deeds brings immense spiritual peace."
        ),
        // House 10
        listOf(
            "{planet} transits your 10th house (career, honor, and public life) {p}, significantly boosting your professional authority, reputation, and impact. Favorable indications point toward career advancement, leadership roles, or major business wins. Fulfill duties diligently.",
            "A strong position of {planet} in the 10th house {p} creates fertile ground for taking your professional trajectory to greater heights. Interactions with seniors and regulatory authorities yield positive outcomes. Exercise thoughtful leadership to reach milestones.",
            "{planet}'s transit {p} brings recognition, respect, and exciting new openings at your workplace. Consistent hard work from past efforts now bears rewarding fruit. Strategic plans formulated with professional partners prove highly successful."
        ),
        // House 11
        listOf(
            "{planet} occupies your 11th house (gains, income, and aspirations) {p}, creating strong potential for financial abundance and fulfillment of cherished desires. Generous support from friends and social networks energizes your plans. New income streams may take root.",
            "The transit of {planet} through the 11th house {p} expands your social circle and solidifies lucrative connections. Past endeavors may yield rewarding financial returns. Active engagement in collaborative platforms and group activities proves fruitful.",
            "{planet}'s benevolent influence {p} opens avenues for wish-fulfillment and sustained economic prosperity. Sound advice from well-wishers fosters solid financial progress. Structure your long-term roadmap with optimism and move forward."
        ),
        // House 12
        listOf(
            "{planet} transits your 12th house (expenses, spirituality, and foreign affairs) {p}, encouraging introspection, charitable deeds, and meditation. Lucrative possibilities linked to distant places or overseas connections may surface. Keep discretionary spending balanced.",
            "The presence of {planet} in the 12th house {p} inspires you to step back from outer noise and enjoy peaceful contemplation. Acts of generosity and selfless service bring profound inner contentment. Prioritize a regular sleep cycle and rest.",
            "{planet}'s influence {p} aids your spiritual evolution and frees you from past emotional burdens. Professionals engaged in international trade or cross-border firms see favorable gains. Maintain inner calm and positive energy."
        )
    )

    // =========================================================================
    // 2. CAREER & BUSINESS READINGS (12 Houses x 3 Variations)
    // =========================================================================
    private val CAREER_HI = listOf(
        // House 1
        listOf(
            "कार्यक्षेत्र में आपकी सक्रियता और सूझबूझ से महत्वपूर्ण निर्णय सफल सिद्ध होंगे। अधिकारी वर्ग आपके योगदान को मान्यता देगा और नई जिम्मेदारियां मिलने के योग हैं। व्यावसायिक साझेदारों के साथ विचार साझा करने से लाभ होगा।",
            "व्यावसायिक क्षेत्र में आपके नेतृत्व कौशल की सराहना होगी। सहकर्मियों के साथ तालमेल बनाकर चलने से कठिन कार्य भी सरलता से पूरे होंगे। नई परियोजनाओं पर काम शुरू करने के लिए समय उत्तम है।",
            "कार्यस्थल पर आपकी प्रतिष्ठा में वृद्धि होगी और वरिष्ठ अधिकारियों का मार्गदर्शन मिलेगा। अपनी कार्यशैली में नवाचार लाने से परिणाम उत्कृष्ट मिलेंगे। किसी जरूरी बैठक में आपकी प्रस्तुति प्रभावशाली रहेगी।"
        ),
        // House 2
        listOf(
            "व्यापारिक वार्ताओं और प्रस्तुतियों में आपकी सम्मोहनकारी वाणी सफलता दिलाएगी। सहकर्मियों के साथ वित्तीय मामलों पर स्पष्टता रखें और नियमों का पालन करें। किसी नए ग्राहक या सौदे से शुभ समाचार मिल सकता है।",
            "कार्यक्षेत्र में आपकी सूझबूझ और संयमित व्यवहार की सराहना होगी। बैंकिंग, वित्त या विपणन से जुड़े जातकों के लिए समय विशेष अनुकूल है। अपने लक्ष्यों की प्राप्ति के लिए ठोस रणनीति बनाएं।",
            "पेशेवर जिम्मेदारियों को समय पर पूरा करने से वरिष्ठ अधिकारी संतुष्ट रहेंगे। साझेदारी के कार्यों में मौखिक वादों के बजाय लिखित समझौतों को प्राथमिकता दें। कार्यस्थल पर सकारात्मक वातावरण बना रहेगा।"
        ),
        // House 3
        listOf(
            "कार्यक्षेत्र में नई जिम्मेदारियां उठाने के लिए आप पूरी तरह तत्पर रहेंगे। मार्केटिंग, मीडिया और जनसंपर्क से जुड़े जातकों को बड़ी सफलता मिल सकती है। आपकी त्वरित निर्णय क्षमता से परियोजनाएं समय से पूर्व पूरी होंगी।",
            "सहकर्मियों के साथ मिलकर किए गए सामूहिक प्रयासों में उत्कृष्ट परिणाम मिलेंगे। तकनीकी कौशल में निपुणता आपके करियर को नई दिशा देगी। व्यावसायिक यात्राएं लाभदायक सिद्ध होंगी।",
            "आपकी कार्यकुशलता और प्रस्तुति शैली उच्चाधिकारियों को प्रभावित करेगी। नए व्यावसायिक सौदों में बातचीत का पलड़ा आपके पक्ष में रहेगा। आत्मविश्वास से भरे निर्णय लें।"
        ),
        // House 4
        listOf(
            "कार्यक्षेत्र में स्थिरता बनी रहेगी और आप शांत चित्त से जटिल समस्याओं का हल निकाल पाएंगे। रियल एस्टेट, निर्माण और घरेलू उत्पादों से जुड़े जातकों को लाभ होगा। घर और दफ्तर में संतुलन बनाए रखें।",
            "वर्क फ्रॉम होम या स्वतंत्र कार्य करने वालों के लिए दिन बहुत उत्पादक रहेगा। आपकी दूरदर्शिता से व्यावसायिक निर्णय सही दिशा में आगे बढ़ेंगे। सहकर्मियों का सहयोग प्राप्त होगा।",
            "कार्यस्थल पर अनुकूल वातावरण रहेगा और आपके प्रयासों को सराहा जाएगा। नए विचारों को धरातल पर उतारने के लिए ठोस योजना बनाएं। वरिष्ठ अधिकारियों से मधुर संबंध रहेंगे।"
        ),
        // House 5
        listOf(
            "रचनात्मक सोच और नवाचार से कार्यस्थल पर आपकी अलग पहचान बनेगी। आईटी, डिजाइनिंग, परामर्श और शिक्षा क्षेत्र से जुड़े जातकों को विशेष सफलता मिलेगी। किसी नई व्यावसायिक योजना का शुभारंभ हो सकता है।",
            "आपकी बुद्धिमत्ता और रणनीतिक दृष्टिकोण कठिन चुनौतियों को आसान बना देगा। नए प्रोजेक्ट्स में आपके सुझावों को सर्वोच्च प्राथमिकता मिलेगी। सहकर्मी आपकी कार्यशैली से प्रेरित होंगे।",
            "व्यावसायिक कौशल और प्रतिभा के प्रदर्शन का उत्तम अवसर मिलेगा। अधिकारियों से सकारात्मक प्रतिक्रिया प्राप्त होगी। अपने कार्यक्षेत्र में नई तकनीकों को अपनाना फायदेमंद रहेगा।"
        ),
        // House 6
        listOf(
            "दैनिक कार्यप्रणाली में अनुशासन और दक्षता से आप लक्ष्यों को सरलता से हासिल करेंगे। प्रतिस्पर्धियों की गतिविधियों पर नजर रखें, विजय आपकी ही होगी। विभागीय कार्यों में बारीकियों पर ध्यान दें।",
            "कार्यालय में आपकी कर्तव्यनिष्ठा और कार्यकुशलता की प्रशंसा होगी। सेवा क्षेत्र, चिकित्सा या कानूनी कार्यों से जुड़े जातकों के लिए समय बहुत अनुकूल है। लंबित कार्यों को निपटाने का प्रयास करें।",
            "कार्यस्थल पर आने वाली चुनौतियों का आप डटकर मुकाबला करेंगे। सहकर्मियों के साथ सामंजस्य बनाकर काम करने से उत्पादकता बढ़ेगी। अपने अधिकारों और कर्तव्यों के प्रति सजग रहें।"
        ),
        // House 7
        listOf(
            "व्यावसायिक साझेदारी और अनुबंधों के लिए दिन अत्यंत फलदायी रहेगा। नए ग्राहकों के साथ दीर्घकालिक संबंध स्थापित होंगे। टीम के साथ मिलकर कार्य करने से सफलता का दायरा बढ़ेगा।",
            "साझेदारी के व्यापार में नए विस्तार की योजनाएं मूर्त रूप ले सकती हैं। सार्वजनिक व्यवहार और सौदेबाजी में आपकी दक्षता लाभ कराएगी। किसी प्रतिष्ठित संस्थान से सहयोग मिल सकता है।",
            "व्यापारिक वार्ताओं में सकारात्मक प्रगति होगी और नए अनुबंध हस्ताक्षर हो सकते हैं। सहकर्मियों और व्यावसायिक भागीदारों का पूर्ण विश्वास प्राप्त होगा। कार्यक्षेत्र में पारदर्शिता बनाए रखें।"
        ),
        // House 8
        listOf(
            "कार्यक्षेत्र में अनुसंधान, विश्लेषण और रणनीतिक योजनाएं बनाने का समय है। गुप्त शत्रुओं या कार्यालय की राजनीति से दूर रहकर अपने काम पर ध्यान दें। डेटा व गोपनीय जानकारी को सुरक्षित रखें।",
            "जटिल और तकनीकी समस्याओं को सुलझाने में आपकी विशेषज्ञता काम आएगी। ऑडिट, टैक्स, इंश्योरेंस या रिसर्च से जुड़े जातकों को लाभ होगा। किसी भी परिवर्तन के लिए तैयार रहें।",
            "पेशेवर जीवन में धैर्य और एकाग्रता बनाए रखना आवश्यक रहेगा। अचानक आई किसी चुनौती को सूझबूझ से अवसर में बदल सकते हैं। जल्दबाजी में कोई बड़ा व्यावसायिक जोखिम न लें।"
        ),
        // House 9
        listOf(
            "करियर में भाग्य का मजबूत सहयोग मिलेगा और उच्चाधिकारियों का मार्गदर्शन प्राप्त होगा। विदेशी परियोजनाओं या दूरस्थ कार्यों में बड़ी सफलता मिलने के योग हैं। अपनी क्षमताओं का पूर्ण विस्तार करें।",
            "व्यावसायिक यात्राएं सफल और लाभदायक रहेंगी। उच्च अध्ययन, शोध या प्रशिक्षण के माध्यम से अपने कौशल को निखारने का समय है। आपकी सलाह से संस्थान को बड़ा लाभ हो सकता है।",
            "कार्यक्षेत्र में आपके नैतिक मूल्यों और सिद्धांतों की सराहना होगी। कानूनी और प्रशासनिक कार्यों में सकारात्मक प्रगति होगी। नए अवसरों को बिना झिझक स्वीकार करें।"
        ),
        // House 10
        listOf(
            "करियर में पदोन्नति, सम्मान और नए नेतृत्वकारी दायित्व मिलने के प्रबल संकेत हैं। सरकारी या प्रशासनिक कार्यों में सफलता मिलेगी। अपने निर्णयों को आत्मविश्वास के साथ लागू करें।",
            "वरिष्ठ अधिकारियों और व्यावसायिक जगत में आपकी साख और प्रतिष्ठा बढ़ेगी। नई परियोजनाओं का नेतृत्व करने का अवसर मिल सकता है। अपनी कार्यकुशलता से सभी को प्रभावित करेंगे।",
            "व्यावसायिक विस्तार और नई शाखाएं शुरू करने के लिए समय उत्तम है। आपकी योजनाएं धरातल पर सफल सिद्ध होंगी। कार्यस्थल पर सकारात्मक ऊर्जा का संचार होगा।"
        ),
        // House 11
        listOf(
            "व्यावसायिक लक्ष्यों की प्राप्ति होगी और बड़े सौदों से भारी लाभ मिलने के योग हैं। कॉर्पोरेट नेटवर्किंग और संपर्कों से नए रास्ते खुलेंगे। टीम वर्क से असाधारण परिणाम हासिल होंगे।",
            "कार्यक्षेत्र में आपके सुझावों से संस्थान को बड़ा आर्थिक व रणनीतिक लाभ होगा। नए सहयोगियों और ग्राहकों का दायरा तेजी से बढ़ेगा। अपनी उपलब्धियों पर गर्व महसूस करेंगे।",
            "व्यापार में अप्रत्याशित सफलता और विस्तार के अवसर मिलेंगे। सहकर्मियों का पूरा समर्थन प्राप्त होगा। अपने भविष्य की व्यावसायिक योजनाओं को और अधिक मजबूत बनाएं।"
        ),
        // House 12
        listOf(
            "विदेशी कंपनियों, आयात-निर्यात या मल्टीनेशनल संस्थानों से जुड़े कार्यों में लाभ होगा। पर्दे के पीछे रहकर की गई योजनाएं अधिक फलदायी साबित होंगी। अनावश्यक प्रचार से बचें।",
            "कार्यक्षेत्र में शांति और एकाग्रता से काम निपटाना आपके हित में रहेगा। सुदूर संपर्कों से नई व्यावसायिक संभावनाएं जन्म ले सकती हैं। अपने दस्तावेजों की सुरक्षा सुनिश्चित करें।",
            "अनुसंधान, लेखन या रचनात्मक कार्यों में लगे जातकों को विशेष सफलता मिलेगी। अपने लक्ष्यों पर मौन रहकर काम करें, परिणाम स्वयं आपकी सफलता की गवाही देंगे।"
        )
    )

    private val CAREER_EN = listOf(
        // House 1
        listOf(
            "Your active initiative and sharp thinking bring success to key workplace decisions {p}. Seniors are likely to recognize your contributions, opening doors to new responsibilities. Sharing strategic ideas with business partners will prove fruitful.",
            "Your leadership abilities receive praise across professional circles {p}. Collaborative teamwork helps you navigate demanding tasks smoothly and efficiently. This is an opportune moment to kickstart new ambitious ventures.",
            "Your professional reputation grows, supported by valuable guidance from mentors and seniors {p}. Innovating your daily workflow produces excellent outcomes. Your presentation in important meetings will leave a positive impact."
        ),
        // House 2
        listOf(
            "Your articulate communication delivers success in business negotiations and client meetings {p}. Maintain financial transparency with colleagues and adhere to protocols. Good news regarding a new client or deal is likely.",
            "Your measured approach and professional diligence earn respect at the workplace {p}. Those in finance, banking, or commerce will find favorable tailwinds. Build a concrete step-by-step strategy to attain milestones.",
            "Delivering responsibilities promptly keeps superiors well-satisfied {p}. In collaborative efforts, prioritize written clarity over verbal understandings. The working environment remains constructive and supportive."
        ),
        // House 3
        listOf(
            "You feel ready to take on fresh responsibilities at work with vigor {p}. Professionals in marketing, media, and outreach may achieve significant breakthroughs. Quick, calculated decisions help complete tasks ahead of schedule.",
            "Collaborative team efforts yield outstanding results across the board {p}. Honing technical skills opens promising doors for career advancement. Short business-related travels prove beneficial.",
            "Your efficiency and persuasive communication style impress senior management {p}. Negotiations in new business deals swing in your favor. Trust your capabilities when executing plans."
        ),
        // House 4
        listOf(
            "Stability prevails at work, enabling you to solve complex issues with a calm, composed mind {p}. Professionals in real estate, architecture, or hospitality see gains. Maintain a healthy work-life balance.",
            "A highly productive period for remote professionals and entrepreneurs {p}. Your practical foresight keeps projects on the right track. Peer collaboration remains supportive and steady.",
            "A pleasant working atmosphere allows your best efforts to shine {p}. Structuring new concepts with concrete roadmaps brings success. Cordial ties with seniors foster career progress."
        ),
        // House 5
        listOf(
            "Creative thinking and innovative solutions distinguish your profile at work {p}. Exceptional outcomes are indicated in IT, design, consulting, and education. Launching a new business plan brings fruitful rewards.",
            "Your intellect and strategic acumen simplify intricate challenges {p}. Your inputs in new projects receive top priority from stakeholders. Colleagues draw inspiration from your work style.",
            "A prime opportunity to showcase your specialized skills and talents {p}. Constructive feedback from managers accelerates your growth. Embracing new technologies will prove advantageous."
        ),
        // House 6
        listOf(
            "Disciplined execution and operational efficiency help you meet targets with ease {p}. Stay watchful of market competitors; triumph is yours. Focus on meticulous detail in departmental workflows.",
            "Your dedication and diligent work ethic earn commendable praise {p}. Those in healthcare, legal, or administrative domains experience favorable developments. Work on resolving backlogs.",
            "You will navigate workplace hurdles with grit and pragmatism {p}. Harmonious cooperation with teammates boosts output. Stay conscious of your professional rights and duties."
        ),
        // House 7
        listOf(
            "A highly fruitful period for joint ventures, trade pacts, and business partnerships {p}. Long-lasting relationships will be forged with valuable clients. Working cohesively with your team expands success.",
            "Expansion plans in joint business ventures begin to materialize {p}. Your finesse in public relations and deal negotiations delivers notable gains. Collaboration with a reputable institution is possible.",
            "Commercial discussions proceed constructively toward concluding key agreements {p}. Partners and coworkers place immense trust in your vision. Maintain absolute transparency in operations."
        ),
        // House 8
        listOf(
            "An ideal time for deep market research, data analysis, and strategic restructuring {p}. Steer clear of office politics and keep undivided focus on your deliverables. Safeguard confidential assets.",
            "Your specialized expertise helps untangle technical and operational bottlenecks {p}. Professionals in auditing, taxation, insurance, and research benefit. Stay ready for dynamic shifts.",
            "Patience and concentrated focus are vital in your professional sphere {p}. A sudden challenge can be astutely transformed into a lucrative opportunity. Avoid uncalculated career gambles."
        ),
        // House 9
        listOf(
            "Fortune strongly supports your professional ambitions alongside valuable mentorship {p}. Remote collaborations and cross-regional projects show immense promise. Broaden your horizons confidently.",
            "Business travels yield profitable and inspiring results {p}. Enhance your qualifications through advanced training or certifications. Your insights may save your organization vital resources.",
            "Your high ethical standards and commitment to quality earn widespread respect {p}. Regulatory and administrative affairs move forward smoothly. Embrace new horizons without hesitation."
        ),
        // House 10
        listOf(
            "Strong indications point toward promotions, accolades, and prestigious leadership duties {p}. Success is favored in administrative and executive tasks. Implement key decisions with confidence.",
            "Your standing and authority expand among industry peers and corporate leaders {p}. You may be entrusted with directing high-impact projects. Your efficiency leaves a memorable impression.",
            "A prime time for commercial expansion and opening new ventures {p}. Your operational blueprints prove triumphant on the ground. A vibrant, productive energy surrounds your workplace."
        ),
        // House 11
        listOf(
            "Professional milestones will be achieved, with large-scale deals offering rich rewards {p}. Corporate networking and social connections open fresh avenues. Team initiatives yield exceptional results.",
            "Your suggestions deliver measurable economic and strategic gains to your enterprise {p}. Your network of allies and valuable clients grows rapidly. Take justified pride in your progress.",
            "Unforeseen breakthroughs and expansion opportunities appear in your business {p}. Solid support from colleagues accelerates your progress. Refine your future commercial roadmap."
        ),
        // House 12
        listOf(
            "Lucrative opportunities emerge in multinational enterprises, export-import, and offshore dealings {p}. Quiet, behind-the-scenes planning pays off better than public fanfare. Avoid unnecessary publicity.",
            "Working in a peaceful, distraction-free environment maximizes your productivity {p}. Fresh business avenues develop through distant contacts. Ensure your documentation is well secured.",
            "Creative writers, researchers, and solo entrepreneurs achieve notable breakthroughs {p}. Work quietly toward your targets; your stellar results will speak for themselves."
        )
    )

    // =========================================================================
    // 3. FINANCE & WEALTH READINGS (12 Houses x 3 Variations)
    // =========================================================================
    private val FINANCE_HI = listOf(
        // House 1
        listOf(
            "आर्थिक स्थिति सुदृढ़ रहेगी और धन आगमन के नए अवसर प्राप्त हो सकते हैं। अपने वित्तीय लक्ष्यों की समीक्षा करें और अनावश्यक खर्चों पर नियंत्रण रखें। दीर्घकालिक योजनाओं में निवेश करना भविष्य के लिए लाभकारी रहेगा।",
            "धन से जुड़े मामलों में स्पष्टता आएगी और आर्थिक संतुलन बना रहेगा। पूर्व में किए गए किसी प्रयास से अप्रत्याशित लाभ मिल सकता है। आज बजट का पालन करना आपके लिए हितकर रहेगा।",
            "आय के नए स्रोत विकसित करने की दिशा में प्रगति होगी। व्यापारिक लेन-देन में सतर्कता बरतें और लिखित प्रमाण अवश्य रखें। व्यय पर नियंत्रण रखने से बैंक बैलेंस में सुधार होगा।"
        ),
        // House 2
        listOf(
            "आर्थिक दृष्टि से दिन अत्यंत शुभ है और धन संचय में वृद्धि के योग बन रहे हैं। किसी पुराने निवेश से अच्छा लाभ मिलने की संभावना है। फिजूलखर्ची से बचें और बचत को प्राथमिकता दें।",
            "वित्तीय स्थिरता बनी रहेगी और रुका हुआ धन वापस मिलने के संकेत हैं। परिवार के लिए किसी आवश्यक वस्तु की खरीदारी में धन व्यय हो सकता है। नए निवेश के लिए विशेषज्ञों की राय लें।",
            "धन आगमन के निरंतर स्रोत बने रहेंगे और बजट नियंत्रण में रहेगा। पैतृक संपत्ति या पारिवारिक सहयोग से वित्तीय लाभ हो सकता है। समझदारी से किए गए खर्च भविष्य में राहत देंगे।"
        ),
        // House 3
        listOf(
            "वित्तीय मामलों में सक्रियता से लाभ मिलेगा और नए अनुबंधों से धन लाभ होगा। संचार या परिवहन से जुड़े कार्यों में किया गया निवेश शुभ रहेगा। छोटे-मोटे जोखिम सोच-समझकर ले सकते हैं।",
            "आय के नए स्रोत विकसित होंगे और आर्थिक स्थिति में सुधार होगा। आवश्यक व्यावसायिक उपकरणों या तकनीकी उन्नयन पर खर्च हो सकता है। बजट का संतुलन बनाए रखें।",
            "रुका हुआ धन मिलने की संभावना है और आर्थिक प्रयास सफल होंगे। अपने खर्चों की प्राथमिकताएं निर्धारित करें ताकि बचत प्रभावित न हो। साझेदारी में पारदर्शिता रखें।"
        ),
        // House 4
        listOf(
            "भूमि, भवन या वाहन से संबंधित निवेश के लिए अनुकूल समय है। पारिवारिक बचत में वृद्धि होगी और आर्थिक सुरक्षा का अहसास होगा। गैर-जरूरी खर्चों से बचकर बजट को संतुलित रखें।",
            "अचल संपत्ति से जुड़े मामलों में वित्तीय लाभ के योग हैं। घर की सुख-सुविधाओं पर किया गया खर्च सार्थक सिद्ध होगा। बचत योजनाओं में निवेश करने से भविष्य सुरक्षित होगा।",
            "धन का प्रवाह स्थिर रहेगा और पारिवारिक सहयोग से आर्थिक लाभ संभव है। किसी पुराने कर्ज से राहत मिलने के संकेत हैं। वित्तीय अनुशासन बनाए रखना शुभ रहेगा।"
        ),
        // House 5
        listOf(
            "शेयर बाजार, म्यूचुअल फंड या रचनात्मक परियोजनाओं में समझदारी से किया गया निवेश लाभ दे सकता है। सट्टेबाजी या अत्यधिक जोखिम वाले लेन-देन से दूर रहें। बच्चों की शिक्षा पर योजनाबद्ध व्यय होगा।",
            "आर्थिक स्थिति में सुधार होगा और नए वित्तीय सौदों से लाभ मिलेगा। अपनी बौद्धिक क्षमता से आप धन संवर्धन के नए तरीके खोजेंगे। सुख-सुविधाओं पर संयमित खर्च करें।",
            "वित्तीय मामलों में भाग्य का साथ मिलेगा और अचानक धन प्राप्ति के योग हैं। पूर्व के निवेशों की समीक्षा करने और मुनाफा वसूलने का अच्छा अवसर है। बजट संतुलित रखें।"
        ),
        // House 6
        listOf(
            "वित्तीय अनुशासन बनाए रखना आवश्यक है, अनावश्यक खर्चों पर कड़ा नियंत्रण रखें। किसी को उधार देने से बचें और अपने पुराने बिलों व कर्जों का समय पर भुगतान करें। समझदारी से बजट का पालन करें।",
            "दैनिक व्यय में संयम बरतें और केवल आवश्यक वस्तुओं पर ही खर्च करें। कोर्ट-कचहरी या विवादों से जुड़े खर्चों में राहत मिलने के संकेत हैं। आपातकालीन बचत निधि को सुदृढ़ बनाएं।",
            "आर्थिक मामलों में सतर्कता बरतें और किसी भी प्रलोभन में न आएं। नौकरी या व्यवसाय में नियमित आय बनी रहेगी। दीर्घकालिक बचत योजनाओं को प्राथमिकता दें।"
        ),
        // House 7
        listOf(
            "साझेदारी के व्यवसाय और नए सौदों से धन लाभ के उत्तम योग हैं। जीवनसाथी के नाम से किया गया निवेश या उनका आर्थिक सहयोग लाभकारी रहेगा। व्यापारिक लेन-देन में स्पष्टता रखें।",
            "आर्थिक स्थिति मजबूत होगी और व्यावसायिक अनुबंधों से नियमित आय सुनिश्चित होगी। संयुक्त वित्तीय योजनाओं में प्रगति होगी। बड़े लेन-देन में कानूनी सलाह लेना उत्तम रहेगा।",
            "धन आगमन के स्रोत बढ़ेंगे और किसी नए प्रोजेक्ट से आर्थिक लाभ होगा। साथी के साथ मिलकर भविष्य के लिए बचत योजनाएं बनाएं। व्यावसायिक विस्तार में धन का सही नियोजन करें।"
        ),
        // House 8
        listOf(
            "अचानक धन लाभ या पैतृक संपत्ति से जुड़े मामलों में प्रगति संभव है। हालांकि, जोखिम भरे निवेश और अनियोजित खर्चों से पूरी तरह परहेज करें। टैक्स व इंश्योरेंस संबंधी कार्य समय पर निपटाएं।",
            "गुप्त स्रोतों या पुराने निवेश से अप्रत्याशित धन प्राप्ति के संकेत हैं। लेन-देन में अत्यधिक सावधानी बरतें और किसी पर अंधविश्वास न करें। अपनी वित्तीय गोपनीयता बनाए रखें।",
            "वित्तीय मामलों में संतुलन आवश्यक है, क्योंकि अचानक कोई अप्रत्याशित व्यय सामने आ सकता है। अपने बजट का दायरा तय रखें और फिजूलखर्ची से बचें।"
        ),
        // House 9
        listOf(
            "भाग्य के सहयोग से आर्थिक मामलों में अप्रत्याशित सुधार होगा। धार्मिक कार्यों, यात्राओं या उच्च शिक्षा पर सार्थक खर्च होगा। दीर्घकालिक व सुरक्षित निवेश आपके भविष्य को संवारेगा।",
            "आर्थिक स्थिति में निरंतर सुधार होगा और दूरस्थ संपर्कों से धन लाभ मिलेगा। दान-पुण्य में धन का सदुपयोग करने से मानसिक शांति व समृद्धि मिलेगी। नए निवेश के लिए शुभ समय है।",
            "धन प्राप्ति के नए मार्ग प्रशस्त होंगे और पूर्व में किए गए निवेश का भरपूर लाभ मिलेगा। वित्तीय निर्णयों में वरिष्ठों की सलाह लाभदायक सिद्ध होगी। बचत में वृद्धि होगी।"
        ),
        // House 10
        listOf(
            "करियर में तरक्की के साथ वेतन वृद्धि या व्यापारिक मुनाफे में बढ़ोतरी के योग हैं। सरकार या उच्च अधिकारियों के सहयोग से वित्तीय लाभ होगा। अपने व्यावसायिक बजट का सही प्रबंधन करें।",
            "आर्थिक स्थिति सुदृढ़ होगी और कार्यक्षेत्र में आपकी प्रतिष्ठा से धन लाभ होगा। नई परियोजनाओं के लिए पर्याप्त संसाधन उपलब्ध होंगे। समझदारी से किया गया निवेश उत्तम प्रतिफल देगा।",
            "व्यावसायिक संपर्कों से बड़े आर्थिक लाभ की संभावना है। संपत्ति व आभूषणों की खरीदारी पर विचार हो सकता है। वित्तीय स्थिरता बनी रहेगी।"
        ),
        // House 11
        listOf(
            "आय में अप्रत्याशित वृद्धि और विभिन्न स्रोतों से धन आगमन के प्रबल योग हैं। किसी बड़ी योजना से भारी मुनाफा मिल सकता है। मित्रों और व्यापारिक साझेदारों के सहयोग से वित्तीय समृद्धि आएगी।",
            "आर्थिक दृष्टि से यह समय अत्यंत लाभकारी है, आपकी सभी वित्तीय इच्छाएं पूर्ण होने की दिशा में हैं। पुराने अटके हुए पैसे वापस मिलेंगे। निवेश के लिए समय सर्वश्रेष्ठ है।",
            "धन संचय में भारी वृद्धि होगी और नए निवेश से नियमित लाभ मिलेगा। सामाजिक संपर्कों का उपयोग करके नए व्यावसायिक सौदे हासिल करेंगे। बजट अत्यंत मजबूत रहेगा।"
        ),
        // House 12
        listOf(
            "विदेश यात्रा, परोपकार या मांगलिक कार्यों पर धन व्यय होने के योग हैं। अनावश्यक खरीदारी से बचें और अपने बजट को नियंत्रित रखें। विदेशी स्रोतों या ऑनलाइन माध्यमों से आय संभव है।",
            "आर्थिक मामलों में योजनाबद्ध तरीके से आगे बढ़ें ताकि खर्च आय से अधिक न हो। अस्पताल या कानूनी प्रक्रियाओं से जुड़े खर्चों में कमी आएगी। बचत को प्राथमिकता दें।",
            "आध्यात्मिक व धार्मिक कार्यों पर किया गया व्यय मानसिक शांति देगा। गुप्त धन लाभ या विदेशी निवेश से लाभ के संकेत हैं। अपने वित्तीय निर्णयों को गोपनीय रखें।"
        )
    )

    private val FINANCE_EN = listOf(
        // House 1
        listOf(
            "Financial prospects remain solid with new earning avenues opening up {p}. Review your budget and curb impulsive spending to preserve your savings. Channeling resources into steady long-term plans will deliver fruitful returns.",
            "Financial clarity improves, helping you maintain a steady balance between income and expenses {p}. Past investments or efforts may yield unexpected gains. Sticking to a disciplined budget ensures continued prosperity.",
            "Steady progress is made toward developing alternative streams of income {p}. Exercise standard caution with business transactions and keep documentation tidy. Prudent spending helps strengthen your financial cushion."
        ),
        // House 2
        listOf(
            "Financially, the day is favorable, with strong indications of wealth accumulation {p}. Past investments may yield healthy returns. Avoid impulsive expenditures and focus on boosting your emergency fund.",
            "Fiscal stability remains intact, and stuck funds or overdue payments may finally arrive {p}. Spending on household essentials or family needs is indicated. Consult experts before venturing into new investments.",
            "Steady inflows keep your budget well balanced and under control {p}. Benefits through ancestral property or family support may materialize. Prudent expenditure now provides long-term peace of mind."
        ),
        // House 3
        listOf(
            "Proactive financial management brings gains through new agreements and ventures {p}. Investments linked to media or transport yield positive returns. Calculated risks taken thoughtfully pay off.",
            "New earning avenues begin to take shape, improving your liquidity {p}. Useful expenditures on professional tools or tech upgrades are indicated. Maintain balanced cash flow.",
            "Recovery of overdue dues is likely, and active financial initiatives succeed {p}. Prioritizing necessary expenses preserves your hard-earned savings. Keep business transactions clear and transparent."
        ),
        // House 4
        listOf(
            "Favorable conditions support investments in property, home improvement, or vehicles {p}. Family savings grow, providing economic reassurance. Keep non-essential expenses in check.",
            "Financial gains through real estate or familial assets are indicated {p}. Expenditures on home comforts prove fulfilling and worthy. Directing surplus into safe savings secures your future.",
            "Cash flow remains steady, with possibilities of monetary support through family channels {p}. Signs point to relief from past liabilities. Maintaining financial discipline yields peace of mind."
        ),
        // House 5
        listOf(
            "Calculated, research-backed allocations in mutual funds or creative ventures yield handsome returns {p}. Steer clear of high-risk speculative bets. Budgeted spending on family or educational goals proceeds smoothly.",
            "Fiscal prospects brighten, accompanied by lucrative new commercial opportunities {p}. Your analytical acumen helps uncover creative wealth-building strategies. Maintain balanced spending.",
            "Fortune favors your finances with indications of unexpected monetary gains {p}. An opportune time to review past portfolios and lock in profits. Keep cash flows healthy."
        ),
        // House 6
        listOf(
            "Upholding fiscal discipline is essential; keep tight control over discretionary spending {p}. Refrain from extending informal loans and service your debts on schedule. Adhere closely to your budget.",
            "Exercise moderation in daily expenditures, focusing only on genuine necessities {p}. Signs indicate relief from legal or dispute-related expenses. Fortify your contingency fund.",
            "Exercise vigilance in monetary dealings and resist get-rich-quick temptations {p}. Regular income flows remain steady. Prioritize secure, long-term savings instruments."
        ),
        // House 7
        listOf(
            "Excellent prospects for monetary gain through commercial alliances and joint agreements {p}. Investments made in your spouse's name or joint ventures bring fruitful gains. Maintain clear transactional records.",
            "Financial strength grows with commercial contracts securing steady revenue streams {p}. Joint fiscal plans progress smoothly. Seeking professional counsel on large transactions is wise.",
            "Monetary inflows expand as fresh projects yield handsome returns {p}. Collaborate with your partner to devise future savings strategies. Allocate business capital prudently."
        ),
        // House 8
        listOf(
            "Possibilities for sudden financial gains or progress in legacy matters emerge {p}. However, strictly avoid volatile speculative ventures and unplanned expenditures. Settle taxes and dues promptly.",
            "Signs suggest unexpected gains through past investments or untapped sources {p}. Exercise thorough caution in transactions and verify credentials. Maintain privacy regarding your net worth.",
            "Balancing your monetary resources is crucial as unforeseen expenses could surface {p}. Define clear budgetary boundaries and curb frivolous purchases."
        ),
        // House 9
        listOf(
            "Fortune bestows unexpected improvements in financial matters {p}. Expenditure directed toward educational, spiritual, or travel pursuits proves deeply rewarding. Safe investments build a secure future.",
            "Financial momentum stays positive with gains arriving through distant connections {p}. Directing funds toward charity and noble causes brings peace and auspicious blessings. Favorable for new investments.",
            "New avenues for wealth generation open up as past allocations bear rich fruit {p}. Listening to seasoned advisors proves highly lucrative. Your savings reserve grows nicely."
        ),
        // House 10
        listOf(
            "Career advancements bring salary increments or surging business profits {p}. Support from executive authorities leads to monetary benefits. Manage your enterprise budget with diligence.",
            "Your financial footing solidifies as professional prestige converts into monetary success {p}. Adequate liquidity will back your upcoming projects. Prudent investments generate stellar returns.",
            "Strategic commercial networking unlocks high-value economic opportunities {p}. Acquisitions of enduring assets or jewelry may be contemplated. Financial stability remains unwavering."
        ),
        // House 11
        listOf(
            "Impressive surges in income and wealth inflows from multiple channels are indicated {p}. Large-scale projects may yield substantial windfalls. Collaborative ventures deliver rich prosperity.",
            "A remarkably lucrative period where your financial aspirations move closer to reality {p}. Overdue funds find their way back to you. The timing is stellar for long-term investments.",
            "Savings experience substantial growth, with fresh ventures generating consistent cash flow {p}. Leveraging social goodwill secures profitable business transactions. Your balance sheet looks robust."
        ),
        // House 12
        listOf(
            "Expenditures on travel, philanthropy, or auspicious celebrations are indicated {p}. Avoid retail therapy and keep expenses aligned with your core budget. Inflows through offshore channels are possible.",
            "Plan your financial steps meticulously so that outflows do not outpace inflows {p}. Medical or dispute-related costs will decrease. Give priority to preserving capital.",
            "Spending on spiritual and charitable activities brings serene peace of mind {p}. Indications point to unpublicized gains or foreign returns. Keep your financial strategies confidential."
        )
    )

    // =========================================================================
    // 4. LOVE & RELATIONSHIPS READINGS (12 Houses x 3 Variations)
    // =========================================================================
    private val LOVE_HI = listOf(
        // House 1
        listOf(
            "पारिवारिक जीवन में सुख-शांति का वातावरण रहेगा और जीवनसाथी का भरपूर सहयोग प्राप्त होगा। प्रियजनों के साथ आत्मीय बातचीत से मन प्रसन्न रहेगा। किसी पुरानी गलतफहमी को सुलझाने के लिए यह उपयुक्त समय है।",
            "रिश्तों में नया उत्साह और अपनापन देखने को मिलेगा। जीवनसाथी के साथ समय बिताना भावनात्मक रूप से संतोषजनक रहेगा। अविवाहितों के लिए विवाह संबंधी अनुकूल संदेश प्राप्त हो सकते हैं।",
            "घर-परिवार में आदर और स्नेह का वातावरण बना रहेगा। साथी की भावनाओं का सम्मान करने से रिश्ते में मधुरता बढ़ेगी। मित्रों के साथ सुखद समय व्यतीत करने का अवसर मिलेगा।"
        ),
        // House 2
        listOf(
            "परिवार और जीवनसाथी के साथ सौहार्दपूर्ण संबंध बने रहेंगे। किसी पारिवारिक आयोजन या सुखद बातचीत से मन प्रसन्न रहेगा। साथी के साथ भविष्य की योजनाओं पर सकारात्मक चर्चा होगी।",
            "रिश्तों में मधुरता और परस्पर विश्वास में वृद्धि होगी। जीवनसाथी की छोटी-छोटी खुशियों का ध्यान रखें। अविवाहितों के लिए पारिवारिक माध्यम से सुखद प्रस्ताव आ सकते हैं।",
            "घर-परिवार में उत्सव जैसा वातावरण रहेगा और आपसी प्रेम बढ़ेगा। साथी के विचारों को खुला दिल से स्वीकार करें। प्रेम संबंधों में गंभीरता और आत्मीयता बढ़ेगी।"
        ),
        // House 3
        listOf(
            "रिश्तों में नया उत्साह और खुलापन आएगा। साथी के साथ किसी रोमांचक यात्रा या आउटिंग की योजना बन सकती है। भाई-बहनों के साथ समय बिताना भावनात्मक रूप से संतोषजनक रहेगा।",
            "संवाद के माध्यम से किसी पुरानी गलतफहमी को दूर करने में सफलता मिलेगी। जीवनसाथी आपकी व्यस्त दिनचर्या में पूरा सहयोग देगा। प्रेम संबंधों में परिपक्वता आएगी।",
            "मित्रों और करीबियों का साथ आपके जीवन में खुशियां लाएगा। साथी को अपनी भावनाओं से अवगत कराएं, इससे निकटता बढ़ेगी। पारिवारिक वातावरण खुशनुमा रहेगा।"
        ),
        // House 4
        listOf(
            "दांपत्य जीवन में सुख और आत्मीयता का विस्तार होगा। जीवनसाथी के साथ घर के वातावरण को सुंदर बनाने पर चर्चा होगी। माता-पिता के साथ संबंधों में प्रगाढ़ता आएगी।",
            "घरेलू माहौल में प्यार और आपसी समझ बनी रहेगी। साथी की छोटी-मोटी चिंताओं को समझकर उनका साथ दें। परिवार के साथ किसी मनोरंजक कार्यक्रम का आनंद लेंगे।",
            "पारिवारिक सौहार्द और प्रेम संबंधों में मजबूती आएगी। जीवनसाथी के साथ पुरानी सुखद यादें साझा करेंगे। अविवाहित जातकों को पारिवारिक सहमति से अच्छा प्रस्ताव मिल सकता है।",
        ),
        // House 5
        listOf(
            "प्रेम संबंधों में रोमांस और भावनात्मक प्रगाढ़ता का संचार होगा। साथी के साथ बिताए गए पल यादगार और आनंददायक रहेंगे। अविवाहितों के जीवन में किसी नए व प्रिय व्यक्ति का आगमन हो सकता है।",
            "रिश्तों में नया आकर्षण और गहरा जुड़ाव अनुभव होगा। साथी को कोई सुंदर उपहार या सरप्राइज देने के लिए दिन उत्तम है। पारिवारिक संबंधों में बच्चों की खुशियां केंद्र में रहेंगी।",
            "आपसी समझ और प्यार में वृद्धि होगी। अपनी भावनाओं को खुलकर व्यक्त करें, इससे रिश्ता और अधिक मजबूत होगा। मित्रों और परिजनों का भरपूर स्नेह मिलेगा।"
        ),
        // House 6
        listOf(
            "जीवनसाथी के साथ मिलकर दैनिक जिम्मेदारियों को निभाना रिश्ते को मजबूत करेगा। छोटी-मोटी नोकझोंक से बचें और एक-दूसरे की भावनाओं का सम्मान करें। परिवार में सहयोग का माहौल रहेगा।",
            "रिश्तों में धैर्य और परिपक्वता से काम लें। साथी की जरूरतों को समझें और उनकी सेहत का ध्यान रखें। सेवा और त्याग की भावना से रिश्ते में मिठास आएगी।",
            "परिवार के सदस्यों के साथ बैठकर पुरानी बातों को सुलझाने का समय है। जीवनसाथी का व्यावहारिक दृष्टिकोण आपको मानसिक संबल देगा। आपसी विश्वास बनाए रखें।"
        ),
        // House 7
        listOf(
            "दांपत्य जीवन में अद्भुत सामंजस्य और प्रेम बना रहेगा। जीवनसाथी के साथ किसी विशेष योजना या यात्रा पर विचार हो सकता है। अविवाहित जातकों के लिए विवाह के शुभ प्रस्ताव आने के प्रबल योग हैं।",
            "रिश्तों में आपसी समझ, विश्वास और आकर्षण नई ऊंचाइयों पर रहेगा। साथी के साथ दिल की बातें साझा करने से भावनात्मक निकटता बढ़ेगी। पारिवारिक माहौल खुशनुमा रहेगा।",
            "प्रेम और वैवाहिक संबंधों में स्थिरता व मधुरता का संचार होगा। एक-दूसरे की उपलब्धियों का जश्न मनाएंगे। सामाजिक स्तर पर आपकी जोड़ी की सराहना होगी।"
        ),
        // House 8
        listOf(
            "रिश्तों में भावनात्मक गहराई और एक-दूसरे के प्रति समर्पण बढ़ेगा। संवेदनशील मुद्दों पर शांत रहकर बातचीत करें। अपने साथी के साथ विश्वास को सबसे अधिक प्राथमिकता दें।",
            "जीवनसाथी के साथ अपने आंतरिक विचारों और चिंताओं को साझा करने से राहत मिलेगी। किसी भी संदेह या गलतफहमी को तुरंत दूर करें। रिश्तों में ईमानदारी बनाए रखें।",
            "प्रेम संबंधों में नया मोड़ आ सकता है जो आपको और अधिक करीब लाएगा। साथी के सहयोग से आप कठिन परिस्थितियों का सामना आसानी से कर पाएंगे। धैर्य से काम लें।"
        ),
        // House 9
        listOf(
            "पारिवारिक और प्रेम संबंधों में आध्यात्मिक शांति और खुशी का वास रहेगा। साथी के साथ किसी धार्मिक स्थल या दर्शनीय स्थल की यात्रा का योग बन सकता है। बड़ों का आशीर्वाद मिलेगा।",
            "जीवनसाथी के साथ वैचारिक तालमेल बहुत सुंदर रहेगा। एक-दूसरे के जीवन लक्ष्यों का समर्थन करेंगे। अविवाहितों के लिए उच्च कुल से रिश्ते के प्रस्ताव आ सकते हैं।",
            "रिश्तों में नैतिक मूल्य और परस्पर आदर का भाव बढ़ेगा। परिवार के साथ ज्ञानवर्धक व सुखद समय व्यतीत होगा। प्रेम संबंधों में प्रगाढ़ता आएगी।"
        ),
        // House 10
        listOf(
            "करियर की व्यस्तता के बावजूद परिवार और जीवनसाथी के लिए समय निकालना आवश्यक रहेगा। साथी आपकी उपलब्धियों पर गर्व महसूस करेगा। दांपत्य जीवन में सामंजस्य बना रहेगा।",
            "पारिवारिक जिम्मेदारियों और पेशेवर जीवन में संतुलन बनाने के आपके प्रयास सफल होंगे। जीवनसाथी का संबल आपको नई ऊर्जा देगा। सामाजिक प्रतिष्ठा में वृद्धि होगी।",
            "रिश्तों में परिपक्वता और आपसी समझदारी से काम लें। साथी की भावनाओं को प्राथमिकता दें और उनकी सलाह को महत्व दें। शाम का समय अपनों के साथ आनंदपूर्वक बीतेगा।"
        ),
        // House 11
        listOf(
            "मित्रों और परिजनों के सहयोग से जीवन में खुशियों का संचार होगा। साथी के साथ सामाजिक आयोजनों में भाग लेने का अवसर मिलेगा। प्रेम संबंधों में नया उत्साह और विश्वास रहेगा।",
            "पारिवारिक माहौल बहुत खुशनुमा और उत्सवपूर्ण रहेगा। किसी पुराने मित्र या रिश्तेदार से सुखद मुलाकात हो सकती है। जीवनसाथी के साथ भविष्य की योजनाओं पर सहमति बनेगी।",
            "रिश्तों में आत्मीयता, मस्ती और नयापन बना रहेगा। साथी के साथ मिलकर किसी बड़े लक्ष्य की दिशा में कदम बढ़ाएंगे। अविवाहितों के लिए मित्रों के जरिए नए संपर्क बनेंगे।"
        ),
        // House 12
        listOf(
            "साथी के साथ एकांत में शांतिपूर्ण समय बिताना मन को सुकून देगा। पुरानी कड़वाहट को भुलाकर एक नई शुरुआत करने का समय है। जीवनसाथी की भावनात्मक जरूरतों को समझें।",
            "रिश्तों में त्याग और निस्वार्थ प्रेम की भावना प्रबल रहेगी। दूर रह रहे प्रियजनों से सुखद संवाद होगा। किसी भी प्रकार की गलतफहमी से बचने के लिए पारदर्शी रहें।",
            "पारिवारिक शांति बनाए रखने के लिए धैर्य का परिचय दें। साथी के साथ ध्यान या शांत वातावरण में समय बिताएं। प्रेम संबंधों में आंतरिक गहराई बढ़ेगी।"
        )
    )

    private val LOVE_EN = listOf(
        // House 1
        listOf(
            "Harmony prevails at home, and your partner provides steady emotional support {p}. Meaningful conversations with loved ones will uplift your spirits. A gentle, empathetic approach helps resolve any past misunderstandings.",
            "Fresh enthusiasm and warmth enter your personal relationships {p}. Spending quality time with your partner strengthens emotional closeness. Eligible singles may receive encouraging news regarding marriage prospects.",
            "Mutual respect and affection define your household atmosphere {p}. Valuing your partner's viewpoint fosters deeper intimacy and trust. An evening spent catching up with close friends brings joy and relaxation."
        ),
        // House 2
        listOf(
            "Warm and harmonious relations flourish with your spouse and kin {p}. A family gathering or heartwarming conversation uplifts everyone. Meaningful discussions about future goals deepen mutual trust.",
            "Sweetness and mutual trust grow across all personal relationships {p}. Caring for your partner's small needs fosters intimacy. Eligible natives may receive promising alliance proposals through family networks.",
            "A joyful and supportive atmosphere prevails within the home {p}. Embracing your partner's perspective with an open heart strengthens your bond. Romance gains depth and genuine emotional security."
        ),
        // House 3
        listOf(
            "Spontaneity and open communication breathe fresh warmth into relationships {p}. Planning an exciting outing or mini-trip with your partner creates joyful memories. Sibling bonds remain affectionate.",
            "Clear, honest conversation helps iron out any previous misunderstandings {p}. Your spouse understands and supports your busy schedule. Romantic relationships gain maturity and stability.",
            "The companionship of close friends and family brings cheer {p}. Sharing your authentic thoughts with your partner deepens intimacy. The domestic mood remains upbeat and lively."
        ),
        // House 4
        listOf(
            "Marital life experiences warmth, affection, and mutual care {p}. Discussing home improvements with your spouse creates shared joy. Relations with parents grow closer and more loving.",
            "Love and understanding illuminate your household {p}. Being attentive to your partner's emotional needs deepens connection. Enjoying a relaxed evening with family brings immense joy.",
            "Domestic unity and romantic bonds strengthen significantly {p}. Sharing cherished memories with your partner sparks nostalgic warmth. Singles may receive promising proposals backed by family approval."
        ),
        // House 5
        listOf(
            "Romance and emotional vibrancy blossom delightfully in your love life {p}. Moments spent together with your companion will be deeply cherished and memorable. Singles might encounter someone truly special.",
            "A magnetic charm and tender connection infuse your romantic bond {p}. A thoughtful gift or romantic gesture will delight your partner. Domestic joy centers around the achievements of young ones.",
            "Mutual empathy and affectionate understanding flourish {p}. Expressing your heartfelt emotions candidly deepens your bond. Warm support from family and friends enriches your day."
        ),
        // House 6
        listOf(
            "Sharing day-to-day household chores with your spouse strengthens mutual respect {p}. Steer clear of petty bickering and honor each other's feelings. Cooperation defines the family mood.",
            "Approach romantic and family relationships with maturity and patience {p}. Understand your companion's needs and attend to their well-being. A spirit of caring nurtures enduring affection.",
            "An opportune moment to sit with family members and resolve pending differences amicably {p}. Your partner's practical perspective provides comforting clarity. Nurture mutual confidence."
        ),
        // House 7
        listOf(
            "Exceptional harmony, mutual devotion, and romance grace your marital bond {p}. You and your partner may plan an inspiring trip or celebrate a shared milestone. Singles enjoy bright marriage prospects.",
            "Understanding, trust, and mutual fascination reach inspiring heights {p}. Heart-to-heart exchanges bring profound emotional proximity. A festive and joyful aura fills your home.",
            "Stability and tenderness flow effortlessly through your romantic life {p}. Celebrate each other's personal and professional accomplishments. Social circles admire your harmonious bond."
        ),
        // House 8
        listOf(
            "Emotional depth and profound mutual commitment enrich your relationship {p}. Address delicate matters with calm, mindful understanding. Prioritize unwavering trust above all else.",
            "Sharing your inner thoughts and vulnerabilities with your partner brings therapeutic relief {p}. Dispel any lurking misunderstandings right away. Uphold honesty in love.",
            "A meaningful transition in romance brings you closer than ever {p}. With your companion's steadfast support, navigating external turbulence becomes easy. Proceed with patience and faith."
        ),
        // House 9
        listOf(
            "Spiritual peace and heartfelt contentment grace your personal and family connections {p}. You and your partner may embark on a sacred pilgrimage or scenic retreat. Elders shower their blessings.",
            "Intellectual and philosophical alignment with your companion is seamless {p}. You actively champion each other's life ambitions. Eligible natives may receive high-stature alliance proposals.",
            "High ethical principles and mutual esteem elevate your partnership {p}. Quality time spent with loved ones is rich with joyful reflections. Romantic commitments gain strength."
        ),
        // House 10
        listOf(
            "Despite a bustling work calendar, carving out devoted time for your companion is vital {p}. Your partner takes immense pride in your accomplishments. Marital understanding stays intact.",
            "Your conscientious efforts to balance professional duties with family needs prove successful {p}. Your spouse's support revitalizes your energy. Your family's public standing is elevated.",
            "Handle personal dynamics with emotional maturity and sensitivity {p}. Value your companion's viewpoints and feelings. The evening promises relaxing, warm moments with loved ones."
        ),
        // House 11
        listOf(
            "Social gatherings and reunions with close kin and friends radiate happiness {p}. Attending social events with your partner deepens shared joy. Love life is energized with optimism and trust.",
            "A celebratory and joyful atmosphere enlivens your household {p}. Heartwarming reunions with longtime companions may unfold. Alignment with your partner regarding future plans is effortless.",
            "Warmth, playful spontaneity, and genuine affection define your relationships {p}. Teaming up with your companion to chase a shared ambition brings excitement. Singles make charming new connections."
        ),
        // House 12
        listOf(
            "Spending tranquil, unhurried time in quiet settings with your partner restores your spirit {p}. Let go of past grievances and embrace a gentle fresh start. Be attentive to your partner's emotional needs.",
            "Selfless devotion and pure affection guide your interactions {p}. Warm communications with loved ones across distances bring comfort. Maintain open transparency to avoid misconceptions.",
            "Cultivate calm patience to nurture domestic tranquility {p}. Sharing quiet contemplative moments or meditation with your companion deepens the spiritual essence of your love."
        )
    )

    // =========================================================================
    // 5. HEALTH & WELLNESS READINGS (12 Houses x 3 Variations)
    // =========================================================================
    private val HEALTH_HI = listOf(
        // House 1
        listOf(
            "शारीरिक ऊर्जा का स्तर उच्च रहेगा और ताजगी बनी रहेगी। नियमित योग व प्राणायाम से मानसिक शांति बनाए रखें।",
            "स्वास्थ्य सामान्यतः अच्छा रहेगा, दिनचर्या में अनुशासन रखें। भरपूर पानी पिएं और पर्याप्त विश्राम को प्राथमिकता दें।",
            "ऊर्जावान महसूस करेंगे, लेकिन दिन के उत्तरार्ध में आंखों और सिर को आराम दें। हल्का और सुपाच्य भोजन लें।"
        ),
        // House 2
        listOf(
            "गले व दांतों की स्वच्छता पर विशेष ध्यान दें। संतुलित व सात्विक आहार लें और ठंडी चीजों के अत्यधिक सेवन से बचें।",
            "स्वास्थ्य उत्तम रहेगा और मानसिक शांति बनी रहेगी। दिनचर्या में सुबह की सैर या हल्का व्यायाम शामिल करें।",
            "ऊर्जा का स्तर अच्छा रहेगा, भोजन में ताजे फल और पर्याप्त जल शामिल करें। तनावमुक्त रहने के लिए संगीत सुनें।"
        ),
        // House 3
        listOf(
            "कंधों और बाहों के खिंचाव से बचने के लिए स्ट्रेचिंग करें। ऊर्जा का स्तर अच्छा रहेगा, शाम को हल्का टहलें।",
            "मानसिक ताजगी बनी रहेगी, लेकिन अत्यधिक भागदौड़ से थकान संभव है। पर्याप्त नींद लें और समय पर भोजन करें।",
            "समग्र स्वास्थ्य अनुकूल रहेगा। नियमित योग और प्राणायाम से श्वसन तंत्र को सुदृढ़ बनाएं।"
        ),
        // House 4
        listOf(
            "छाती और फेफड़ों की देखभाल करें, ठंडी हवा से बचें। मानसिक शांति के लिए ध्यान और प्राणायाम करें।",
            "स्वास्थ्य सामान्य रहेगा, पर्याप्त विश्राम को अपनी प्राथमिकता बनाएं। घर का बना शुद्ध और ताजा भोजन लें।",
            "शारीरिक व मानसिक शांति का अनुभव होगा। शाम को हल्का ध्यान या टहलना ऊर्जा को संतुलित रखेगा।"
        ),
        // House 5
        listOf(
            "पेट और पाचन तंत्र का ध्यान रखें, जंक फूड से परहेज करें। मानसिक एकाग्रता बहुत अच्छी रहेगी, योग करें।",
            "ऊर्जा व उत्साह बना रहेगा। खेलकूद या व्यायाम में समय बिताना आपके स्वास्थ्य को बेहतर बनाएगा।",
            "मानसिक स्वास्थ्य उत्तम रहेगा। नियमित दिनचर्या और पर्याप्त पानी का सेवन आपको तरोताजा रखेगा।"
        ),
        // House 6
        listOf(
            "मौसम में बदलाव के प्रति सतर्क रहें और खानपान में स्वच्छता बरतें। नियमित दिनचर्या और व्यायाम से रोग प्रतिरोधक क्षमता बढ़ेगी।",
            "पाचन संबंधी छोटी-मोटी समस्या हो सकती है, हल्का व सुपाच्य भोजन लें। तनाव से बचने के लिए गहरी सांस लेने का अभ्यास करें।",
            "स्वास्थ्य को प्राथमिकता दें और समय पर दवा व जांच का ध्यान रखें। पर्याप्त नींद और विश्राम से स्फूर्ति बनी रहेगी।"
        ),
        // House 7
        listOf(
            "कमर और गुर्दों की सेहत के लिए पर्याप्त पानी पिएं। दिनचर्या में संतुलन रखें और अति-श्रम से बचें।",
            "स्वास्थ्य अनुकूल रहेगा, मन में सकारात्मक विचार बनाए रखें। साथी के साथ टहलना ताजगी देगा।",
            "शारीरिक ऊर्जा अच्छी रहेगी। खानपान में हरी सब्जियां और फल शामिल करें ताकि ताजगी बनी रहे।"
        ),
        // House 8
        listOf(
            "ड्राइविंग करते समय सावधानी बरतें और भारी वजन उठाने से बचें। ध्यान और प्राणायाम से मानसिक तनाव को दूर करें।",
            "पुरानी बीमारी या दर्द में राहत के लिए नियमित दिनचर्या अपनाएं। पर्याप्त नींद लें और चिंतामुक्त रहें।",
            "ऊर्जा के स्तर में उतार-चढ़ाव हो सकता है, शरीर को आवश्यक आराम दें। सात्विक आहार ग्रहण करें।"
        ),
        // House 9
        listOf(
            "जांघों और जोड़ों के लचीलेपन के लिए हल्का व्यायाम करें। यात्रा के दौरान खानपान की शुद्धता का ध्यान रखें।",
            "स्वास्थ्य बहुत उत्तम रहेगा और सकारात्मक ऊर्जा का संचार होगा। सुबह की धूप में समय बिताना लाभकारी रहेगा।",
            "मानसिक व शारीरिक स्फूर्ति बनी रहेगी। प्रकृति के बीच कुछ समय बिताने से मन शांत रहेगा।"
        ),
        // House 10
        listOf(
            "घुटनों और जोड़ों के दर्द से बचने के लिए बैठने का सही पोस्चर रखें। कार्य के बीच में छोटे ब्रेक अवश्य लें।",
            "काम के दबाव के कारण थकान महसूस हो सकती है, पर्याप्त विश्राम करें। संतुलित आहार लें और तनाव से बचें।",
            "स्वास्थ्य सामान्य रहेगा। शाम को योग या हल्की सैर करने से दिनभर की थकान दूर हो जाएगी।"
        ),
        // House 11
        listOf(
            "पैरों और पिंडलियों की मालिश से आराम मिलेगा। ऊर्जा का स्तर उत्कृष्ट रहेगा, सामाजिक रूप से सक्रिय रहें।",
            "समग्र स्वास्थ्य बहुत अच्छा रहेगा और उत्साह बना रहेगा। नियमित व्यायाम से फिटनेस का स्तर बनाए रखें।",
            "शारीरिक स्फूर्ति और मानसिक प्रसन्नता बनी रहेगी। संतुलित खानपान और पर्याप्त नींद को अपनाएं।"
        ),
        // House 12
        listOf(
            "पैरों और आंखों को आराम दें, स्क्रीन टाइम कम करें। गहरी और आरामदायक नींद के लिए सोने से पहले ध्यान करें।",
            "मानसिक शांति के लिए एकांत और ध्यान का सहारा लें। पैरों की देखभाल करें और भरपूर पानी पिएं।",
            "स्वास्थ्य सामान्य रहेगा, अत्यधिक सोचने से बचें। सात्विक व हल्का भोजन आपके पाचन को स्वस्थ रखेगा।"
        )
    )

    private val HEALTH_EN = listOf(
        // House 1
        listOf(
            "Physical vitality and energy levels stay high {p}. Practicing yoga or mindful breathing will keep your mental focus sharp.",
            "Overall health remains sound with balanced vitality {p}. Stay hydrated throughout the day and ensure you get restorative rest.",
            "You feel energetic throughout the day, though giving your eyes and head brief rests is advised. Opt for light, nutritious meals."
        ),
        // House 2
        listOf(
            "Pay attention to throat and oral wellness. Stick to a balanced diet and avoid excessive cold or greasy foods.",
            "Health stays sound with steady mental serenity {p}. Incorporating morning walks or light workouts enhances stamina.",
            "Energy levels remain good; include fresh fruits and adequate hydration. Listening to soothing music helps you stay relaxed."
        ),
        // House 3
        listOf(
            "Do gentle stretches to relieve any stiffness in shoulders or arms. Vitality stays good; take an evening walk.",
            "Mental energy is brisk, though physical hustle may cause mild fatigue. Ensure adequate sleep and timely meals.",
            "Overall health is favorable {p}. Regular breathing exercises and yoga strengthen your vitality and stamina."
        ),
        // House 4
        listOf(
            "Protect your chest and lungs from sudden chill. Meditation and deep breathing help maintain mental peace.",
            "Health remains stable; prioritize restorative sleep. Enjoy freshly prepared, wholesome home-cooked meals.",
            "Experience a soothing sense of physical and mental calm {p}. Evening mindfulness keeps your energy balanced."
        ),
        // House 5
        listOf(
            "Care for your digestive wellness; steer clear of heavy fast food. Mental focus is sharp; gentle yoga keeps you centered.",
            "High stamina and enthusiasm define your physical state {p}. Engaging in recreational sports or fitness refreshes your spirit.",
            "Mental well-being is excellent. Maintaining disciplined daily routines and plentiful water intake keeps you vibrant."
        ),
        // House 6
        listOf(
            "Stay attentive to weather transitions and maintain dietary hygiene. Consistent workouts enhance your immunity.",
            "Minor digestive sensitivities may arise; choose light, wholesome meals. Deep breathing exercises reduce daily stress.",
            "Prioritize physical wellness and maintain routine checkups or medication. Restorative sleep keeps you recharged."
        ),
        // House 7
        listOf(
            "Ensure ample hydration to support kidney and lower back health. Balance your schedule and avoid overexertion.",
            "Health is favorable with positive mental vigor. An evening stroll with your partner provides refreshing vitality.",
            "Physical stamina is strong. Incorporate leafy greens and seasonal fruits to preserve your youthful radiance."
        ),
        // House 8
        listOf(
            "Exercise caution while driving and avoid lifting excessive weight. Meditation helps release residual emotional tension.",
            "Adopting a structured routine helps alleviate lingering aches. Ensure sound sleep and let go of unnecessary anxieties.",
            "Energy levels may fluctuate; grant your body adequate recovery time. Wholesome, nourishing meals keep you grounded."
        ),
        // House 9
        listOf(
            "Practice gentle stretches for hip and joint flexibility. Maintain dietary hygiene while on the move or traveling.",
            "Health is flourishing with vibrant positive energy. Soaking in gentle morning sunlight boosts your vitality.",
            "Mental and physical agility remain high. Spending time amidst nature brings profound calm to your senses."
        ),
        // House 10
        listOf(
            "Maintain ergonomic posture to protect knees and joints during work. Take brief walking breaks throughout your day.",
            "Heavy work commitments could cause mild tiredness; ensure adequate rest. Balanced nutrition helps sustain your drive.",
            "General health remains good. An evening yoga session or leisurely walk melts away the day's fatigue."
        ),
        // House 11
        listOf(
            "Gentle calf massages relieve any leg fatigue. Vitality levels are excellent; stay active and socially engaged.",
            "Overall health is stellar, radiating cheerful enthusiasm. A steady exercise regimen keeps your fitness at its peak.",
            "Physical vigor and mental joy stay strong {p}. Pair balanced nutrition with restorative sleep for lasting vitality."
        ),
        // House 12
        listOf(
            "Rest your feet and eyes; reduce excessive screen exposure. Meditate before bedtime to ensure deep, restorative sleep.",
            "Embrace quiet reflection and meditation to maintain mental peace. Stay well hydrated and pamper your feet.",
            "Health is stable; avoid overthinking. Nourishing, easily digestible meals keep your metabolism balanced."
        )
    )

    // =========================================================================
    // PUBLIC API
    // =========================================================================

    /**
     * Computes the complete horoscope readings for all 12 signs for the given period.
     * @param period "TODAY", "WEEK", or "MONTH"
     */
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

    /** Backward-compatible default — used by MainViewModel / initial states. */
    fun getDailyHoroscope(): List<RashifalData> = getHoroscope("TODAY")

    private fun generateRashiData(
        id: Int, en: String, hi: String, sym: String, elem: String, ruler: String, period: String, today: Date
    ): RashifalData {
        val stones = listOf(
            "नीलम (Blue Sapphire)", "पुखराज (Yellow Sapphire)", "पन्ना (Emerald)",
            "मूंगा (Red Coral)", "मोती (Pearl)", "माणिक्य (Ruby)", "हीरा (Diamond)"
        )
        val colorsHi = listOf(
            "लाल व गुलाबी", "सफेद व सिल्वर", "हरा व फिरोजी", "पीला व केसरिया", "नीला व आसमानी"
        )
        val colorsEn = listOf(
            "Red & Pink", "White & Silver", "Green & Turquoise", "Yellow & Saffron", "Blue & Sky Blue"
        )
        val luckyTimesHi = listOf(
            "प्रातः 07:30 - 09:00",
            "प्रातः 09:15 - 10:45",
            "दोपहर 11:30 - 01:00",
            "अपराह्न 02:15 - 03:45",
            "सायं 05:00 - 06:30",
            "सायं 07:15 - 08:45"
        )
        val luckyTimesEn = listOf(
            "07:30 AM - 09:00 AM",
            "09:15 AM - 10:45 AM",
            "11:30 AM - 01:00 PM",
            "02:15 PM - 03:45 PM",
            "05:00 PM - 06:30 PM",
            "07:15 PM - 08:45 PM"
        )

        val rashiIdx = id - 1 // 0-based
        val driverPlanet = TransitCalculator.driverPlanetForPeriod(period)
        val house = TransitCalculator.getDriverHouse(rashiIdx, period, today) // 1-12 calculated
        val planetHi = TransitCalculator.planetNameHi(driverPlanet)
        val houseIdx = (house - 1).coerceIn(0, 11)

        val periodHi = TransitCalculator.periodWordHi(period)
        val periodEn = TransitCalculator.periodWordEn(period)
        val periodEnCap = TransitCalculator.periodWordEnCap(period)

        // Rotation variation index based on period date
        val cal = Calendar.getInstance().apply { time = today }
        val variationIdx = when (period.uppercase()) {
            "WEEK" -> (cal.get(Calendar.WEEK_OF_YEAR) + rashiIdx) % 3
            "MONTH" -> (cal.get(Calendar.MONTH) + rashiIdx) % 3
            else -> (cal.get(Calendar.DAY_OF_MONTH) + rashiIdx) % 3
        }

        // Deterministic picks based on house + rashi + date
        val colorIdx = (rashiIdx + house) % colorsHi.size
        val stoneIdx = (rashiIdx * 3 + house) % stones.size
        val rating = 3 + ((rashiIdx + house) % 3) // 3..5
        val luckyNum = 1 + ((rashiIdx * 7 + house * 3) % 9)
        val timeIdx = (rashiIdx * 5 + house + cal.get(Calendar.DAY_OF_MONTH)) % luckyTimesHi.size

        // These arrive combined — "अग्नि (Fire)", "मंगल (Mars)", "मेष (Aries)" — so
        // split them into the Hindi and English halves the model asks for.
        fun hiPart(v: String) = v.substringBefore(" (").trim()
        fun enPart(v: String) = v.substringAfter("(", "").substringBefore(")").trim().ifBlank { v }

        return RashifalData(
            rashiId = id,
            rashiNameEn = en,
            rashiNameHi = hiPart(hi),
            symbol = sym,
            elementHi = hiPart(elem),
            elementEn = enPart(elem),
            rulerHi = hiPart(ruler),
            rulerEn = enPart(ruler),
            ratingStars = rating,
            luckyNumber = luckyNum,
            luckyColorEn = colorsEn[colorIdx],
            luckyColorHi = colorsHi[colorIdx],
            luckyStoneHi = stones[stoneIdx],
            luckyTimeHi = luckyTimesHi[timeIdx],
            luckyTimeEn = luckyTimesEn[timeIdx],
            generalReadingHi = GENERAL_HI[houseIdx][variationIdx].replace("{planet}", planetHi).replace("{P}", periodHi),
            generalReadingEn = GENERAL_EN[houseIdx][variationIdx].replace("{planet}", driverPlanet).replace("{p}", periodEn).replace("{Pcap}", periodEnCap),
            careerReadingHi = CAREER_HI[houseIdx][variationIdx].replace("{P}", periodHi),
            careerReadingEn = CAREER_EN[houseIdx][variationIdx].replace("{p}", periodEn).replace("{Pcap}", periodEnCap),
            healthReadingHi = HEALTH_HI[houseIdx][variationIdx].replace("{P}", periodHi),
            healthReadingEn = HEALTH_EN[houseIdx][variationIdx].replace("{p}", periodEn).replace("{Pcap}", periodEnCap),
            loveReadingHi = LOVE_HI[houseIdx][variationIdx].replace("{P}", periodHi),
            loveReadingEn = LOVE_EN[houseIdx][variationIdx].replace("{p}", periodEn).replace("{Pcap}", periodEnCap),
            financeReadingHi = FINANCE_HI[houseIdx][variationIdx].replace("{P}", periodHi),
            financeReadingEn = FINANCE_EN[houseIdx][variationIdx].replace("{p}", periodEn).replace("{Pcap}", periodEnCap),
            period = period
        )
    }
}
