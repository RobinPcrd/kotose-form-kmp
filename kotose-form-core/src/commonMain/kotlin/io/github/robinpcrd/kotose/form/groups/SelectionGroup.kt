/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.groups

import io.github.robinpcrd.kotose.form.Field
import io.github.robinpcrd.kotose.form.Group
import io.github.robinpcrd.kotose.form.dsl.SelectionGroupDslMarker
import io.github.robinpcrd.kotose.form.fields.SelectionOptionField
import io.github.robinpcrd.kotose.form.fields.SelectionPresentation
import io.github.robinpcrd.kotose.resources.StrRes
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

/**
 * [presentation] applies on every platform unless [iosPresentation] replaces it on iOS. Any
 * presentation other than [SelectionPresentation.Inline] shows the options as one row labelled
 * by [title], so it requires one.
 */
@SelectionGroupDslMarker
sealed class SelectionGroup<ValueType>(
    tag: String? = null,
    val title: StrRes? = null,
    val presentation: SelectionPresentation = SelectionPresentation.Inline,
    val iosPresentation: SelectionPresentation? = null,
) : Group(tag) {
    init {
        require(title != null || listOfNotNull(presentation, iosPresentation).all { it == SelectionPresentation.Inline }) {
            "A selection group shown as a row requires a title"
        }
    }

    fun presentationFor(ios: Boolean): SelectionPresentation =
        if (ios) iosPresentation ?: presentation else presentation

    val selectableFields: ImmutableList<SelectionOptionField<ValueType>>
        get() = fields.filterIsInstance<SelectionOptionField<ValueType>>().toImmutableList()

    private var changingSelection = false

    abstract fun clearSelection()

    abstract fun select(field: SelectionOptionField<ValueType>)

    override fun allValues(includeHidden: Boolean): Map<String, Any?> = when {
        !includeHidden && !isVisible -> emptyMap()
        tag != null -> mapOf(tag to selectionValue(includeHidden))
        else -> super.allValues(includeHidden)
    }

    override fun setBaseValue(value: Any?) {
        when (this) {
            is Multiple -> setValues(value)
            is Single -> setValue(value)
        }
    }

    private val selectionValue: Any? get() = selectionValue(includeHidden = true)

    /** Hidden options keep their selection in Kotlin but are not submitted by default. */
    private fun selectionValue(includeHidden: Boolean): Any? {
        val options = selectableFields.filter { includeHidden || it.isVisible }
        return when (this) {
            is Multiple -> options.mapNotNull { it.value }.toImmutableList()
            is Single -> options.firstOrNull { it.value != null }?.value
        }
    }

    protected fun notifyForm() {
        notifyValueChange(selectionValue)
    }

    internal override fun onFieldValueChanged() {
        if (!changingSelection) {
            notifyForm()
            super.onFieldValueChanged()
        }
    }

    protected override fun onFieldsAdded(
        idx: Int,
        fields: Collection<Field<*>>,
        skipGroupValidation: Boolean,
        reload: Boolean,
    ) {
        super.onFieldsAdded(idx, fields, skipGroupValidation, reload)
        if (fields.any { it is SelectionOptionField<*> }) onFieldValueChanged()
    }

    protected override fun onFieldsRemoved(fields: Collection<Field<*>>) {
        if (fields.any { it is SelectionOptionField<*> }) onFieldValueChanged()
    }

    /** Aggregate observers and group rules see a complete programmatic selection. */
    protected fun changeSelection(change: () -> Unit) {
        val wasChanging = changingSelection
        changingSelection = true
        try {
            change()
        } finally {
            changingSelection = wasChanging
            if (!wasChanging) onFieldValueChanged()
        }
    }

    protected fun canSelect(field: SelectionOptionField<ValueType>): Boolean =
        field in selectableFields && isVisible && isEnabled && field.isVisible && field.isEnabled

    /** Shown [SelectionPresentation.Inline], in a sheet or on a page: a menu closes after each tap. */
    class Multiple<ValueType>(
        tag: String? = null,
        title: StrRes? = null,
        presentation: SelectionPresentation = SelectionPresentation.Inline,
        iosPresentation: SelectionPresentation? = null,
    ) : SelectionGroup<ValueType>(tag, title, presentation, iosPresentation) {
        init {
            require(listOfNotNull(presentation, iosPresentation).none { it == SelectionPresentation.Menu || it == SelectionPresentation.Segmented }) {
                "A multiple selection is shown inline, in a sheet or on a page"
            }
        }

        var selectedValues: ImmutableList<ValueType>
            get() = selectableFields.mapNotNull { it.value }.toImmutableList()
            set(value) = setValues(value)

        fun setValues(list: Any?) {
            val values = list as? Collection<*> ?: emptyList<Any?>()
            changeSelection {
                selectableFields.forEach { option ->
                    option.value = option.selectableValue.takeIf { it in values }
                }
            }
        }

        override fun clearSelection() = setValues(emptyList<Any?>())

        override fun select(field: SelectionOptionField<ValueType>) {
            if (canSelect(field)) {
                field.value = if (field.value == null) field.selectableValue else null
            }
        }
    }

    class Single<ValueType>(
        tag: String? = null,
        title: StrRes? = null,
        private val enableDeselection: Boolean = false,
        presentation: SelectionPresentation = SelectionPresentation.Inline,
        iosPresentation: SelectionPresentation? = null,
    ) : SelectionGroup<ValueType>(tag, title, presentation, iosPresentation) {
        var selectedValue: ValueType?
            get() = selectableFields.firstOrNull { it.value != null }?.value
            set(value) {
                if (value == null) {
                    clearSelection()
                } else {
                    selectableFields.firstOrNull { it.selectableValue == value }?.let {
                        applySelection(it)
                    }
                }
            }

        fun setValue(value: Any?) {
            if (value == selectedValue) return
            if (value == null) {
                clearSelection()
            } else {
                selectableFields.firstOrNull { it.selectableValue == value }?.let {
                    applySelection(it)
                }
            }
        }

        override fun clearSelection() {
            if (enableDeselection) changeSelection {
                selectableFields.forEach { it.value = null }
            }
        }

        override fun select(field: SelectionOptionField<ValueType>) {
            if (canSelect(field)) applySelection(field)
        }

        private fun applySelection(field: SelectionOptionField<ValueType>) {
            val fieldValue = field.value
            changeSelection {
                selectableFields.forEach { it.value = null }
                field.value =
                    if (fieldValue != null && enableDeselection) null else field.selectableValue
            }
        }
    }
}
