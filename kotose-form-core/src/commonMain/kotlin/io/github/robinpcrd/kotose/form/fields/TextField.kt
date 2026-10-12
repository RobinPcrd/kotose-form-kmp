/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.fields

import io.github.robinpcrd.kotose.form.FocusableField
import io.github.robinpcrd.kotose.resources.StrRes

/** Text value and validation shared by every renderer. */
open class TextField(
    tag: String? = null,
    val label: StrRes? = null,
    val placeholder: StrRes? = label,
    val lineLimits: TextLineLimits = TextLineLimits.SingleLine,
) : FocusableField<String>(tag) {
    override fun setBaseValue(value: Any?) {
        this.value = value as? String
    }

    fun setValue(value: String?) {
        this.value = value
    }
}
