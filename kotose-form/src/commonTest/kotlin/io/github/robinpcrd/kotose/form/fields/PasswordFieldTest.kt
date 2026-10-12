/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.fields

import io.github.robinpcrd.kotose.form.RequiredFieldRule
import io.github.robinpcrd.kotose.form.dsl.form
import io.github.robinpcrd.kotose.form.dsl.group
import io.github.robinpcrd.kotose.form.dsl.password
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PasswordFieldTest {
    @Test
    fun legacyDslKeepsItsStyledTypeAndUsesSharedPasswordState() {
        lateinit var password: PasswordField
        val form = form {
            +group {
                password = password("password") { addRule(RequiredFieldRule()) }
                +password
            }
        }
        assertIs<SecureTextField>(password)
        assertTrue(form.validate().isNotEmpty())
        password.setValue("  Same value!  ")
        assertEquals("  Same value!  ", form.allValues()["password"])
        assertTrue(form.validate().isEmpty())
        password.setBaseValue(123)
        assertNull(password.value)
    }
}
