package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.util.AstroAnalytics
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AstroAnalyticsTest {

    @Test
    fun testAstroAnalyticsMethods_executeSafelyWithoutException() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        AstroAnalytics.init(context)

        AstroAnalytics.logAppOpen()
        AstroAnalytics.logFirstOpen()
        AstroAnalytics.logOnboardingStep(1, "BirthDetails")
        AstroAnalytics.logOnboardingComplete()
        AstroAnalytics.logLogin("Google", true)
        AstroAnalytics.logKundaliGenerated(true, "KundaliScreen")
        AstroAnalytics.logHoroscopeView(1, "Mesh", "TODAY")
        AstroAnalytics.logPanchangView("Jaipur")
        AstroAnalytics.logKundaliMatching(28.0, false)
        AstroAnalytics.logNumerologyCalculated(5, 7)
        AstroAnalytics.logAiAstrologerQuery(35)
        AstroAnalytics.logShareEvent("Rashifal", "1")
        AstroAnalytics.logPurchaseInitiated("pro_monthly")
        AstroAnalytics.logPurchaseSuccess("pro_monthly")
        AstroAnalytics.logPurchaseFailed("pro_monthly", "User cancelled")
        AstroAnalytics.logScreenView("PanchangScreen")
        AstroAnalytics.setUserProperties("hi", true, "DARK")
        AstroAnalytics.recordNonFatal(Exception("Test non-fatal"), "UnitTest", "Details")
    }
}
