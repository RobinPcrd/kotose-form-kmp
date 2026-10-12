/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form

import io.github.robinpcrd.kotose.form.dsl.form
import io.github.robinpcrd.kotose.form.dsl.group
import io.github.robinpcrd.kotose.form.dsl.input
import io.github.robinpcrd.kotose.form.dsl.switch
import io.github.robinpcrd.kotose.form.fields.InputField
import io.github.robinpcrd.kotose.form.fields.LabelField
import io.github.robinpcrd.kotose.form.fields.SwitchField
import io.github.robinpcrd.kotose.resources.StrRes
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FormTest {
    @Test
    fun emptyFormHasNoErrors() {
        assertTrue(Form().validate().isEmpty())
    }

    @Test
    fun validateReportsRequiredFieldAndInsertsErrorRow() {
        val form = Form()
        val group = Group()
        group.addField(LabelField(defaultValue = StrRes(text = "Hey")))
        group.addField(
            InputField(tag = "name", label = StrRes(text = "Name")).apply {
                addRule(RequiredFieldRule())
            }
        )
        form.add(group, reload = true)
        assertEquals(2, form.fields.value.size)

        val errors = form.validate()

        assertEquals(listOf(ValidationError.Required), errors)
        assertEquals(3, form.fields.value.size)
        assertTrue(form.fields.value[2].isErrorField)
        assertEquals(1, form.errorScrollIndex.value)
    }

    @Test
    fun fixingTheValueRemovesTheErrorRowImmediately() {
        val form = form {
            +group {
                +input(tag = "name") { addRule(RequiredFieldRule()) }
            }
        }
        form.validate()
        assertEquals(2, form.fields.value.size)
        val name = assertNotNull(form.getTypedFieldByTag<InputField>("name"))

        name.value = "Spock"

        assertFalse(name.hasErrors)
        assertEquals(1, form.fields.value.size)
        assertTrue(form.validate().isEmpty())
    }

    @Test
    fun allValuesOnlyIncludesTaggedFields() {
        val form = form {
            +group {
                +input(tag = "name") { value = "Spock" }
                +input(label = StrRes(text = "untagged")) { value = "ignored" }
                +switch(tag = "newsletter", label = StrRes(text = "News")) { value = true }
                +input(tag = "empty")
            }
        }

        assertEquals(
            mapOf("name" to "Spock", "newsletter" to true, "empty" to null),
            form.allValues(),
        )
        assertEquals(
            mapOf("name" to "Spock", "newsletter" to true),
            form.allNonNullValues(),
        )
    }

    @Test
    fun setValuesUpdatesFieldsByTag() {
        val form = form {
            +group {
                +input(tag = "name")
                +switch(tag = "ok", label = StrRes(text = "Ok"))
            }
        }

        form.setValues(mapOf("name" to "Kirk", "ok" to "true", "missing" to 1))

        assertEquals("Kirk", form.getTypedFieldByTag<InputField>("name")?.value)
        assertEquals(true, form.getTypedFieldByTag<SwitchField>("ok")?.value)
        assertEquals("Kirk", form.fieldValue<String>("name"))
    }

    @Test
    fun duplicateTagsAcrossGroupsAreRejected() {
        val form = form {
            +group { +input(tag = "name") }
        }
        val duplicate = Group().apply { addField(InputField(tag = "name")) }

        assertFailsWith<IllegalStateException> { form.add(duplicate) }
    }

    @Test
    fun valueChangesNotifyListeners() {
        val form = form {
            +group { +input(tag = "name") }
        }
        val received = mutableListOf<Map<String, Any?>>()
        form.onValueChange = { received.add(it) }
        val listener = object : FormEvent {
            override fun onValueChange(form: Form, values: Map<String, Any?>) {
                received.add(values)
            }
        }
        form.addListener(listener)

        form.getTypedFieldByTag<InputField>("name")?.value = "Uhura"

        val expected: Map<String, Any?> = mapOf("name" to "Uhura")
        assertEquals(listOf(expected, expected), received)

        form.removeListener(listener)
        form.getTypedFieldByTag<InputField>("name")?.value = null

        assertEquals(3, received.size)
        assertEquals(emptyMap<String, Any?>(), received.last())
    }

    @Test
    fun removingAGroupDropsItsFieldsAndTags() {
        val form = form {
            +group {
                tag = "g1"
                +input(tag = "a")
            }
            +group {
                tag = "g2"
                +input(tag = "b")
            }
        }
        val g1 = assertNotNull(form.getGroupByTag("g1"))

        form.remove(g1, reload = true)

        assertEquals(1, form.groups.size)
        assertNull(form.getGroupByTag("g1"))
        assertNull(form.getFieldByTag("a"))
        assertEquals(listOf("b"), form.fields.value.map { it.tag })
    }

    @Test
    fun addWithoutReloadDefersTheFieldState() {
        val form = Form()
        val group = Group().apply { addField(InputField(tag = "a")) }

        form.add(group)

        assertTrue(form.fields.value.isEmpty())
        assertNotNull(form.getFieldByTag("a"))

        form.reload()

        assertEquals(listOf("a"), form.fields.value.map { it.tag })
    }
}
