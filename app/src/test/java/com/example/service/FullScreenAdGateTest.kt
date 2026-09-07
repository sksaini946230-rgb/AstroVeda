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

    @Test
    fun `a second ad has to wait a minute after the first is dismissed`() {
        FullScreenAdGate.onShown(1_000_000L)
        FullScreenAdGate.onDismissed()

        assertFalse("three seconds later", FullScreenAdGate.canShow(1_003_000L))
        assertFalse("fifty-nine seconds later", FullScreenAdGate.canShow(1_059_000L))
        assertTrue("a minute later", FullScreenAdGate.canShow(1_060_000L))
    }

    @Test
    fun `a dismissed ad releases the gate`() {
        FullScreenAdGate.onShown(1_000_000L)
        FullScreenAdGate.onDismissed()
        assertFalse(FullScreenAdGate.isShowing)
    }
}
