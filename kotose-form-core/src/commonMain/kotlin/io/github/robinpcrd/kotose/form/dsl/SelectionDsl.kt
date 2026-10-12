/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.dsl

import io.github.robinpcrd.kotose.form.fields.SelectionOptionField
import io.github.robinpcrd.kotose.form.groups.SelectionGroup
import io.github.robinpcrd.kotose.resources.StrRes

fun <FieldType> FormBuilder.singleSelectionGroup(
    init: SingleSelectionGroupBuilder<FieldType>.() -> Unit
): SelectionGroup.Single<FieldType> {
    return SingleSelectionGroupBuilder<FieldType>().apply(init).build()
}

fun <FieldType> FormBuilder.multipleSelectionGroup(
    init: MultipleSelectionGroupBuilder<FieldType>.() -> Unit
): SelectionGroup.Multiple<FieldType> {
    return MultipleSelectionGroupBuilder<FieldType>().apply(init).build()
}

fun <ValueType> SelectableGroupBuilder<ValueType>.selectionOption(
    tag: String? = null,
    selectableValue: ValueType,
    labelForValue: StrRes? = null,
    init: SelectionOptionField<ValueType>.() -> Unit = {},
): SelectionOptionField<ValueType> =
    SelectionOptionField(tag, selectableValue, labelForValue).apply(init)
