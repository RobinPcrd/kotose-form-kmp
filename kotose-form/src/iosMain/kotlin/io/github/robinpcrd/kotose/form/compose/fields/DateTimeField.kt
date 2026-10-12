/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.compose.fields

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.uikit.LocalUIViewController
import io.github.robinpcrd.kotose.form.fields.DateTimeField
import io.github.robinpcrd.kotose.form.fields.applyPickedDateTime
import platform.UIKit.UIDatePickerMode

@Composable
actual fun rememberDefaultDateTimePicker(field: DateTimeField): (() -> Unit)? {
    val presentingViewController = LocalUIViewController.current
    val value by field.valueState

    val currentValue by rememberUpdatedState(value)
    val currentField by rememberUpdatedState(field)

    return {
        showNativeDatePicker(
            presentingViewController = presentingViewController,
            mode = UIDatePickerMode.UIDatePickerModeDateAndTime,
            initialDate = (currentValue ?: currentLocalDateTime()).toNSDate(),
            pickerMode = currentField.iosPickerMode,
        ) { date ->
            val selectedDateTime = date.toLocalDateTime()
            currentField.applyPickedDateTime(
                date = selectedDateTime.date,
                hour = selectedDateTime.hour,
                minute = selectedDateTime.minute,
            )
        }
    }
}
