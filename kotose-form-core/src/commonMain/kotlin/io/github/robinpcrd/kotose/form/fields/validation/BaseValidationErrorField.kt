/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.fields.validation

import io.github.robinpcrd.kotose.form.Field

abstract class BaseValidationErrorField<ValueType>(
    tag: String?,
    val elementKey: String? = null,
) : Field<ValueType>(tag)
