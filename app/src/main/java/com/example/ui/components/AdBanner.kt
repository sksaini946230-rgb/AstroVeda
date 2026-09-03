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
 * start — left the app with no banner for the rest of the session.
 *
 * The fix is the retry, not a gate. An earlier attempt at this refused to ask
 * until [AdsInitState] said the SDK was up, and that is worse: if the signal
 * never arrives the banner never appears at all, which is a bigger failure than
 * the one being fixed. So this asks straight away and asks again on failure, at
 * 4s, 12s and 36s. The readiness signal only earns an extra attempt when it
 * arrives — it can never hold the banner back.
 *
 * Giving up renders nothing rather than an empty grey strip.
 */
@Composable
fun AdBanner(
    onRemoveAdsClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val bannerId = remember {
        // A debug build never asks for a real ad. Google's policy is explicit
        // that development traffic must use the test units, and a device that
        // spends a day requesting live ads and never clicking one is exactly
        // what invalid-traffic enforcement looks for — the risk is the AdMob
        // account, not a wasted impression. Test units also always fill, so a
        // blank banner on debug means our integration is broken rather than
        // demand being thin.
        if (app.revati.jyotish.BuildConfig.DEBUG) {
            TEST_BANNER_ID
        } else {
            try {
                app.revati.jyotish.BuildConfig.ADMOB_BANNER_ID.takeIf { it.isNotBlank() } ?: ""
            } catch (e: Throwable) {
                ""
            }
        }
    }

    val adsReady by AdsInitState.ready.collectAsState()
    
    // Bumping this re-requests through the update block below.
    var attempt by remember { mutableIntStateOf(0) }
    var failures by remember { mutableIntStateOf(0) }
    var loaded by remember { mutableStateOf(false) }
    var givenUp by remember { mutableStateOf(false) }

    LaunchedEffect(failures) {
        if (failures == 0) return@LaunchedEffect
        if (failures > MAX_RETRIES) {
            givenUp = true
            return@LaunchedEffect
        }
        // 4s, 12s, 36s — long enough for a network or the SDK to come back,
        // short enough that someone who opened the app on a bad signal still
        // ends up with a banner.
        delay(RETRY_BASE_MS * BACKOFF[failures - 1])
        attempt++
    }

    // The SDK finishing initialisation is the most likely reason an early
    // request failed, so it is worth one attempt of its own.
    LaunchedEffect(adsReady) {
        if (adsReady && !loaded && !givenUp) attempt++
    }

    if (bannerId.isBlank() || givenUp) {
        Box(modifier = Modifier.size(0.dp))
        return
    }

    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .testTag("ad_banner_container"),
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                adUnitId = bannerId
                adListener = object : AdListener() {
                    override fun onAdFailedToLoad(error: LoadAdError) {
                        // Worth keeping: a banner that silently declines to
                        // appear is exactly the bug that took a day to find.
                        android.util.Log.w(
                            "AdBanner",
                            "load failed: code=${error.code} domain=${error.domain} " +
                                "msg=${error.message} cause=${error.cause}"
                        )
                        failures++
                    }

                    override fun onAdLoaded() {
                        android.util.Log.i("AdBanner", "loaded")
                        loaded = true
                        failures = 0
                    }
                }
                setTag(ATTEMPT_TAG, 0)
                loadAd(AdRequest.Builder().build())
            }
        },
        update = { adView ->
            if (adView.getTag(ATTEMPT_TAG) != attempt) {
                adView.setTag(ATTEMPT_TAG, attempt)
                adView.loadAd(AdRequest.Builder().build())
            }
        },
        onRelease = { it.destroy() }
    )
}

private const val TEST_BANNER_ID = "ca-app-pub-3940256099942544/6300978111"
private const val MAX_RETRIES = 3
private const val RETRY_BASE_MS = 4_000L
private val BACKOFF = longArrayOf(1, 3, 9)
private val ATTEMPT_TAG = "revati_ad_attempt".hashCode()
