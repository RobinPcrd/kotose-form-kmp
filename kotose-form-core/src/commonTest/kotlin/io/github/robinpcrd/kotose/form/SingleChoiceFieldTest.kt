/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form

import io.github.robinpcrd.kotose.form.dsl.form
import io.github.robinpcrd.kotose.form.dsl.group
import io.github.robinpcrd.kotose.form.fields.DropdownOption
import io.github.robinpcrd.kotose.form.fields.SingleChoiceField
import io.github.robinpcrd.kotose.resources.StrRes
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SingleChoiceFieldTest {
    private data class Delivery(val code: Int)
    private val standard = DropdownOption(Delivery(10), StrRes(text = "Delivery"), "standard")
    private val express = DropdownOption(Delivery(20), StrRes(text = "Delivery"), "express")

    @Test
    fun identityIsIndependentOfLabelsAndOrderAndSubmissionKeepsTheDomainType() {
        val field = choice()
        val form = form { +group { +field } }
        assertTrue(field.selectOption("express"))
        assertEquals(Delivery(20), form.allValues()["delivery"])
        field.possibleValues = listOf(express.copy(label = StrRes(text = "Updated")), standard)
        assertEquals("express", field.selectedOption.value?.id)
        assertEquals(Delivery(20), field.value)
        assertTrue(field.selectOption("standard"))
        assertEquals(Delivery(10), form.allValues()["delivery"])
    }

    @Test
    fun requiredValidationAndUserClearingUseTheSameValueAndNeverRequestKeyboardFocus() {
        val field = choice().apply { addRule(RequiredFieldRule()) }
        val form = form { +group { +field } }
        assertTrue(form.validate().isNotEmpty())
        assertEquals(field.key, form.navigationRequest.value?.targetKey)
        assertNull(form.currentFocus)
        assertTrue(field.selectOption("standard"))
        assertTrue(field.errors.isEmpty())
        assertTrue(form.validate().isEmpty())
        assertTrue(field.selectOption(null))
        assertNull(form.allValues()["delivery"])
        assertTrue(form.validate().isNotEmpty())
    }

    @Test
    fun removingAnOptionClearsItsValueAndConditionsAndRejectsStaleActions() {
        val field = choice()
        val details = SingleChoiceField("details", listOf(standard)).apply {
            isVisibleCondition = Condition("delivery") { it.getFieldByTag("delivery")?.value == Delivery(20) }
        }
        val form = form { +group { +field; +details } }
        field.selectOption("express")
        assertTrue(details.isVisible)
        field.possibleValues = listOf(standard)
        assertNull(field.value)
        assertNull(form.allValues()["delivery"])
        assertFalse(details.isVisible)
        assertFalse(field.selectOption("express"))
        field.possibleValues = emptyList()
        assertNull(field.selectedOption.value)
        assertFalse(field.selectOption("standard"))
    }

    @Test
    fun unavailableFieldsRejectUserActionsButAllowProgrammaticChanges() {
        val field = choice()
        val form = form { +group { +field } }
        field.selectOption("express")
        field.isEnabledCondition = Condition.False
        assertFalse(field.selectOption("standard"))
        assertFalse(field.selectOption(null))
        assertEquals(Delivery(20), field.value)
        form.setValues(mapOf("delivery" to Delivery(10)))
        assertEquals(Delivery(10), field.value)
        field.isEnabledCondition = Condition.True
        field.isVisibleCondition = Condition.False
        assertFalse(field.selectOption("express"))
        field.isVisibleCondition = Condition.True
        assertTrue(field.selectOption("express"))
    }

    @Test
    fun programmaticValuesAreCheckedAndResetIsIndependentOfUserClearing() {
        val field = SingleChoiceField("delivery", listOf(standard, express), isClearable = false)
        val form = form { +group { +field } }
        form.setValues(mapOf("delivery" to Delivery(20)))
        assertEquals("express", field.selectedOption.value?.id)
        assertFalse(field.selectOption(null))
        assertEquals(Delivery(20), field.value)
        form.setValues(mapOf("delivery" to "20"))
        assertNull(field.value)
        field.setBaseValue(Delivery(99))
        assertNull(field.value)
        field.selectOption("standard")
        form.setValues(mapOf("delivery" to null))
        assertNull(field.value)
    }

    @Test
    fun ambiguousConfigurationIsRejectedBeforeChangingExistingOptions() {
        val field = choice()
        assertFailsWith<IllegalArgumentException> { field.possibleValues = listOf(standard, express.copy(id = standard.id)) }
        assertFailsWith<IllegalArgumentException> { field.possibleValues = listOf(standard, standard.copy(id = "other")) }
        assertFailsWith<IllegalArgumentException> { field.possibleValues = listOf(standard.copy(id = "")) }
        assertEquals(listOf(standard, express), field.possibleValues)
        val input = mutableListOf(standard)
        field.possibleValues = input
        input.add(express)
        assertEquals(listOf(standard), field.possibleValues)
    }

    private fun choice() = SingleChoiceField("delivery", listOf(standard, express), isClearable = true)
}
