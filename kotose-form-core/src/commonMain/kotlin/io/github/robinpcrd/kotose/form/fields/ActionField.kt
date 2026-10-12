/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.fields

import io.github.robinpcrd.kotose.form.Field
import io.github.robinpcrd.kotose.resources.StrRes

/**
 * A button whose behavior stays in Kotlin. Renderers call [perform]; the callback runs only
 * while the field is visible and enabled.
 */
open class ActionField(
    tag: String? = null,
    open val title: StrRes,
    val onClick: () -> Unit,
) : Field<Unit>(tag) {
    override fun setBaseValue(value: Any?) = Unit

    fun perform(): Boolean {
        if (!isVisible || !isEnabled) return false
        onClick()
        return true
    }
}
