/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

@file:OptIn(ExperimentalKotoseSwiftUIApi::class)

package io.github.robinpcrd.kotose.form.swiftui

import io.github.robinpcrd.kotose.form.Condition
import io.github.robinpcrd.kotose.form.RequiredFieldRule
import io.github.robinpcrd.kotose.form.dsl.form
import io.github.robinpcrd.kotose.form.dsl.group
import io.github.robinpcrd.kotose.form.fields.SecureTextField
import io.github.robinpcrd.kotose.form.fields.TextField
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class NativePasswordTest {
    @Test
    fun passwordsUseSecureDispatchAndRetainEditsAcrossAdapterReplacement() = runBlocking {
        val password = SecureTextField("password").apply { addRule(RequiredFieldRule()) }
        val last = TextField("last")
        val form = form { +group { +password; +last } }
        val native = NativeFormAdapter(form)
        assertFalse(native.validate())
        val initial = native.snapshots.first()
        assertEquals(NativeFieldKind.PASSWORD, initial.fields.first().kind)
        assertEquals(NativeFieldKind.TEXT, initial.fields.last().kind)
        assertEquals("password", initial.focusedFieldKey)
        assertEquals("last", initial.fields.first().nextFocusKey)
        assertTrue(native.setText("password", "  Päss 🔑 123!  "))
        assertTrue(native.validate())
        native.stopEditing()
        assertNull(form.currentFocus)
        val replacement = NativeFormAdapter(form)
        assertEquals("  Päss 🔑 123!  ", replacement.snapshots.first().fields.first().textValue)
        form.setValues(mapOf("password" to "replacement"))
        assertEquals("replacement", replacement.snapshots.first().fields.first().textValue)
        replacement.focusNext("password")
        assertEquals(last, form.currentFocus)
        assertTrue(replacement.setText("password", ""))
        assertFalse(replacement.validate())
    }

    @Test
    fun nativeEditsRejectDisabledHiddenAndRemovedPasswords() = runBlocking {
        val password = SecureTextField("password")
        val form = form { +group { +password } }
        val adapter = NativeFormAdapter(form)
        password.isEnabledCondition = Condition.False
        assertFalse(adapter.setText("password", "blocked"))
        form.setValues(mapOf("password" to "preset"))
        assertEquals("preset", adapter.snapshots.first().fields.single().textValue)
        password.isEnabledCondition = Condition.True
        password.isVisibleCondition = Condition.False
        assertFalse(adapter.setText("password", "blocked"))
        assertTrue(adapter.snapshots.first().fields.isEmpty())
        password.remove(reload = true)
        assertFalse(adapter.setText("password", "stale"))
        assertEquals("preset", password.value)
    }
}
