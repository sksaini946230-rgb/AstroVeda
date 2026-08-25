package com.example.util

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * AstroFeatureFlags — Remote Kill-Switch & Soft Launch Feature Flag Engine.
 * Allows instant remote toggling of high-risk or new features (AI Astrologer, In-App Billing, Ads)
 * without requiring a new Google Play binary release.
 */
object AstroFeatureFlags {

    data class FlagsState(
        val isAiAstrologerEnabled: Boolean = true,
        val isAdsEnabled: Boolean = true,
        val isInAppBillingEnabled: Boolean = true,
        val isKundaliMatchingEnabled: Boolean = true,
        val isFestivalNotificationsEnabled: Boolean = true,
        val isNumerologyEnabled: Boolean = true,
        val isProSubscriptionPromoEnabled: Boolean = true,
        val softLaunchRolloutPercentage: Int = 10,
        val minSupportedVersionCode: Int = 1,
        val supportWhatsAppNumber: String = "+919462308945",
        val supportEmail: String = "support@astroveda.app",
        val maintenanceMode: Boolean = false,
        val maintenanceMessageHi: String = "एस्ट्रोवेदा में तकनीकी सुधार कार्य चल रहा है। कृपया कुछ समय बाद पुनः प्रयास करें।",
        val maintenanceMessageEn: String = "AstroVeda is undergoing scheduled cosmic maintenance. Please check back shortly."
    )

    private val _flags = MutableStateFlow(FlagsState())
    val flags: StateFlow<FlagsState> = _flags.asStateFlow()

    // Helper getters for fast inline access
    val isAiAstrologerEnabled: Boolean get() = _flags.value.isAiAstrologerEnabled
    val isAdsEnabled: Boolean get() = _flags.value.isAdsEnabled
    val isInAppBillingEnabled: Boolean get() = _flags.value.isInAppBillingEnabled
    val isKundaliMatchingEnabled: Boolean get() = _flags.value.isKundaliMatchingEnabled
    val isMaintenanceMode: Boolean get() = _flags.value.maintenanceMode
    val supportWhatsAppNumber: String get() = _flags.value.supportWhatsAppNumber
    val supportEmail: String get() = _flags.value.supportEmail

    /**
     * Updates feature flags dynamically from Remote Config / Server API.
     */
    fun updateFlags(
        isAiAstrologerEnabled: Boolean? = null,
        isAdsEnabled: Boolean? = null,
        isInAppBillingEnabled: Boolean? = null,
        isKundaliMatchingEnabled: Boolean? = null,
        isFestivalNotificationsEnabled: Boolean? = null,
        maintenanceMode: Boolean? = null,
        rolloutPercentage: Int? = null
    ) {
        _flags.value = _flags.value.copy(
            isAiAstrologerEnabled = isAiAstrologerEnabled ?: _flags.value.isAiAstrologerEnabled,
            isAdsEnabled = isAdsEnabled ?: _flags.value.isAdsEnabled,
            isInAppBillingEnabled = isInAppBillingEnabled ?: _flags.value.isInAppBillingEnabled,
            isKundaliMatchingEnabled = isKundaliMatchingEnabled ?: _flags.value.isKundaliMatchingEnabled,
            isFestivalNotificationsEnabled = isFestivalNotificationsEnabled ?: _flags.value.isFestivalNotificationsEnabled,
            maintenanceMode = maintenanceMode ?: _flags.value.maintenanceMode,
            softLaunchRolloutPercentage = rolloutPercentage ?: _flags.value.softLaunchRolloutPercentage
        )
    }

    /**
     * Resets flags to default baseline state.
     */
    fun resetToDefaults() {
        _flags.value = FlagsState()
    }
}
