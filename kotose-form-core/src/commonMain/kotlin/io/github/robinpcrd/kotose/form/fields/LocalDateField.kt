/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.fields

import io.github.robinpcrd.kotose.form.Field
import io.github.robinpcrd.kotose.resources.StrRes
import kotlinx.datetime.LocalDate

/**
 * Calendar date shared by renderers. The value has no time zone; renderers convert
 * platform dates to calendar components without shifting the day.
 * [showPicker] and [iosPickerMode] configure how the active renderer presents its picker.
 */
open class LocalDateField(
    tag: String? = null,
    val placeholder: StrRes? = null,
    val formatter: ((LocalDate) -> String) = ::defaultLocalDateFormatter,
    val showPicker: (() -> Unit)? = null,
    val iosPickerMode: IosDatePickerMode = IosDatePickerMode.Compact,
) : Field<LocalDate>(tag) {
    override fun setBaseValue(value: Any?) {
        this.value = value as? LocalDate
    }
}

fun LocalDateField.applyPickedDate(date: LocalDate) {
    value = date
}

fun defaultLocalDateFormatter(date: LocalDate): String =
    "${date.month.readableName()} ${date.day}, ${date.year}"

internal fun kotlinx.datetime.Month.readableName(): String =
    name.lowercase().replaceFirstChar { char -> char.uppercase() }
