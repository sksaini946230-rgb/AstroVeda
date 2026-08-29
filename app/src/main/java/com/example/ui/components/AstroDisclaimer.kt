package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GlassCardBorder
import com.example.ui.theme.SurfaceBackground
import com.example.util.LanguageManager

/**
 * The standing disclaimer.
 *
 * The app had none — not on screen, not in the privacy policy, not in the terms —
 * while telling people they are Manglik, warning that a Nadi Dosha "impacts
 * genetic harmony and child wellbeing", and running an open-ended AI astrologer.
 * People make marriage decisions on these screens.
 *
 * [DisclaimerScope] picks the wording, because a Panchang and a match report
 * warrant different sentences. Keep it quiet and small: it should read as an
 * honest footnote, not a warning label.
 */
enum class DisclaimerScope {
    /** Panchang, Muhurat, Choghadiya — computed timings. */
    TIMINGS,

    /** Kundali, Dasha, Rashifal, Numerology — interpretation. */
    READING,

    /** Guna Milan — the one people act on hardest. */
    MATCHING,

    /** The AI astrologer. */
    AI
}

@Composable
fun AstroDisclaimer(
    scope: DisclaimerScope,
    modifier: Modifier = Modifier
) {
    val text = when (scope) {
        DisclaimerScope.TIMINGS -> LanguageManager.getString(
            "ये समय खगोलीय गणना से निकाले गए हैं और आपके चुने हुए शहर पर आधारित हैं। " +
                "स्थानीय पंचांग या परंपरा से थोड़ा अंतर हो सकता है।",
            "These timings are computed astronomically for your selected city. " +
                "A local almanac or tradition may differ slightly."
        )

        DisclaimerScope.READING -> LanguageManager.getString(
            "यह पारंपरिक ज्योतिष पर आधारित मार्गदर्शन है, कोई पेशेवर सलाह नहीं। " +
                "स्वास्थ्य, कानूनी या आर्थिक निर्णयों के लिए योग्य विशेषज्ञ से ही परामर्श लें।",
            "This is guidance based on traditional astrology, not professional advice. " +
                "For health, legal or financial decisions, please consult a qualified expert."
        )

        DisclaimerScope.MATCHING -> LanguageManager.getString(
            "गुण मिलान पारंपरिक ज्योतिष की एक पद्धति है — यह किसी रिश्ते की सफलता या " +
                "असफलता का निर्णय नहीं है। कम गुण आने पर विवाह न करने का कोई कारण नहीं बनता, " +
                "और अधिक गुण आने पर किसी को हाँ कहने का दबाव भी नहीं। यह निर्णय आपका है।",
            "Guna Milan is one traditional method — it is not a verdict on whether a " +
                "relationship will work. A low score is not a reason to refuse a marriage, " +
                "and a high one is no reason to accept. The decision is yours."
        )

        // Kept deliberately short and quiet. It does not name a vendor or a model,
        // and it does not lead with the word "AI" — but it does say the answers are
        // generated, because letting someone believe a human astrologer replied
        // would be a false impression, and the helpline has to survive for the
        // person who is actually in trouble.
        DisclaimerScope.AI -> LanguageManager.getString(
            "उत्तर स्वतः तैयार होते हैं; त्रुटि संभव है। संकट में हों तो Tele-MANAS 14416।",
            "Answers are generated automatically and can be wrong. In distress: Tele-MANAS 14416."
        )
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(SurfaceBackground)
            .border(1.dp, GlassCardBorder, RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = Icons.Outlined.Info,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
                lineHeight = 16.sp,
                textAlign = TextAlign.Start
            )
        )
    }
}
