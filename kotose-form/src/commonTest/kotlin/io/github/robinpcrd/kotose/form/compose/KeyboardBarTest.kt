/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.compose

import io.github.robinpcrd.kotose.form.Condition
import io.github.robinpcrd.kotose.form.dsl.form
import io.github.robinpcrd.kotose.form.dsl.group
import io.github.robinpcrd.kotose.form.dsl.textField
import io.github.robinpcrd.kotose.form.fields.TextField
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class KeyboardBarTest {
    private val labels = KeyboardBarLabels("Previous", "Next", "Done")

    @AfterTest
    fun resetEntry() {
        KeyboardBarEntry.forward = null
    }

    @Test
    fun barMovesInFormOrderAndRecordsTheDirection() {
        val first = TextField("first")
        val last = TextField("last")
        val form = form {
            +group {
                +first
                +textField("disabled") { isEnabledCondition = Condition.False }
                +last
            }
        }
        val onFirst = KeyboardBarContext(form, first, "first", labels, done = {})
        assertFalse(onFirst.hasPrevious)
        assertTrue(onFirst.hasNext)
        onFirst.next()
        assertEquals(last, form.currentFocus)
        assertEquals(true, KeyboardBarEntry.forward)

        val onLast = KeyboardBarContext(form, last, "last", labels, done = {})
        assertFalse(onLast.hasNext)
        onLast.previous()
        assertEquals(first, form.currentFocus)
        assertEquals(false, KeyboardBarEntry.forward)
    }

    @Test
    fun phoneRowInputsComeBeforeTheOtherFields() {
        val phone = TextField("phone")
        val form = form { +group { +phone } }
        var moved = ""
        val onDialCode = KeyboardBarContext(form, phone, "phone-dial-code", labels, done = {}, nextInput = { moved = "number" })
        assertTrue(onDialCode.hasNext)
        onDialCode.next()
        assertEquals("number", moved)
        assertEquals(null, KeyboardBarEntry.forward)
    }

    @Test
    fun barEntersAPhoneRowOnTheDialCodeForwardAndOnTheNumberBack() {
        assertTrue(focusesNumber(dialCode = null, forward = true))
        assertFalse(focusesNumber(dialCode = "33", forward = true))
        assertTrue(focusesNumber(dialCode = "", forward = false))
        assertTrue(focusesNumber(dialCode = "33", forward = null))
        assertFalse(focusesNumber(dialCode = "", forward = null))
    }
}
