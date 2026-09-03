package com.example.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.service.AdsInitState
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import kotlinx.coroutines.delay

/**
 * The banner.
 *
 * It used to give up permanently on its first failure: one `onAdFailedToLoad`
 * set a flag that was never cleared, so a transient no-fill — or simply asking
 * before the ads SDK had finished initialising, which is what happens on a cold
 * start — left the app with no banner for the rest of the session. That is how
 * the ads went quiet.
 *
 * Now it waits until the SDK is actually up, and a failure is retried a few
 * times with a widening gap before it gives up quietly. Giving up still shows
 * nothing rather than an empty grey box.
 */
@Composable
fun AdBanner(
    onRemoveAdsClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val bannerId = remember {
        try {
            val id = app.revati.jyotish.BuildConfig.ADMOB_BANNER_ID
            if (id.isNotBlank()) id
            else if (app.revati.jyotish.BuildConfig.DEBUG) TEST_BANNER_ID
            else ""
        } catch (e: Throwable) {
            if (app.revati.jyotish.BuildConfig.DEBUG) TEST_BANNER_ID else ""
        }
    }

    val adsReady by AdsInitState.ready.collectAsState()

    // Bumping this rebuilds the AdView, which is what actually re-requests.
    var attempt by remember { mutableIntStateOf(0) }
    var failures by remember { mutableIntStateOf(0) }
    var givenUp by remember { mutableStateOf(false) }

    LaunchedEffect(failures) {
        if (failures == 0) return@LaunchedEffect
        if (failures > MAX_RETRIES) {
            givenUp = true
            return@LaunchedEffect
        }
        // 4s, 12s, 36s — long enough for a network to come back, short enough
        // that a user who opened the app on a bad signal still sees a banner.
        delay(RETRY_BASE_MS * THREE_POW[failures - 1])
        attempt++
    }

    if (bannerId.isBlank() || givenUp || !adsReady) {
        Box(modifier = Modifier.size(0.dp))
        return
    }

    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .testTag("ad_banner_container"),
        // The key is what forces a fresh AdView per attempt; without it the
        // retry would recycle the view that already failed.
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                adUnitId = bannerId
                adListener = object : AdListener() {
                    override fun onAdFailedToLoad(error: LoadAdError) {
                        failures++
                    }

                    override fun onAdLoaded() {
                        failures = 0
                    }
                }
                loadAd(AdRequest.Builder().build())
            }
        },
        update = { adView ->
            if (adView.getTag(ATTEMPT_TAG) != attempt) {
                adView.setTag(ATTEMPT_TAG, attempt)
                if (attempt > 0) adView.loadAd(AdRequest.Builder().build())
            }
        },
        onRelease = { it.destroy() }
    )
}

private const val TEST_BANNER_ID = "ca-app-pub-3940256099942544/6300978111"
private const val MAX_RETRIES = 3
private const val RETRY_BASE_MS = 4_000L
private val THREE_POW = longArrayOf(1, 3, 9)
private val ATTEMPT_TAG = "revati_ad_attempt".hashCode()
