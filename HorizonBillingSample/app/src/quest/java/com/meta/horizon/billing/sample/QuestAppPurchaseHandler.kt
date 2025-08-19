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
import com.google.common.collect.ImmutableList
import com.meta.horizon.billing.sample.common.DateTimeHelper
import com.meta.horizon.billing.sample.purchase.CoreProductType
import com.meta.horizon.billing.sample.purchase.IBillingHandler
import com.meta.horizon.billing.sample.purchase.PurchaseEntryDetails
import com.meta.horizon.billingclient.api.AcknowledgePurchaseParams
import com.meta.horizon.billingclient.api.BillingClient
import com.meta.horizon.billingclient.api.BillingClientStateListener
import com.meta.horizon.billingclient.api.BillingFlowParams
import com.meta.horizon.billingclient.api.BillingResult
import com.meta.horizon.billingclient.api.ConsumeParams
import com.meta.horizon.billingclient.api.ProductDetails
import com.meta.horizon.billingclient.api.PurchasesUpdatedListener
import com.meta.horizon.billingclient.api.QueryProductDetailsParams
import com.meta.horizon.billingclient.api.QueryPurchasesParams
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

class QuestAppPurchaseHandler : IBillingHandler {

  private val TAG: String = "QuestAppPurchaseHandler"
  private var billingClient: BillingClient? = null
  private var activity: Activity? = null

  override fun initialize(
      activity: Activity,
      serviceReadyCallback: () -> Unit,
      successCallback: (itemName: String, quantity: String, orderDate: String) -> Unit,
      failureCallback: (errorMsg: String) -> Unit,
  ) {
    Log.d(TAG, "Entered initialize function for QuestAppPurchaseHandler")
    this.activity = activity
    val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
      run {
        if (
            billingResult.responseCode == BillingClient.BillingResponseCode.OK &&
                purchases.isNotEmpty()
        ) {
          // Acknowledge each purchase that was made (not required on Quest)
          for (purchase in purchases) {
            acknowledgePurchase(purchase.purchaseToken)
          }
          val purchase = purchases[0]
          successCallback(
              purchase.products[0],
              purchase.quantity.toString(),
              DateTimeHelper.convertToTimestamp(purchase.purchaseTime),
          )
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
                  activity.applicationContext.getString(R.string.billing_unsupported_feature)
              )
            }

            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
              failureCallback(activity.applicationContext.getString(R.string.billing_item_owned))
            }

            BillingClient.BillingResponseCode.ITEM_NOT_OWNED -> {
              failureCallback(
                  activity.applicationContext.getString(R.string.billing_item_not_owned)
              )
            }

            BillingClient.BillingResponseCode.ITEM_UNAVAILABLE -> {
              failureCallback(
                  activity.applicationContext.getString(R.string.billing_item_unavailable)
              )
            }

            BillingClient.BillingResponseCode.NETWORK_ERROR -> {
              failureCallback(activity.applicationContext.getString(R.string.billing_network_error))
            }

            BillingClient.BillingResponseCode.SERVICE_DISCONNECTED -> {
              failureCallback(
                  activity.applicationContext.getString(R.string.billing_service_disconnect)
              )
            }

            BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE -> {
              failureCallback(
                  activity.applicationContext.getString(R.string.billing_service_unavailable)
              )
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

    this.billingClient =
        BillingClient.newBuilder(activity.applicationContext)
            .setAppId(BuildConfig.QUEST_APP_ID)
            .setListener(purchasesUpdatedListener)
            .build()

    billingClient?.startConnection(
        object : BillingClientStateListener {
          override fun onBillingSetupFinished(result: BillingResult) {
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
              Log.d(TAG, "Quest IAP BillingClient connected!")
              serviceReadyCallback()
            }
          }

          override fun onBillingServiceDisconnected() {
            Log.d(TAG, "Quest IAP BillingClient disconnected!")
          }
        }
    )
  }

  override fun requestProductDetails(
      productId: String,
      productType: CoreProductType,
      successCallback: (result: List<Any>) -> Unit,
      failureCallback: (errorMsg: String) -> Unit,
  ) {
    Log.d(TAG, "Entered requestProductDetails function for QuestAppPurchaseHandler")
    val questProductType =
        if (productType == CoreProductType.INAPP) BillingClient.ProductType.INAPP
        else BillingClient.ProductType.SUBS
    val queryProductDetailsParams =
        QueryProductDetailsParams.newBuilder()
            .setProductList(
                ImmutableList.of(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(productId)
                        .setProductType(questProductType)
                        .build()
                )
            )
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
      failureCallback: (errorMsg: String) -> Unit,
  ) {
    Log.d(TAG, "Entered requestPurchases function for QuestAppPurchaseHandler")
    val questProductType =
        if (productType == CoreProductType.INAPP) BillingClient.ProductType.INAPP
        else BillingClient.ProductType.SUBS
    val queryPurchaseParams =
        QueryPurchasesParams.newBuilder().setProductType(questProductType).build()

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

  override fun launchBillingFlow(
      activity: Activity,
      productDetails: List<Any>,
  ) {
    Log.d(TAG, "Entered launchBillingFlow function for QuestAppPurchaseHandler")
    // Retrieve Quest Product Details and Offer (if available)
    val questProductDetails = productDetails[0] as ProductDetails

    // Create Product Details for Order
    val productDetailsBuilder =
        BillingFlowParams.ProductDetailsParams.newBuilder().setProductDetails(questProductDetails)

    // Create Billing Flow parameters
    val billingFlowParams =
        BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(
                ImmutableList.of<BillingFlowParams.ProductDetailsParams>(
                    productDetailsBuilder.build()
                )
            )
            .build()
    billingClient?.launchBillingFlow(activity, billingFlowParams)
  }

  override fun acknowledgePurchase(purchaseToken: String) {
    Log.d(TAG, "Calling acknowledgePurchase with token: $purchaseToken")
    val acknowledgePurchaseParams =
        AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchaseToken).build()

    billingClient?.acknowledgePurchase(acknowledgePurchaseParams) { billingResult: BillingResult ->
      Log.d(TAG, billingResult.responseCode.toString())
    }
  }

  override fun consumePurchase(purchaseToken: String, listenerCallback: () -> Unit) {
    Log.d(TAG, "Calling consumePurchase with token: $purchaseToken")
    val consumeParams = ConsumeParams.newBuilder().setPurchaseToken(purchaseToken).build()
    billingClient?.consumeAsync(consumeParams) { billingResult: BillingResult, _: String ->
      Log.d(TAG, billingResult.responseCode.toString())
      listenerCallback()
    }
  }
}

@Module
@InstallIn(SingletonComponent::class)
class QuestAppPurchaseModule {

  @Singleton
  @Provides
  fun provideAppPurchaseHandler(): IBillingHandler {
    return QuestAppPurchaseHandler()
  }
}
