# Quest IAP Sample

This project demonstrates how developers can integrate Quest services to enable in-app purchases (IAP) of
items and subscriptions through the Meta Horizon Store. The included flow demonstrates how to implement
the Meta Horizon service alongside Google Play Billing, enabling developers to port over their existing apps onto Meta Quest and integrate similar services. To get started, see the [Integrate Meta Horizon Billing Compatibility SDK](https://developers.meta.com/horizon/documentation/spatial-sdk/horizon-billing-compatibility-sdk) guide to learn about adding IAP through the Meta Horizon Store.

The following features are covered by this sample:

1. Purchase (single) Durable and (one or more) Consumable items
2. Purchase a subscription
3. Load purchases (containing items purchased, cost, remaining consumable items, etc)

## Project Structure

Core functionality is stored within the `main` folder, while any custom behavior within the `mobile` and `quest` folders. This allows developers to select the "product" to build when creating a Debug or Release version of their app for Android or Meta Quest devices. For more details, see [Build Variants](https://developer.android.com/build/build-variants) from Android.

### Main Folder

This folder contains all of the shared logic, such as activities, interfaces, and core systems to demonstrate IAP within an Android application. Specifically, the `main` folder contains the `IBillingHandler`, allowing us to delegate the IAP implementation depending on the selected Build Variant using [Dependency Injection with Hilt](https://developer.android.com/training/dependency-injection/hilt-android). As such, direct usage of client SDKs is discouraged in this folder and should use our own logic to indicate any commands to the relevant variant.

### Mobile Folder

Contains the `MobileAppPurchaseHandler`, which implements the `IBillingHandler` interface. This folder
uses the Google Play Billing library to handle IAP within the app.

### Quest Folder

Contains the `QuestAppPurchaseHandler`, which implements the `IBillingHandler` interface. This folder
uses the Horizon Billing library to handle IAP within the app. To review the API reference for this library, please see [com.meta.horizon.billingclient.api](https://developers.meta.com/horizon/documentation/spatial-sdk/horizon-billing-compatibility/api-reference/1.0.0/root/com.meta.horizon.billingclient.api) for more details.

*Note: When testing on Meta Quest, update the **QUEST_APP_ID** value within the `apikey.properties` file to match your Meta Horizon Store application.*
