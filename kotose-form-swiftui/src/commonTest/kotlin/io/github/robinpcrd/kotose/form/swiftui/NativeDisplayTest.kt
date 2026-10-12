/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

@file:OptIn(ExperimentalKotoseSwiftUIApi::class)

package io.github.robinpcrd.kotose.form.swiftui

import io.github.robinpcrd.kotose.form.Condition
import io.github.robinpcrd.kotose.form.dsl.actionField
import io.github.robinpcrd.kotose.form.dsl.displayParagraph
import io.github.robinpcrd.kotose.form.dsl.displayText
import io.github.robinpcrd.kotose.form.dsl.form
import io.github.robinpcrd.kotose.form.dsl.group
import io.github.robinpcrd.kotose.form.fields.DisplayTextField
import io.github.robinpcrd.kotose.resources.StrRes
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NativeDisplayTest {
    @Test
    fun displayFieldsResolveTextAndActionsRunInKotlin() = runBlocking {
        var clicks = 0
        val form = form {
            +group {
                +displayText("title", StrRes(text = "Summary"), isTitle = true)
                +displayText("count", StrRes(text = "0 guests"))
                +displayParagraph("lines", listOf(StrRes(text = "First"), StrRes(text = "Second")))
                +actionField("add", StrRes(text = "Add guest")) { clicks++ }
            }
        }
        val counter = form.fields.value.filterIsInstance<DisplayTextField>().last()
        val native = NativeFormAdapter(form)

        val fields = native.snapshots.first().fields
        assertEquals(
            listOf(NativeFieldKind.LABEL, NativeFieldKind.LABEL, NativeFieldKind.PARAGRAPH, NativeFieldKind.ACTION),
            fields.map { it.kind },
        )
        assertEquals("Summary", fields[0].label)
        assertEquals(true, fields[0].display?.isTitle)
        assertEquals(false, fields[1].display?.isTitle)
        assertEquals(listOf("First", "Second"), fields[2].display?.lines)
        assertEquals("Add guest", fields[3].label)

        assertEquals(0, clicks)
        assertTrue(native.performAction("add"))
        assertEquals(1, clicks)
        counter.value = StrRes(text = "$clicks guest")
        assertEquals("1 guest", native.snapshots.first().fields[1].label)

        val action = form.fields.value.last()
        action.isEnabledCondition = Condition.False
        assertFalse(native.performAction("add"))
        action.isEnabledCondition = Condition.True
        action.isVisibleCondition = Condition.False
        assertFalse(native.performAction("add"))
        assertEquals(1, clicks)
        assertFalse(native.performAction("title"))
        assertFalse(native.setText("title", "x"))
        assertFalse(native.setToggle("add", true))
    }
}
