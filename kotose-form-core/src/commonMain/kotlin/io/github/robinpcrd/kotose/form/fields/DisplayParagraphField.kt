/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.fields

import io.github.robinpcrd.kotose.form.Field
import io.github.robinpcrd.kotose.resources.StrRes
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

/** Read-only lines of text shared by renderers. */
open class DisplayParagraphField(
    tag: String? = null,
    lines: List<StrRes> = emptyList(),
) : Field<ImmutableList<StrRes>>(tag) {
    init {
        if (lines.isNotEmpty()) this.value = lines.toImmutableList()
    }

    override fun setBaseValue(value: Any?) {
        this.value = (value as? Collection<*>)?.filterIsInstance<StrRes>()?.toImmutableList()
    }
}
