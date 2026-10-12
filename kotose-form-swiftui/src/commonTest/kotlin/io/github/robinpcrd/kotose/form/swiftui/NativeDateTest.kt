/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

@file:OptIn(ExperimentalKotoseSwiftUIApi::class)

package io.github.robinpcrd.kotose.form.swiftui

import io.github.robinpcrd.kotose.form.Condition
import io.github.robinpcrd.kotose.form.RequiredFieldRule
import io.github.robinpcrd.kotose.form.dsl.form
import io.github.robinpcrd.kotose.form.dsl.group
import io.github.robinpcrd.kotose.form.fields.DateTimeField
import io.github.robinpcrd.kotose.form.fields.IosDatePickerMode
import io.github.robinpcrd.kotose.form.fields.LocalDateField
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Month
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class NativeDateTest {
    @Test
    fun adaptersShareDateEditsAndRejectInvalidOrUnavailableComponents() = runBlocking {
        val date = LocalDateField("date").apply { addRule(RequiredFieldRule()) }
        val dateTime = DateTimeField("when", iosPickerMode = IosDatePickerMode.Wheels)
        val form = form { +group { +date; +dateTime } }
        val native = NativeFormAdapter(form)

        val empty = native.snapshots.first().fields
        assertEquals(NativeFieldKind.DATE, empty[0].kind)
        assertEquals(NativeFieldKind.DATE_TIME, empty[1].kind)
        assertEquals(false, empty[0].date?.hasValue)
        assertEquals(true, empty[1].date?.includesTime)
        assertEquals(IosDatePickerMode.Compact, empty[0].date?.pickerMode)
        assertEquals(IosDatePickerMode.Wheels, empty[1].date?.pickerMode)
        assertEquals("", empty[0].textValue)
        assertFalse(native.validate())

        assertTrue(native.setDate("date", 2026, 5, 30, 23, 59))
        assertEquals(LocalDate(2026, Month.MAY, 30), date.value)
        assertTrue(native.setDate("when", 2026, 5, 30, 14, 5))
        assertEquals(LocalDateTime(2026, Month.MAY, 30, 14, 5), dateTime.value)
        assertTrue(native.validate())

        val filled = native.snapshots.first().fields
        assertEquals("May 30, 2026", filled[0].textValue)
        assertEquals(listOf(2026, 5, 30, 0, 0), filled[0].date?.let { listOf(it.year, it.month, it.day, it.hour, it.minute) })
        assertEquals(listOf(2026, 5, 30, 14, 5), filled[1].date?.let { listOf(it.year, it.month, it.day, it.hour, it.minute) })

        assertFalse(native.setDate("date", 2026, 2, 30, 0, 0))
        assertFalse(native.setDate("when", 2026, 5, 30, 24, 0))
        assertEquals(LocalDate(2026, Month.MAY, 30), date.value)
        assertFalse(native.setDate("missing", 2026, 5, 30, 0, 0))
        date.isEnabledCondition = Condition.False
        assertFalse(native.setDate("date", 2026, 6, 1, 0, 0))
        date.isEnabledCondition = Condition.True
        date.isVisibleCondition = Condition.False
        assertFalse(native.setDate("date", 2026, 6, 1, 0, 0))
        date.isVisibleCondition = Condition.True

        form.setValues(mapOf("date" to LocalDate(2026, Month.JUNE, 1), "when" to null))
        assertEquals(LocalDate(2026, Month.JUNE, 1), form.allValues()["date"])
        assertNull(form.allValues()["when"])
        assertEquals(false, native.snapshots.first().fields[1].date?.hasValue)
    }
}
