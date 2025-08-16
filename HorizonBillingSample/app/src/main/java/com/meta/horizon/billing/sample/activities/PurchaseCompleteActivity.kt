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
import com.meta.horizon.billing.sample.databinding.ActivityPurchaseCompleteBinding

class PurchaseCompleteActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val binding =
        DataBindingUtil.setContentView<ActivityPurchaseCompleteBinding>(
            this,
            R.layout.activity_purchase_complete,
        )
    binding.activity = this
    binding.returnToMainBtn.setOnClickListener { navigateToMainScreen() }
    binding.itemName = this.intent.getStringExtra("itemName")
    binding.quantity = this.intent.getStringExtra("quantity")
    binding.orderDate = this.intent.getStringExtra("orderDate")
  }

  private fun navigateToMainScreen() {
    val intent = Intent(this, MainActivity::class.java)
    startActivity(intent)
    finishAffinity()
  }
}
