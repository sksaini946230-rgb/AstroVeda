package com.example.service

import android.util.Base64
import android.util.Log
import java.security.KeyFactory
import java.security.PublicKey
import java.security.Signature
import java.security.spec.X509EncodedKeySpec

/**
 * Checks that a purchase really came from Google Play.
 *
 * Every purchase Play returns is signed with the developer account's private
 * key. Without checking that signature, the app takes the billing library's
 * word for it — and on a rooted device the billing library can be replaced with
 * one that says whatever the user wants. That is how Pro gets unlocked for free.
 *
 * This is client-side verification, so it is not the last word: an attacker who
 * can swap the billing library can also patch this class out. It raises the cost
 * from "install an app" to "modify the APK", which is worth having. The only
 * verification that cannot be bypassed happens on a server the attacker does not
 * control, checking the token against the Play Developer API — that needs a
 * backend Revati does not have yet.
 *
 * The key goes in .env (which is not in git) as PLAY_LICENSE_KEY, alongside the
 * other secrets; find it in Play Console under Monetise → Monetisation setup →
 * Licensing. With no key configured, verification is skipped and a warning is
 * logged, so a debug build and a fresh checkout still work.
 */
object PurchaseVerifier {

    private const val TAG = "PurchaseVerifier"
    private const val KEY_ALGORITHM = "RSA"
    private const val SIGNATURE_ALGORITHM = "SHA1withRSA"

    /**
     * The secrets plugin cannot emit an empty BuildConfig string, so an unset
     * key is spelled out rather than left blank.
     */
    private const val UNSET = "NOT_CONFIGURED"

    private val licenseKey: String
        get() = app.revati.jyotish.BuildConfig.PLAY_LICENSE_KEY
            .takeUnless { it == UNSET }
            .orEmpty()

    /** True when a key is configured and verification can actually run. */
    val isConfigured: Boolean get() = licenseKey.isNotBlank()

    /**
     * Verifies [signature] against [signedData] — the raw values the billing
     * library hands back as `purchase.originalJson` and `purchase.signature`.
     *
     * Returns true when the purchase is genuine, and also when no key is
     * configured: refusing every purchase would be worse than the exposure, and
     * a release build without a key is caught by the warning below rather than
     * by a wave of "I paid and got nothing" reviews.
     */
    fun isPurchaseValid(signedData: String, signature: String): Boolean {
        val key = licenseKey
        if (key.isBlank()) {
            // Fail-open, deliberately and narrowly: a release with no key would
            // otherwise refuse every genuine purchase, which is worse than the
            // exposure while nothing is purchasable at all (Play Console will not
            // even show the Subscriptions page until the merchant account exists).
            //
            // The moment that changes this must not still be the behaviour. The
            // warning is at error level so it is impossible to miss on the device —
            // a plain Log.w was invisible on hardware whose ROM only records
            // error-level logs, which is exactly the test device this is checked on.
            Log.e(
                TAG,
                "PLAY_LICENSE_KEY is not configured — purchase signatures are NOT being " +
                    "checked. Set it in .env before enabling subscriptions in Play Console."
            )
            return true
        }
        if (signedData.isBlank() || signature.isBlank()) {
            Log.w(TAG, "Purchase arrived with no data or no signature; rejecting.")
            return false
        }

        return try {
            val publicKey = decodePublicKey(key)
            val verifier = Signature.getInstance(SIGNATURE_ALGORITHM)
            verifier.initVerify(publicKey)
            verifier.update(signedData.toByteArray(Charsets.UTF_8))
            val ok = verifier.verify(Base64.decode(signature, Base64.DEFAULT))
            if (!ok) Log.w(TAG, "Purchase signature did not match; rejecting.")
            ok
        } catch (e: Exception) {
            // A malformed key or signature is not a reason to hand out Pro.
            Log.e(TAG, "Could not verify the purchase signature", e)
            false
        }
    }

    private fun decodePublicKey(encodedKey: String): PublicKey {
        val decoded = Base64.decode(encodedKey, Base64.DEFAULT)
        return KeyFactory.getInstance(KEY_ALGORITHM).generatePublic(X509EncodedKeySpec(decoded))
    }
}
