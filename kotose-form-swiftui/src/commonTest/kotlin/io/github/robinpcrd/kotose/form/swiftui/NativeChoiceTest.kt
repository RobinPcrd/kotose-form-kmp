/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

@file:OptIn(ExperimentalKotoseSwiftUIApi::class)

package io.github.robinpcrd.kotose.form.swiftui

import io.github.robinpcrd.kotose.form.Condition
import io.github.robinpcrd.kotose.form.RequiredFieldRule
import io.github.robinpcrd.kotose.form.dsl.form
import io.github.robinpcrd.kotose.form.dsl.group
import io.github.robinpcrd.kotose.form.fields.DropdownOption
import io.github.robinpcrd.kotose.form.fields.SingleChoiceField
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
import kotlin.test.assertNull
import kotlin.test.assertTrue

class NativeChoiceTest {
    private data class Delivery(val code: Int)

    @Test
    fun observesTypedSelectionOptionsAndValidationAndRejectsStaleOrUnavailableEdits() = runBlocking {
        val first = DropdownOption(Delivery(10), StrRes(text = "Same label"), "first")
        val second = DropdownOption(Delivery(20), StrRes(text = "Same label"), "second")
        val field = SingleChoiceField("delivery", listOf(first, second), StrRes(text = "Delivery"), true)
            .apply { addRule(RequiredFieldRule()) }
        val form = form { +group { +field } }
        val adapter = NativeFormAdapter(form)
        val updates = Channel<NativeFormSnapshot>(Channel.UNLIMITED)
        val collector = launch(start = CoroutineStart.UNDISPATCHED) {
            adapter.snapshots.collect { updates.send(it) }
        }
        suspend fun awaitField(predicate: (NativeFieldSnapshot) -> Boolean): NativeFieldSnapshot = withTimeout(2_000L) {
            var next = updates.receive().fields.single()
            while (!predicate(next)) next = updates.receive().fields.single()
            next
        }
        try {
            val initial = awaitField { true }
            assertEquals(NativeFieldKind.SINGLE_CHOICE, initial.kind)
            assertEquals(listOf("first", "second"), initial.choice?.options?.map { it.id })
            assertNull(initial.choice?.selectedId)
            assertFalse(adapter.validate())
            awaitField { it.messages.isNotEmpty() }
            assertTrue(adapter.selectOption("delivery", "second"))
            awaitField { it.choice?.selectedId == "second" && it.messages.isEmpty() }
            assertEquals(Delivery(20), form.allValues()["delivery"])
            field.possibleValues = listOf(second.copy(label = StrRes(text = "Updated")), first)
            val reordered = awaitField { it.choice?.options?.firstOrNull()?.label == "Updated" }
            assertEquals("second", reordered.choice?.selectedId)
            field.isEnabledCondition = Condition.False
            awaitField { !it.enabled }
            assertFalse(adapter.selectOption("delivery", "first"))
            assertFalse(adapter.selectOption("delivery", null))
            field.isEnabledCondition = Condition.True
            form.setValues(mapOf("delivery" to Delivery(10)))
            awaitField { it.choice?.selectedId == "first" }
            field.possibleValues = listOf(second)
            awaitField { it.choice?.options?.size == 1 && it.choice.selectedId == null }
            assertFalse(adapter.selectOption("delivery", "first"))
            assertTrue(adapter.selectOption("delivery", "second"))
            assertTrue(adapter.selectOption("delivery", null))
            assertNull(field.value)
            field.isVisibleCondition = Condition.False
            assertFalse(adapter.selectOption("delivery", "second"))
            field.isVisibleCondition = Condition.True
            field.remove(reload = true)
            assertFalse(adapter.selectOption("delivery", "second"))
        } finally {
            collector.cancelAndJoin()
        }
    }
}
