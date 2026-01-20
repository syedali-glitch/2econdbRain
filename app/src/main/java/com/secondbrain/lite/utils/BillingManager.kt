package com.secondbrain.lite.utils

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BillingManager(
    private val context: Context,
    private val preferenceManager: PreferenceManager
) : PurchasesUpdatedListener {
    
    private var billingClient: BillingClient? = null
    private var onPurchaseComplete: ((Boolean) -> Unit)? = null
    
    companion object {
        private const val TAG = "BillingManager"
        const val REMOVE_ADS_PRODUCT_ID = "remove_ads"
    }
    
    init {
        setupBillingClient()
    }
    
    private fun setupBillingClient() {
        billingClient = BillingClient.newBuilder(context)
            .setListener(this)
            .enablePendingPurchases()
            .build()
        
        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "Billing client connected")
                    // Check if user already purchased
                    queryPurchases()
                } else {
                    Log.e(TAG, "Billing setup failed: ${result.debugMessage}")
                }
            }
            
            override fun onBillingServiceDisconnected() {
                Log.d(TAG, "Billing service disconnected")
                // Try to reconnect on next purchase attempt
            }
        })
    }
    
    private fun queryPurchases() {
        billingClient?.let { client ->
            CoroutineScope(Dispatchers.IO).launch {
                val params = QueryPurchasesParams.newBuilder()
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build()
                
                val result = client.queryPurchasesAsync(params)
                result.purchasesList.forEach { purchase ->
                    if (purchase.products.contains(REMOVE_ADS_PRODUCT_ID) && 
                        purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        preferenceManager.adsRemoved = true
                        acknowledgePurchase(purchase)
                    }
                }
            }
        }
    }
    
    fun launchPurchaseFlow(activity: Activity, callback: (Boolean) -> Unit) {
        onPurchaseComplete = callback
        
        if (billingClient?.isReady == false) {
            setupBillingClient()
        }
        
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(REMOVE_ADS_PRODUCT_ID)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )
        
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()
        
        billingClient?.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && 
                productDetailsList.isNotEmpty()) {
                
                val productDetails = productDetailsList[0]
                val productDetailsParamsList = listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetails)
                        .build()
                )
                
                val flowParams = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(productDetailsParamsList)
                    .build()
                
                billingClient?.launchBillingFlow(activity, flowParams)
            } else {
                Log.e(TAG, "Failed to query product details: ${billingResult.debugMessage}")
                onPurchaseComplete?.invoke(false)
            }
        }
    }
    
    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                handlePurchase(purchase)
            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            Log.d(TAG, "User canceled purchase")
            onPurchaseComplete?.invoke(false)
        } else {
            Log.e(TAG, "Purchase failed: ${billingResult.debugMessage}")
            onPurchaseComplete?.invoke(false)
        }
    }
    
    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                acknowledgePurchase(purchase)
            }
            
            if (purchase.products.contains(REMOVE_ADS_PRODUCT_ID)) {
                preferenceManager.adsRemoved = true
                onPurchaseComplete?.invoke(true)
            }
        }
    }
    
    private fun acknowledgePurchase(purchase: Purchase) {
        if (!purchase.isAcknowledged) {
            val params = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
            
            billingClient?.acknowledgePurchase(params) { billingResult ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "Purchase acknowledged")
                }
            }
        }
    }
    
    fun destroy() {
        billingClient?.endConnection()
    }
}
