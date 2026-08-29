package com.example.service

import android.util.Log
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory

/**
 * App Check for debug builds.
 *
 * The debug provider ships only in `debugImplementation`, so referencing it from
 * shared code breaks the release compile even inside an `if (BuildConfig.DEBUG)`
 * branch — the reference still has to resolve. Variant source sets are the way to
 * do this; there is a matching file under src/release.
 *
 * On first run this prints a debug token to logcat. Paste it into
 * Firebase Console -> App Check -> Apps -> Manage debug tokens, once per machine.
 */
object AppCheckInitializer {
    fun install() {
        try {
            FirebaseAppCheck.getInstance()
                .installAppCheckProviderFactory(DebugAppCheckProviderFactory.getInstance())
            Log.i("AppCheck", "Debug provider installed — look for the debug token above/below this line")
        } catch (e: Throwable) {
            Log.w("AppCheck", "Not initialised: ${e.message}")
        }
    }
}
