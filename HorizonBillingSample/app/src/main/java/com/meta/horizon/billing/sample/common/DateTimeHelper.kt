/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the Meta Platform Technologies SDK license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.meta.horizon.billing.sample.common

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class DateTimeHelper {
  companion object {
    // Given a timestamp in milliseconds, returns the string representation based on a common format
    fun convertToTimestamp(timeInMillis: Long): String {
      val inputTime = Instant.ofEpochMilli(timeInMillis)
      return commonTimestampFormat(LocalDateTime.ofInstant(inputTime, ZoneId.systemDefault()))
    }

    // Takes the current time and returns a string representation based on a common format
    fun convertToTimestamp(): String {
      val currTime = LocalDateTime.now()
      return commonTimestampFormat(currTime)
    }

    // Returns LocalDateTime requests into a format like "Aug 15 2024 9:30 AM"
    private fun commonTimestampFormat(localTime: LocalDateTime): String {
      return localTime.format(DateTimeFormatter.ofPattern("MMM d yyyy h:mm a"))
    }
  }
}
