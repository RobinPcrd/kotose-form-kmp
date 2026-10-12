/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.fields

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import io.github.robinpcrd.kotose.form.Field
import io.github.robinpcrd.kotose.resources.StrRes

/** Boolean value and label shared by every renderer. */
open class ToggleField(
    tag: String? = null,
    defaultLabel: StrRes,
) : Field<Boolean>(tag) {
    val labelState: State<StrRes>
        field = mutableStateOf(defaultLabel)

    var label: StrRes
        get() = labelState.value
        set(value) {
            labelState.value = value
        }

    override fun setBaseValue(value: Any?) {
        when (value) {
            null -> this.value = null
            is String -> value.toBooleanStrictOrNull()?.let { this.value = it }
            is Boolean -> this.value = value
        }
    }
}
