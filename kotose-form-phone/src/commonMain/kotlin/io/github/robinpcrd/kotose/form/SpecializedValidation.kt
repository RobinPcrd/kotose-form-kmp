/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form

import io.github.robinpcrd.kotose.form.fields.PhoneData
import io.github.robinpcrd.kotose.form.utils.PhoneNumberBackend


/**
 * This validation rule checks if the phone number is possible with [PhoneNumberBackend.current].
 * Without a backend it only checks that the number has a dial code.
 *
 * This rule doesn't return any error if the value is null or the phone number is blank
 *
 * @return null if the phone number is valid, [ValidationError.PhoneIsValid] otherwise
 */
object IsValidPhoneRule : ValidationRule.Field<PhoneData>() {
    override fun isValid(value: PhoneData?): ValidationError? = when {
        value == null -> null
        value.phone.isBlank() -> null
        else -> {
            val backend = PhoneNumberBackend.current
            val possibleNumber = if (backend == null) {
                value.country.code > 0
            } else {
                backend.isPossibleNumber(value.phone, value.country.region)
            }
            if (possibleNumber) null else ValidationError.PhoneIsValid
        }
    }
}
