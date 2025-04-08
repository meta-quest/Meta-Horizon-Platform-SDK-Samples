/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the Meta Platform Technologies SDK license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.meta.horizon.billing.sample.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.meta.horizon.billing.sample.R
import com.meta.horizon.billing.sample.databinding.ActivityPurchaseErrorBinding

class PurchaseErrorActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val binding =
        DataBindingUtil.setContentView<ActivityPurchaseErrorBinding>(
            this, R.layout.activity_purchase_error)
    binding.activity = this
    binding.returnToMainBtn.setOnClickListener { navigateToMainScreen() }
    binding.purchaseErrorTxt.text = this.intent.getStringExtra("errorMsg")
  }

  private fun navigateToMainScreen() {
    val intent = Intent(this, MainActivity::class.java)
    startActivity(intent)
    finishAffinity()
  }
}
