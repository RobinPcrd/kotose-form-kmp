/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.compose

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import io.github.robinpcrd.kotose.form.Condition
import io.github.robinpcrd.kotose.form.dsl.form
import io.github.robinpcrd.kotose.form.dsl.group
import io.github.robinpcrd.kotose.form.dsl.multipleSelectionGroup
import io.github.robinpcrd.kotose.form.dsl.selectionOption
import io.github.robinpcrd.kotose.form.dsl.singleChoiceField
import io.github.robinpcrd.kotose.form.dsl.singleSelectionGroup
import io.github.robinpcrd.kotose.form.fields.DropdownOption
import io.github.robinpcrd.kotose.form.fields.SelectionPresentation
import io.github.robinpcrd.kotose.form.fields.SingleChoiceField
import io.github.robinpcrd.kotose.form.groups.SelectionGroup
import io.github.robinpcrd.kotose.resources.StrRes
import kotlin.test.Test
import kotlin.test.assertEquals

/** Runs on iOS, so the iOS presentation applies. */
@OptIn(ExperimentalTestApi::class)
class SelectionPresentationTest {
    private fun planForm(presentation: SelectionPresentation, iosPresentation: SelectionPresentation? = null) = form {
        +singleSelectionGroup<Int> {
            tag = "plan"
            title = StrRes(text = "Plan")
            this.presentation = presentation
            this.iosPresentation = iosPresentation
            +selectionOption("basic", 1, StrRes(text = "Basic"))
            +selectionOption("plus", 2, StrRes(text = "Plus"))
        }
    }

    private fun io.github.robinpcrd.kotose.form.Form.plan() = groups.single() as SelectionGroup.Single<*>

    @Test
    fun eachCollapsedPresentationDrawsOneRowAndClosesOnPick() {
        for (presentation in listOf(SelectionPresentation.Menu, SelectionPresentation.Sheet, SelectionPresentation.Page)) {
            runComposeUiTest {
                val form = planForm(presentation)
                setContent { FormScreen(form = form) }

                onNodeWithTag("plan-title").assertExists()
                onNodeWithTag("basic").assertDoesNotExist()
                onNodeWithTag("plan").performClick()
                onNodeWithTag("plus").performClick()
                onNodeWithTag("basic").assertDoesNotExist()
                assertEquals(2, form.plan().selectedValue, "$presentation")
                onNodeWithText("Plus").assertExists()
            }
        }
    }

    @Test
    fun iosPresentationReplacesTheInlineDefault() = runComposeUiTest {
        val form = planForm(SelectionPresentation.Inline, iosPresentation = SelectionPresentation.Page)
        setContent { FormScreen(form = form) }

        onNodeWithTag("basic").assertDoesNotExist()
        onNodeWithTag("plan").performClick()
        onNodeWithTag("plan-close").performClick()
        onNodeWithTag("basic").assertDoesNotExist()
        assertEquals(null, form.plan().selectedValue)
    }

    @Test
    fun segmentedSelectsOnTapKeepsTheSelectedSegmentAndDisablesUnavailableOptions() = runComposeUiTest {
        val form = form {
            +singleSelectionGroup<Int> {
                tag = "plan"
                title = StrRes(text = "Plan")
                enableDeselection = true
                presentation = SelectionPresentation.Segmented
                +selectionOption("basic", 1, StrRes(text = "Basic"))
                +selectionOption("plus", 2, StrRes(text = "Plus"))
                +selectionOption("pro", 3, StrRes(text = "Pro")) { isEnabledCondition = Condition.False }
            }
        }
        setContent { FormScreen(form = form) }

        onNodeWithTag("plan-title").assertExists()
        onNodeWithTag("pro").assertIsNotEnabled()
        onNodeWithTag("plus").performClick()
        assertEquals(2, form.plan().selectedValue)
        onNodeWithTag("plus").performClick()
        assertEquals(2, form.plan().selectedValue)
        onNodeWithTag("basic").performClick()
        assertEquals(1, form.plan().selectedValue)
    }

    @Test
    fun multipleSelectionSheetTogglesSeveralOptionsAndStaysOpen() = runComposeUiTest {
        val form = form {
            +multipleSelectionGroup<Int> {
                tag = "extras"
                title = StrRes(text = "Extras")
                presentation = SelectionPresentation.Sheet
                +selectionOption("breakfast", 1, StrRes(text = "Breakfast"))
                +selectionOption("parking", 2, StrRes(text = "Parking"))
            }
        }
        setContent { FormScreen(form = form) }
        val extras = form.groups.single() as SelectionGroup.Multiple<*>

        onNodeWithTag("extras").performClick()
        onNodeWithTag("breakfast").performClick()
        onNodeWithTag("parking").performClick()
        assertEquals<List<Any?>>(listOf(1, 2), extras.selectedValues)
        onNodeWithTag("extras-close").performClick()
        onNodeWithTag("breakfast").assertDoesNotExist()
        onNodeWithText("Breakfast, Parking").assertExists()
    }

    @Test
    fun segmentedSingleChoiceFieldSelectsItsOption() = runComposeUiTest {
        val form = form {
            +group {
                +singleChoiceField(
                    "size",
                    listOf(DropdownOption(1, StrRes(text = "Small"), "small"), DropdownOption(2, StrRes(text = "Large"), "large")),
                    StrRes(text = "Size"),
                    presentation = SelectionPresentation.Segmented,
                )
            }
        }
        setContent { FormScreen(form = form) }
        val size = form.fields.value.filterIsInstance<SingleChoiceField<*>>().single()

        onNodeWithTag("size-title").assertExists()
        onNodeWithTag("size-option-large").performClick()
        assertEquals(2, size.value)
    }

    @Test
    fun singleChoiceFieldsRenderInlineAndCollapsed() = runComposeUiTest {
        fun options() = listOf(DropdownOption(1, StrRes(text = "Small"), "small"), DropdownOption(2, StrRes(text = "Large"), "large"))
        val form = form {
            +group {
                +singleChoiceField("inline", options(), StrRes(text = "Size"), presentation = SelectionPresentation.Inline)
                +singleChoiceField("sheet", options(), StrRes(text = "Size"), presentation = SelectionPresentation.Sheet)
            }
        }
        setContent { FormScreen(form = form) }
        val (inline, sheet) = form.fields.value.filterIsInstance<SingleChoiceField<*>>()

        onNodeWithTag("inline-option-large").performClick()
        assertEquals(2, inline.value)
        onNodeWithTag("sheet-option-small").assertDoesNotExist()
        onNodeWithTag("sheet").performClick()
        onNodeWithTag("sheet-option-small").performClick()
        onNodeWithTag("sheet-option-small").assertDoesNotExist()
        assertEquals(1, sheet.value)
    }
}
