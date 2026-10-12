/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.dsl

import io.github.robinpcrd.kotose.form.fields.PhoneNumberField
import io.github.robinpcrd.kotose.resources.StrRes

fun GroupBuilder.phoneNumberField(
    tag: String? = null,
    placeholder: StrRes? = null,
    init: PhoneNumberField.() -> Unit = {},
): PhoneNumberField = PhoneNumberField(tag, placeholder).apply(init)
