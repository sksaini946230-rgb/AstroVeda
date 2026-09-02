package com.example.ui.components

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.MainViewModel
import com.google.android.play.core.review.ReviewManagerFactory

@Composable
fun RateUsDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedStars by remember { mutableIntStateOf(0) }
    var feedbackText by remember { mutableStateOf("") }
    val maxChars = 200

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("rate_us_dialog_surface")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Row with Close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = com.example.util.LanguageManager.getString(
                            "Revati को रेट करें",
                            "Rate Revati"
                        ),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 20.sp
                        )
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_rate_us_dialog")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = com.example.util.LanguageManager.getString("डायलॉग बंद करें", "Close Dialog"),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = com.example.util.LanguageManager.getString(
                        "आपकी राय से हम बेहतर और अधिक सटीक वैदिक ज्योतिष दे पाते हैं!",
                        "Your feedback helps us grow and provide more accurate Vedic astrology insights!"
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Interactive 5 Stars
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (starIndex in 1..5) {
                        val isSelected = starIndex <= selectedStars
                        Icon(
                            imageVector = if (isSelected) Icons.Default.Star else Icons.Outlined.StarBorder,
                            contentDescription = "Rate $starIndex stars",
                            tint = if (isSelected) com.example.ui.theme.StarRatingFilled else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier
                                .size(42.dp)
                                .clickable { selectedStars = starIndex }
                                .testTag("rate_star_$starIndex")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Low rating logic (1 to 3 stars) -> feedback form
                AnimatedVisibility(
                    visible = selectedStars in 1..3,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = com.example.util.LanguageManager.getString(
                                "खेद है कि अनुभव अच्छा नहीं रहा। बताइए हम क्या सुधारें:",
                                "We are sorry you had a sub-par experience. Please tell us how we can improve:"
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        OutlinedTextField(
                            value = feedbackText,
                            onValueChange = { if (it.length <= maxChars) feedbackText = it },
                            placeholder = {
                                Text(
                                    com.example.util.LanguageManager.getString(
                                        "अपने सुझाव यहाँ लिखें...",
                                        "Write your suggestions here..."
                                    ),
                                    fontSize = 14.sp
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .testTag("feedback_text_input"),
                            shape = RoundedCornerShape(12.dp),
                            maxLines = 4,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp, end = 4.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Text(
                                text = "${feedbackText.length}/$maxChars",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                viewModel.submitFeedback(selectedStars, feedbackText)
                                Toast.makeText(
                                    context,
                                    com.example.util.LanguageManager.getString(
                                        "आपकी राय के लिए धन्यवाद! हम इससे ऐप बेहतर बनाएंगे।",
                                        "Thank you for your valuable feedback! We'll use it to improve."
                                    ),
                                    Toast.LENGTH_LONG
                                ).show()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("submit_feedback_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(
                                com.example.util.LanguageManager.getString("सुझाव भेजें", "Submit Feedback"),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // High rating logic (4 or 5 stars) -> store prompt
                AnimatedVisibility(
                    visible = selectedStars >= 4,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = com.example.util.LanguageManager.getString(
                                "आपको Revati पसंद आया, यह जानकर खुशी हुई! 🌟 हमारे काम को सहयोग देने के लिए Play Store पर रेटिंग दें।",
                                "We're thrilled that you are enjoying Revati! 🌟 Please rate us on the Play Store to support our work."
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        Button(
                            onClick = {
                                viewModel.markAsRated()
                                triggerPlayStoreReviewFlow(context)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("rate_on_store_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(
                                com.example.util.LanguageManager.getString(
                                    "Google Play पर रेटिंग दें",
                                    "Rate on Google Play"
                                ),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // No stars selected or general footer dismiss buttons
                if (selectedStars == 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("rate_later_button")
                        ) {
                            Text(
                                com.example.util.LanguageManager.getString("बाद में", "Maybe Later"),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun triggerPlayStoreReviewFlow(context: Context) {
    val manager = ReviewManagerFactory.create(context)
    val request = manager.requestReviewFlow()
    request.addOnCompleteListener { task ->
        if (task.isSuccessful) {
            val reviewInfo = task.result
            val activity = context.findActivity()
            if (activity != null) {
                manager.launchReviewFlow(activity, reviewInfo).addOnCompleteListener {
                    // Review flow completed (or dismissed)
                    Toast.makeText(
                        context,
                        com.example.util.LanguageManager.getString(
                            "Revati को सहयोग देने के लिए धन्यवाद!",
                            "Thank you for supporting Revati!"
                        ),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                launchPlayStoreFallback(context)
            }
        } else {
            launchPlayStoreFallback(context)
        }
    }
}

private fun launchPlayStoreFallback(context: Context) {
    val packageName = context.packageName
    try {
        val playStoreIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
        playStoreIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(playStoreIntent)
    } catch (e: ActivityNotFoundException) {
        val playStoreWebIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName"))
        playStoreWebIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(playStoreWebIntent)
    }
}

private fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
