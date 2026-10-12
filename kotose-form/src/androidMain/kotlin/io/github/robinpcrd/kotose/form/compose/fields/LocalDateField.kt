/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

@file:JvmName("LocalDateFieldAndroid")

package io.github.robinpcrd.kotose.form.compose.fields

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import io.github.robinpcrd.kotose.form.fields.LocalDateField
import io.github.robinpcrd.kotose.form.fields.applyPickedDate

@Composable
actual fun rememberDefaultLocalDatePicker(field: LocalDateField): (() -> Unit)? {
    val value by field.valueState
    var showDatePicker by remember { mutableStateOf(false) }

    val currentValue by rememberUpdatedState(value)
    val currentField by rememberUpdatedState(field)

    if (showDatePicker) {
        DefaultDatePickerDialog(
            initialDate = currentValue ?: currentLocalDate(),
            onDismissRequest = { showDatePicker = false },
        ) { date ->
            showDatePicker = false
            currentField.applyPickedDate(date)
        }
    }

    return {
        showDatePicker = true
    }
}

private fun currentLocalDate() = currentLocalDateTime().date
