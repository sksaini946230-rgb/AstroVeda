package com.example.service

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd

/**
 * The ad shown when someone comes back to the app.
 *
 * A Panchang app is opened several times a day for a few seconds each — to read
 * a tithi, to check a Choghadiya — so returns to the foreground are the most
 * common thing that happens in it, and the format with the most inventory here.
 *
 * What it deliberately does not do:
 *
 *  - **It does not show on a cold start.** Google's guidance is that an app-open
 *    ad belongs over a loading screen the user is already waiting through, not
 *    in front of an app they just launched; showing it on the very first
 *    foreground is how these get reported as disruptive. The first foreground of
 *    the process is skipped, always.
 *  - **It does not stack with the interstitial.** Both are full screen and both
 *    fire on events the other knows nothing about. [FullScreenAdGate] is what
 *    stops "return to the app, change tab, get two ads".
 *  - **It does not show over a dialog or mid-task.** It only fires when an
 *    Activity comes to the foreground after the app was actually backgrounded,
 *    not on a configuration change.
 *  - **It does not show to a PRO user.** [isProUser] is supplied by the app
 *    rather than read here, so this file knows nothing about billing.
 *
 * The four-hour expiry is Google's documented rule: an app-open ad response is
 * only valid that long, after which it must be discarded and re-requested. An
 * expired ad shown anyway is an impression that will not be paid for.
 */
object AppOpenAdManager {

    private const val TAG = "AppOpenAd"

    /** Google's documented validity window for an app-open ad response. */
    private const val AD_TTL_MS = 4 * 60 * 60 * 1000L

    /**
     * A backgrounding shorter than this does not earn an ad.
     *
     * Answering a notification, glancing at a message and coming straight back
     * is one continuous use of the app as far as the person is concerned.
     */
    private const val MIN_BACKGROUND_MS = 30_000L

    private var appOpenAd: AppOpenAd? = null
    private var loadedAt = 0L
    private var loading = false

    private var currentActivity: Activity? = null
    private var startedActivities = 0
    private var backgroundedAt = 0L
    private var hadFirstForeground = false

    /** Set when a foreground transition is seen, acted on once the Activity resumes. */
    private var showOnNextResume = false

    private val handler = android.os.Handler(android.os.Looper.getMainLooper())

    /**
     * How long after onResume to attempt the show.
     *
     * Long enough for the process to actually become foreground, short enough
     * that the ad still reads as part of opening the app rather than as an
     * interruption of something the user has started doing.
     */
    private const val SHOW_DELAY_MS = 600L

    /** One more attempt, for the case where 600ms was not enough. */
    private const val RETRY_SHOW_DELAY_MS = 1_200L

    /** Set by the app; this file does not depend on the billing layer. */
    @Volatile
    var isProUser: () -> Boolean = { false }

    private val adUnitId: String
        get() = AdIds.resolve(
            try {
                app.revati.jyotish.BuildConfig.ADMOB_APP_OPEN_ID
            } catch (e: Throwable) {
                null
            },
            AdIds.TEST_APP_OPEN
        )

    /**
     * Watches the process's foreground state.
     *
     * Counting started Activities rather than using ProcessLifecycleOwner keeps
     * this free of a new dependency and, more usefully, makes a rotation a
     * non-event: the count never reaches zero, so coming back from a rotation
     * cannot be mistaken for coming back from the launcher.
     */
    fun register(app: Application) {
        app.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
            override fun onActivityCreated(a: Activity, b: Bundle?) {}
            override fun onActivityDestroyed(a: Activity) {
                if (currentActivity === a) currentActivity = null
            }

            override fun onActivityStarted(a: Activity) {
                currentActivity = a
                val wasBackground = startedActivities == 0
                startedActivities++
                if (!wasBackground) return

                if (!hadFirstForeground) {
                    // The cold start. Load one for next time and show nothing.
                    hadFirstForeground = true
                    load()
                    return
                }

                if (System.currentTimeMillis() - backgroundedAt < MIN_BACKGROUND_MS) {
                    return
                }

                // Decided here, shown in onActivityResumed. onStart is where the
                // foreground *transition* can be detected, but the Activity is
                // not actually foreground yet — showing from here gets
                // "The ad can not be shown when app is not in foreground" from
                // the SDK and burns the loaded ad. Found on the device; the log
                // line is the only reason it was visible at all.
                showOnNextResume = true
            }

            override fun onActivityStopped(a: Activity) {
                startedActivities = (startedActivities - 1).coerceAtLeast(0)
                if (startedActivities == 0) {
                    showOnNextResume = false
                    backgroundedAt = System.currentTimeMillis()
                    // Have one ready for the return rather than starting the
                    // request at the moment it is needed.
                    load()
                }
            }

            override fun onActivityResumed(a: Activity) {
                currentActivity = a
                if (!showOnNextResume) return
                showOnNextResume = false
                // Posted, not called straight through. onResume runs before the
                // process reaches foreground importance, and the SDK checks
                // that: showing from here directly gets "The ad can not be
                // shown when app is not in foreground". Verified on the device
                // from both onStart and onResume before this was added.
                handler.postDelayed({
                    val act = currentActivity
                    if (act != null && !act.isFinishing) showIfAvailable(act, retryOnNotForeground = true)
                }, SHOW_DELAY_MS)
            }
            override fun onActivityPaused(a: Activity) {}
            override fun onActivitySaveInstanceState(a: Activity, b: Bundle) {}
        })
    }

    private fun isExpired(): Boolean = System.currentTimeMillis() - loadedAt >= AD_TTL_MS

    fun load() {
        val unit = adUnitId
        if (unit.isBlank()) return
        if (loading) return
        if (appOpenAd != null && !isExpired()) return
        if (isProUser()) return

        val activity = currentActivity ?: return
        loading = true
        AppOpenAd.load(
            activity,
            unit,
            AdRequest.Builder().build(),
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    loading = false
                    appOpenAd = ad
                    loadedAt = System.currentTimeMillis()
                    // Error level on purpose — the test device keeps nothing
                    // below E, so an absent success line and an absent failure
                    // line look identical. Same reason as AdBanner.
                    Log.e(TAG, "loaded")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    loading = false
                    appOpenAd = null
                    Log.e(TAG, "load failed: code=${error.code} msg=${error.message}")
                }
            }
        )
    }

    private fun showIfAvailable(activity: Activity, retryOnNotForeground: Boolean = false) {
        if (isProUser()) return
        if (!FullScreenAdGate.canShow()) return

        val ad = appOpenAd
        if (ad == null || isExpired()) {
            appOpenAd = null
            load()
            return
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                FullScreenAdGate.onShown()
            }

            override fun onAdDismissedFullScreenContent() {
                FullScreenAdGate.onDismissed()
                appOpenAd = null
                load()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                FullScreenAdGate.onDismissed()
                Log.e(TAG, "show failed: ${error.message}")

                // Keep the ad. A failure to show is almost always the
                // foreground race above, not a bad ad — and the response is
                // valid for four hours, so throwing it away meant paying for a
                // load and then discarding it. It used to be nulled here.
                if (isExpired()) {
                    appOpenAd = null
                    load()
                    return
                }
                if (retryOnNotForeground) {
                    handler.postDelayed({
                        val act = currentActivity
                        if (act != null && !act.isFinishing) showIfAvailable(act)
                    }, RETRY_SHOW_DELAY_MS)
                }
            }
        }

        try {
            ad.show(activity)
        } catch (e: Throwable) {
            FullScreenAdGate.onDismissed()
            appOpenAd = null
        }
    }
}
