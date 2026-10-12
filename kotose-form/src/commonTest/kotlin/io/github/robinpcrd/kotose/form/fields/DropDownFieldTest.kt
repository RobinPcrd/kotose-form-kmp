/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.fields

import io.github.robinpcrd.kotose.form.dsl.form
import io.github.robinpcrd.kotose.form.dsl.group
import io.github.robinpcrd.kotose.resources.StrRes
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DropDownFieldTest {
    @Test
    fun existingComposeModelUsesSharedStateAndPreservesProgrammaticAssignment() {
        val first = DropdownOption(10, StrRes(text = "First"))
        val second = DropdownOption(20, StrRes(text = "Second"))
        val field = DropDownField("choice", listOf(first, second), isClearable = true)
        val form = form { +group { +field } }
        form.setValues(mapOf("choice" to 20))
        assertEquals(second, field.selectedOption.value)
        field.possibleValues = listOf(second, first)
        assertEquals(20, form.allValues()["choice"])
        field.possibleValues = listOf(first)
        assertNull(field.value)
        field.selectOption(first.id)
        assertEquals(10, form.allValues()["choice"])
    }
}
