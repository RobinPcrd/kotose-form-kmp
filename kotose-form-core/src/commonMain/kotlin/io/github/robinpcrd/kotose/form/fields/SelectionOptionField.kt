/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.fields

import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.robinpcrd.kotose.form.Field
import io.github.robinpcrd.kotose.form.dsl.SelectionGroupDslMarker
import io.github.robinpcrd.kotose.form.groups.SelectionGroup
import io.github.robinpcrd.kotose.resources.StrRes

/** One typed option, with its own conditions and validation, in a selection group. */
@SelectionGroupDslMarker
open class SelectionOptionField<ValueType>(
    tag: String? = null,
    override val selectableValue: ValueType,
    defaultLabelForValue: StrRes? = null,
) : Field<ValueType>(tag), ISelectableField<ValueType> {
    private val _labelForValueState = mutableStateOf(defaultLabelForValue)
    val labelForValueState: State<StrRes?> = _labelForValueState

    var labelForValue: StrRes?
        get() = _labelForValueState.value
        set(value) { _labelForValueState.value = value }

    /** Secondary text under the label, for example what or who the option stands for. */
    var subtitle: StrRes? by mutableStateOf(null)

    @Suppress("UNCHECKED_CAST")
    val selectionGroup get() = parent as? SelectionGroup<ValueType>

    val isSelected: Boolean get() = value != null

    fun select() {
        setSelected(!isSelected)
    }

    /** User action. Programmatic setters remain available for unavailable options. */
    fun setSelected(selected: Boolean): Boolean {
        if (!isVisible || !isEnabled) return false
        val group = selectionGroup
        if (group != null &&
            (this !in group.selectableFields || !group.isVisible || !group.isEnabled)) return false
        if (selected != isSelected) {
            if (group != null) group.select(this)
            else value = if (selected) selectableValue else null
        }
        return isSelected == selected
    }

    override fun setBaseValue(value: Any?) {
        when (val group = selectionGroup) {
            is SelectionGroup.Multiple -> group.setValues(listOf(value))
            is SelectionGroup.Single -> group.setValue(value)
            null -> this.value = selectableValue.takeIf { it == value }
        }
    }
}

interface ISelectableField<ValueType> {
    val selectableValue: ValueType?
}
