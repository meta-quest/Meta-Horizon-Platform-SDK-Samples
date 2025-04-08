// Copyright (c) Meta Platforms, Inc. and affiliates.
//
// This source code is licensed under the Meta Platform Technologies SDK license found in the
// LICENSE file in the root directory of this source tree.

import java.util.Properties

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.jetbrains.kotlin.android)
  id("kotlin-kapt")
  id("com.google.dagger.hilt.android")
}

android {
  namespace = "com.meta.horizon.billing.sample"
  compileSdk = 35
  defaultConfig {
    applicationId = "com.meta.horizon.billing.sample"
    minSdk = 29
    //noinspection ExpiredTargetSdkVersion
    targetSdk = 32
    versionCode = 1
    versionName = "1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  buildTypes {
    release {
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
    getByName("debug") { signingConfig = signingConfigs.getByName("debug") }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
  }
  kotlinOptions { jvmTarget = "1.8" }

  buildFeatures {
    buildConfig = true
    compose = true
    dataBinding = true
  }

  composeOptions { kotlinCompilerExtensionVersion = "1.5.1" }
  flavorDimensions += listOf("devices")
  productFlavors {
    /**
     * Load API values from local properties file (excluded via .gitignore), retrieving the
     * appropriate value for the different Product Flavors (see the Quest App ID topic below).
     */
    val keystoreFile = project.rootProject.file("apikey.properties")
    val properties = Properties()
    properties.load(keystoreFile.inputStream())

    // Create the product variants
    create("mobile") {
      dimension = "devices"
      dependencies {
        implementation(libs.billing)
        implementation(libs.billing.ktx)
      }
      targetSdk = 34
      manifestPlaceholders["orientation"] = "portrait"
    }
    create("quest") {
      dimension = "devices"
      dependencies {
        implementation(libs.horizon.billing.compatibility)
        implementation(libs.android.platform.sdk)
      }
      /** Load the Quest App ID and set the value within our BuildConfig */
      val questAppId = properties.getProperty("QUEST_APP_ID") ?: ""
      buildConfigField(
          type = "String",
          name = "QUEST_APP_ID",
          value = questAppId,
      )
      manifestPlaceholders["orientation"] = "landscape"
    }
  }
}

dependencies {
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.appcompat)
  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.androidx.material3)
  implementation(libs.androidx.constraintlayout)
  implementation(libs.material)
  implementation(libs.guava)
  implementation(libs.hilt.android)
  kapt(libs.hilt.android.compiler)
  testImplementation(libs.junit)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.espresso.core)
}

kapt { correctErrorTypes = true }
