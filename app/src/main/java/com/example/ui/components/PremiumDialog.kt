package com.example.ui.components

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.MainViewModel
import com.example.ui.theme.ElevatedSurface
import com.example.ui.theme.PrimaryButtonPressed
import com.example.ui.theme.PrimaryButtonText
import com.example.ui.theme.ProBadgeColor
import com.example.ui.theme.ShubhSuccessColor

@Composable
fun PremiumDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val billingError by viewModel.billingErrorMessage.collectAsState()

    // Show toast for error/status messages gracefully
    LaunchedEffect(billingError) {
        billingError?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = ElevatedSurface,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, ProBadgeColor, RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_premium_dialog")) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(ProBadgeColor, PrimaryButtonPressed)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = PrimaryButtonText, modifier = Modifier.size(32.dp))
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "AstroVeda PRO Gold",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = ProBadgeColor,
                        fontSize = 22.sp
                    )
                )
                Text(
                    text = "Unlock Complete Vedic Wisdom & AI Features",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    FeatureRow("100% Ad-Free Glassmorphism Experience")
                    FeatureRow("Unlimited Gemini 1.5 AI Astrologer Queries")
                    FeatureRow("HD PDF Kundali Export & Printing")
                    FeatureRow("Swiss Ephemeris Micro-Second Precision")
                    FeatureRow("Unlimited Saved Kundali Profiles in Room DB")
                }
                Spacer(modifier = Modifier.height(20.dp))
                GoldGlowButton(
                    text = "प्रारंभ करें ₹199/वर्ष (Get PRO Gold)",
                    onClick = {
                        val activity = context as? Activity
                        if (activity != null) {
                            viewModel.makePurchase(activity)
                        } else {
                            Toast.makeText(context, "Billing error: Activity context not available", Toast.LENGTH_SHORT).show()
                        }
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "subscribe_pro_gold_button"
                )
            }
        }
    }
}

@Composable
fun FeatureRow(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = ShubhSuccessColor, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall.copy(
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Normal,
                fontSize = 13.sp
            )
        )
    }
}
