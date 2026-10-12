/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.fields

import io.github.robinpcrd.kotose.resources.StrRes
import io.github.robinpcrd.kotose.form.FocusableField

/** Password text shared by renderers. Masking and reveal state belong to the active UI. */
open class SecureTextField(
    tag: String? = null,
    open val label: StrRes? = null,
    open val placeholder: StrRes? = label,
) : FocusableField<String>(tag) {
    override fun setBaseValue(value: Any?) {
        this.value = value as? String
    }

    open fun setValue(value: String?) {
        this.value = value
    }
}
