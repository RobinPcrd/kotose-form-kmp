/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form

import io.github.robinpcrd.kotose.resources.StrRes

enum class ValidationOption(val priority: Int) {
    ON_DEMAND(0),
    ON_VALUE_CHANGE(1),
    ALWAYS(2);

    infix fun isAtLeast(opt2: ValidationOption) = isAtLeast(this, opt2)

    companion object {
        fun isAtLeast(opt1: ValidationOption, opt2: ValidationOption) =
            opt1.priority >= opt2.priority
    }
}

/**
 * The built-in errors expose a settable [error] so the app owns their wording, for example
 * `ValidationError.Required.error = R.string.required.toStrRes()` once at startup.
 */
abstract class ValidationError {
    abstract val error: StrRes
    open val isSilent: Boolean = false
    open val asWarning: Boolean = false

    object Required : ValidationError() {
        override var error: StrRes = StrRes(text = "This field is required")
    }

    object RequiredSelectableSection : ValidationError() {
        override var error: StrRes = StrRes(text = "Please select an option")
    }

    object PhoneIsValid : ValidationError() {
        override val asWarning: Boolean = true
        override var error: StrRes = StrRes(text = "Phone number is invalid")
    }

    @Deprecated("Deprecated", level = DeprecationLevel.ERROR)
    object PhoneIncorrectLength : ValidationError() {
        override val error: StrRes
            get() = StrRes(text = "Phone number has incorrect length")
    }
}

sealed class ValidationRule {
    abstract class Group(val error: ValidationError) : ValidationRule() {
        abstract fun isValid(): ValidationError?
    }

    abstract class Field<ValueType> : ValidationRule() {
        abstract fun isValid(value: ValueType?): ValidationError?
    }
}

/** A domain value that can report whether it satisfies a required-value rule. */
interface RequiredValue {
    val isEmptyForRequired: Boolean
}

/**
 * This validation rule makes sure a field is not null or empty.
 *
 * For fields with [ValueType] of [String], it makes sure the string is not blank.
 * Values implementing [RequiredValue] supply their domain-specific empty check.
 *
 * @return null if the field is valid, [ValidationError.Required] otherwise
 */
class RequiredFieldRule<ValueType> : ValidationRule.Field<ValueType>() {
    override fun isValid(value: ValueType?): ValidationError? {
        if (value == null)
            return ValidationError.Required
        if (value is String? && value.isBlank())
            return ValidationError.Required
        if (value is RequiredValue && value.isEmptyForRequired)
            return ValidationError.Required
        return null
    }
}

fun shouldValidate(triggerSource: ValidationTriggerSource, option: ValidationOption) =
    when (triggerSource) {
        ValidationTriggerSource.FORM -> option isAtLeast ValidationOption.ON_DEMAND
        ValidationTriggerSource.VALUE_CHANGE -> option isAtLeast ValidationOption.ON_VALUE_CHANGE
    }
