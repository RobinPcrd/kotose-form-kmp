/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.fields

import io.github.robinpcrd.kotose.form.Field
import io.github.robinpcrd.kotose.resources.StrRes
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

/** Calendar date and wall-clock time shared by renderers. The value has no time zone. */
open class DateTimeField(
    tag: String? = null,
    val placeholder: StrRes? = null,
    val formatter: ((LocalDateTime) -> String) = ::defaultDateTimeFormatter,
    val showPicker: (() -> Unit)? = null,
    val iosPickerMode: IosDatePickerMode = IosDatePickerMode.Compact,
) : Field<LocalDateTime>(tag) {
    override fun setBaseValue(value: Any?) {
        this.value = value as? LocalDateTime
    }
}

fun DateTimeField.applyPickedDateTime(
    date: LocalDate,
    hour: Int,
    minute: Int,
) {
    value = LocalDateTime(
        year = date.year,
        month = date.month,
        day = date.day,
        hour = hour,
        minute = minute,
    )
}

fun defaultDateTimeFormatter(dateTime: LocalDateTime): String {
    val date = defaultLocalDateFormatter(dateTime.date)
    val hour = dateTime.hour.twoDigits()
    val minute = dateTime.minute.twoDigits()
    return "$date $hour:$minute"
}

private fun Int.twoDigits(): String =
    if (this < 10) {
        "0$this"
    } else {
        toString()
    }
