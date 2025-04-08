/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the Meta Platform Technologies SDK license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.meta.horizon.billing.sample.purchase

import android.app.Activity
import android.content.Context

interface IBillingHandler {
  fun initialize(
      activity: Activity,
      serviceReadyCallback: () -> Unit,
      successCallback: (itemName: String, quantity: String, orderDate: String) -> Unit,
      failureCallback: (errorMsg: String) -> Unit
  )

  fun requestProductDetails(
      productId: String,
      productType: CoreProductType,
      successCallback: (result: List<Any>) -> Unit,
      failureCallback: (errorMsg: String) -> Unit
  )

  fun requestPurchases(
      context: Context,
      productType: CoreProductType,
      successCallback: (result: List<PurchaseEntryDetails>) -> Unit,
      failureCallback: (errorMsg: String) -> Unit
  )

  fun launchBillingFlow(
      activity: Activity,
      productDetails: List<Any>,
  )

  fun acknowledgePurchase(purchaseToken: String)

  fun consumePurchase(purchaseToken: String, listenerCallback: () -> Unit)
}
