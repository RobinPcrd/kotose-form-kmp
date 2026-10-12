/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.example.catalog.fields

import io.github.robinpcrd.kotose.form.Condition
import io.github.robinpcrd.kotose.form.RequiredFieldRule
import io.github.robinpcrd.kotose.form.dsl.displayParagraph
import io.github.robinpcrd.kotose.form.dsl.displayText
import io.github.robinpcrd.kotose.form.dsl.form
import io.github.robinpcrd.kotose.form.dsl.group
import io.github.robinpcrd.kotose.form.dsl.singleChoiceField
import io.github.robinpcrd.kotose.form.example.Res
import io.github.robinpcrd.kotose.form.example.catalog.CatalogPage
import io.github.robinpcrd.kotose.form.example.catalog.PageAction
import io.github.robinpcrd.kotose.form.example.catalog.PageLabels
import io.github.robinpcrd.kotose.form.example.catalog.res
import io.github.robinpcrd.kotose.form.example.dropdown_clearable_hint
import io.github.robinpcrd.kotose.form.example.dropdown_express
import io.github.robinpcrd.kotose.form.example.dropdown_page_title
import io.github.robinpcrd.kotose.form.example.dropdown_pickup
import io.github.robinpcrd.kotose.form.example.dropdown_placeholder
import io.github.robinpcrd.kotose.form.example.dropdown_standard
import io.github.robinpcrd.kotose.form.example.caption_clearable
import io.github.robinpcrd.kotose.form.fields.DropdownOption

data class Delivery(val code: Int)

/** Typed single choice: the value is a domain object, the renderer only sees stable option ids. */
fun dropdownPage(labels: PageLabels): CatalogPage {
    fun options() = listOf(
        DropdownOption(Delivery(10), res(Res.string.dropdown_standard), "standard"),
        DropdownOption(Delivery(20), res(Res.string.dropdown_express), "express"),
        DropdownOption(Delivery(30), res(Res.string.dropdown_pickup), "pickup"),
    )
    val form = form {
        +group {
            +displayText(text = labels.empty, isTitle = true)
            +singleChoiceField("dropdown-empty", options(), res(Res.string.dropdown_placeholder))
        }
        +group {
            +displayText(text = labels.filled, isTitle = true)
            +singleChoiceField("dropdown-filled", options(), res(Res.string.dropdown_placeholder)) { value = Delivery(20) }
        }
        +group {
            +displayText(text = res(Res.string.caption_clearable), isTitle = true)
            +displayParagraph(lines = listOf(res(Res.string.dropdown_clearable_hint)))
            +singleChoiceField("dropdown-clearable", options(), res(Res.string.dropdown_placeholder), isClearable = true) {
                value = Delivery(10)
            }
        }
        +group {
            +displayText(text = labels.disabled, isTitle = true)
            +singleChoiceField("dropdown-disabled", options(), res(Res.string.dropdown_placeholder)) {
                value = Delivery(30)
                isEnabledCondition = Condition.False
            }
        }
        +group {
            +displayText(text = labels.error, isTitle = true)
            +singleChoiceField("dropdown-error", options(), res(Res.string.dropdown_placeholder)) { addRule(RequiredFieldRule()) }
        }
    }
    form.validate(scrollToFirstError = false)
    return CatalogPage(
        "dropdown", CatalogPage.Section.FIELDS, res(Res.string.dropdown_page_title), form, labels,
        listOf(
            PageAction("reset", labels.reset) {
                form.setValues(mapOf("dropdown-empty" to null, "dropdown-filled" to Delivery(20), "dropdown-clearable" to Delivery(10), "dropdown-error" to null))
            },
        ),
    )
}
