package com.example.service

/**
 * One full-screen ad at a time, across the whole app.
 *
 * There are now two things that can cover the screen — the interstitial on a
 * tab change, and the app-open ad when the app returns to the foreground — and
 * they are driven by unrelated events. Nothing stopped them from firing
 * together: come back to the app and immediately change tab and you could be
 * handed two full-screen ads back to back, which is the "unexpected or
 * excessive" case Play's disruptive-ads policy is about.
 *
 * They also should not crowd each other in time. A shared floor means the app
 * cannot show a full-screen ad, dismiss it, and show a different kind three
 * seconds later.
 *
 * Deliberately in-memory and process-wide. It is about what is on the screen
 * right now, so it has nothing to persist.
 */
object FullScreenAdGate {

    /** No second full-screen ad within this long of the last one, of any kind. */
    private const val MIN_GAP_MS = 60_000L

    @Volatile
    private var showing = false

    @Volatile
    private var lastShownAt = 0L

    /** True while a full-screen ad is on the screen. */
    val isShowing: Boolean get() = showing

    /**
     * Asks whether a full-screen ad may be shown now. Callers keep their own
     * gates as well — this only enforces what the two share.
     */
    fun canShow(now: Long = System.currentTimeMillis()): Boolean =
        !showing && (now - lastShownAt) >= MIN_GAP_MS

    fun onShown(now: Long = System.currentTimeMillis()) {
        showing = true
        lastShownAt = now
    }

    fun onDismissed() {
        showing = false
    }
}
