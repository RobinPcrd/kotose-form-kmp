/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.dsl

import io.github.robinpcrd.kotose.form.fields.DateTimeField
import io.github.robinpcrd.kotose.form.fields.IosDatePickerMode
import io.github.robinpcrd.kotose.form.fields.LocalDateField
import io.github.robinpcrd.kotose.form.fields.defaultDateTimeFormatter
import io.github.robinpcrd.kotose.form.fields.defaultLocalDateFormatter
import io.github.robinpcrd.kotose.resources.StrRes
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

fun GroupBuilder.localDatePicker(
    tag: String? = null,
    placeholder: StrRes? = null,
    formatter: ((LocalDate) -> String) = ::defaultLocalDateFormatter,
    showPicker: (() -> Unit)? = null,
    iosPickerMode: IosDatePickerMode = IosDatePickerMode.Compact,
    init: LocalDateField.() -> Unit = {}
) = LocalDateField(
    tag = tag,
    placeholder = placeholder,
    formatter = formatter,
    showPicker = showPicker,
    iosPickerMode = iosPickerMode,
).apply(init)

fun GroupBuilder.dateTimePicker(
    tag: String? = null,
    placeholder: StrRes? = null,
    showPicker: (() -> Unit)? = null,
    formatter: ((LocalDateTime) -> String) = ::defaultDateTimeFormatter,
    iosPickerMode: IosDatePickerMode = IosDatePickerMode.Compact,
    init: DateTimeField.() -> Unit = {}
) = DateTimeField(
    tag = tag,
    placeholder = placeholder,
    formatter = formatter,
    showPicker = showPicker,
    iosPickerMode = iosPickerMode,
).apply(init)
