/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.fields

import io.github.robinpcrd.kotose.form.Field
import io.github.robinpcrd.kotose.resources.StrRes

/**
 * Read-only text shared by renderers. The value is the displayed text and can change at
 * runtime; [isTitle] asks the renderer for heading emphasis.
 */
open class DisplayTextField(
    tag: String? = null,
    val text: StrRes? = null,
    val isTitle: Boolean = false,
) : Field<StrRes>(tag) {
    init {
        this.value = text
    }

    override fun setBaseValue(value: Any?) {
        this.value = value as? StrRes
    }
}
