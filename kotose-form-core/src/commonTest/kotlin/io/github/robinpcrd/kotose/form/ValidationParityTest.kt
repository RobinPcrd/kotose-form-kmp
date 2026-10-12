/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form

import io.github.robinpcrd.kotose.form.dsl.form
import io.github.robinpcrd.kotose.form.dsl.group
import io.github.robinpcrd.kotose.form.fields.TextField
import io.github.robinpcrd.kotose.form.fields.validation.ValidationErrorField
import io.github.robinpcrd.kotose.form.fields.validation.ValidationErrorGroupField
import io.github.robinpcrd.kotose.resources.StrRes
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ValidationParityTest {
    @Test
    fun builtInMessageUsesTheAppOverride() {
        val default = ValidationError.Required.error
        ValidationError.Required.error = StrRes(text = "Champ requis")
        try {
            val form = form { +group { +TextField("name").apply { addRule(RequiredFieldRule()) } } }
            form.validate(scrollToFirstError = false)

            val row = form.fields.value.filterIsInstance<ValidationErrorField>().single()
            assertEquals(StrRes(text = "Champ requis"), row.error)
        } finally {
            ValidationError.Required.error = default
        }
    }

    @Test
    fun alwaysValidationPreservesWarningAndSilentFlagsBeforeSubmission() {
        val field = TextField("name").apply {
            validationOption = ValidationOption.ALWAYS
            addRule(fieldRule(Warning))
            addRule(fieldRule(Silent))
        }
        val form = form { +group { +field } }

        val rows = form.fields.value.filterIsInstance<ValidationErrorField>()
        assertEquals(1, rows.size)
        assertEquals(Warning.error, rows.single().error)
        assertTrue(rows.single().asWarning)
        assertEquals(listOf(Silent), form.validate(scrollToFirstError = false))
        assertEquals(listOf(Warning, Silent), form.validate(scrollToFirstError = false, warningAsError = true))
    }

    @Test
    fun onDemandGroupErrorsClearOnEditAndPreserveChildErrors() {
        val first = TextField("first").apply { value = "one" }
        val second = TextField("second").apply { value = "two" }
        val required = TextField("required").apply { addRule(RequiredFieldRule()) }
        val group = Group().apply {
            addFields(listOf(first, second, required))
            addRule(groupRule { if (first.value == second.value) null else Mismatch })
        }
        val form = form { +group }
        second.value = "three"
        assertTrue(group.errors.isEmpty())
        assertEquals(listOf(Mismatch, ValidationError.Required), form.validate(scrollToFirstError = false))

        second.value = "one"

        assertEquals(listOf(ValidationError.Required), group.errors.toList())
        assertTrue(first.parentErrors.isEmpty())
        assertTrue(form.fields.value.none { it is ValidationErrorGroupField })
        assertEquals(1, form.fields.value.filterIsInstance<ValidationErrorField>().size)
        required.value = "complete"
        assertTrue(form.validate(scrollToFirstError = false).isEmpty())
    }

    @Test
    fun onValueChangeGroupReevaluatesCrossFieldRulesWithoutValidatingUntouchedFields() {
        val first = TextField("first")
        val second = TextField("second")
        val untouched = TextField("untouched").apply { addRule(RequiredFieldRule()) }
        val group = Group().apply {
            addFields(listOf(first, second, untouched))
            validationOption = ValidationOption.ON_VALUE_CHANGE
            addRule(groupRule { if (first.value == second.value) null else Mismatch })
        }
        val form = form { +group }

        first.value = "one"
        assertEquals(listOf(Mismatch), group.errors.toList())
        assertTrue(untouched.errors.isEmpty())
        second.value = "one"
        assertTrue(group.errors.isEmpty())
        assertTrue(form.fields.value.none { it.isErrorField })
    }

    @Test
    fun replacingGroupErrorWithWarningRefreshesChildrenEvenWhenCountIsUnchanged() {
        val first = TextField("first")
        val group = Group().apply {
            addField(first)
            validationOption = ValidationOption.ON_VALUE_CHANGE
            addRule(groupRule { if (first.value == "warn") Warning else Mismatch })
        }
        val form = form { +group }
        form.validate(scrollToFirstError = false)
        assertEquals(listOf(Mismatch), first.parentErrors)

        first.value = "warn"

        assertEquals(listOf(Warning), first.parentErrors)
        assertFalse(group.hasErrors)
        assertTrue(form.fields.value.filterIsInstance<ValidationErrorGroupField>().single().asWarning)
        assertTrue(form.validate(scrollToFirstError = false).isEmpty())
    }

    @Test
    fun hiddenOwnersSuppressMessagesAndSubmissionWhileDisabledFieldsStillValidate() {
        val hidden = TextField("hidden").apply {
            isVisibleCondition = Condition.False
            addRule(RequiredFieldRule())
        }
        val disabled = TextField("disabled").apply {
            isEnabledCondition = Condition.False
            addRule(fieldRule(Silent))
        }
        val hiddenGroup = Group("hidden-group").apply {
            addField(TextField("child"))
            isVisibleCondition = Condition.False
            addRule(groupRule { Mismatch })
        }
        val form = form {
            +group {
                +hidden
                +disabled
            }
            +hiddenGroup
        }

        assertEquals(listOf(Silent), form.validate(scrollToFirstError = false))
        assertTrue(form.fields.value.none { it.isErrorField && it.isVisible })
        assertEquals(
            listOf(ValidationError.Required, Silent, Mismatch),
            form.validate(scrollToFirstError = false, includeHidden = true),
        )
        assertTrue(form.fields.value.none { it.isErrorField && it.isVisible })
    }

    private fun fieldRule(error: ValidationError) = object : ValidationRule.Field<String>() {
        override fun isValid(value: String?) = error
    }

    private fun groupRule(check: () -> ValidationError?) = object : ValidationRule.Group(Mismatch) {
        override fun isValid() = check()
    }

    private object Mismatch : ValidationError() {
        override val error = StrRes(text = "Values must match")
    }

    private object Warning : ValidationError() {
        override val error = StrRes(text = "Check this value")
        override val asWarning = true
    }

    private object Silent : ValidationError() {
        override val error = StrRes(text = "Must not be displayed")
        override val isSilent = true
    }
}
