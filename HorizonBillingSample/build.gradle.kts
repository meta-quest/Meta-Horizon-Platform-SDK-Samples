// Copyright (c) Meta Platforms, Inc. and affiliates.
//
// This source code is licensed under the Meta Platform Technologies SDK license found in the
// LICENSE file in the root directory of this source tree.

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.jetbrains.kotlin.android) apply false
  alias(libs.plugins.compose.compiler) apply false
  id("com.google.dagger.hilt.android") version "2.51.1" apply false
}
