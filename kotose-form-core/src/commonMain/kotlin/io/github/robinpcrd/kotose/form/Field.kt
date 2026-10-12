/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import io.github.robinpcrd.kotose.form.dsl.FieldDslMarker
import io.github.robinpcrd.kotose.form.fields.validation.BaseValidationErrorField
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

internal interface FieldEvent {
    fun onValueChanged()
    fun onParentValidationChanged(errors: List<ValidationError>)
}

@FieldDslMarker
abstract class FocusableField<ValueType>(tag: String?) : Field<ValueType>(tag)

@FieldDslMarker
abstract class Field<ValueType>(
    tag: String?
) : Element<ValidationRule.Field<ValueType>>(tag), FieldEvent {
    var parent: Group? = null
        internal set(value) {
            field = value
            if (value != null) {
                _parentErrorState.value = value.groupErrors
            } else {
                _parentErrorState.value = emptyList()
            }
        }

    val indexInGroup get() = parent?.fields?.indexOf(this) ?: -1

    var value: ValueType? = null
        set(value) {
            if (field == value)
                return
            field = value
            _valueState.value = value
            onValueChange(value)
            if (shouldValidate(
                    ValidationTriggerSource.VALUE_CHANGE,
                    validationOption
                ) || hasErrorsOrWarnings
            )
                validate(ValidationTriggerSource.VALUE_CHANGE, false)
            tag?.let { tag ->
                form?.onFieldValueChange(tag, value)
                form?.enabledMap?.get(tag)?.forEach(GenericElement::evaluateEnabled)
                form?.visibilityMap?.get(tag)?.forEach(GenericElement::evaluateVisibility)
                form?.customConditionMap?.get(tag)?.forEach(GenericElement::evaluateCustomCondition)
            }
            parent?.onFieldValueChanged()
        }

    private val _valueState = mutableStateOf(value)
    val valueState: State<ValueType?> = _valueState

    var onValueChange: (ValueType?) -> Unit = {}

    private val _parentErrorState = mutableStateOf(emptyList<ValidationError>())
    val parentErrorState: State<List<ValidationError>> = _parentErrorState
    val parentErrors get() = _parentErrorState.value

    /** Value contributed by this field to [Form.allValues]. */
    open val submittedValue: Any? get() = value

    /**
     * Called by the form on [Form.setValues].
     */
    abstract fun setBaseValue(value: Any?)

    override fun validate(
        triggerSource: ValidationTriggerSource,
        includeHidden: Boolean,
    ): ImmutableList<ValidationError> {
        val skipForOption = !shouldValidate(triggerSource, validationOption) && !hasErrorsOrWarnings
        if (skipForOption || (!isVisible && !includeHidden))
            return persistentListOf()
        val previousErrorCount = errors.size
        val errors = rules.mapNotNull { it.isValid(value) }
        setValidationErrors(errors, triggerSource)
        if (previousErrorCount != 0 || errors.isNotEmpty() && triggerSource != ValidationTriggerSource.FORM)
            parent?.updateFieldErrors(this)
        return errors.toImmutableList()
    }

    internal fun wasAddedToGroup(group: Group) {
        this.parent = group
        group.form?.let {
            wasAddedToForm(it)
        }
    }

    override fun evaluateEnabled() {
        if (parent?.isEnabled == false) {
            setEnabled(false)
            return
        }
        super.evaluateEnabled()
    }

    override fun evaluateVisibility() {
        if (parent?.isVisible == false) {
            setVisibility(false)
            return
        }
        super.evaluateVisibility()
        parent?.onFieldVisibilityChanged(this)
    }

    override fun onParentValidationChanged(errors: List<ValidationError>) {
        _parentErrorState.value = errors
    }

    fun remove(reload: Boolean = false) {
        parent?.removeField(this, reload)
    }

    override fun onValueChanged() = Unit

    val isErrorField: Boolean get() = this is BaseValidationErrorField
}

fun Collection<Field<*>>.ensureSingleTags() {
    val existingTags = mutableListOf<String>()
    for (field in this) {
        field.tag?.let { tag ->
            if (existingTags.contains(tag))
                error("Duplicate tag found")
            existingTags.add(tag)
        }
    }
}
