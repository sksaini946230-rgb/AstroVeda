package com.example.service

import android.app.Activity
import android.util.Log
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentForm
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Ad consent, via Google's User Messaging Platform.
 *
 * The app served AdMob with no consent mechanism at all — no UMP, no
 * ConsentInformation, nothing. Google's policy requires one for users in the EEA
 * and the UK, so serving there without it is a policy violation regardless of
 * where the app is primarily aimed.
 *
 * UMP decides whether a form is needed based on the user's region, so this is a
 * no-op for a user in India and shows the form to a user in Europe.
 */
object AdConsentManager {

    private const val TAG = "AdConsentManager"

    /** Guards against initialising the ads SDK twice if consent resolves twice. */
    private val adsInitialised = AtomicBoolean(false)

    /**
     * Gathers consent if required, then invokes [onReady] exactly once — whether
     * consent was granted, refused, not required, or the request failed. Ads must
     * still initialise on failure, otherwise a UMP outage silently turns off
     * revenue for everyone.
     */
    fun gatherConsent(activity: Activity, onReady: () -> Unit) {
        val params = ConsentRequestParameters.Builder()
            .setTagForUnderAgeOfConsent(false)
            .apply {
                if (app.revati.jyotish.BuildConfig.DEBUG) {
                    // Lets the form be exercised on a debug build without a VPN.
                    setConsentDebugSettings(
                        ConsentDebugSettings.Builder(activity)
                            .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
                            .build()
                    )
                }
            }
            .build()

        val consentInformation = UserMessagingPlatform.getConsentInformation(activity)

        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
                    if (formError != null) {
                        Log.w(TAG, "Consent form: ${formError.errorCode} ${formError.message}")
                    }
                    fireOnce(onReady)
                }
            },
            { requestError ->
                Log.w(TAG, "Consent info update failed: ${requestError.errorCode} ${requestError.message}")
                fireOnce(onReady)
            }
        )
    }

    private fun fireOnce(onReady: () -> Unit) {
        if (adsInitialised.compareAndSet(false, true)) onReady()
    }

    /**
     * True when we are allowed to request ads. UMP reports this for every region;
     * outside the EEA/UK it is true immediately.
     */
    fun canRequestAds(activity: Activity): Boolean = try {
        UserMessagingPlatform.getConsentInformation(activity).canRequestAds()
    } catch (e: Throwable) {
        true
    }

    /**
     * True when the user's region gives them an ongoing right to change their
     * choice — the Settings entry should only appear then.
     */
    fun isPrivacyOptionsRequired(activity: Activity): Boolean = try {
        UserMessagingPlatform.getConsentInformation(activity).privacyOptionsRequirementStatus ==
            ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED
    } catch (e: Throwable) {
        false
    }

    /** Reopens the consent form so the user can change their mind. */
    fun showPrivacyOptions(activity: Activity, onDone: (String?) -> Unit = {}) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity) { formError ->
            onDone(formError?.message)
        }
    }
}
