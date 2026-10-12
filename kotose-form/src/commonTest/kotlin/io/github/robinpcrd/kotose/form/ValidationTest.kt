/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form

import io.github.robinpcrd.kotose.form.dsl.form
import io.github.robinpcrd.kotose.form.dsl.group
import io.github.robinpcrd.kotose.form.dsl.input
import io.github.robinpcrd.kotose.form.dsl.selectableField
import io.github.robinpcrd.kotose.form.dsl.singleSelectionGroup
import io.github.robinpcrd.kotose.form.fields.InputField
import io.github.robinpcrd.kotose.form.fields.PhoneData
import io.github.robinpcrd.kotose.form.fields.SelectableField
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ValidationTest {
    @Test
    fun requiredRuleRejectsNullBlankAndEmptyPhone() {
        val rule = RequiredFieldRule<Any>()

        assertEquals(ValidationError.Required, rule.isValid(null))
        assertEquals(ValidationError.Required, rule.isValid("   "))
        assertEquals(ValidationError.Required, rule.isValid(PhoneData(belgium, "")))
        assertNull(rule.isValid("Spock"))
        assertNull(rule.isValid(PhoneData(belgium, "471234567")))
        assertNull(rule.isValid(0))
    }

    @Test
    fun validationOptionsAreOrdered() {
        assertTrue(ValidationOption.ALWAYS isAtLeast ValidationOption.ON_DEMAND)
        assertTrue(ValidationOption.ON_VALUE_CHANGE isAtLeast ValidationOption.ON_VALUE_CHANGE)
        assertFalse(ValidationOption.ON_DEMAND isAtLeast ValidationOption.ON_VALUE_CHANGE)

        assertTrue(shouldValidate(ValidationTriggerSource.FORM, ValidationOption.ON_DEMAND))
        assertFalse(shouldValidate(ValidationTriggerSource.VALUE_CHANGE, ValidationOption.ON_DEMAND))
        assertTrue(shouldValidate(ValidationTriggerSource.VALUE_CHANGE, ValidationOption.ON_VALUE_CHANGE))
    }

    @Test
    fun warningsAreNotErrorsUnlessRequested() {
        val form = form {
            +group {
                +input(tag = "name") {
                    value = "Data"
                    addRule(NoLetterARule)
                }
            }
        }

        assertTrue(form.validate().isEmpty())

        val name = assertNotNull(form.getTypedFieldByTag<InputField>("name"))
        assertTrue(name.hasWarningsState.value)
        assertFalse(name.hasErrorsState.value)
        assertTrue(name.hasErrorsOrWarnings)
        assertEquals(2, form.fields.value.size)

        assertEquals(listOf(NoLetterAWarning), form.validate(warningAsError = true))
    }

    @Test
    fun hiddenFieldsAreSkippedUnlessIncluded() {
        val form = form {
            +group {
                +input(tag = "secret") {
                    isVisibleCondition = Condition.False
                    addRule(RequiredFieldRule())
                }
            }
        }

        assertTrue(form.validate().isEmpty())
        assertEquals(1, form.validate(includeHidden = true).size)
    }

    @Test
    fun onValueChangeValidatesWhileTyping() {
        val form = form {
            +group {
                +input(tag = "name") {
                    validationOption = ValidationOption.ON_VALUE_CHANGE
                    addRule(RequiredFieldRule())
                }
            }
        }
        val name = assertNotNull(form.getTypedFieldByTag<InputField>("name"))
        assertFalse(name.hasErrors)

        name.value = "x"
        name.value = ""

        assertTrue(name.hasErrors)
        assertEquals(2, form.fields.value.size)

        name.value = "Spock"

        assertFalse(name.hasErrors)
        assertEquals(1, form.fields.value.size)
    }

    @Test
    fun onDemandFieldsStayQuietUntilValidatedThenClearLive() {
        val form = form {
            +group {
                +input(tag = "name") { addRule(RequiredFieldRule()) }
            }
        }
        val name = assertNotNull(form.getTypedFieldByTag<InputField>("name"))

        name.value = "x"
        name.value = ""

        assertFalse(name.hasErrorsOrWarnings)
        assertEquals(1, form.fields.value.size)

        form.validate()

        assertTrue(name.hasErrors)
        assertEquals(2, form.fields.value.size)

        name.value = "Spock"

        assertFalse(name.hasErrors)
        assertEquals(1, form.fields.value.size)
    }

    @Test
    fun alwaysValidatedFieldsShowErrorsOnBuild() {
        val form = form {
            +group {
                +input(tag = "name") {
                    validationOption = ValidationOption.ALWAYS
                    addRule(RequiredFieldRule())
                }
            }
        }

        assertEquals(2, form.fields.value.size)
        assertTrue(form.fields.value[1].isErrorField)
    }

    @Test
    fun requiredSelectionGroupReportsAGroupError() {
        val form = form {
            +singleSelectionGroup<YesNo> {
                tag = "yes-no"
                isRequired = true
                YesNo.entries.forEach { +selectableField(it.tag, it) }
            }
        }

        assertEquals(listOf(ValidationError.RequiredSelectableSection), form.validate())
        assertEquals(3, form.fields.value.size)

        form.getTypedFieldByTag<SelectableField<*>>(YesNo.YES.tag)?.select()

        assertTrue(form.validate().isEmpty())
        assertEquals(2, form.fields.value.size)
        assertEquals(YesNo.YES, form.allValues()["yes-no"])
    }
}
