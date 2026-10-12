/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.fields

import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import io.github.robinpcrd.kotose.form.Field
import io.github.robinpcrd.kotose.resources.StrRes
import kotlinx.collections.immutable.toImmutableList
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/** Supply an explicit [id] when recreating options. IDs identify values, never labels or positions. */
@OptIn(ExperimentalUuidApi::class)
data class DropdownOption<T>(
    val value: T,
    val label: StrRes,
    val id: String = Uuid.random().toString(),
)

/**
 * Renderer-neutral dropdown state. Kotlin owns the typed value; renderers select an option ID.
 * [presentation] applies on every platform unless [iosPresentation] replaces it on iOS.
 */
open class SingleChoiceField<T>(
    tag: String? = null,
    possibleValues: List<DropdownOption<T>>,
    val placeholder: StrRes? = null,
    val isClearable: Boolean = false,
    val presentation: SelectionPresentation = SelectionPresentation.Menu,
    val iosPresentation: SelectionPresentation? = null,
) : Field<T>(tag) {
    val possibleValuesState: State<List<DropdownOption<T>>>
        field = mutableStateOf(checkedOptions(possibleValues))

    var possibleValues: List<DropdownOption<T>>
        get() = possibleValuesState.value
        set(options) {
            possibleValuesState.value = checkedOptions(options)
            if (options.none { it.value == value }) value = null
        }

    val selectedOption: State<DropdownOption<T>?> = derivedStateOf {
        possibleValuesState.value.firstOrNull { it.value == valueState.value }
    }

    fun presentationFor(ios: Boolean): SelectionPresentation =
        if (ios) iosPresentation ?: presentation else presentation

    /** User selection; stale options and unavailable fields cannot mutate the form. */
    fun selectOption(id: String?): Boolean {
        if (!isEnabled || !isVisible) return false
        if (id == null) {
            if (!isClearable) return false
            value = null
        } else {
            val option = possibleValues.firstOrNull { it.id == id } ?: return false
            value = option.value
        }
        return true
    }

    /** Programmatic assignment accepts only a current value; null always resets the field. */
    override fun setBaseValue(value: Any?) {
        this.value = possibleValues.firstOrNull { it.value == value }?.value
    }

    private fun checkedOptions(options: List<DropdownOption<T>>): List<DropdownOption<T>> {
        require(options.all { it.id.isNotBlank() && it.value != null }) { "Options require nonblank IDs and nonnull values" }
        require(options.map { it.id }.distinct().size == options.size) { "Option IDs must be unique within a field" }
        require(options.map { it.value }.distinct().size == options.size) { "Option values must be unique within a field" }
        return options.toImmutableList()
    }
}
