/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.fields.selectable

import androidx.compose.runtime.Composable
import io.github.robinpcrd.kotose.form.compose.fields.selectable.MultipleSelectionField
import io.github.robinpcrd.kotose.form.fields.SelectableField
import io.github.robinpcrd.kotose.form.utils.ComposableFun
import io.github.robinpcrd.kotose.resources.StrRes

class MultipleSelectionField<ValueType>(
    tag: String? = null,
    selectableValue: ValueType,
    labelForValue: StrRes? = null,
) : SelectableField<ValueType>(tag, selectableValue, labelForValue) {
    override var view: ComposableFun = @Composable {
        MultipleSelectionField(field = this)
    }
}
