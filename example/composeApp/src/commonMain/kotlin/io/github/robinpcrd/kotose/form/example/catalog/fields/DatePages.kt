/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.example.catalog.fields

import io.github.robinpcrd.kotose.form.Condition
import io.github.robinpcrd.kotose.form.RequiredFieldRule
import io.github.robinpcrd.kotose.form.dsl.dateTimePicker
import io.github.robinpcrd.kotose.form.dsl.displayParagraph
import io.github.robinpcrd.kotose.form.dsl.displayText
import io.github.robinpcrd.kotose.form.dsl.form
import io.github.robinpcrd.kotose.form.dsl.group
import io.github.robinpcrd.kotose.form.dsl.localDatePicker
import io.github.robinpcrd.kotose.form.example.caption_wheels
import io.github.robinpcrd.kotose.form.example.Res
import io.github.robinpcrd.kotose.form.example.catalog.CatalogPage
import io.github.robinpcrd.kotose.form.example.catalog.PageAction
import io.github.robinpcrd.kotose.form.example.catalog.PageLabels
import io.github.robinpcrd.kotose.form.example.catalog.res
import io.github.robinpcrd.kotose.form.example.date_hint
import io.github.robinpcrd.kotose.form.example.date_label
import io.github.robinpcrd.kotose.form.example.date_page_title
import io.github.robinpcrd.kotose.form.example.datetime_hint
import io.github.robinpcrd.kotose.form.example.datetime_label
import io.github.robinpcrd.kotose.form.example.datetime_page_title
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Month
import io.github.robinpcrd.kotose.form.fields.IosDatePickerMode

private val sampleDate = LocalDate(2026, Month.MAY, 30)
private val sampleDateTime = LocalDateTime(2026, Month.MAY, 30, 14, 5)

/** Calendar date without a time zone; each platform shows its own picker. */
fun datePage(labels: PageLabels): CatalogPage {
    val form = form {
        +group {
            +displayText(text = labels.empty, isTitle = true)
            +displayParagraph(lines = listOf(res(Res.string.date_hint)))
            +localDatePicker("date-empty", res(Res.string.date_label))
        }
        +group {
            +displayText(text = labels.filled, isTitle = true)
            +localDatePicker("date-filled", res(Res.string.date_label)) { value = sampleDate }
        }
        +group {
            +displayText(text = res(Res.string.caption_wheels), isTitle = true)
            +localDatePicker("date-wheels", res(Res.string.date_label), iosPickerMode = IosDatePickerMode.Wheels) {
                value = sampleDate
            }
        }
        +group {
            +displayText(text = labels.disabled, isTitle = true)
            +localDatePicker("date-disabled", res(Res.string.date_label)) {
                value = sampleDate
                isEnabledCondition = Condition.False
            }
        }
        +group {
            +displayText(text = labels.error, isTitle = true)
            +localDatePicker("date-error", res(Res.string.date_label)) { addRule(RequiredFieldRule()) }
        }
    }
    form.validate(scrollToFirstError = false)
    return CatalogPage(
        "date", CatalogPage.Section.FIELDS, res(Res.string.date_page_title), form, labels,
        listOf(
            PageAction("reset", labels.reset) {
                form.setValues(mapOf("date-empty" to null, "date-filled" to sampleDate, "date-wheels" to sampleDate, "date-error" to null))
            },
        ),
    )
}

/** Calendar date plus wall-clock time, still without a time zone. */
fun dateTimePage(labels: PageLabels): CatalogPage {
    val form = form {
        +group {
            +displayText(text = labels.empty, isTitle = true)
            +displayParagraph(lines = listOf(res(Res.string.datetime_hint)))
            +dateTimePicker("datetime-empty", res(Res.string.datetime_label))
        }
        +group {
            +displayText(text = labels.filled, isTitle = true)
            +dateTimePicker("datetime-filled", res(Res.string.datetime_label)) { value = sampleDateTime }
        }
        +group {
            +displayText(text = res(Res.string.caption_wheels), isTitle = true)
            +dateTimePicker("datetime-wheels", res(Res.string.datetime_label), iosPickerMode = IosDatePickerMode.Wheels) {
                value = sampleDateTime
            }
        }
        +group {
            +displayText(text = labels.disabled, isTitle = true)
            +dateTimePicker("datetime-disabled", res(Res.string.datetime_label)) {
                value = sampleDateTime
                isEnabledCondition = Condition.False
            }
        }
        +group {
            +displayText(text = labels.error, isTitle = true)
            +dateTimePicker("datetime-error", res(Res.string.datetime_label)) { addRule(RequiredFieldRule()) }
        }
    }
    form.validate(scrollToFirstError = false)
    return CatalogPage(
        "datetime", CatalogPage.Section.FIELDS, res(Res.string.datetime_page_title), form, labels,
        listOf(
            PageAction("reset", labels.reset) {
                form.setValues(mapOf("datetime-empty" to null, "datetime-filled" to sampleDateTime, "datetime-wheels" to sampleDateTime, "datetime-error" to null))
            },
        ),
    )
}
