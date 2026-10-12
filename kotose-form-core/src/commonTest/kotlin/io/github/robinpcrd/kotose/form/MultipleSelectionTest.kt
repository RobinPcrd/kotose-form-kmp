/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form

import io.github.robinpcrd.kotose.form.dsl.form
import io.github.robinpcrd.kotose.form.dsl.group
import io.github.robinpcrd.kotose.form.dsl.multipleSelectionGroup
import io.github.robinpcrd.kotose.form.dsl.selectionOption
import io.github.robinpcrd.kotose.form.fields.SelectionOptionField
import io.github.robinpcrd.kotose.form.fields.ToggleField
import io.github.robinpcrd.kotose.form.groups.SelectionGroup
import io.github.robinpcrd.kotose.resources.StrRes
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MultipleSelectionTest {
    private data class Extra(val code: Int)
    private val first = Extra(10)
    private val second = Extra(20)

    @Test
    fun neutralDslKeepsTypedValuesInOptionOrderAndSupportsDeselection() {
        val form = form {
            +multipleSelectionGroup<Extra> {
                tag = "extras"
                +selectionOption("first", first, StrRes(text = "Extra"))
                +selectionOption("second", second, StrRes(text = "Extra"))
            }
        }
        val group = requireNotNull(form.getMultipleSelectionGroupByTag<Extra>("extras"))
        group.selectableFields[1].select()
        group.selectableFields[0].select()
        assertEquals(listOf(first, second), form.allValues()["extras"])
        assertEquals(setOf("extras"), form.allValues().keys)
        group.selectableFields[0].select()
        assertEquals(listOf(second), group.selectedValues)
        group.clearSelection()
        assertEquals(emptyList(), group.selectedValues)
    }

    @Test
    fun presetsMatchAvailableTypedValuesWithoutAcceptingForeignPayloads() {
        val group = selection()
        val form = form { +group }
        form.setValues(mapOf("extras" to listOf(second, first, Extra(99), "10")))
        assertEquals(listOf(first, second), group.selectedValues)
        group.setValues(listOf(second, second))
        assertEquals(listOf(second), group.selectedValues)
        group.setValues("20")
        assertEquals(emptyList(), group.selectedValues)
        form.setValues(mapOf("extras" to null))
        assertEquals(emptyList<Extra>(), form.allValues()["extras"])
    }

    @Test
    fun unavailableOptionsAndGroupsRejectUserEditsButKeepProgrammaticSelection() {
        val group = selection()
        val form = form { +group }
        val firstOption = group.selectableFields[0]
        val secondOption = group.selectableFields[1]
        firstOption.isVisibleCondition = Condition.False
        secondOption.isEnabledCondition = Condition.False
        assertFalse(firstOption.setSelected(true))
        assertFalse(secondOption.setSelected(true))
        group.select(firstOption)
        group.select(secondOption)
        assertTrue(group.selectedValues.isEmpty())
        form.setValues(mapOf("extras" to listOf(first, second)))
        assertEquals(listOf(first, second), group.selectedValues)
        assertFalse(firstOption.setSelected(false))
        assertFalse(secondOption.setSelected(false))
        firstOption.isVisibleCondition = Condition.True
        secondOption.isEnabledCondition = Condition.True
        group.isEnabledCondition = Condition.False
        assertFalse(firstOption.setSelected(false))
        group.isEnabledCondition = Condition.True
        group.isVisibleCondition = Condition.False
        assertFalse(firstOption.setSelected(false))
        group.isVisibleCondition = Condition.True
        assertTrue(firstOption.setSelected(false))
        assertEquals(listOf(second), group.selectedValues)
    }

    @Test
    fun directEditsRemovalAndClearRefreshGroupDependenciesAndValidation() {
        val group = selection().apply {
            validationOption = ValidationOption.ON_VALUE_CHANGE
            addRule(object : ValidationRule.Group(ValidationError.RequiredSelectableSection) {
                override fun isValid() = error.takeIf { selectedValues.size < 2 }
            })
        }
        val details = ToggleField("details", StrRes(text = "Details")).apply {
            isVisibleCondition = Condition("extras") { group.selectedValues.contains(second) }
            isEnabledCondition = Condition("second") { it.getFieldByTag("second")?.value != null }
        }
        val form = form { +group; +group { +details } }
        val firstOption = group.selectableFields[0]
        val secondOption = group.selectableFields[1]
        assertFalse(details.isVisible)
        firstOption.value = first
        assertTrue(group.hasErrors)
        secondOption.value = second
        assertTrue(details.isVisible)
        assertFalse(group.hasErrors)
        assertTrue(firstOption.parentErrors.isEmpty())
        secondOption.remove(reload = true)
        assertEquals(listOf(first), form.allValues()["extras"])
        assertFalse(details.isVisible)
        assertFalse(details.isEnabled)
        assertTrue(group.hasErrors)
        assertFalse(secondOption.setSelected(false))
        group.select(secondOption)
        assertEquals(listOf(first), group.selectedValues)
        group.clear(reload = true)
        assertEquals(emptyList<Extra>(), form.allValues()["extras"])
        assertTrue(group.hasErrors)
    }

    @Test
    fun groupRulesSeeCompletePresetsAndOptionRulesRemainIndependent() {
        val group = selection()
        val firstOption = group.selectableFields[0]
        firstOption.addRule(RequiredFieldRule())
        val reads = mutableListOf<List<Extra>>()
        group.validationOption = ValidationOption.ON_VALUE_CHANGE
        group.addRule(object : ValidationRule.Group(ValidationError.RequiredSelectableSection) {
            override fun isValid(): ValidationError? {
                reads.add(group.selectedValues)
                return error.takeIf { group.selectedValues.size != 2 }
            }
        })
        val form = form { +group }
        assertTrue(form.validate().isNotEmpty())
        assertEquals(listOf<ValidationError>(ValidationError.Required), firstOption.errors)
        group.setValues(listOf(first, second))
        assertEquals(listOf(first, second), reads.last())
        assertTrue(firstOption.errors.isEmpty())
        reads.clear()
        group.setValues(listOf(second))
        assertEquals(listOf(listOf(second)), reads)
        assertTrue(group.hasErrors)
        assertEquals(listOf(ValidationError.RequiredSelectableSection), firstOption.parentErrors)
    }

    @Test
    fun addingAPreselectedOptionRefreshesDependenciesAndRemovedOptionsCannotMutateTheGroup() {
        val group = selection()
        val details = ToggleField("details", StrRes(text = "Details")).apply {
            isVisibleCondition = Condition("extras") { group.selectedValues.contains(Extra(30)) }
        }
        val form = form { +group; +group { +details } }
        val third = SelectionOptionField("third", Extra(30)).apply { value = selectableValue }
        group.addField(third, reload = true)
        assertTrue(details.isVisible)
        assertEquals(listOf(Extra(30)), form.allValues()["extras"])
        group.removeFields(listOf(third), reload = true)
        assertFalse(details.isVisible)
        assertFalse(third.setSelected(false))
        assertEquals(emptyList(), group.selectedValues)
    }

    private fun selection() = SelectionGroup.Multiple<Extra>("extras").apply {
        addFields(listOf(
            SelectionOptionField("first", first, StrRes(text = "First")),
            SelectionOptionField("second", second, StrRes(text = "Second")),
        ))
    }
}
