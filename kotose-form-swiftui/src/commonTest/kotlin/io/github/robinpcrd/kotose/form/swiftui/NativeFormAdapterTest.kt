/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

@file:OptIn(
    io.github.robinpcrd.kotose.form.ExperimentalKotoseFormApi::class,
    ExperimentalKotoseSwiftUIApi::class,
)

package io.github.robinpcrd.kotose.form.swiftui

import io.github.robinpcrd.kotose.form.Condition
import io.github.robinpcrd.kotose.form.Field
import io.github.robinpcrd.kotose.form.Form
import io.github.robinpcrd.kotose.form.Group
import io.github.robinpcrd.kotose.form.RequiredFieldRule
import io.github.robinpcrd.kotose.form.ValidationError
import io.github.robinpcrd.kotose.form.ValidationOption
import io.github.robinpcrd.kotose.form.ValidationRule
import io.github.robinpcrd.kotose.form.dsl.form
import io.github.robinpcrd.kotose.form.dsl.group
import io.github.robinpcrd.kotose.form.dsl.textField
import io.github.robinpcrd.kotose.form.dsl.toggleField
import io.github.robinpcrd.kotose.form.fields.CustomField
import io.github.robinpcrd.kotose.form.fields.TextField
import io.github.robinpcrd.kotose.form.fields.validation.ValidationErrorField
import io.github.robinpcrd.kotose.form.fields.validation.ValidationErrorGroupField
import io.github.robinpcrd.kotose.resources.StrRes
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NativeFormAdapterTest {
    @Test
    fun customFieldsUseTypedDispatchAndExposeTheLiveKotlinField() = runBlocking {
        val rating = object : Field<Int>("rating"), CustomField {
            override val kind = "rating"
            override val label = StrRes(text = "Rating")

            override fun setBaseValue(value: Any?) {
                this.value = value as? Int
            }
        }
        val form = form { +group { +rating } }
        val adapter = NativeFormAdapter(form)
        val snapshot = adapter.snapshots.first().fields.single()

        assertEquals(NativeFieldKind.CUSTOM, snapshot.kind)
        assertEquals("rating", snapshot.customKind)
        assertEquals("Rating", snapshot.label)
        assertEquals("", snapshot.textValue)

        // The native custom renderer casts the live field and edits typed state directly.
        assertTrue(adapter.customField("rating") === rating)
        rating.value = 4
        assertEquals("4", adapter.snapshots.first().fields.single().textValue)
        assertEquals(4, form.allValues()["rating"])
        assertFalse(adapter.setText("rating", "5"))
        rating.isVisibleCondition = Condition.False
        assertEquals(null, adapter.customField("rating"))
        assertEquals(null, adapter.customField("missing"))
    }

    @Test
    fun focusAndNavigationAreObservedWithoutAComposeRenderer() = runBlocking {
        val first = TextField("first").apply { addRule(RequiredFieldRule()) }
        val last = TextField("last")
        val form = form {
            +group {
                +first
                +textField("hidden") { isVisibleCondition = Condition.False }
                +textField("disabled") { isEnabledCondition = Condition.False }
                +last
            }
        }
        val adapter = NativeFormAdapter(form)
        assertFalse(adapter.validate())
        var snapshot = adapter.snapshots.first()
        val request = requireNotNull(snapshot.navigationRequest)
        assertEquals("first", snapshot.focusedFieldKey)
        assertEquals("last", snapshot.fields.first().nextFocusKey)
        assertTrue(adapter.isNavigationCurrent(request.id))
        assertTrue(adapter.completeNavigation(request.id))
        assertFalse(adapter.isNavigationCurrent(request.id))
        assertTrue(adapter.setText("first", "Ready"))
        assertEquals(null, adapter.snapshots.first().navigationRequest)
        adapter.focusNext("first")
        snapshot = adapter.snapshots.first()
        assertEquals("last", snapshot.focusedFieldKey)
        assertEquals("last", snapshot.navigationRequest?.targetKey)
        assertEquals("first", snapshot.fields.last().previousFocusKey)
        assertEquals(null, snapshot.fields.first().previousFocusKey)
        adapter.focusPrevious("last")
        snapshot = adapter.snapshots.first()
        assertEquals("first", snapshot.focusedFieldKey)
        assertEquals("first", snapshot.navigationRequest?.targetKey)
        adapter.focusNext("first")
        adapter.focusNext("last")
        assertEquals(null, adapter.snapshots.first().focusedFieldKey)
        form.setFocus(first)
        adapter.stopEditing()
        snapshot = adapter.snapshots.first()
        assertEquals(null, snapshot.focusedFieldKey)
        assertEquals(null, snapshot.navigationRequest)
    }

    @Test
    fun messagesPreserveComposeOrderSeverityAndOwnersWithoutDuplicatingChildErrors() = runBlocking {
        val fieldError = message("Field error")
        val warning = message("Field warning", warning = true)
        val silent = message("Never show", silent = true)
        val groupError = message("Group error")
        val groupWarning = message("Group warning", warning = true)
        val form = form {
            +group {
                tag = "first"
                +textField("name") {
                    addRule(fieldRule(fieldError))
                    addRule(fieldRule(warning))
                    addRule(fieldRule(silent))
                }
            }.apply {
                addRule(groupRule { groupError })
                addRule(groupRule { groupWarning })
                addRule(groupRule { silent })
            }
            +group {
                tag = "second"
                +textField("other") { addRule(RequiredFieldRule()) }
            }
        }
        val adapter = NativeFormAdapter(form)
        assertFalse(adapter.validate())
        val snapshot = adapter.snapshots.first()
        assertEquals(listOf("first", "second"), snapshot.groups.map { it.key })
        assertEquals(listOf("name", "other"), snapshot.fields.map { it.key })
        assertEquals(listOf("Field error", "Field warning"), snapshot.fields.first().messages)
        assertEquals(listOf(false, true), snapshot.fields.first().validationMessages.map { it.asWarning })
        assertEquals(listOf("Group error", "Group warning"), snapshot.groups.first().validationMessages.map { it.text })
        assertSameVisibleRows(form, snapshot)
    }

    @Test
    fun submissionUsesSharedWarningSilentAndHiddenPolicies() = runBlocking {
        val warning = message("Check value", warning = true)
        val hidden = TextField("hidden").apply {
            isVisibleCondition = Condition.False
            addRule(RequiredFieldRule())
        }
        val form = form {
            +group {
                +hidden
                +textField("warning") { addRule(fieldRule(warning)) }
            }.apply { addRule(groupRule { message("Silent warning", warning = true, silent = true) }) }
        }
        val adapter = NativeFormAdapter(form)
        assertTrue(adapter.validate())
        assertFalse(adapter.validate(includeHidden = false, warningAsError = true))
        assertFalse(adapter.validate(includeHidden = true, warningAsError = false))
        val snapshot = adapter.snapshots.first()
        assertEquals(listOf("warning"), snapshot.fields.map { it.key })
        assertEquals(listOf("Check value"), snapshot.fields.single().messages)
        assertTrue(snapshot.groups.single().validationMessages.isEmpty())
        assertSameVisibleRows(form, snapshot)

        val silentForm = form {
            +group { +textField("silent") { addRule(fieldRule(message("Silent error", silent = true))) } }
        }
        val silentAdapter = NativeFormAdapter(silentForm)
        assertFalse(silentAdapter.validate())
        assertTrue(silentAdapter.snapshots.first().fields.single().validationMessages.isEmpty())

        val silentGroupForm = form {
            +Group().apply { addRule(groupRule { message("Silent group error", silent = true) }) }
        }
        val silentGroupAdapter = NativeFormAdapter(silentGroupForm)
        assertFalse(silentGroupAdapter.validate())
        assertTrue(silentGroupAdapter.snapshots.first().groups.isEmpty())
    }

    @Test
    fun liveGroupMessagesRefreshAfterEditsVisibilityAndGroupReplacement() = runBlocking<Unit> {
        val field = TextField("value")
        val group = Group("pair").apply {
            addField(field)
            validationOption = ValidationOption.ON_VALUE_CHANGE
            addRule(groupRule {
                when (field.value) {
                    "valid" -> null
                    "warn" -> message("Group warning", warning = true)
                    else -> message("Group error")
                }
            })
        }
        val form = form { +group }
        val adapter = NativeFormAdapter(form)
        val updates = Channel<NativeFormSnapshot>(Channel.UNLIMITED)
        val collector = launch(start = CoroutineStart.UNDISPATCHED) {
            adapter.snapshots.collect { updates.send(it) }
        }
        suspend fun awaitSnapshot(predicate: (NativeFormSnapshot) -> Boolean): NativeFormSnapshot =
            withTimeout(2_000L) {
                var next = updates.receive()
                while (!predicate(next)) next = updates.receive()
                assertSameVisibleRows(form, next)
                next
            }
        try {
            awaitSnapshot { it.groups.single().validationMessages.isEmpty() }
            assertFalse(adapter.validate())
            awaitSnapshot { it.groups.single().validationMessages.firstOrNull()?.text == "Group error" }
            assertTrue(adapter.setText("value", "warn"))
            awaitSnapshot { it.groups.single().validationMessages.firstOrNull()?.asWarning == true }
            assertTrue(adapter.setText("value", "valid"))
            awaitSnapshot { it.groups.single().validationMessages.isEmpty() }
            form.setValues(mapOf("value" to "invalid"))
            awaitSnapshot { it.groups.single().validationMessages.isNotEmpty() }
            group.isVisibleCondition = Condition.False
            awaitSnapshot { it.groups.isEmpty() }
            assertTrue(adapter.validate())
            group.isVisibleCondition = Condition.True
            awaitSnapshot { it.groups.isNotEmpty() && it.groups.single().validationMessages.isEmpty() }
            form.remove(group, reload = true)
            awaitSnapshot { it.groups.isEmpty() }
            val replacement = Group("replacement").apply {
                addRule(groupRule { message("Empty group error") })
            }
            form.add(replacement, reload = true)
            assertFalse(adapter.validate())
            awaitSnapshot { it.groups.singleOrNull()?.key == "replacement" }
        } finally {
            collector.cancelAndJoin()
        }
    }

    private suspend fun assertSameVisibleRows(form: Form, snapshot: NativeFormSnapshot) {
        val composeRows = form.fields.value.filter { it.isVisible }.map { field ->
            when (field) {
                is ValidationErrorField -> Triple(field.elementKey, field.error.getStringSuspend(), field.asWarning)
                is ValidationErrorGroupField -> Triple(field.elementKey, field.error.getStringSuspend(), field.asWarning)
                else -> Triple(field.key, null, false)
            }
        }
        val nativeRows = snapshot.groups.flatMap { group ->
            group.fields.flatMap { field ->
                listOf(Triple(field.key, null, false)) + field.validationMessages.map {
                    Triple(field.key, it.text, it.asWarning)
                }
            } + group.validationMessages.map { Triple(group.key, it.text, it.asWarning) }
        }
        assertEquals(composeRows, nativeRows)
    }

    private fun message(text: String, warning: Boolean = false, silent: Boolean = false) = object : ValidationError() {
        override val error = StrRes(text = text)
        override val asWarning = warning
        override val isSilent = silent
    }

    private fun fieldRule(error: ValidationError) = object : ValidationRule.Field<String>() {
        override fun isValid(value: String?) = error
    }

    private fun groupRule(check: () -> ValidationError?) = object : ValidationRule.Group(ValidationError.Required) {
        override fun isValid() = check()
    }

    @Test
    fun observesDirectMutationsValidationAndConditionsWithoutCompose() = runBlocking {
        val form = form {
            +group {
                +textField("name") { addRule(RequiredFieldRule()) }
                +toggleField("show", StrRes(text = "Show details")) { value = false }
                +textField("details") {
                    isVisibleCondition = Condition("show") {
                        it.getFieldByTag("show")?.value == true
                    }
                }
            }
        }
        val adapter = NativeFormAdapter(form)
        val updates = Channel<NativeFormSnapshot>(Channel.UNLIMITED)
        val collector = launch(start = CoroutineStart.UNDISPATCHED) {
            adapter.snapshots.collect { updates.send(it) }
        }
        suspend fun awaitSnapshot(predicate: (NativeFormSnapshot) -> Boolean): NativeFormSnapshot =
            withTimeout(2_000L) {
                var next = updates.receive()
                while (!predicate(next)) next = updates.receive()
                next
            }

        assertEquals(listOf("name", "show"), awaitSnapshot { true }.fields.map { it.key })
        assertFalse(adapter.validate())
        awaitSnapshot { it.fields.first().messages.isNotEmpty() }
        form.setValues(mapOf("name" to "Ada", "show" to true))
        val changed = awaitSnapshot {
            it.fields.size == 3 && it.fields.first().textValue == "Ada" &&
                it.fields.first().messages.isEmpty()
        }
        assertTrue(changed.fields[1].toggleValue)
        form.getFieldByTag("name")?.isEnabledCondition = Condition.False
        awaitSnapshot { !it.fields.first().enabled }
        assertFalse(adapter.setText("name", "Ignored"))
        assertTrue(adapter.setText("details", "Native edit"))
        assertEquals("Native edit", form.allValues()["details"])
        collector.cancelAndJoin()

        // Reattaching must get the current form, with no stale subscription state.
        val reattached = launch(start = CoroutineStart.UNDISPATCHED) {
            adapter.snapshots.collect { updates.send(it) }
        }
        awaitSnapshot { it.fields.last().textValue == "Native edit" }
        reattached.cancelAndJoin()
    }
}
