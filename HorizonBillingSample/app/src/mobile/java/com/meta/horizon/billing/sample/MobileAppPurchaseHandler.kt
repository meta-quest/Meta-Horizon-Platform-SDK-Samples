/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the Meta Platform Technologies SDK license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.meta.horizon.billing.sample

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingFlowParams.ProductDetailsParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.google.common.collect.ImmutableList
import com.meta.horizon.billing.sample.common.DateTimeHelper
import com.meta.horizon.billing.sample.purchase.CoreProductType
import com.meta.horizon.billing.sample.purchase.IBillingHandler
import com.meta.horizon.billing.sample.purchase.PurchaseEntryDetails
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

class MobileAppPurchaseHandler : IBillingHandler {

  private val TAG: String = "MobileAppPurchaseHandler"
  private var billingClient: BillingClient? = null
  private var activity: Activity? = null

  override fun initialize(
      activity: Activity,
      serviceReadyCallback: () -> Unit,
      successCallback: (itemName: String, quantity: String, orderDate: String) -> Unit,
      failureCallback: (errorMsg: String) -> Unit
  ) {
    Log.d(TAG, "Entered initialize function for MobileAppPurchaseHandler")
    this.activity = activity
    val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
      run {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK &&
            !purchases.isNullOrEmpty()) {
          // Acknowledge each purchase that was made (not required on Quest)
          for (purchase in purchases) {
            acknowledgePurchase(purchase.purchaseToken)
          }
          val purchase = purchases[0]
          successCallback(
              purchase.products[0],
              purchase.quantity.toString(),
              DateTimeHelper.convertToTimestamp(purchase.purchaseTime))
        } else {
          when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.ERROR -> {
              failureCallback(activity.applicationContext.getString(R.string.billing_error))
            }

            BillingClient.BillingResponseCode.BILLING_UNAVAILABLE -> {
              failureCallback(activity.applicationContext.getString(R.string.billing_unavailable))
            }

            BillingClient.BillingResponseCode.DEVELOPER_ERROR -> {
              failureCallback(activity.applicationContext.getString(R.string.billing_error))
            }

            BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED -> {
              failureCallback(
                  activity.applicationContext.getString(R.string.billing_unsupported_feature))
            }

            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
              failureCallback(activity.applicationContext.getString(R.string.billing_item_owned))
            }

            BillingClient.BillingResponseCode.ITEM_NOT_OWNED -> {
              failureCallback(
                  activity.applicationContext.getString(R.string.billing_item_not_owned))
            }

            BillingClient.BillingResponseCode.ITEM_UNAVAILABLE -> {
              failureCallback(
                  activity.applicationContext.getString(R.string.billing_item_unavailable))
            }

            BillingClient.BillingResponseCode.NETWORK_ERROR -> {
              failureCallback(activity.applicationContext.getString(R.string.billing_network_error))
            }

            BillingClient.BillingResponseCode.SERVICE_DISCONNECTED -> {
              failureCallback(
                  activity.applicationContext.getString(R.string.billing_service_disconnect))
            }

            BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE -> {
              failureCallback(
                  activity.applicationContext.getString(R.string.billing_service_unavailable))
            }

            BillingClient.BillingResponseCode.USER_CANCELED -> {
              failureCallback(activity.applicationContext.getString(R.string.billing_user_cancel))
            }

            else -> {
              failureCallback("Unknown error")
            }
          }
        }
      }
    }

    // Required with BillingClient 7.0.0 and higher
    val pendingPurchasesParams = PendingPurchasesParams.newBuilder().enableOneTimeProducts().build()

    this.billingClient =
        BillingClient.newBuilder(activity.applicationContext)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases(pendingPurchasesParams)
            .build()

    billingClient?.startConnection(
        object : BillingClientStateListener {
          override fun onBillingSetupFinished(billingResult: BillingResult) {
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
              Log.d(TAG, "GMS BillingClient connected!")
              serviceReadyCallback()
            }
          }

          override fun onBillingServiceDisconnected() {
            Log.d(TAG, "GMS BillingClient disconnected!")
          }
        })
  }

  override fun requestProductDetails(
      productId: String,
      productType: CoreProductType,
      successCallback: (result: List<Any>) -> Unit,
      failureCallback: (errorMsg: String) -> Unit
  ) {
    Log.d(TAG, "Entered requestProductDetails function for MobileAppPurchaseHandler")
    val gmsProductType =
        if (productType == CoreProductType.INAPP) BillingClient.ProductType.INAPP
        else BillingClient.ProductType.SUBS
    val queryProductDetailsParams =
        QueryProductDetailsParams.newBuilder()
            .setProductList(
                ImmutableList.of(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(productId)
                        .setProductType(gmsProductType)
                        .build()))
            .build()

    billingClient?.queryProductDetailsAsync(queryProductDetailsParams) {
        billingResult,
        productDetailsList ->
      if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
        failureCallback(billingResult.debugMessage)
      } else if (productDetailsList.isEmpty()) {
        failureCallback(activity!!.applicationContext.getString(R.string.billing_item_unavailable))
      } else {
        successCallback(productDetailsList)
      }
    }
  }

  override fun requestPurchases(
      context: Context,
      productType: CoreProductType,
      successCallback: (result: List<PurchaseEntryDetails>) -> Unit,
      failureCallback: (errorMsg: String) -> Unit
  ) {
    Log.d(TAG, "Entered requestPurchases function for MobileAppPurchaseHandler")
    val gmsProductType =
        if (productType == CoreProductType.INAPP) BillingClient.ProductType.INAPP
        else BillingClient.ProductType.SUBS
    val queryPurchaseParams =
        QueryPurchasesParams.newBuilder().setProductType(gmsProductType).build()

    Log.d(TAG, "Product Type: $gmsProductType")

    billingClient?.queryPurchasesAsync(queryPurchaseParams) { billingResult, purchaseList ->
      if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
        failureCallback(billingResult.debugMessage)
      } else {
        val result =
            purchaseList.map { purchase ->
              PurchaseEntryDetails(
                  purchase.products[0],
                  purchase.quantity,
                  purchase.purchaseTime,
                  purchase.purchaseToken,
                  purchase.products[0].contains("consume"),
              )
            }
        successCallback(result)
      }
    }
  }

  override fun launchBillingFlow(activity: Activity, productDetails: List<Any>) {
    Log.d(TAG, "Entered startPurchaseFlow function for MobileAppPurchaseHandler")
    // Retrieve GMS Product Details and Offer (if available)
    val gmsProductDetails = productDetails[0] as ProductDetails
    val gmsOfferToken = gmsProductDetails.subscriptionOfferDetails?.get(0)?.offerToken

    // Create Product Details for Order
    val productDetailsBuilder =
        ProductDetailsParams.newBuilder().setProductDetails(gmsProductDetails)
    if (gmsOfferToken != null) {
      productDetailsBuilder.setOfferToken(gmsOfferToken)
    }

    // Create Billing Flow parameters
    val billingFlowParams =
        BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(
                ImmutableList.of<ProductDetailsParams>(productDetailsBuilder.build()))
            .build()

    billingClient?.launchBillingFlow(activity, billingFlowParams)
  }

  override fun acknowledgePurchase(purchaseToken: String) {
    val acknowledgePurchaseParams =
        AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchaseToken).build()
    billingClient?.acknowledgePurchase(acknowledgePurchaseParams) { billingResult: BillingResult ->
      Log.d(TAG, billingResult.responseCode.toString())
    }
  }

  override fun consumePurchase(purchaseToken: String, listenerCallback: () -> Unit) {
    val consumeParams = ConsumeParams.newBuilder().setPurchaseToken(purchaseToken).build()
    billingClient?.consumeAsync(consumeParams) { billingResult: BillingResult, _: String ->
      Log.d(TAG, billingResult.responseCode.toString())
      listenerCallback()
    }
  }
}

@Module
@InstallIn(SingletonComponent::class)
class MobileAppPurchaseModule {

  @Singleton
  @Provides
  fun provideAppPurchaseHandler(): IBillingHandler {
    return MobileAppPurchaseHandler()
  }
}
