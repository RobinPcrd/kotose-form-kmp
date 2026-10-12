/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form

import io.github.robinpcrd.kotose.form.dsl.form
import io.github.robinpcrd.kotose.form.dsl.group
import io.github.robinpcrd.kotose.form.dsl.textField
import io.github.robinpcrd.kotose.form.dsl.toggleField
import io.github.robinpcrd.kotose.form.fields.TextField
import io.github.robinpcrd.kotose.form.fields.validation.ValidationErrorGroupField
import io.github.robinpcrd.kotose.resources.StrRes
import androidx.compose.runtime.snapshots.Snapshot
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FormNavigationTest {
    @Test
    fun validateMovesFocusToTheFirstErrorWithoutDroppingIt() {
        val form = form {
            +group {
                +textField("editing")
                +textField("required") { addRule(RequiredFieldRule()) }
            }
        }
        val editing = form.fields.value.first { it.key == "editing" } as FocusableField<*>
        form.onFocusChanged(editing, focused = true)
        var sawNoFocus = false
        val handle = Snapshot.registerApplyObserver { _, _ -> if (form.currentFocus == null) sawNoFocus = true }
        try {
            form.validate()
        } finally {
            handle.dispose()
        }
        assertEquals("required", form.currentFocus?.key)
        assertNull(form.dismissKeyboardEvent.value)
        assertFalse(sawNoFocus, "focus was published as cleared before moving to the error")
        (form.fields.value.first { it.key == "required" } as TextField).value = "ok"
        form.validate()
        assertNull(form.currentFocus)
        assertEquals(Form.DismissKeyboardEvent, form.dismissKeyboardEvent.value)
    }

    @Test
    fun submitUsesVisibleBlockingErrorsInRenderedOrder() {
        val form = form {
            +group {
                +textField("hidden") { isVisibleCondition = Condition.False; addRule(RequiredFieldRule()) }
                +textField("silent") { addRule(rule(message(silent = true))) }
                +textField("warning") { addRule(rule(message(warning = true))) }
                +textField("required") { addRule(RequiredFieldRule()) }
            }.apply { addRule(groupRule()) }
        }
        form.validate(includeHidden = true)
        assertEquals("required", form.navigationRequest.value?.targetKey)
        assertEquals("required", form.currentFocus?.key)
        form.validate(warningAsError = true)
        assertEquals("warning", form.navigationRequest.value?.targetKey)
    }

    @Test
    fun disabledErrorCanBeRevealedButGroupOnlyErrorDoesNotGuessFocus() {
        val disabled = TextField("disabled").apply {
            isEnabledCondition = Condition.False
            addRule(RequiredFieldRule())
        }
        val form = form {
            +group { tag = "group"; +disabled; +textField("editable") }.apply { addRule(groupRule()) }
        }
        form.validate()
        assertEquals("disabled", form.navigationRequest.value?.targetKey)
        assertNull(form.currentFocus)
        disabled.value = "Present"
        form.validate()
        val request = requireNotNull(form.navigationRequest.value)
        assertEquals("group", request.targetKey)
        assertTrue(form.fields.value[form.navigationIndex(request.id)] is ValidationErrorGroupField)
        assertNull(form.currentFocus)
        form.remove(form.groups.single(), reload = true)
        assertNull(form.navigationRequest.value)
    }

    @Test
    fun silentHiddenAndNonBlockingWarningsDoNotNavigate() {
        val form = form {
            +group {
                +textField("hidden") { isVisibleCondition = Condition.False; addRule(RequiredFieldRule()) }
                +textField("silent") { addRule(rule(message(silent = true))) }
                +textField("warning") { addRule(rule(message(warning = true))) }
            }
        }
        assertTrue(form.validate(includeHidden = true).isNotEmpty())
        assertNull(form.navigationRequest.value)
        assertNull(form.currentFocus)
    }

    @Test
    fun requestsSurviveRowInsertionAndRepeatedSubmissionButNotRemoval() {
        val first = TextField("first")
        val target = TextField("target").apply { addRule(RequiredFieldRule()) }
        val group = Group().apply { addFields(listOf(first, target)) }
        val form = form { +group }
        form.validate()
        val request = requireNotNull(form.navigationRequest.value)
        first.addRule(RequiredFieldRule())
        first.validationOption = ValidationOption.ON_VALUE_CHANGE
        first.validate(ValidationTriggerSource.VALUE_CHANGE, false)
        assertEquals(target, form.fields.value[form.navigationIndex(request.id)])
        assertEquals(request, form.navigationRequest.value)
        first.value = "Valid"
        form.validate()
        val repeated = requireNotNull(form.navigationRequest.value)
        assertEquals(request.targetKey, repeated.targetKey)
        assertTrue(repeated.id > request.id)
        assertFalse(form.completeNavigation(request.id))
        assertEquals(repeated, form.navigationRequest.value)
        target.remove(reload = true)
        assertNull(form.currentFocus)
        assertNull(form.navigationRequest.value)
        assertEquals(-1, form.navigationIndex(repeated.id))
    }

    @Test
    fun editsDoNotCreateNavigationAndAcknowledgedRequestsDoNotReplay() {
        val field = TextField("field").apply {
            validationOption = ValidationOption.ON_VALUE_CHANGE
            addRule(RequiredFieldRule())
        }
        val form = form { +group { +field } }
        form.validate()
        assertTrue(form.completeNavigation(requireNotNull(form.navigationRequest.value).id))
        field.value = "Valid"
        field.value = ""
        assertNull(form.navigationRequest.value)
        assertEquals(-1, form.errorScrollIndex.value)
        form.stopEditing(true)
        assertNull(form.currentFocus)
    }

    @Test
    fun nextSkipsUnavailableFieldsAndFocusClearsWhenTargetBecomesUnavailable() {
        val first = TextField("first")
        val last = TextField("last")
        val form = form {
            +group {
                +first
                +textField("hidden") { isVisibleCondition = Condition.False }
                +textField("disabled") { isEnabledCondition = Condition.False }
                +toggleField("toggle", StrRes(text = "Toggle"))
                +last
            }
        }
        assertEquals(first, form.previousFocusableField(last))
        assertNull(form.previousFocusableField(first))
        form.setFocus(first)
        form.focusNext(first)
        assertEquals(last, form.currentFocus)
        form.focusNext(last)
        assertNull(form.currentFocus)
        assertNull(form.navigationRequest.value)
        form.setFocus(last)
        last.isEnabledCondition = Condition.False
        assertNull(form.currentFocus)
        form.setFocus(last)
        assertNull(form.currentFocus)
        last.isEnabledCondition = Condition.True
        form.setFocus(last)
        last.isVisibleCondition = Condition.False
        assertNull(form.currentFocus)
        assertNull(form.navigationRequest.value)
        assertNull(form.nextFocusableField(TextField("foreign")))
        assertNull(form.previousFocusableField(TextField("foreign")))
    }

    private fun message(warning: Boolean = false, silent: Boolean = false) = object : ValidationError() {
        override val error = StrRes(text = "Invalid")
        override val asWarning = warning
        override val isSilent = silent
    }

    private fun rule(error: ValidationError) = object : ValidationRule.Field<String>() {
        override fun isValid(value: String?) = error
    }

    private fun groupRule() = object : ValidationRule.Group(ValidationError.Required) {
        override fun isValid() = error
    }
}
