package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.background
import androidx.compose.material3.Scaffold
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.ui.AppTab
import com.example.ui.MainViewModel
import com.example.ui.components.AdBanner
import com.example.ui.components.BottomNavBar
import com.example.ui.components.PremiumDialog
import com.example.ui.components.RateUsDialog
import com.example.ui.components.TopHeaderBar
import com.example.ui.components.FeatureDiscoveryOverlay
import com.example.ui.components.DiscoveryStep
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.WbSunny
import com.example.ui.screens.CalendarScreen
import com.example.ui.screens.KundaliScreen
import com.example.ui.screens.MatchingScreen
import com.example.ui.screens.MoreScreen
import com.example.ui.screens.MuhuratScreen
import com.example.ui.screens.NumerologyScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.PanchangScreen
import com.example.ui.screens.RashifalScreen
import com.example.ui.screens.SavedProfilesScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.AstroVedaTheme
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.Lifecycle
import kotlinx.coroutines.launch
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback


class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    private var mInterstitialAd: InterstitialAd? = null
    private var lastInterstitialShowTime = 0L

    private val requestNotificationPermissionLauncher =
        registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                com.example.worker.AstroNotificationWorker.scheduleDailyNotification(this)
                com.example.worker.FestivalNotificationWorker.scheduleFestivalNotification(this)
                com.example.worker.MuhuratNotificationWorker.scheduleMuhuratNotification(this)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        try {
            MobileAds.initialize(this) {}
            loadInterstitialAd()
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                if (androidx.core.content.ContextCompat.checkSelfPermission(
                        this,
                        android.Manifest.permission.POST_NOTIFICATIONS
                    ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                ) {
                    com.example.worker.AstroNotificationWorker.scheduleDailyNotification(this)
                    com.example.worker.FestivalNotificationWorker.scheduleFestivalNotification(this)
                    com.example.worker.MuhuratNotificationWorker.scheduleMuhuratNotification(this)
                } else {
                    requestNotificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                }
            } else {
                com.example.worker.AstroNotificationWorker.scheduleDailyNotification(this)
                com.example.worker.FestivalNotificationWorker.scheduleFestivalNotification(this)
                com.example.worker.MuhuratNotificationWorker.scheduleMuhuratNotification(this)
            }
        } catch (e: Throwable) {
            // fail gracefully
        }

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainViewModel.showInterstitialTrigger.collect {
                    showInterstitialAd()
                }
            }
        }

        setContent {
            AstroVedaTheme {
                val selectedTab by mainViewModel.selectedTab.collectAsState()
                val showPremium by mainViewModel.showPremiumDialog.collectAsState()
                val showRateUsDialog by mainViewModel.showRateUsDialog.collectAsState()
                val isOnboardingCompleted by mainViewModel.isOnboardingCompleted.collectAsState()
                val isOffline by mainViewModel.isOffline.collectAsState()
                val isSyncing by mainViewModel.isSyncing.collectAsState()
                val isFirestoreSyncing by mainViewModel.isFirestoreSyncing.collectAsState()
                val isFirstRunSyncing by mainViewModel.isFirstRunSyncing.collectAsState()
                val isStartupComplete by mainViewModel.isStartupComplete.collectAsState()
                val isDiscoveryCompleted by mainViewModel.isDiscoveryCompleted.collectAsState()
                val currentUser by mainViewModel.currentUser.collectAsState()
                val isCloudBackupEnabled = currentUser != null

                if (!isOnboardingCompleted) {
                    OnboardingScreen(
                        viewModel = mainViewModel,
                        onComplete = { mainViewModel.completeOnboarding() }
                    )
                } else if (isFirstRunSyncing) {
                    FirstRunSyncingOverlay()
                } else {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        topBar = {
                            Column {
                                TopHeaderBar(
                                    isOffline = isOffline,
                                    isSyncing = isSyncing,
                                    isFirestoreSyncing = isFirestoreSyncing,
                                    isCloudBackupEnabled = isCloudBackupEnabled,
                                    onLanguageToggle = { mainViewModel.toggleLanguage() },
                                    onPremiumClick = { mainViewModel.showPremiumDialog.value = true },
                                    onSettingsClick = { mainViewModel.navigateToMore(subTab = 1) }
                                )
                                if (isCloudBackupEnabled && isFirestoreSyncing) {
                                    LinearProgressIndicator(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(2.dp)
                                            .testTag("firestore_sync_progress_bar"),
                                        color = MaterialTheme.colorScheme.primary,
                                        trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                    )
                                }
                            }
                        },
                        bottomBar = {
                            Column {
                                val isPro by mainViewModel.isProUser.collectAsState()
                                if (!isPro && selectedTab == AppTab.PANCHANG) {
                                    androidx.compose.animation.AnimatedVisibility(
                                        visible = isStartupComplete,
                                        enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.expandVertically()
                                    ) {
                                        AdBanner(
                                            onRemoveAdsClick = { mainViewModel.showPremiumDialog.value = true }
                                        )
                                    }
                                }
                                BottomNavBar(
                                    selectedTab = selectedTab,
                                    onTabSelected = { mainViewModel.selectTab(it) }
                                )
                            }
                        }
                    ) { innerPadding ->
                        val globalError by mainViewModel.globalError.collectAsState()

                        com.example.ui.components.ErrorBoundary(
                            externalError = globalError,
                            onClearError = { mainViewModel.clearGlobalError() },
                            onRetry = {
                                // Clear error and reset tab or rerun last query
                                mainViewModel.clearGlobalError()
                            },
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                @OptIn(ExperimentalSharedTransitionApi::class)
                                SharedTransitionLayout {
                                    AnimatedContent(
                                        targetState = selectedTab,
                                        label = "TabTransition",
                                        transitionSpec = {
                                            fadeIn(animationSpec = tween(400))
                                                .togetherWith(fadeOut(animationSpec = tween(300)))
                                        }
                                    ) { tab ->
                                        when (tab) {
                                            AppTab.PANCHANG -> PanchangScreen(
                                                viewModel = mainViewModel,
                                                sharedTransitionScope = this@SharedTransitionLayout,
                                                animatedVisibilityScope = this@AnimatedContent
                                            )
                                            AppTab.RASHIFAL -> RashifalScreen(mainViewModel)
                                            AppTab.KUNDALI -> KundaliScreen(
                                                viewModel = mainViewModel,
                                                sharedTransitionScope = this@SharedTransitionLayout,
                                                animatedVisibilityScope = this@AnimatedContent
                                            )
                                            AppTab.MUHURAT -> MuhuratScreen(mainViewModel)
                                            AppTab.MORE -> MoreScreen(mainViewModel)
                                        }
                                    }
                                }

                                if (showPremium) {
                                    PremiumDialog(
                                        viewModel = mainViewModel,
                                        onDismiss = { mainViewModel.showPremiumDialog.value = false }
                                    )
                                }

                                if (showRateUsDialog) {
                                    RateUsDialog(
                                        viewModel = mainViewModel,
                                        onDismiss = { mainViewModel.dismissRateUs() }
                                    )
                                }

                                if (!isDiscoveryCompleted && isOnboardingCompleted && !isFirstRunSyncing) {
                                    FeatureDiscoveryOverlay(
                                        steps = listOf(
                                            DiscoveryStep(
                                                titleHi = "दैनिक पंचांग",
                                                titleEn = "Daily Panchang",
                                                descriptionHi = "तिथि, नक्षत्र और सूर्योदय के समय के साथ अपने दिन की शुरुआत दिव्य रूप से करें।",
                                                descriptionEn = "Start your day divinely with precise Tithi, Nakshatra, and Sunrise timings.",
                                                icon = Icons.Default.WbSunny
                                            ),
                                            DiscoveryStep(
                                                titleHi = "विस्तृत कुंडली",
                                                titleEn = "Detailed Kundali",
                                                descriptionHi = "अपने जन्म विवरण के साथ अपनी विस्तृत जन्म कुंडली और ग्रह स्थितियों का विश्लेषण करें।",
                                                descriptionEn = "Generate and analyze your detailed birth chart and planetary positions with ease.",
                                                icon = Icons.Default.AutoAwesome
                                            ),
                                            DiscoveryStep(
                                                titleHi = "शुभ मुहूर्त",
                                                titleEn = "Auspicious Muhurat",
                                                descriptionHi = "अपनी महत्वपूर्ण गतिविधियों के लिए सबसे शुभ समय खोजें और सफलता सुनिश्चित करें।",
                                                descriptionEn = "Find the most auspicious timings for your important activities and ensure success.",
                                                icon = Icons.Default.Schedule
                                            )
                                        ),
                                        onComplete = { mainViewModel.completeDiscovery() }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun loadInterstitialAd() {
        val interstitialId = try {
            com.example.BuildConfig.ADMOB_INTERSTITIAL_ID.ifBlank { "ca-app-pub-3940256099942544/1033173712" }
        } catch (e: Throwable) {
            "ca-app-pub-3940256099942544/1033173712"
        }

        if (interstitialId.isBlank()) return

        val adRequest = AdRequest.Builder().build()
        try {
            InterstitialAd.load(this, interstitialId, adRequest, object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    mInterstitialAd = null
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    mInterstitialAd = interstitialAd
                }
            })
        } catch (e: Throwable) {
            // fail gracefully
        }
    }

    private fun showInterstitialAd() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastInterstitialShowTime < 60_000L) {
            // Frequency capped - do not show too often
            return
        }

        if (mainViewModel.isProUser.value) {
            return
        }

        try {
            mInterstitialAd?.let { ad ->
                ad.show(this)
                mInterstitialAd = null
                lastInterstitialShowTime = currentTime
                loadInterstitialAd() // Preload the next one
            } ?: run {
                loadInterstitialAd()
            }
        } catch (e: Throwable) {
            // fail gracefully
        }
    }
}

@Composable
fun FirstRunSyncingOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(androidx.compose.material3.MaterialTheme.colorScheme.background),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        androidx.compose.foundation.layout.Column(
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
        ) {
            androidx.compose.material3.CircularProgressIndicator(
                color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(64.dp)
            )
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(24.dp))
            androidx.compose.material3.Text(
                text = "ब्रह्मांडीय डेटा सिंक हो रहा है...",
                style = androidx.compose.material3.MaterialTheme.typography.titleLarge.copy(
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.primary
                )
            )
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(8.dp))
            androidx.compose.material3.Text(
                text = "Syncing cosmic data for offline access...",
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium.copy(
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

