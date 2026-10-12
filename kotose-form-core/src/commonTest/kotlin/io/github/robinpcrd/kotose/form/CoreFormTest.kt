/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

@file:OptIn(ExperimentalKotoseFormApi::class)

package io.github.robinpcrd.kotose.form

import io.github.robinpcrd.kotose.form.dsl.form
import io.github.robinpcrd.kotose.form.dsl.group
import io.github.robinpcrd.kotose.form.dsl.textField
import io.github.robinpcrd.kotose.form.dsl.toggleField
import io.github.robinpcrd.kotose.form.fields.CustomField
import io.github.robinpcrd.kotose.form.fields.validation.ValidationErrorField
import io.github.robinpcrd.kotose.resources.StrRes
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CoreFormTest {
    @Test
    fun customFieldAndProgrammaticUpdatesShareValidationAndConditions() {
        val rating = RatingField()
        val form = form {
            +group {
                +textField("name") {
                    validationOption = ValidationOption.ON_VALUE_CHANGE
                    addRule(RequiredFieldRule())
                }
                +toggleField("show", StrRes(text = "Show")) { value = false }
                +textField("details") {
                    isVisibleCondition = Condition("show") { it.fieldValue<Boolean>("show") == true }
                    isEnabledCondition = Condition("name") { !it.fieldValue<String>("name").isNullOrBlank() }
                }
                +rating
            }
        }
        val details = requireNotNull(form.getFieldByTag("details"))
        assertFalse(details.isVisible)
        assertFalse(details.isEnabled)
        assertEquals(1, form.validate(scrollToFirstError = false).size)
        assertEquals(1, form.fields.value.filterIsInstance<ValidationErrorField>().size)

        form.setValues(mapOf("name" to "Ada", "show" to true, "details" to "Shared", "rating" to 3))
        assertTrue(details.isVisible)
        assertTrue(details.isEnabled)
        assertTrue(form.validate(scrollToFirstError = false).isEmpty())
        assertTrue(form.fields.value.none { it.isErrorField })
        assertEquals(mapOf("name" to "Ada", "show" to true, "details" to "Shared", "rating" to 3), form.allValues())
        rating.value = 5
        assertEquals(5, form.allValues()["rating"])
        rating.setBaseValue(9)
        assertEquals(5, form.allValues()["rating"])

        form.setValues(mapOf("name" to "", "show" to false))
        assertFalse(details.isVisible)
        assertFalse(details.isEnabled)
        assertTrue(requireNotNull(form.getFieldByTag("name")).errorsState.value.isNotEmpty())
    }

    @Test
    fun specializedValuesKeepRequiredAndSubmissionBehavior() {
        data class Value(val raw: String) : RequiredValue {
            override val isEmptyForRequired: Boolean get() = raw.isBlank()
        }
        val field = object : Field<Value>("domain") {
            override val submittedValue: Any? get() = value?.raw?.uppercase()
            override fun setBaseValue(value: Any?) { this.value = value as? Value }
        }.apply { addRule(RequiredFieldRule()) }
        val form = form { +group { +field } }
        form.setValues(mapOf("domain" to Value("")))
        assertFalse(form.validate(scrollToFirstError = false).isEmpty())
        form.setValues(mapOf("domain" to Value("shared")))
        assertTrue(form.validate(scrollToFirstError = false).isEmpty())
        assertEquals("SHARED", form.allValues()["domain"])
    }

    @Test
    fun toggleConversionPreservesNullAndIgnoresInvalidValues() {
        val form = form { +group { +toggleField("toggle", StrRes(text = "Toggle")) } }
        form.setValues(mapOf("toggle" to "true"))
        assertEquals(true, form.allValues()["toggle"])
        form.setValues(mapOf("toggle" to "invalid"))
        assertEquals(true, form.allValues()["toggle"])
        form.setValues(mapOf("toggle" to false))
        assertEquals(false, form.allValues()["toggle"])
        form.setValues(mapOf("toggle" to null))
        assertEquals(null, form.allValues()["toggle"])
    }

    private class RatingField : Field<Int>("rating"), CustomField {
        override val kind = "rating"
        override val label = StrRes(text = "Rating")
        override fun setBaseValue(value: Any?) {
            (value as? Int)?.takeIf { it in 0..5 }?.let { this.value = it }
        }
    }
}
