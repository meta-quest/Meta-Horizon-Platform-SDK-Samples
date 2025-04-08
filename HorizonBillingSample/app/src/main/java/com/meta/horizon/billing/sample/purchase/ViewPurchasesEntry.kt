/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the Meta Platform Technologies SDK license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.meta.horizon.billing.sample.purchase

import android.annotation.SuppressLint
import android.content.Context
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.meta.horizon.billing.sample.R
import com.meta.horizon.billing.sample.common.DateTimeHelper

@SuppressLint("ViewConstructor")
class ViewPurchasesEntry(
    context: Context,
    purchaseDetails: PurchaseEntryDetails,
    onConsumeBtn: (purchaseToken: String) -> Unit
) : ConstraintLayout(context) {
  init {
    inflate(context, R.layout.view_purchases_entry, this)
    val itemNameTextView = findViewById<TextView>(R.id.item_name)
    itemNameTextView.text = purchaseDetails.itemName
    val quantityTextView = findViewById<TextView>(R.id.quantity_val)
    quantityTextView.text = purchaseDetails.quantity.toString()
    val purchaseDateTextView = findViewById<TextView>(R.id.purchase_date)
    purchaseDateTextView.text = DateTimeHelper.convertToTimestamp(purchaseDetails.purchaseDate)
    if (purchaseDetails.consumable) {
      val consumeBtn = findViewById<Button>(R.id.consume_item_btn)
      consumeBtn.visibility = VISIBLE
      consumeBtn.setOnClickListener { onConsumeBtn(purchaseDetails.purchaseToken) }
    }
  }
}
