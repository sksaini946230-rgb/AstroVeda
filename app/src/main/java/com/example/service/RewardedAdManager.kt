package com.example.service

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

/**
 * The opt-in ad in front of the PDF report.
 *
 * Rewarded is the highest-paying format and the only one the user chooses, so
 * it belongs on something they deliberately asked for — the Ashtakoot PDF —
 * rather than in their way.
 *
 * The rule this file exists to keep: **a failure to load must never cost the
 * user the report.** The PDF has always been free and is the output of a
 * calculation they already ran; AdMob having no ad to serve is not their
 * problem, and turning a no-fill into a refused report would be a worse app for
 * no revenue. So [showForReward] hands back `granted = true` whenever there is
 * no ad to show, and the caller cannot tell the difference between "watched"
 * and "there was nothing to watch". Only an ad that was actually shown and
 * actually abandoned counts as a refusal.
 */
object RewardedAdManager {

    private const val TAG = "RewardedAd"

    private var rewardedAd: RewardedAd? = null
    private var loading = false

    private val adUnitId: String
        get() = AdIds.resolve(
            try {
                app.revati.jyotish.BuildConfig.ADMOB_REWARDED_ID
            } catch (e: Throwable) {
                null
            },
            AdIds.TEST_REWARDED
        )

    /** True when an ad is ready, so the UI can offer to play one rather than promising it. */
    val isReady: Boolean get() = rewardedAd != null

    fun load(context: Context) {
        val unit = adUnitId
        if (unit.isBlank() || loading || rewardedAd != null) return
        loading = true
        RewardedAd.load(
            context,
            unit,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    loading = false
                    rewardedAd = ad
                    Log.e(TAG, "loaded")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    loading = false
                    rewardedAd = null
                    Log.e(TAG, "load failed: code=${error.code} msg=${error.message}")
                }
            }
        )
    }

    /**
     * Shows the ad and reports whether the reward was earned.
     *
     * [onResult] is called exactly once, on the main thread, with `true` when
     * the user should get the report — which includes every case where no ad
     * could be shown at all. It is `false` only when an ad played and was
     * dismissed before the reward.
     */
    fun showForReward(activity: Activity, onResult: (granted: Boolean) -> Unit) {
        val ad = rewardedAd
        if (ad == null || !FullScreenAdGate.canShow()) {
            // Nothing to watch, or another full-screen ad is in the way. The
            // report is not held hostage to either.
            load(activity)
            onResult(true)
            return
        }

        var earned = false
        var delivered = false
        fun deliver(granted: Boolean) {
            if (delivered) return
            delivered = true
            onResult(granted)
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                FullScreenAdGate.onShown()
            }

            override fun onAdDismissedFullScreenContent() {
                FullScreenAdGate.onDismissed()
                rewardedAd = null
                load(activity)
                deliver(earned)
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                FullScreenAdGate.onDismissed()
                rewardedAd = null
                Log.e(TAG, "show failed: ${error.message}")
                load(activity)
                // Failing to show is our problem, not theirs.
                deliver(true)
            }
        }

        try {
            ad.show(activity) { earned = true }
        } catch (e: Throwable) {
            FullScreenAdGate.onDismissed()
            rewardedAd = null
            deliver(true)
        }
    }
}
