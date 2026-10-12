/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form

import io.github.robinpcrd.kotose.form.fields.validation.BaseValidationErrorField
import io.github.robinpcrd.kotose.form.fields.validation.ValidationErrorField
import io.github.robinpcrd.kotose.form.fields.validation.ValidationErrorGroupField
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

open class Group(
    tag: String? = null
) : Element<ValidationRule.Group>(tag) {
    protected val _fields = mutableListOf<Field<*>>()
    val fields: ImmutableList<Field<*>> get() = _fields.toImmutableList()

    private var _groupErrors: ImmutableList<ValidationError> = persistentListOf()
    internal val groupErrors get() = _groupErrors

    /**
     * Values contributed by this group when submitting the form. Hidden groups and fields
     * contribute nothing unless [includeHidden] is set, mirroring [Form.validate].
     */
    open fun allValues(includeHidden: Boolean = false): Map<String, Any?> = fields
        .filter { includeHidden || (isVisible && it.isVisible) }
        .mapNotNull { field -> field.tag?.let { it to field.submittedValue } }
        .toMap()

    /** A specialized group may accept a value under its own tag. */
    open fun setBaseValue(value: Any?) = Unit

    /** Publish a specialized group's changed value through the existing observers. */
    protected fun notifyValueChange(value: Any?) {
        tag?.let { tag ->
            form?.onFieldValueChange(tag, value)
            form?.enabledMap?.get(tag)?.forEach { it.evaluateEnabled() }
            form?.visibilityMap?.get(tag)?.forEach { it.evaluateVisibility() }
            form?.customConditionMap?.get(tag)?.forEach { it.evaluateCustomCondition() }
        }
    }

    val indexInForm get() = form?.groups?.indexOf(this) ?: -1

    override fun validate(
        triggerSource: ValidationTriggerSource,
        includeHidden: Boolean,
    ): ImmutableList<ValidationError> =
        validate(
            triggerSource = triggerSource,
            skipFieldValidation = false,
            includeHidden = includeHidden
        )

    fun validate(
        triggerSource: ValidationTriggerSource,
        skipFieldValidation: Boolean = false,
        includeHidden: Boolean,
    ): ImmutableList<ValidationError> {
        val previousErrors = _groupErrors
        _groupErrors =
            if ((!shouldValidate(triggerSource, validationOption) && _groupErrors.isEmpty()) ||
                (!isVisible && !includeHidden))
                persistentListOf()
            else
                rules.mapNotNull(ValidationRule.Group::isValid).toImmutableList()
        val shouldNotifyFields = previousErrors != _groupErrors

        @Suppress("SimplifiableCall")
        _fields.filter { it is BaseValidationErrorField }
            .forEach(Field<*>::remove)
        val source = _fields.filterNot { it is BaseValidationErrorField }
        _fields.clear()

        val fieldsErrors = if (!skipFieldValidation)
            source.flatMap { it.validate(triggerSource, includeHidden) }
        else
            source.filter { it.isVisible || includeHidden }.flatMap { it.errors }
        setValidationErrors(_groupErrors + fieldsErrors, triggerSource)

        val newFields = buildList {
            for (field in source) {
                add(field)
                if (field.isVisible || (!field.isVisible && includeHidden))
                    addAll(
                        field.errors.filterNot(ValidationError::isSilent).map {
                            ValidationErrorField(
                                elementKey = field.key,
                                error = it.error,
                                asWarning = it.asWarning,
                            ).apply { isVisibleCondition = Condition(field.isVisible) }
                        }
                    )
            }
            addAll(
                _groupErrors.filterNot(ValidationError::isSilent).map {
                    ValidationErrorGroupField(
                        elementKey = key,
                        error = it.error,
                        asWarning = it.asWarning,
                    )
                }
            )
        }

        _fields.addAll(newFields)
        newFields.forEach {
            // Do not add again already existing fields to avoid crash on duplicate tags
            if (it is BaseValidationErrorField)
                it.wasAddedToGroup(this)
            if (shouldNotifyFields)
                it.onParentValidationChanged(_groupErrors)
        }

        if (triggerSource != ValidationTriggerSource.FORM)
            form?.reload()
        return errors
    }

    override fun wasAddedToForm(form: Form) {
        super.wasAddedToForm(form)
        for (field in fields) {
            field.wasAddedToForm(form)
        }
    }

    override fun evaluateEnabled() {
        super.evaluateEnabled()
        _fields.forEach { it.evaluateEnabled() }
    }

    override fun evaluateVisibility() {
        super.evaluateVisibility()
        _fields.forEach {
            it.evaluateVisibility()
        }
    }

    open fun clear(reload: Boolean = false) {
        val removed = fields
        _fields.apply {
            forEach { form?.elementWasRemoved(it, reload) }
            clear()
        }
        onFieldsRemoved(removed)
    }

    protected open fun onFieldsAdded(
        idx: Int = -1,
        fields: Collection<Field<*>>,
        skipGroupValidation: Boolean = true,
        reload: Boolean = false,
    ) {
        if (!skipGroupValidation && rules.isNotEmpty())
            validate(ValidationTriggerSource.FORM, false)
        if (reload)
            form?.reload()
    }

    fun onFieldVisibilityChanged(field: Field<*>) {
        getFollowingErrorFields(field).forEach {
            it.isVisibleCondition = Condition(field.isVisible)
        }
    }

    /** Recheck cross-field rules after edits, without validating untouched child fields. */
    internal open fun onFieldValueChanged() {
        if (rules.isNotEmpty() &&
            (shouldValidate(ValidationTriggerSource.VALUE_CHANGE, validationOption) || _groupErrors.isNotEmpty())) {
            validate(ValidationTriggerSource.VALUE_CHANGE, skipFieldValidation = true, includeHidden = false)
        }
    }

    private fun mapFieldsWithErrorsOnaAdd(listOfField: Collection<Field<*>>): List<Field<*>> {
        return buildList {
            for (field in listOfField) {
                add(field)
                if (field.validationOption == ValidationOption.ALWAYS) {
                    val errors = field.validate(ValidationTriggerSource.FORM, false)
                    if (errors.isNotEmpty()) {
                        addAll(errors.filterNot { it.isSilent }.map {
                            ValidationErrorField(
                                elementKey = field.key,
                                error = it.error,
                                asWarning = it.asWarning,
                            )
                        })
                    }
                }
            }
        }
    }

    private fun internalAddFields(
        idx: Int = -1,
        fields: Collection<Field<*>>,
        skipGroupValidation: Boolean,
        reload: Boolean,
    ) {
        val fieldsToAdd = mapFieldsWithErrorsOnaAdd(fields)
        if (idx != -1)
            _fields.addAll(idx, fieldsToAdd)
        else
            _fields.addAll(fieldsToAdd)
        onFieldsAdded(idx, fieldsToAdd, skipGroupValidation, reload)
        fieldsToAdd.forEach {
            it.wasAddedToGroup(this)
        }
    }

    fun addField(
        idx: Int,
        field: Field<*>,
        skipGroupValidation: Boolean = true,
        reload: Boolean = false
    ) {
        var previousFieldAtIdx = _fields.getOrNull(idx)
        if (previousFieldAtIdx != null) {
            previousFieldAtIdx = findFieldForError(previousFieldAtIdx)
        }
        val previousFieldHadErrors = previousFieldAtIdx?.let { clearErrorsBeforeAdd(it) } ?: false

        internalAddFields(idx, listOf(field), skipGroupValidation, reload)

        if (previousFieldHadErrors)
            previousFieldAtIdx?.validate(ValidationTriggerSource.FORM, false)
    }

    fun addField(field: Field<*>, skipGroupValidation: Boolean = true, reload: Boolean = false) {
        internalAddFields(
            fields = listOf(field),
            skipGroupValidation = skipGroupValidation,
            reload = reload
        )
    }

    fun addFields(
        idx: Int,
        fields: Collection<Field<*>>,
        skipGroupValidation: Boolean = true,
        reload: Boolean = false,
    ) {
        var previousFieldAtIdx = _fields.getOrNull(idx)
        if (previousFieldAtIdx != null) {
            previousFieldAtIdx = findFieldForError(previousFieldAtIdx)
        }
        val previousFieldHadErrors = previousFieldAtIdx?.let { clearErrorsBeforeAdd(it) } ?: false

        internalAddFields(idx, fields, skipGroupValidation, reload)

        if (previousFieldHadErrors)
            previousFieldAtIdx?.validate(ValidationTriggerSource.FORM, false)
    }

    fun addFields(
        fields: Collection<Field<*>>,
        skipGroupValidation: Boolean = true,
        reload: Boolean = false
    ) {
        internalAddFields(
            fields = fields,
            skipGroupValidation = skipGroupValidation,
            reload = reload
        )
    }

    fun removeField(field: Field<*>, reload: Boolean = false) {
        if (field !in _fields) return
        val fields = getFollowingErrorFields(field).plus(field).toSet()
        _fields.removeAll(fields)
        for (f in fields) {
            form?.elementWasRemoved(f, reload)
        }
        onFieldsRemoved(fields)
    }

    fun removeFields(fields: Collection<Field<*>>, reload: Boolean = false) {
        val allFields = fields.filter { it in _fields }
            .flatMap { getFollowingErrorFields(it).plus(it) }.toSet()
        _fields.removeAll(allFields)
        for (f in allFields) {
            form?.elementWasRemoved(f, reload)
        }
        onFieldsRemoved(allFields)
    }

    /** Allows specialized groups to refresh aggregate values after structural edits. */
    protected open fun onFieldsRemoved(fields: Collection<Field<*>>) = Unit

    fun updateFieldErrors(field: Field<*>, reload: Boolean = true) {
        clearFollowingErrorFields(field)

        if (field.isVisible) {
            val newFields =
                field.errors.filterNot(ValidationError::isSilent).map { validationError ->
                    ValidationErrorField(
                        elementKey = field.key,
                        error = validationError.error,
                        asWarning = validationError.asWarning,
                    )
                }
            val idx = _fields.indexOf(field)
            if (idx != -1) {
                addFields(idx + 1, newFields, reload = reload)
            }
        }
    }

    private fun clearErrorsBeforeAdd(field: Field<*>): Boolean = if (field.hasErrorsOrWarnings) {
        clearFollowingErrorFields(field)
        true
    } else false


    private fun findFieldForError(field: Field<*>): Field<*>? {
        if (!field.isErrorField)
            return null
        var idx = _fields.indexOf(field)
        if (idx > 0)
            idx -= 1
        else
            return null
        while (idx > -1) {
            if (!_fields[idx].isErrorField)
                return _fields[idx]
            idx -= 1
        }
        return null
    }

    private fun getFollowingErrorFields(field: Field<*>): List<Field<*>> {
        var _idx = _fields.indexOf(field)
        val errorFields = mutableListOf<ValidationErrorField>()
        if (_idx != -1) {
            while (_idx + 1 <= _fields.lastIndex) {
                _idx += 1
                val errorField = _fields[_idx]
                if (errorField is ValidationErrorField && errorField.elementKey == field.key) {
                    (errorField as? ValidationErrorField)?.let {
                        errorFields.add(it)
                    }
                } else break
            }
        }
        return errorFields
    }

    private fun clearFollowingErrorFields(field: Field<*>) {
        removeFields(getFollowingErrorFields(field), true)
    }
}
