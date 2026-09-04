package com.example.service

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * The money path had no tests at all.
 *
 * PurchaseVerifier is a pure function with an obvious fail-open branch, so this
 * costs almost nothing and pins the behaviour that decides whether Pro can be
 * unlocked for free. Robolectric is needed only because the implementation uses
 * android.util.Base64.
 *
 * Note what the first test asserts: with no PLAY_LICENSE_KEY configured the
 * verifier accepts everything. That is the current, deliberate behaviour, and it
 * is safe only while nothing is purchasable. The test exists so that the day the
 * merchant account is created and a key is set, anyone changing this has to
 * change the test too and think about it.
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [34])
class PurchaseVerifierTest {

    @Test
    fun `with no license key configured verification is skipped, not enforced`() {
        // BuildConfig.PLAY_LICENSE_KEY is NOT_CONFIGURED in .env.example, which is
        // what the test build uses.
        org.junit.Assume.assumeFalse(
            "This test describes the unconfigured state only",
            PurchaseVerifier.isConfigured
        )
        assertTrue(PurchaseVerifier.isPurchaseValid("""{"productId":"anything"}""", "not-a-signature"))
    }

    @Test
    fun `isConfigured is false while the key is the NOT_CONFIGURED sentinel`() {
        // The secrets plugin cannot emit an empty string, hence the sentinel.
        // If this starts failing, a real key has been configured — and the
        // fail-open branch above is no longer the live path, which is the goal.
        assertFalse(PurchaseVerifier.isConfigured)
    }

    @Test
    fun `a blank signature is rejected once a key is configured`() {
        org.junit.Assume.assumeTrue(PurchaseVerifier.isConfigured)
        assertFalse(PurchaseVerifier.isPurchaseValid("""{"productId":"pro"}""", ""))
        assertFalse(PurchaseVerifier.isPurchaseValid("", "sig"))
    }

    @Test
    fun `a malformed signature is rejected rather than throwing`() {
        org.junit.Assume.assumeTrue(PurchaseVerifier.isConfigured)
        assertFalse(PurchaseVerifier.isPurchaseValid("""{"productId":"pro"}""", "!!!not-base64!!!"))
    }
}
