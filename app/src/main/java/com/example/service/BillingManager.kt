package com.example.service

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import com.example.util.LanguageManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BillingManager(
    private val context: Context,
    private val onPremiumUnlocked: (Boolean) -> Unit
) : PurchasesUpdatedListener, BillingClientStateListener {

    companion object {
        const val SUB_PRODUCT_ID_PRO = "astroveda_premium_pro_subscription"
        private const val TAG = "BillingManager"
    }

    private var billingClient: BillingClient? = try {
        BillingClient.newBuilder(context)
            .setListener(this)
            .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
            .enableAutoServiceReconnection()
            .build()
    } catch (e: Throwable) {
        null
    }

    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    private val _productDetails = MutableStateFlow<ProductDetails?>(null)
    val productDetails: StateFlow<ProductDetails?> = _productDetails.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // SupervisorJob so destroy() can actually cancel this. It used to be a bare
    // CoroutineScope(Dispatchers.Main) that nothing ever cancelled — destroy()
    // ended the billing connection and left the scope running.
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    init {
        startConnection()
    }

    fun startConnection() {
        val client = billingClient
        if (client != null) {
            try {
                client.startConnection(this)
            } catch (e: Exception) {
                    android.util.Log.e(TAG, "Billing connection failed", e)
                _errorMessage.value = LanguageManager.getString(
                    "Play Billing से जुड़ नहीं सका। कृपया बाद में पुनः प्रयास करें।",
                    "Could not reach Google Play Billing. Please try again later."
                )
            }
        } else {
            _errorMessage.value = LanguageManager.getString(
                "इस डिवाइस पर Google Play Billing उपलब्ध नहीं है।",
                "Google Play Billing is not available on this device."
            )
        }
    }

    override fun onBillingSetupFinished(billingResult: BillingResult) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            _isReady.value = true
            _errorMessage.value = null
            queryAvailableProducts()
            queryPurchases()
        } else {
            _isReady.value = false
            android.util.Log.w(TAG, "Billing setup failed: ${billingResult.debugMessage}")
            _errorMessage.value = LanguageManager.getString(
                "Play Billing तैयार नहीं हो सका।",
                "Google Play Billing could not start."
            )
        }
    }

    override fun onBillingServiceDisconnected() {
        _isReady.value = false
    }

    private fun queryAvailableProducts() {
        val client = billingClient
        if (!_isReady.value || client == null) return

        val queryProductDetailsParams = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(SUB_PRODUCT_ID_PRO)
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build()
                )
            )
            .build()

        try {
            client.queryProductDetailsAsync(queryProductDetailsParams) { billingResult, queryProductDetailsResult ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    val details = queryProductDetailsResult.productDetailsList.firstOrNull { it.productId == SUB_PRODUCT_ID_PRO }
                    _productDetails.value = details
                } else {
                    android.util.Log.w(TAG, "Product query failed: ${billingResult.debugMessage}")
                    _errorMessage.value = LanguageManager.getString(
                        "सदस्यता की जानकारी नहीं मिल सकी।",
                        "Could not load the subscription details."
                    )
                }
            }
        } catch (e: Throwable) {
            android.util.Log.e(TAG, "Product details query threw", e)
            _errorMessage.value = LanguageManager.getString(
                "सदस्यता की जानकारी नहीं मिल सकी।",
                "Could not load the subscription details."
            )
        }
    }

    fun launchPurchaseFlow(activity: Activity) {
        val client = billingClient
        if (client == null) {
            _errorMessage.value = LanguageManager.getString(
                "Play Billing उपलब्ध नहीं है। कृपया बाद में पुनः प्रयास करें।",
                "Google Play Billing is unavailable. Please try again later."
            )
            if (app.revati.jyotish.BuildConfig.DEBUG) {
                onPremiumUnlocked(true)
            }
            return
        }

        val details = _productDetails.value
        if (details == null) {
            _errorMessage.value = LanguageManager.getString(
                "सदस्यता अभी उपलब्ध नहीं है। कृपया बाद में पुनः प्रयास करें।",
                "The subscription is not available right now. Please try again later."
            )
            if (app.revati.jyotish.BuildConfig.DEBUG) {
                onPremiumUnlocked(true)
            }
            return
        }

        val offerToken = details.subscriptionOfferDetails?.firstOrNull()?.offerToken ?: ""
        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(details)
                .setOfferToken(offerToken)
                .build()
        )

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        try {
            com.example.util.AstroAnalytics.logPurchaseInitiated(SUB_PRODUCT_ID_PRO)
            val billingResult = client.launchBillingFlow(activity, billingFlowParams)
            if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
                android.util.Log.w(TAG, "launchBillingFlow: ${billingResult.debugMessage}")
                com.example.util.AstroAnalytics.logPurchaseFailed(SUB_PRODUCT_ID_PRO, "launch_${billingResult.responseCode}")
                _errorMessage.value = LanguageManager.getString(
                    "खरीद शुरू नहीं हो सकी।",
                    "Could not start the purchase."
                )
            }
        } catch (e: Throwable) {
            android.util.Log.e(TAG, "Purchase flow threw", e)
            com.example.util.AstroAnalytics.recordNonFatal(e, "launchPurchaseFlow")
            com.example.util.AstroAnalytics.logPurchaseFailed(SUB_PRODUCT_ID_PRO, "launch_threw")
            _errorMessage.value = LanguageManager.getString(
                "खरीद शुरू नहीं हो सकी।",
                "Could not start the purchase."
            )
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                handlePurchase(purchase)
            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            com.example.util.AstroAnalytics.logPurchaseFailed(SUB_PRODUCT_ID_PRO, "user_cancelled")
            _errorMessage.value = LanguageManager.getString(
                "खरीद रद्द कर दी गई।",
                "Purchase cancelled."
            )
        } else {
            android.util.Log.w(TAG, "onPurchasesUpdated: ${billingResult.debugMessage}")
            com.example.util.AstroAnalytics.logPurchaseFailed(SUB_PRODUCT_ID_PRO, "code_${billingResult.responseCode}")
            _errorMessage.value = LanguageManager.getString(
                "खरीद पूरी नहीं हो सकी।",
                "The purchase did not go through."
            )
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        val client = billingClient
        // Play signs every purchase; without checking that signature the app
        // takes a swapped-out billing library at its word, which is exactly how
        // Pro gets unlocked for free on a rooted device.
        if (!PurchaseVerifier.isPurchaseValid(purchase.originalJson, purchase.signature)) {
            com.example.util.AstroAnalytics.logPurchaseFailed(SUB_PRODUCT_ID_PRO, "signature_invalid")
            _errorMessage.value = LanguageManager.getString(
                "यह खरीद Google Play से सत्यापित नहीं हो सकी।",
                "This purchase could not be verified with Google Play."
            )
            return
        }
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged && client != null) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                try {
                    client.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
                        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                            com.example.util.AstroAnalytics.logPurchaseSuccess(SUB_PRODUCT_ID_PRO)
                            coroutineScope.launch {
                                onPremiumUnlocked(true)
                            }
                        } else {
                            com.example.util.AstroAnalytics.logPurchaseFailed(SUB_PRODUCT_ID_PRO, "ack_${billingResult.responseCode}")
                        }
                    }
                } catch (e: Throwable) {
                    android.util.Log.e(TAG, "acknowledgePurchase threw", e)
                    com.example.util.AstroAnalytics.recordNonFatal(e, "acknowledgePurchase")
                    _errorMessage.value = LanguageManager.getString(
                        "खरीद की पुष्टि नहीं हो सकी।",
                        "Could not confirm the purchase."
                    )
                }
            } else if (purchase.isAcknowledged) {
                onPremiumUnlocked(true)
            }
        }
    }

    fun queryPurchases() {
        val client = billingClient
        if (!_isReady.value || client == null) return

        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()

        try {
            client.queryPurchasesAsync(params) { billingResult, purchasesList ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    var hasActiveSubscription = false
                    for (purchase in purchasesList) {
                        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED &&
                            purchase.products.contains(SUB_PRODUCT_ID_PRO)
                        ) {
                            hasActiveSubscription = true
                            handlePurchase(purchase)
                        }
                    }
                    // This is the only place Pro can be taken away, and it used to
                    // compute hasActiveSubscription and then throw it away — so an
                    // expired, cancelled or refunded subscription stayed Pro forever
                    // and the ads never came back. Play answered OK here, meaning
                    // this list is authoritative, so an absent subscription is a
                    // real absence rather than a connectivity blip.
                    if (!hasActiveSubscription) {
                        coroutineScope.launch { onPremiumUnlocked(false) }
                    }
                }
            }
        } catch (e: Throwable) {
            // fail gracefully
        }
    }

    fun destroy() {
        try {
            billingClient?.endConnection()
        } catch (_: Throwable) {}
        coroutineScope.cancel()
    }
}
