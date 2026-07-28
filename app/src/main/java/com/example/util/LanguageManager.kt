package com.example.util

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

enum class AppLanguage(val code: String, val displayName: String) {
    HINDI("hi", "हिन्दी"),
    ENGLISH("en", "English")
}

object LanguageManager {
    var currentLanguage by mutableStateOf(AppLanguage.HINDI)
        private set

    fun toggleLanguage() {
        currentLanguage = if (currentLanguage == AppLanguage.HINDI) AppLanguage.ENGLISH else AppLanguage.HINDI
    }

    fun setLanguage(lang: AppLanguage) {
        currentLanguage = lang
    }

    // Helper translation string getter
    fun getString(hi: String, en: String): String {
        return if (currentLanguage == AppLanguage.HINDI) hi else en
    }
}
