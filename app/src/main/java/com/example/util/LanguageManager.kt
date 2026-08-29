package com.example.util

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

enum class AppLanguage(val code: String, val displayName: String) {
    HINDI("hi", "हिन्दी"),
    ENGLISH("en", "English")
}

/**
 * The app's language.
 *
 * This used to be an in-memory mutableStateOf and nothing else, so the choice the
 * onboarding screen explicitly asks the user to make was discarded the moment the
 * process died. Anyone who preferred English had to hit the toggle on every single
 * launch. It is persisted now.
 */
object LanguageManager {

    private const val PREFS = "astro_prefs"
    private const val KEY_LANGUAGE = "app_language"

    private var appContext: Context? = null

    var currentLanguage by mutableStateOf(AppLanguage.HINDI)
        private set

    /** Call once from Application/Activity start, before the first frame. */
    fun init(context: Context) {
        val ctx = context.applicationContext
        appContext = ctx
        val saved = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_LANGUAGE, null)
        currentLanguage = AppLanguage.entries.firstOrNull { it.code == saved } ?: AppLanguage.HINDI
    }

    fun toggleLanguage() {
        setLanguage(if (currentLanguage == AppLanguage.HINDI) AppLanguage.ENGLISH else AppLanguage.HINDI)
    }

    fun setLanguage(lang: AppLanguage) {
        currentLanguage = lang
        appContext?.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            ?.edit()
            ?.putString(KEY_LANGUAGE, lang.code)
            ?.apply()
    }

    /** Picks the Hindi or English variant for the active language. */
    fun getString(hi: String, en: String): String {
        return if (currentLanguage == AppLanguage.HINDI) hi else en
    }
}
