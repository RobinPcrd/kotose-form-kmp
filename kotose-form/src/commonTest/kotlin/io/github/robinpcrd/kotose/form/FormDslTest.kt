/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form

import io.github.robinpcrd.kotose.form.dsl.buttonField
import io.github.robinpcrd.kotose.form.dsl.form
import io.github.robinpcrd.kotose.form.dsl.group
import io.github.robinpcrd.kotose.form.dsl.input
import io.github.robinpcrd.kotose.form.dsl.multipleSelectionGroup
import io.github.robinpcrd.kotose.form.dsl.password
import io.github.robinpcrd.kotose.form.dsl.selectableField
import io.github.robinpcrd.kotose.form.dsl.singleSelectionGroup
import io.github.robinpcrd.kotose.form.dsl.switch
import io.github.robinpcrd.kotose.form.dsl.title
import io.github.robinpcrd.kotose.form.fields.InputField
import io.github.robinpcrd.kotose.form.fields.LabelField
import io.github.robinpcrd.kotose.form.fields.PasswordField
import io.github.robinpcrd.kotose.form.fields.SelectableField
import io.github.robinpcrd.kotose.resources.StrRes
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class FormDslTest {
    @Test
    fun buildsGroupsAndFieldsInDeclarationOrder() {
        val form = form {
            +group {
                tag = "identity"
                +title(title = StrRes(text = "Who"))
                +input(tag = "name")
                +password(tag = "pwd")
            }
            +group {
                +switch(tag = "terms", label = StrRes(text = "Terms"))
                +buttonField(title = StrRes(text = "Go"))
            }
        }

        assertEquals(listOf("identity", null), form.groups.map { it.tag })
        assertEquals(listOf(null, "name", "pwd", "terms", null), form.fields.value.map { it.tag })
        assertIs<LabelField>(form.fields.value.first())
        assertIs<InputField>(form.getFieldByTag("name"))
        assertIs<PasswordField>(form.getFieldByTag("pwd"))
    }

    @Test
    fun duplicateTagsInAGroupAreRejected() {
        assertFailsWith<IllegalStateException> {
            form {
                +group {
                    +input(tag = "a")
                    +input(tag = "a")
                }
            }
        }
    }

    @Test
    fun singleSelectionGroupKeepsOneValue() {
        val form = form {
            +singleSelectionGroup<YesNo> {
                tag = "yes-no"
                YesNo.entries.forEach { +selectableField(it.tag, it) }
            }
        }
        val group = assertNotNull(form.getSingleSelectionGroupByTag<YesNo>("yes-no"))
        assertNull(group.selectedValue)

        form.getTypedFieldByTag<SelectableField<*>>("yes")?.select()
        assertEquals(YesNo.YES, group.selectedValue)

        form.getTypedFieldByTag<SelectableField<*>>("no")?.select()
        assertEquals(YesNo.NO, group.selectedValue)
        assertEquals(YesNo.NO, form.allValues()["yes-no"])

        form.getTypedFieldByTag<SelectableField<*>>("no")?.select()
        assertEquals(YesNo.NO, group.selectedValue)
    }

    @Test
    fun singleSelectionGroupCanDeselectWhenEnabled() {
        val form = form {
            +singleSelectionGroup<YesNo> {
                tag = "yes-no"
                enableDeselection = true
                YesNo.entries.forEach { +selectableField(it.tag, it) }
            }
        }
        val group = assertNotNull(form.getSingleSelectionGroupByTag<YesNo>("yes-no"))

        form.getTypedFieldByTag<SelectableField<*>>("yes")?.select()
        form.getTypedFieldByTag<SelectableField<*>>("yes")?.select()

        assertNull(group.selectedValue)
    }

    @Test
    fun multipleSelectionGroupCollectsValues() {
        val form = form {
            +multipleSelectionGroup<String> {
                tag = "toppings"
                +selectableField("cheese", "cheese")
                +selectableField("ham", "ham")
            }
        }
        val group = assertNotNull(form.getMultipleSelectionGroupByTag<String>("toppings"))

        form.getTypedFieldByTag<SelectableField<*>>("cheese")?.select()
        form.getTypedFieldByTag<SelectableField<*>>("ham")?.select()

        assertEquals(listOf("cheese", "ham"), group.selectedValues)
        assertEquals(listOf("cheese", "ham"), form.allValues()["toppings"])

        form.getTypedFieldByTag<SelectableField<*>>("cheese")?.select()

        assertEquals(listOf("ham"), group.selectedValues)
    }

    @Test
    fun setValuesSelectsInSelectionGroups() {
        val form = form {
            +singleSelectionGroup<YesNo> {
                tag = "yes-no"
                YesNo.entries.forEach { +selectableField(it.tag, it) }
            }
            +multipleSelectionGroup<String> {
                tag = "toppings"
                +selectableField("cheese", "cheese")
                +selectableField("ham", "ham")
            }
        }

        form.setValues(mapOf("yes-no" to YesNo.NO, "toppings" to listOf("ham")))

        val yesNo = assertNotNull(form.getSingleSelectionGroupByTag<YesNo>("yes-no"))
        val toppings = assertNotNull(form.getMultipleSelectionGroupByTag<String>("toppings"))
        assertEquals(YesNo.NO, yesNo.selectedValue)
        assertEquals(listOf("ham"), toppings.selectedValues)
    }
}
