/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form

import io.github.robinpcrd.kotose.form.dsl.form
import io.github.robinpcrd.kotose.form.dsl.group
import io.github.robinpcrd.kotose.form.dsl.secureTextField
import io.github.robinpcrd.kotose.form.fields.SecureTextField
import io.github.robinpcrd.kotose.form.fields.TextField
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SecureTextFieldTest {
    @Test
    fun validationAndSubmissionPreserveExactPasswordText() {
        lateinit var password: SecureTextField
        val form = form {
            +group {
                password = secureTextField("password") { addRule(RequiredFieldRule()) }
                +password
            }
        }
        assertTrue(form.validate().isNotEmpty())
        assertEquals(password, form.currentFocus)
        val exact = "  Päss 🔑 123!  "
        password.setValue(exact)
        assertTrue(password.errors.isEmpty())
        assertTrue(form.validate().isEmpty())
        assertEquals(exact, form.allValues()["password"])
        form.setValues(mapOf("password" to 123))
        assertNull(password.value)
        form.setValues(mapOf("password" to exact))
        assertEquals(exact, password.value)
        password.setValue(null)
        assertTrue(form.validate().isNotEmpty())
    }

    @Test
    fun navigationSkipsUnavailableFieldsAndProgrammaticUpdatesStillWork() {
        val password = SecureTextField("password")
        val disabled = SecureTextField("disabled").apply { isEnabledCondition = Condition.False }
        val hidden = SecureTextField("hidden").apply { isVisibleCondition = Condition.False }
        val last = TextField("last")
        val form = form { +group { +password; +disabled; +hidden; +last } }
        form.setFocus(password)
        form.focusNext(password)
        assertEquals(last, form.currentFocus)
        form.focusNext(last)
        assertNull(form.currentFocus)
        form.setValues(mapOf("disabled" to "preset", "hidden" to "preset"))
        assertEquals("preset", disabled.value)
        assertEquals("preset", hidden.value)
    }
}
