/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form

import io.github.robinpcrd.kotose.form.dsl.form
import io.github.robinpcrd.kotose.form.dsl.group
import io.github.robinpcrd.kotose.form.dsl.multipleSelectionGroup
import io.github.robinpcrd.kotose.form.dsl.selectionOption
import io.github.robinpcrd.kotose.form.dsl.singleSelectionGroup
import io.github.robinpcrd.kotose.form.fields.TextField
import io.github.robinpcrd.kotose.form.groups.SelectionGroup
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class HiddenValuesTest {
    @Test
    fun hiddenFieldsAndGroupsAreExcludedFromValuesUnlessRequested() {
        val hiddenField = TextField("note").apply { value = "kept" }
        val shownField = TextField("name").apply { value = "Ada" }
        val hiddenGroupField = TextField("extra").apply { value = "kept too" }
        val form = form {
            +group { +shownField; +hiddenField }
            +group { +hiddenGroupField }
        }
        hiddenField.isVisibleCondition = Condition.False
        form.groups.last().isVisibleCondition = Condition.False

        assertEquals(mapOf("name" to "Ada"), form.allValues())
        assertEquals(mapOf("name" to "Ada"), form.allNonNullValues())
        assertEquals(
            mapOf("name" to "Ada", "note" to "kept", "extra" to "kept too"),
            form.allValues(includeHidden = true),
        )
        assertEquals("kept", hiddenField.value)
        hiddenField.isVisibleCondition = Condition.True
        assertEquals("kept", form.allValues()["note"])
    }

    @Test
    fun hiddenSelectionsStayInKotlinButAreNotSubmittedOrRequiredSatisfying() {
        val form = form {
            +singleSelectionGroup<Int> {
                tag = "plan"
                isRequired = true
                +selectionOption("plan-basic", 1)
                +selectionOption("plan-pro", 3)
            }
            +multipleSelectionGroup<Int> {
                tag = "extras"
                +selectionOption("extra-a", 10)
                +selectionOption("extra-b", 20)
            }
        }
        val single = form.groups.first() as SelectionGroup.Single<*>
        val multiple = form.groups.last() as SelectionGroup.Multiple<*>
        val pro = single.selectableFields.last()
        val extraB = multiple.selectableFields.last()
        form.setValues(mapOf("plan" to 3, "extras" to listOf(10, 20)))
        assertTrue(form.validate().isEmpty())

        pro.isVisibleCondition = Condition.False
        extraB.isVisibleCondition = Condition.False
        assertNull(form.allValues()["plan"])
        assertEquals(listOf(10), form.allValues()["extras"])
        assertEquals(3, form.allValues(includeHidden = true)["plan"])
        assertEquals(listOf(10, 20), form.allValues(includeHidden = true)["extras"])
        assertEquals(3, single.selectedValue)
        assertFalse(form.validate().isEmpty())
        assertEquals<List<ValidationError>>(listOf(ValidationError.RequiredSelectableSection), single.errors)

        pro.isVisibleCondition = Condition.True
        extraB.isVisibleCondition = Condition.True
        assertEquals(3, form.allValues()["plan"])
        assertEquals(listOf(10, 20), form.allValues()["extras"])
        assertTrue(form.validate().isEmpty())
    }
}
