/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

@file:OptIn(ExperimentalKotoseSwiftUIApi::class)

package io.github.robinpcrd.kotose.form.swiftui

import io.github.robinpcrd.kotose.form.Condition
import io.github.robinpcrd.kotose.form.RequiredFieldRule
import io.github.robinpcrd.kotose.form.ValidationError
import io.github.robinpcrd.kotose.form.ValidationRule
import io.github.robinpcrd.kotose.form.dsl.form
import io.github.robinpcrd.kotose.form.fields.SelectionOptionField
import io.github.robinpcrd.kotose.form.groups.SelectionGroup
import io.github.robinpcrd.kotose.resources.StrRes
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NativeMultipleSelectionTest {
    private data class Extra(val code: Int)

    @Test
    fun observesTypedSelectionsPerOptionConditionsAndGroupValidationWithoutAComposeView() = runBlocking {
        val first = SelectionOptionField("first", Extra(10), StrRes(text = "Same label"))
            .apply { addRule(RequiredFieldRule()) }
        val second = SelectionOptionField("second", Extra(20), StrRes(text = "Same label"))
        val group = SelectionGroup.Multiple<Extra>("extras").apply {
            addFields(listOf(first, second))
            addRule(object : ValidationRule.Group(ValidationError.RequiredSelectableSection) {
                override fun isValid() = error.takeIf { selectedValues.size < 2 }
            })
        }
        val form = form { +group }
        val adapter = NativeFormAdapter(form)
        val updates = Channel<NativeFormSnapshot>(Channel.UNLIMITED)
        val collector = launch(start = CoroutineStart.UNDISPATCHED) {
            adapter.snapshots.collect { updates.send(it) }
        }
        suspend fun awaitSnapshot(predicate: (NativeFormSnapshot) -> Boolean): NativeFormSnapshot = withTimeout(2_000L) {
            var next = updates.receive()
            while (!predicate(next)) next = updates.receive()
            next
        }
        try {
            val initial = awaitSnapshot { true }
            assertEquals(listOf("first", "second"), initial.fields.map { it.key })
            assertTrue(initial.fields.all { it.kind == NativeFieldKind.MULTIPLE_SELECTION && !it.selected })
            assertFalse(adapter.validate())
            awaitSnapshot {
                it.groups.single().validationMessages.isNotEmpty() &&
                    it.fields.first().validationMessages.isNotEmpty()
            }
            assertTrue(adapter.setSelected("second", true))
            assertTrue(adapter.setSelected("second", true))
            assertEquals(listOf(Extra(20)), group.selectedValues)
            assertTrue(adapter.setSelected("first", true))
            awaitSnapshot {
                it.fields.all { it.selected && it.validationMessages.isEmpty() } &&
                    it.groups.single().validationMessages.isEmpty()
            }
            assertEquals(listOf(Extra(10), Extra(20)), form.allValues()["extras"])
            second.isEnabledCondition = Condition.False
            awaitSnapshot { !it.fields.last().enabled }
            assertFalse(adapter.setSelected("second", false))
            first.isVisibleCondition = Condition.False
            awaitSnapshot { it.fields.map { row -> row.key } == listOf("second") }
            assertFalse(adapter.setSelected("first", false))
            form.setValues(mapOf("extras" to listOf(Extra(10))))
            awaitSnapshot { !it.fields.single().selected }
            assertEquals(listOf(Extra(10)), group.selectedValues)
            first.isVisibleCondition = Condition.True
            second.isEnabledCondition = Condition.True
            first.labelForValue = StrRes(text = "Renamed")
            awaitSnapshot { it.fields.firstOrNull()?.label == "Renamed" }
            first.remove(reload = true)
            awaitSnapshot { it.fields.map { row -> row.key } == listOf("second") }
            assertEquals(emptyList<Extra>(), form.allValues()["extras"])
            assertFalse(adapter.setSelected("first", true))
            assertFalse(adapter.setSelected("missing", true))
            assertFalse(adapter.setToggle("second", true))
            assertFalse(adapter.selectOption("second", "first"))
        } finally {
            collector.cancelAndJoin()
        }
    }
}
