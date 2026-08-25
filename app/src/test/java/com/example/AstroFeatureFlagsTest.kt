package com.example

import com.example.util.AstroFeatureFlags
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class AstroFeatureFlagsTest {

    @Before
    fun setUp() {
        AstroFeatureFlags.resetToDefaults()
    }

    @Test
    fun testDefaultFeatureFlagsState() {
        assertTrue("AI Astrologer should be enabled by default", AstroFeatureFlags.isAiAstrologerEnabled)
        assertTrue("Ads should be enabled by default", AstroFeatureFlags.isAdsEnabled)
        assertTrue("Billing should be enabled by default", AstroFeatureFlags.isInAppBillingEnabled)
        assertFalse("Maintenance mode should be false by default", AstroFeatureFlags.isMaintenanceMode)
        assertEquals(100, AstroFeatureFlags.flags.value.softLaunchRolloutPercentage)
    }

    @Test
    fun testRemoteKillSwitchUpdate() {
        // Simulate remote emergency kill-switch for AI Astrologer
        AstroFeatureFlags.updateFlags(isAiAstrologerEnabled = false)
        assertFalse("AI Astrologer must be disabled after kill-switch", AstroFeatureFlags.isAiAstrologerEnabled)

        // Other flags must remain undisturbed
        assertTrue("Billing must remain enabled", AstroFeatureFlags.isInAppBillingEnabled)
        assertTrue("Ads must remain enabled", AstroFeatureFlags.isAdsEnabled)
    }

    @Test
    fun testRolloutPercentageProgression() {
        AstroFeatureFlags.updateFlags(rolloutPercentage = 10)
        assertEquals(10, AstroFeatureFlags.flags.value.softLaunchRolloutPercentage)

        // Progress soft launch rollout: 10% -> 25% -> 50% -> 100%
        AstroFeatureFlags.updateFlags(rolloutPercentage = 25)
        assertEquals(25, AstroFeatureFlags.flags.value.softLaunchRolloutPercentage)

        AstroFeatureFlags.updateFlags(rolloutPercentage = 50)
        assertEquals(50, AstroFeatureFlags.flags.value.softLaunchRolloutPercentage)

        AstroFeatureFlags.updateFlags(rolloutPercentage = 100)
        assertEquals(100, AstroFeatureFlags.flags.value.softLaunchRolloutPercentage)
    }
}
