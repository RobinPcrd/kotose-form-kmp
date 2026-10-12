/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

@file:OptIn(ExperimentalKotoseSwiftUIApi::class)

package io.github.robinpcrd.kotose.form.swiftui

import io.github.robinpcrd.kotose.form.Condition
import io.github.robinpcrd.kotose.form.dsl.form
import io.github.robinpcrd.kotose.form.dsl.group
import io.github.robinpcrd.kotose.form.dsl.multipleSelectionGroup
import io.github.robinpcrd.kotose.form.dsl.selectionOption
import io.github.robinpcrd.kotose.form.dsl.singleChoiceField
import io.github.robinpcrd.kotose.form.dsl.singleSelectionGroup
import io.github.robinpcrd.kotose.form.fields.DropdownOption
import io.github.robinpcrd.kotose.form.fields.SelectionPresentation
import io.github.robinpcrd.kotose.form.groups.SelectionGroup
import io.github.robinpcrd.kotose.resources.StrRes
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class NativeSingleSelectionTest {
    private data class Plan(val code: Int)

    @Test
    fun selectionGroupTitleReachesTheGroupSnapshot() = runBlocking {
        val form = form {
            +singleSelectionGroup<Plan> {
                tag = "plan"
                title = StrRes(text = "Plan")
                +selectionOption("plan-basic", Plan(1), StrRes(text = "Basic"))
            }
        }
        assertEquals("Plan", NativeFormAdapter(form).snapshots.first().groups.single().title)
    }

    @Test
    fun snapshotsCarryTheIosPresentationOnEveryHost() = runBlocking {
        val form = form {
            +singleSelectionGroup<Plan> {
                tag = "plan"
                title = StrRes(text = "Plan")
                iosPresentation = SelectionPresentation.Page
                +selectionOption("plan-basic", Plan(1), StrRes(text = "Basic"))
            }
            +group {
                +singleChoiceField(
                    tag = "size",
                    possibleValues = listOf(DropdownOption(Plan(1), StrRes(text = "Small"), "small")),
                    presentation = SelectionPresentation.Inline,
                    iosPresentation = SelectionPresentation.Sheet,
                )
            }
            +singleSelectionGroup<Plan> {
                +selectionOption("other", Plan(2), StrRes(text = "Other"))
            }
            +multipleSelectionGroup<Plan> {
                title = StrRes(text = "Extras")
                presentation = SelectionPresentation.Sheet
                +selectionOption("extra", Plan(3), StrRes(text = "Extra"))
            }
        }
        val groups = NativeFormAdapter(form).snapshots.first().groups
        assertEquals(SelectionPresentation.Page, groups[0].presentation)
        assertEquals(SelectionPresentation.Sheet, groups[1].fields.single().choice?.presentation)
        assertEquals(SelectionPresentation.Inline, groups[1].presentation)
        assertEquals(SelectionPresentation.Inline, groups[2].presentation)
        assertEquals(SelectionPresentation.Sheet, groups[3].presentation)
    }

    @Test
    fun optionSubtitlesReachTheSnapshotAndFollowUpdates() = runBlocking {
        val form = form {
            +singleSelectionGroup<Plan> {
                +selectionOption("plan-basic", Plan(1), StrRes(text = "Basic")) { subtitle = StrRes(text = "1 user") }
                +selectionOption("plan-plus", Plan(2), StrRes(text = "Plus"))
            }
        }
        val native = NativeFormAdapter(form)
        assertEquals(listOf("1 user", null), native.snapshots.first().fields.map { it.subtitle })

        (form.groups.single() as SelectionGroup.Single<*>).selectableFields.last().subtitle = StrRes(text = "Up to 5 users")
        assertEquals("Up to 5 users", native.snapshots.first().fields.last().subtitle)
    }

    @Test
    fun singleSelectionMovesBetweenOptionsAndFollowsKotlinPolicies() = runBlocking {
        val form = form {
            +singleSelectionGroup<Plan> {
                tag = "plan"
                isRequired = true
                +selectionOption("plan-basic", Plan(1), StrRes(text = "Basic"))
                +selectionOption("plan-plus", Plan(2), StrRes(text = "Plus"))
            }
        }
        val group = form.groups.single() as SelectionGroup.Single<*>
        val plus = group.selectableFields.last()
        val native = NativeFormAdapter(form)

        val initial = native.snapshots.first()
        assertTrue(initial.fields.all { it.kind == NativeFieldKind.SINGLE_SELECTION && !it.selected })
        assertFalse(native.validate())
        assertEquals(listOf("Please select an option"), native.snapshots.first().groups.single().validationMessages.map { it.text })

        assertTrue(native.setSelected("plan-basic", true))
        assertTrue(native.setSelected("plan-plus", true))
        assertEquals(listOf(false, true), native.snapshots.first().fields.map { it.selected })
        assertEquals(Plan(2), form.allValues()["plan"])
        assertTrue(native.validate())

        // Deselection is disabled by default, so the Compose-style toggle keeps the selection.
        assertFalse(native.setSelected("plan-plus", false))
        assertEquals(Plan(2), form.allValues()["plan"])

        plus.isEnabledCondition = Condition.False
        assertFalse(native.setSelected("plan-plus", true))
        assertTrue(native.setSelected("plan-basic", true))
        assertEquals(Plan(1), form.allValues()["plan"])
        plus.isEnabledCondition = Condition.True
        plus.isVisibleCondition = Condition.False
        assertFalse(native.setSelected("plan-plus", true))
        assertEquals(listOf("plan-basic"), native.snapshots.first().fields.map { it.key })
        plus.isVisibleCondition = Condition.True

        form.setValues(mapOf("plan" to Plan(2)))
        assertEquals(listOf(false, true), native.snapshots.first().fields.map { it.selected })
        form.setValues(mapOf("plan" to null))
        assertEquals(Plan(2), form.allValues()["plan"])
        assertFalse(native.setSelected("missing", true))
        assertFalse(native.selectOption("plan-basic", "plan-plus"))
    }

    @Test
    fun deselectionClearsWhenTheGroupAllowsIt() = runBlocking {
        val form = form {
            +singleSelectionGroup<Plan> {
                tag = "plan"
                enableDeselection = true
                +selectionOption("plan-basic", Plan(1), StrRes(text = "Basic"))
            }
        }
        val native = NativeFormAdapter(form)
        assertTrue(native.setSelected("plan-basic", true))
        assertTrue(native.setSelected("plan-basic", false))
        assertNull(form.allValues()["plan"])
        assertFalse(native.snapshots.first().fields.single().selected)
    }
}
