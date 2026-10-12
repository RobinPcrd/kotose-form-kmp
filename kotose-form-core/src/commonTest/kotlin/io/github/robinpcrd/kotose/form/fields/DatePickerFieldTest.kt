/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.fields

import io.github.robinpcrd.kotose.form.dsl.GroupBuilder
import io.github.robinpcrd.kotose.form.dsl.dateTimePicker
import io.github.robinpcrd.kotose.form.dsl.localDatePicker
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Month
import kotlin.test.Test
import kotlin.test.assertEquals

class DatePickerFieldTest {
    @Test
    fun localDatePickerDefaultsToIosCompactMode() {
        val field = LocalDateField()

        assertEquals(IosDatePickerMode.Compact, field.iosPickerMode)
    }

    @Test
    fun dateTimePickerDefaultsToIosCompactMode() {
        val field = DateTimeField()

        assertEquals(IosDatePickerMode.Compact, field.iosPickerMode)
    }

    @Test
    fun localDatePickerCanUseIosWheelsMode() {
        val field = LocalDateField(iosPickerMode = IosDatePickerMode.Wheels)

        assertEquals(IosDatePickerMode.Wheels, field.iosPickerMode)
    }

    @Test
    fun dateTimePickerCanUseIosWheelsMode() {
        val field = DateTimeField(iosPickerMode = IosDatePickerMode.Wheels)

        assertEquals(IosDatePickerMode.Wheels, field.iosPickerMode)
    }

    @Test
    fun localDatePickerDslCanUseIosWheelsMode() {
        val group = GroupBuilder().apply {
            +localDatePicker(iosPickerMode = IosDatePickerMode.Wheels)
        }.build()

        val field = group.fields.single() as LocalDateField
        assertEquals(IosDatePickerMode.Wheels, field.iosPickerMode)
    }

    @Test
    fun dateTimePickerDslCanUseIosWheelsMode() {
        val group = GroupBuilder().apply {
            +dateTimePicker(iosPickerMode = IosDatePickerMode.Wheels)
        }.build()

        val field = group.fields.single() as DateTimeField
        assertEquals(IosDatePickerMode.Wheels, field.iosPickerMode)
    }

    @Test
    fun localDateDefaultFormatterUsesReadableDate() {
        val field = LocalDateField()

        val formattedDate = field.formatter(LocalDate(2026, Month.MAY, 30))

        assertEquals("May 30, 2026", formattedDate)
    }

    @Test
    fun dateTimeDefaultFormatterUsesReadableDateAndTime() {
        val field = DateTimeField()

        val formattedDateTime = field.formatter(
            LocalDateTime(2026, Month.MAY, 30, 14, 5)
        )

        assertEquals("May 30, 2026 14:05", formattedDateTime)
    }

    @Test
    fun localDatePickerSelectionUpdatesFieldValue() {
        val field = LocalDateField()
        val selectedDate = LocalDate(2026, Month.MAY, 30)

        field.applyPickedDate(selectedDate)

        assertEquals(selectedDate, field.value)
    }

    @Test
    fun dateTimePickerSelectionUpdatesFieldValueWithPickedDateAndTime() {
        val field = DateTimeField()

        field.applyPickedDateTime(
            date = LocalDate(2026, Month.MAY, 30),
            hour = 14,
            minute = 45,
        )

        assertEquals(
            LocalDateTime(2026, Month.MAY, 30, 14, 45),
            field.value,
        )
    }

    @Test
    fun baseValuesAcceptMatchingTypesAndClearOtherwise() {
        val date = LocalDateField()
        val dateTime = DateTimeField()

        date.setBaseValue(LocalDate(2026, Month.MAY, 30))
        dateTime.setBaseValue(LocalDateTime(2026, Month.MAY, 30, 14, 5))
        assertEquals(LocalDate(2026, Month.MAY, 30), date.value)
        assertEquals(LocalDateTime(2026, Month.MAY, 30, 14, 5), dateTime.value)

        date.setBaseValue("2026-05-30")
        dateTime.setBaseValue(null)
        assertEquals(null, date.value)
        assertEquals(null, dateTime.value)
    }
}
