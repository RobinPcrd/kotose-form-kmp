/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.compose.fields

import androidx.compose.runtime.Composable
import io.github.robinpcrd.kotose.form.fields.PhoneCountryPickerPresenter
import io.github.robinpcrd.kotose.form.fields.PhoneNumberField

@Composable
internal expect fun rememberDefaultPhoneCountryPickerPresenter(): PhoneCountryPickerPresenter?

internal expect fun showPhoneCountryPicker(
    field: PhoneNumberField,
    presenter: PhoneCountryPickerPresenter,
)
