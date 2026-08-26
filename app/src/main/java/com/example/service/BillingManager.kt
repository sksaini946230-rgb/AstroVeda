package com.example.service

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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

    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    init {
        startConnection()
    }

    fun startConnection() {
        val client = billingClient
        if (client != null) {
            try {
                client.startConnection(this)
            } catch (e: Exception) {
                _errorMessage.value = "Billing connection failed: ${e.message}"
            }
        } else {
            _errorMessage.value = "Google Play Services not available. Running in offline/sandbox mode."
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
            _errorMessage.value = "Billing Setup Failed: ${billingResult.debugMessage}"
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
                    _errorMessage.value = "Query available products failed: ${billingResult.debugMessage}"
                }
            }
        } catch (e: Throwable) {
            _errorMessage.value = "Product details query exception: ${e.message}"
        }
    }

    fun launchPurchaseFlow(activity: Activity) {
        val client = billingClient
        if (client == null) {
            _errorMessage.value = "Billing client unavailable. Please try again later."
            if (com.example.BuildConfig.DEBUG) {
                onPremiumUnlocked(true)
            }
            return
        }

        val details = _productDetails.value
        if (details == null) {
            _errorMessage.value = "Subscription is temporarily unavailable, please try again later."
            if (com.example.BuildConfig.DEBUG) {
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
            val billingResult = client.launchBillingFlow(activity, billingFlowParams)
            if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
                _errorMessage.value = "Error starting purchase flow: ${billingResult.debugMessage}"
            }
        } catch (e: Throwable) {
            _errorMessage.value = "Purchase flow exception: ${e.message}"
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                handlePurchase(purchase)
            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            _errorMessage.value = "Subscription purchase canceled by user."
        } else {
            _errorMessage.value = "Error updating purchase: ${billingResult.debugMessage}"
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        val client = billingClient
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged && client != null) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                try {
                    client.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
                        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                            coroutineScope.launch {
                                onPremiumUnlocked(true)
                            }
                        }
                    }
                } catch (e: Throwable) {
                    _errorMessage.value = "Exception acknowledging purchase."
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
    }
}
