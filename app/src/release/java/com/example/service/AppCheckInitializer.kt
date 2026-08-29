package com.example.service

import android.util.Log
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory

/**
 * App Check for release builds.
 *
 * Play Integrity attests that the request comes from a genuine install of this
 * app, signed with this keystore, on a real device. It is what lets Firebase AI
 * Logic be called with no API key on the device at all.
 *
 * The project previously depended on firebase-appcheck-recaptcha, which is the
 * WEB provider and does nothing on Android, and never installed any provider.
 */
object AppCheckInitializer {
    fun install() {
        try {
            FirebaseAppCheck.getInstance()
                .installAppCheckProviderFactory(PlayIntegrityAppCheckProviderFactory.getInstance())
        } catch (e: Throwable) {
            // The AI feature falls back to its on-device answers; nothing else
            // in the app depends on App Check.
            Log.w("AppCheck", "Not initialised: ${e.message}")
        }
    }
}
