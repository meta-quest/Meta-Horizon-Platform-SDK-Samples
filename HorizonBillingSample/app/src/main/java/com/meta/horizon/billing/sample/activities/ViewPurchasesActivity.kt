/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the Meta Platform Technologies SDK license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.meta.horizon.billing.sample.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import com.meta.horizon.billing.sample.R
import com.meta.horizon.billing.sample.databinding.ActivityViewPurchasesBinding
import com.meta.horizon.billing.sample.purchase.CoreProductType
import com.meta.horizon.billing.sample.purchase.IBillingHandler
import com.meta.horizon.billing.sample.purchase.PurchaseEntryDetails
import com.meta.horizon.billing.sample.purchase.ViewPurchasesEntry
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ViewPurchasesActivity : AppCompatActivity() {
  @Inject lateinit var billingHandler: IBillingHandler
  private val TAG: String = "ViewPurchasesActivity"

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    billingHandler.initialize(this, ::onServiceReady, ::placeholderSuccess, ::placeholderFailure)

    val binding =
        DataBindingUtil.setContentView<ActivityViewPurchasesBinding>(
            this, R.layout.activity_view_purchases)
    binding.activity = this
    binding.returnBtn.setOnClickListener { navigateToMainScreen() }
  }

  private fun onServiceReady() {
    // showLoadingScreen()
    billingHandler.requestPurchases(
        applicationContext, CoreProductType.INAPP, ::handleOnSuccess, ::handleOnFailure)
  }

  private fun handleOnSuccess(result: List<PurchaseEntryDetails>) {
    hideLoadingScreen()
    Log.d(TAG, "Successful call requesting In App purchases: ${result.count()}")
    val purchaseList = findViewById<LinearLayout>(R.id.purchase_list)
    val context = this.applicationContext

    runOnUiThread {
      result.forEach {
        val purchaseEntryView = ViewPurchasesEntry(context, it, ::onConsumeBtn)
        purchaseList.addView(purchaseEntryView)
      }
    }
  }

  private fun handleOnFailure(errorMsg: String) {
    hideLoadingScreen()
    Log.d(TAG, "Error when requesting In App purchases: $errorMsg")
  }

  private fun placeholderSuccess(itemName: String, quantity: String, orderDate: String) {
    // Do nothing
  }

  private fun placeholderFailure(errorMsg: String) {
    // Do nothing
  }

  private fun onConsumeBtn(purchaseToken: String) {
    showLoadingScreen()
    billingHandler.consumePurchase(purchaseToken) { requestPurchaseHistory() }
  }

  private fun navigateToMainScreen() {
    val intent = Intent(this, MainActivity::class.java)
    startActivity(intent)
    finishAffinity()
  }

  private fun requestPurchaseHistory() {
    runOnUiThread {
      val purchaseList = findViewById<LinearLayout>(R.id.purchase_list)
      purchaseList.removeAllViews()
    }
    billingHandler.requestPurchases(
        applicationContext, CoreProductType.INAPP, ::handleOnSuccess, ::handleOnFailure)
  }

  private fun showLoadingScreen() {
    runOnUiThread {
      val loadingView = findViewById<ConstraintLayout>(R.id.history_loading_view)
      loadingView.visibility = ConstraintLayout.VISIBLE
    }
  }

  private fun hideLoadingScreen() {
    runOnUiThread {
      val loadingView = findViewById<ConstraintLayout>(R.id.history_loading_view)
      loadingView.visibility = ConstraintLayout.GONE
    }
  }
}
