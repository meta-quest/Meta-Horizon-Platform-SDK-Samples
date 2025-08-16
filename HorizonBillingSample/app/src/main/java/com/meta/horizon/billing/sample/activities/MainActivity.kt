/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the Meta Platform Technologies SDK license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.meta.horizon.billing.sample.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import com.meta.horizon.billing.sample.BuildConfig
import com.meta.horizon.billing.sample.R
import com.meta.horizon.billing.sample.databinding.ActivityMainBinding
import com.meta.horizon.billing.sample.purchase.CoreProductType
import com.meta.horizon.billing.sample.purchase.IBillingHandler
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

  @Inject lateinit var billingHandler: IBillingHandler
  private val TAG: String = "MainActivity"

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    billingHandler.initialize(this, ::onServiceReady, ::onSuccessPurchase, ::onFailurePurchase)

    val binding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
    binding.activity = this
    binding.consumeFreeBtn.setOnClickListener { onFreeConsumeItemPurchase() }
    binding.consumePaidBtn.setOnClickListener { onPaidConsumeItemPurchase() }
    binding.durableFreeBtn.setOnClickListener { onFreeDurableItemPurchase() }
    binding.durablePaidBtn.setOnClickListener { onPaidDurableItemPurchase() }
    binding.subscriptionBtn.setOnClickListener { onSubscriptionPurchase() }
    binding.purchaseHistoryBtn.setOnClickListener { onPurchaseHistory() }

    // Include the Version Name and Code to highlight the build for the app
    binding.versionDetails.text =
        "Build (${BuildConfig.VERSION_NAME}) | Code (${BuildConfig.VERSION_CODE})"

    // Check Device name
    binding.deviceId.text = "Device: ${Build.MANUFACTURER} ${Build.MODEL} (${Build.DEVICE})"

    // Check Client Type
    val clientTypeText =
        if (billingHandler.javaClass.typeName.contains("Quest")) {
          "Quest IAP"
        } else {
          "GMS IAP"
        }
    binding.clientType.text = "Client: $clientTypeText"

    // Check if device and model configuration are correct
    if ((Build.MODEL.contains("Quest") && clientTypeText == "Quest IAP") ||
        (!Build.MODEL.contains("Quest") && clientTypeText == "GMS IAP")) {
      binding.correctClientIndicator.setImageResource(R.drawable.success_img)
    }
  }

  private fun onFreeConsumeItemPurchase() {
    showLoadingScreen()
    billingHandler.requestProductDetails(
        "test_consume_free",
        CoreProductType.INAPP,
        ::onSuccessProductDetails,
        ::onFailureProductDetails,
    )
  }

  private fun onPaidConsumeItemPurchase() {
    showLoadingScreen()
    billingHandler.requestProductDetails(
        "test_consume_paid",
        CoreProductType.INAPP,
        ::onSuccessProductDetails,
        ::onFailureProductDetails,
    )
  }

  private fun onFreeDurableItemPurchase() {
    showLoadingScreen()
    billingHandler.requestProductDetails(
        "test_durable_free",
        CoreProductType.INAPP,
        ::onSuccessProductDetails,
        ::onFailureProductDetails,
    )
  }

  private fun onPaidDurableItemPurchase() {
    showLoadingScreen()
    billingHandler.requestProductDetails(
        "test_durable_paid",
        CoreProductType.INAPP,
        ::onSuccessProductDetails,
        ::onFailureProductDetails,
    )
  }

  private fun onSubscriptionPurchase() {
    showLoadingScreen()
    billingHandler.requestProductDetails(
        "test_sub_1",
        CoreProductType.SUBS,
        ::onSuccessProductDetails,
        ::onFailureProductDetails,
    )
  }

  private fun onServiceReady() {
    Log.d(TAG, "BillingClient ready for MainActivity!")
  }

  private fun onSuccessProductDetails(result: List<Any>) {
    Log.d(TAG, "Successful call for ProductDetails")
    billingHandler.launchBillingFlow(this, result)
  }

  private fun onFailureProductDetails(errorMsg: String) {
    Log.d(TAG, "Error when calling ProductDetails: $errorMsg")
    navigateToPurchaseErrorScreen(errorMsg)
  }

  private fun onSuccessPurchase(itemName: String, quantity: String, orderDate: String) {
    Log.d(TAG, "Successful purchase!")
    navigateToCompletionScreen(itemName, quantity, orderDate)
  }

  private fun onFailurePurchase(errorMsg: String) {
    Log.d(TAG, "Error when calling ProductDetails: $errorMsg")
    navigateToPurchaseErrorScreen(errorMsg)
  }

  private fun onPurchaseHistory() {
    val purchaseHistoryIntent = Intent(this, ViewPurchasesActivity::class.java)
    startActivity(purchaseHistoryIntent)
    finishAffinity()
  }

  private fun navigateToCompletionScreen(itemName: String, quantity: String, orderDate: String) {
    hideLoadingScreen()
    val completionIntent =
        Intent(this, PurchaseCompleteActivity::class.java).apply {
          putExtra("itemName", itemName)
          putExtra("quantity", quantity)
          putExtra("orderDate", orderDate)
        }
    startActivity(completionIntent)
    finishAffinity()
  }

  private fun navigateToPurchaseErrorScreen(errorMsg: String) {
    hideLoadingScreen()
    val purchaseErrorIntent =
        Intent(this, PurchaseErrorActivity::class.java).apply { putExtra("errorMsg", errorMsg) }
    startActivity(purchaseErrorIntent)
    finishAffinity()
  }

  private fun showLoadingScreen() {
    runOnUiThread {
      val loadingView = findViewById<ConstraintLayout>(R.id.loading_view)
      loadingView.visibility = ConstraintLayout.VISIBLE
    }
  }

  private fun hideLoadingScreen() {
    runOnUiThread {
      val loadingView = findViewById<ConstraintLayout>(R.id.loading_view)
      loadingView.visibility = ConstraintLayout.GONE
    }
  }
}
