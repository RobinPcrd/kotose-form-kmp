/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.compose.fields

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.uikit.LocalUIViewController
import io.github.robinpcrd.kotose.form.fields.LocalDateField
import io.github.robinpcrd.kotose.form.fields.applyPickedDate
import platform.UIKit.UIDatePickerMode

@Composable
actual fun rememberDefaultLocalDatePicker(field: LocalDateField): (() -> Unit)? {
    val presentingViewController = LocalUIViewController.current
    val value by field.valueState

    val currentValue by rememberUpdatedState(value)
    val currentField by rememberUpdatedState(field)

    return {
        showNativeDatePicker(
            presentingViewController = presentingViewController,
            mode = UIDatePickerMode.UIDatePickerModeDate,
            initialDate = (currentValue ?: currentLocalDateTime().date).toNSDate(),
            pickerMode = currentField.iosPickerMode,
        ) { date ->
            currentField.applyPickedDate(date.toLocalDate())
        }
    }
}
