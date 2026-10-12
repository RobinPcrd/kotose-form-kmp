/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.fields

import io.github.robinpcrd.kotose.form.Condition
import io.github.robinpcrd.kotose.form.dsl.actionField
import io.github.robinpcrd.kotose.form.dsl.displayParagraph
import io.github.robinpcrd.kotose.form.dsl.displayText
import io.github.robinpcrd.kotose.form.dsl.form
import io.github.robinpcrd.kotose.form.dsl.group
import io.github.robinpcrd.kotose.resources.StrRes
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DisplayFieldsTest {
    @Test
    fun displayTextAndParagraphHoldTextAndAcceptMatchingBaseValues() {
        val title = DisplayTextField("title", StrRes(text = "Summary"), isTitle = true)
        val paragraph = DisplayParagraphField("lines", listOf(StrRes(text = "a"), StrRes(text = "b")))

        assertEquals(StrRes(text = "Summary"), title.value)
        assertTrue(title.isTitle)
        assertEquals(2, paragraph.value?.size)

        title.setBaseValue(StrRes(text = "Changed"))
        paragraph.setBaseValue(listOf(StrRes(text = "c"), 42))
        assertEquals(StrRes(text = "Changed"), title.value)
        assertEquals<List<StrRes>?>(listOf(StrRes(text = "c")), paragraph.value)

        title.setBaseValue(12)
        paragraph.setBaseValue("no")
        assertNull(title.value)
        assertNull(paragraph.value)
    }

    @Test
    fun actionRunsOnlyWhileVisibleAndEnabledAndSubmitsNothing() {
        var clicks = 0
        val form = form {
            +group {
                +displayText("title", StrRes(text = "Summary"), isTitle = true)
                +displayParagraph("lines", listOf(StrRes(text = "a")))
                +actionField("add", StrRes(text = "Add")) { clicks++ }
            }
        }
        val action = form.fields.value.filterIsInstance<ActionField>().single()

        assertEquals(0, clicks)
        assertTrue(action.perform())
        assertEquals(1, clicks)
        action.isEnabledCondition = Condition.False
        assertFalse(action.perform())
        action.isEnabledCondition = Condition.True
        action.isVisibleCondition = Condition.False
        assertFalse(action.perform())
        assertEquals(1, clicks)

        action.isVisibleCondition = Condition.True
        assertEquals(setOf("title", "lines", "add"), form.allValues().keys)
        assertNull(form.allValues()["add"])
        assertTrue(form.validate().isEmpty())
    }
}
