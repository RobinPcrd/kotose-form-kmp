/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form

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
import kotlin.test.assertFailsWith

class SelectionPresentationTest {
    private val title = StrRes(text = "Plan")

    @Test
    fun defaultsKeepTheCurrentLookOnEveryPlatform() {
        val group = SelectionGroup.Single<Int>()
        val choice = SingleChoiceField(possibleValues = listOf(DropdownOption(1, title, "one")))
        for (ios in listOf(false, true)) {
            assertEquals(SelectionPresentation.Inline, group.presentationFor(ios))
            assertEquals(SelectionPresentation.Menu, choice.presentationFor(ios))
        }
    }

    @Test
    fun iosPresentationReplacesThePresentationOnIosOnly() {
        val group = SelectionGroup.Single<Int>(
            title = title,
            presentation = SelectionPresentation.Inline,
            iosPresentation = SelectionPresentation.Page,
        )
        val choice = SingleChoiceField(
            possibleValues = listOf(DropdownOption(1, title, "one")),
            presentation = SelectionPresentation.Sheet,
            iosPresentation = SelectionPresentation.Inline,
        )
        assertEquals(SelectionPresentation.Inline, group.presentationFor(ios = false))
        assertEquals(SelectionPresentation.Page, group.presentationFor(ios = true))
        assertEquals(SelectionPresentation.Sheet, choice.presentationFor(ios = false))
        assertEquals(SelectionPresentation.Inline, choice.presentationFor(ios = true))
    }

    @Test
    fun nonInlineGroupsRequireATitleWhicheverPlatformSetsThem() {
        assertFailsWith<IllegalArgumentException> {
            SelectionGroup.Single<Int>(presentation = SelectionPresentation.Menu)
        }
        assertFailsWith<IllegalArgumentException> {
            SelectionGroup.Single<Int>(iosPresentation = SelectionPresentation.Sheet)
        }
        assertFailsWith<IllegalArgumentException> {
            SelectionGroup.Single<Int>(presentation = SelectionPresentation.Segmented)
        }
        SelectionGroup.Single<Int>(iosPresentation = SelectionPresentation.Inline)
        SelectionGroup.Single<Int>(title = title, presentation = SelectionPresentation.Page)
    }

    @Test
    fun multipleSelectionIsInlineInASheetOrOnAPage() {
        assertEquals(SelectionPresentation.Inline, SelectionGroup.Multiple<Int>().presentationFor(ios = true))
        val page = SelectionGroup.Multiple<Int>(title = title, presentation = SelectionPresentation.Sheet, iosPresentation = SelectionPresentation.Page)
        assertEquals(SelectionPresentation.Sheet, page.presentationFor(ios = false))
        assertEquals(SelectionPresentation.Page, page.presentationFor(ios = true))
        assertFailsWith<IllegalArgumentException> {
            SelectionGroup.Multiple<Int>(title = title, presentation = SelectionPresentation.Menu)
        }
        assertFailsWith<IllegalArgumentException> {
            SelectionGroup.Multiple<Int>(title = title, iosPresentation = SelectionPresentation.Segmented)
        }
        assertFailsWith<IllegalArgumentException> {
            SelectionGroup.Multiple<Int>(presentation = SelectionPresentation.Page)
        }
        val form = form {
            +multipleSelectionGroup<Int> {
                tag = "extras"
                this.title = this@SelectionPresentationTest.title
                presentation = SelectionPresentation.Sheet
                +selectionOption("breakfast", 1, StrRes(text = "Breakfast"))
            }
        }
        assertEquals(SelectionPresentation.Sheet, form.getMultipleSelectionGroupByTag<Int>("extras")?.presentation)
    }

    @Test
    fun dslPassesPresentationThrough() {
        val form = form {
            +singleSelectionGroup<Int> {
                tag = "plan"
                title = this@SelectionPresentationTest.title
                presentation = SelectionPresentation.Sheet
                iosPresentation = SelectionPresentation.Page
                +selectionOption("free", 1, StrRes(text = "Free"))
            }
            +group {
                +singleChoiceField(
                    tag = "size",
                    possibleValues = listOf(DropdownOption(1, title, "one")),
                    presentation = SelectionPresentation.Inline,
                    iosPresentation = SelectionPresentation.Menu,
                )
            }
        }
        val group = requireNotNull(form.getSingleSelectionGroupByTag<Int>("plan"))
        assertEquals(SelectionPresentation.Sheet, group.presentation)
        assertEquals(SelectionPresentation.Page, group.iosPresentation)
        val choice = form.fields.value.filterIsInstance<SingleChoiceField<*>>().single()
        assertEquals(SelectionPresentation.Inline, choice.presentation)
        assertEquals(SelectionPresentation.Menu, choice.iosPresentation)
    }
}
