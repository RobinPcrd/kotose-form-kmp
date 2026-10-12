/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.fields.validation

import io.github.robinpcrd.kotose.resources.StrRes

open class ValidationErrorGroupField(
    tag: String? = null,
    elementKey: String? = null,
    val error: StrRes,
    val asWarning: Boolean = false,
) : BaseValidationErrorField<Unit>(tag, elementKey) {
    override fun setBaseValue(value: Any?) = Unit
}
