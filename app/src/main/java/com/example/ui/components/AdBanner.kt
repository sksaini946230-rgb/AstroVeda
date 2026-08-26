package com.example.ui.components

import android.view.View
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

@Composable
fun AdBanner(
    onRemoveAdsClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val bannerId = remember {
        try {
            val id = com.example.BuildConfig.ADMOB_BANNER_ID
            if (id.isNotBlank()) id
            else if (com.example.BuildConfig.DEBUG) "ca-app-pub-3940256099942544/6300978111"
            else ""
        } catch (e: Throwable) {
            if (com.example.BuildConfig.DEBUG) "ca-app-pub-3940256099942544/6300978111" else ""
        }
    }

    var adFailedToLoad by remember { mutableStateOf(false) }

    if (adFailedToLoad || bannerId.isBlank()) {
        // Fail gracefully (show nothing)
        Box(modifier = Modifier.size(0.dp))
    } else {
        AndroidView(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .testTag("ad_banner_container"),
            factory = { context ->
                try {
                    AdView(context).apply {
                        setAdSize(AdSize.BANNER)
                        adUnitId = bannerId
                        val adRequest = AdRequest.Builder().build()
                        adListener = object : AdListener() {
                            override fun onAdFailedToLoad(error: LoadAdError) {
                                super.onAdFailedToLoad(error)
                                adFailedToLoad = true
                            }
                        }
                        loadAd(adRequest)
                    }
                } catch (e: Throwable) {
                    adFailedToLoad = true
                    View(context)
                }
            },
            update = { adView ->
                // No update needed
            }
        )
    }
}
