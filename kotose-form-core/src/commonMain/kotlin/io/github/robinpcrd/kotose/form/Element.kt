/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import io.github.robinpcrd.kotose.form.dsl.FormDslMarker
import io.github.robinpcrd.kotose.form.dsl.GroupDslMarker
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

typealias GenericElement = Element<*>

enum class ValidationTriggerSource {
    FORM,
    VALUE_CHANGE;
}

@FormDslMarker
@GroupDslMarker
sealed class Element<RuleType : ValidationRule>(
    val tag: String?
) {
    @OptIn(ExperimentalUuidApi::class)
    private val uuid: String by lazy { Uuid.random().toString() }

    /**
     * Element key
     */
    val key: String = tag ?: uuid

    var form: Form? = null
        internal set

    private val _isEnabledCache = mutableStateOf(true)
    val isEnabledState: State<Boolean> = _isEnabledCache
    val isEnabled: Boolean get() = _isEnabledCache.value

    var isEnabledCondition = Condition()
        set(value) {
            removeFromEnabledObserver()
            field = value
            addToEnabledObserver()
            evaluateEnabled()
        }

    private val _isVisibleCache = mutableStateOf(true)
    val isVisibleState: State<Boolean> = _isVisibleCache
    val isVisible: Boolean get() = _isVisibleCache.value

    var isVisibleCondition = Condition()
        set(value) {
            removeFromVisibilityObserver()
            field = value
            addToVisibilityObserver()
            evaluateVisibility()
        }

    var customCondition: CustomCondition? = null
        set(value) {
            removeFromCustomConditionObserver()
            field = value
            addToCustomConditionObserver()
            evaluateCustomCondition()
        }

    var validationOption: ValidationOption = ValidationOption.ON_DEMAND

    private val _rules = mutableListOf<RuleType>()
    val rules get() = _rules.toList()

    private val _errors: MutableState<ImmutableList<ValidationError>> =
        mutableStateOf(persistentListOf())
    val errorsState: State<ImmutableList<ValidationError>> = _errors
    val errors get() = _errors.value

    /** return true if the element has errors, not warnings */
    val hasErrorsState = derivedStateOf { errorsState.value.any { !it.asWarning } }
    val hasWarningsState = derivedStateOf { errorsState.value.any { it.asWarning } }
    val hasErrorsNotSilentState =
        derivedStateOf { errorsState.value.any { !it.asWarning && !it.isSilent } }

    /** return true if the element has errors, not warnings */
    val hasErrors get() = hasErrorsState.value
    val hasErrorsOrWarnings get() = errorsState.value.isNotEmpty()

    val hasErrorsNotSilent get() = hasErrorsNotSilentState.value
    val hasErrorAndVisible get() = hasErrors && isVisible
    val hasErrorOrWarningAndVisible get() = hasErrorsOrWarnings && isVisible

    var onValidationChange: () -> Unit = {}

    abstract fun validate(
        triggerSource: ValidationTriggerSource,
        includeHidden: Boolean
    ): ImmutableList<ValidationError>

    fun addRule(rule: RuleType) {
        _rules.add(rule)
    }

    fun removeRule(rule: RuleType) {
        _rules.remove(rule)
    }

    fun setValidationErrors(errors: List<ValidationError>, triggerSource: ValidationTriggerSource) {
        if (_errors.value == errors)
            return
        _errors.value = errors.toImmutableList()
        onValidationChange()
    }

    fun clearValidationErrors(triggerSource: ValidationTriggerSource) {
        setValidationErrors(emptyList(), triggerSource)
    }

    internal open fun wasAddedToForm(form: Form) {
        this.form = form
        form.addToCache(this)
        addToEnabledObserver()
        addToVisibilityObserver()
        addToCustomConditionObserver()
        evaluateEnabled()
        evaluateVisibility()
        evaluateCustomCondition()
    }

    private fun addToEnabledObserver() {
        form?.addToEnabledObserver(this)
    }

    private fun removeFromEnabledObserver() {
        form?.removeFromEnabledObserver(this)
    }

    internal fun setEnabled(isEnabled: Boolean) {
        _isEnabledCache.value = isEnabled
        form?.onElementAvailabilityChanged(this)
    }

    open fun evaluateEnabled() {
        val form = form ?: return
        val isEnabled = isEnabledCondition.condition.invoke(form)
        setEnabled(isEnabled)
    }

    private fun addToVisibilityObserver() {
        form?.addToVisibilityObserver(this)
    }

    private fun removeFromVisibilityObserver() {
        form?.removeFromVisibilityObserver(this)
    }

    internal fun setVisibility(isVisible: Boolean) {
        _isVisibleCache.value = isVisible
        form?.onElementAvailabilityChanged(this)
    }

    open fun evaluateVisibility() {
        val form = form ?: return
        val isVisible = isVisibleCondition.condition.invoke(form)
        setVisibility(isVisible)
    }

    private fun addToCustomConditionObserver() {
        form?.addToCustomConditionObserver(this)
    }

    private fun removeFromCustomConditionObserver() {
        form?.removeFromCustomConditionObserver(this)
    }

    open fun evaluateCustomCondition() {
        val form = form ?: return
        customCondition?.onEval?.invoke(this, form)
    }
}
