/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.fields

sealed interface PhoneCountryPicker {
    data class BuiltIn(
        val presenter: PhoneCountryPickerPresenter? = null,
    ) : PhoneCountryPicker

    data object Disabled : PhoneCountryPicker

    class Custom(
        val show: (PhoneField) -> Unit,
    ) : PhoneCountryPicker
}

expect class PhoneCountryPickerPresenter
