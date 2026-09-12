package com.example.service

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Two full-screen ads must not arrive together.
 *
 * The interstitial fires on a tab change and the app-open ad fires on a return
 * to the foreground. Neither knows about the other, so "come back to the app,
 * change tab" was two full-screen ads back to back — the "unexpected or
 * excessive" case Play's disruptive-ads policy is about, and the kind of thing
 * that is reported once and costs the placement.
 */
class FullScreenAdGateTest {

    @Before
    fun reset() {
        // The gate is a process-wide object, so each test has to put it back.
        FullScreenAdGate.onDismissed()
        FullScreenAdGate.onShown(0L)
        FullScreenAdGate.onDismissed()
    }

    @Test
    fun `nothing may show while something is showing`() {
        FullScreenAdGate.onShown(1_000_000L)
        assertTrue(FullScreenAdGate.isShowing)
        assertFalse(FullScreenAdGate.canShow(1_000_001L))
    }

    /**
     * Forty-five seconds, down from sixty when the owner asked for more ad
     * inventory. The floor is not about pace — the three limits in MainActivity
     * do that — it is about two full-screen ads arriving back to back, which is
     * the thing Play's disruptive-ads policy names outright. Any positive floor
     * prevents that; this one is deliberately still far above zero.
     */
    @Test
    fun `a second ad has to wait forty-five seconds after the first is dismissed`() {
        FullScreenAdGate.onShown(1_000_000L)
        FullScreenAdGate.onDismissed()

        assertFalse("three seconds later", FullScreenAdGate.canShow(1_003_000L))
        assertFalse("forty-four seconds later", FullScreenAdGate.canShow(1_044_000L))
        assertTrue("forty-five seconds later", FullScreenAdGate.canShow(1_045_000L))
    }

    @Test
    fun `a dismissed ad releases the gate`() {
        FullScreenAdGate.onShown(1_000_000L)
        FullScreenAdGate.onDismissed()
        assertFalse(FullScreenAdGate.isShowing)
    }
}
