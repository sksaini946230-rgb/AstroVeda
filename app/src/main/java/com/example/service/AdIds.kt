package com.example.service

/**
 * Which ad unit to ask for, and when not to ask at all.
 *
 * Two rules, both of which have a history behind them.
 *
 * **Debug never asks for a real ad.** Google's policy is explicit that
 * development traffic must use the test units, and a device that spends a day
 * requesting live ads and never clicking one is what invalid-traffic
 * enforcement looks for. The risk is the AdMob account, not a wasted
 * impression. Test units also always fill, so a blank placement on a debug
 * build means the integration is broken rather than demand being thin.
 *
 * **Release never serves a test ad.** `.env.example` holds Google's test ids so
 * the project builds without secrets, and the secrets plugin falls back to it
 * for any key `.env` does not define. That is the right default for a clone or
 * for CI — and exactly the wrong thing to ship, because a release built without
 * `.env` would quietly serve test ads to real users and earn nothing while
 * looking like it worked. A test id in a release build is treated as no id at
 * all, so the placement stays empty and the absence is visible.
 */
object AdIds {

    /** Google's sample publisher. Anything under it is a test unit. */
    private const val TEST_PUBLISHER = "ca-app-pub-3940256099942544"

    const val TEST_BANNER = "$TEST_PUBLISHER/6300978111"
    const val TEST_INTERSTITIAL = "$TEST_PUBLISHER/1033173712"
    const val TEST_APP_OPEN = "$TEST_PUBLISHER/9257395921"
    const val TEST_REWARDED = "$TEST_PUBLISHER/5224354917"

    /**
     * [configured] is the BuildConfig value; [test] is the unit to use on debug.
     * Returns an empty string when there is nothing safe to ask for, and every
     * caller must treat that as "no ad here" rather than as an error.
     */
    fun resolve(configured: String?, test: String): String {
        if (app.revati.jyotish.BuildConfig.DEBUG) return test
        val id = configured?.trim().orEmpty()
        if (id.isBlank()) return ""
        // The secrets plugin cannot emit an empty string, so an unset key is the
        // sentinel instead — the same reason PLAY_LICENSE_KEY is NOT_CONFIGURED
        // rather than blank. Without this line the sentinel would be passed to
        // AdMob as if it were a unit id.
        if (id == NOT_CONFIGURED) return ""
        if (id.startsWith(TEST_PUBLISHER)) return ""
        // A real unit id is "ca-app-pub-<publisher>/<unit>". Anything else is a
        // typo or a leftover, and asking AdMob about it just logs an error.
        if (!id.startsWith("ca-app-pub-") || !id.contains('/')) return ""
        return id
    }

    private const val NOT_CONFIGURED = "NOT_CONFIGURED"
}
