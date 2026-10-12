/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form

import io.github.robinpcrd.kotose.form.dsl.form
import io.github.robinpcrd.kotose.form.dsl.group
import io.github.robinpcrd.kotose.form.dsl.phone
import io.github.robinpcrd.kotose.form.fields.PhoneData
import io.github.robinpcrd.kotose.form.fields.PhoneField
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class FormPhoneValuesTest {
    @Test
    fun allValuesFormatsPhoneNumbersInternationally() {
        val form = form {
            +group {
                +phone(tag = "phone") { value = PhoneData(belgium, "471234567") }
                +phone(tag = "empty")
            }
        }

        assertEquals(mapOf("phone" to "+32471234567", "empty" to null), form.allValues())
    }

    @Test
    fun requiredRuleAcceptsAPhoneWithDigits() {
        val form = form {
            +group {
                +phone(tag = "phone") { addRule(RequiredFieldRule()) }
            }
        }

        assertEquals(listOf(ValidationError.Required), form.validate())

        val phone = assertNotNull(form.getTypedFieldByTag<PhoneField>("phone"))
        phone.value = PhoneData(belgium, "471234567")

        assertEquals(emptyList(), form.validate())
    }
}
