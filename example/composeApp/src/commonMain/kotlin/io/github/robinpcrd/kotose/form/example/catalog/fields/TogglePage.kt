/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.example.catalog.fields

import io.github.robinpcrd.kotose.form.Condition
import io.github.robinpcrd.kotose.form.ValidationError
import io.github.robinpcrd.kotose.form.ValidationRule
import io.github.robinpcrd.kotose.form.dsl.displayParagraph
import io.github.robinpcrd.kotose.form.dsl.displayText
import io.github.robinpcrd.kotose.form.dsl.form
import io.github.robinpcrd.kotose.form.dsl.group
import io.github.robinpcrd.kotose.form.dsl.toggleField
import io.github.robinpcrd.kotose.form.example.Res
import io.github.robinpcrd.kotose.form.example.catalog.CatalogPage
import io.github.robinpcrd.kotose.form.example.catalog.PageAction
import io.github.robinpcrd.kotose.form.example.catalog.PageLabels
import io.github.robinpcrd.kotose.form.example.catalog.res
import io.github.robinpcrd.kotose.form.example.toggle_error_hint
import io.github.robinpcrd.kotose.form.example.toggle_label
import io.github.robinpcrd.kotose.form.example.toggle_must_accept
import io.github.robinpcrd.kotose.form.example.toggle_page_title

/** Boolean switch, including a rule that requires it to be on. */
fun togglePage(labels: PageLabels): CatalogPage {
    val mustAccept = object : ValidationError() {
        override val error = res(Res.string.toggle_must_accept)
    }
    val form = form {
        +group {
            +displayText(text = labels.empty, isTitle = true)
            +toggleField("toggle-off", res(Res.string.toggle_label)) { value = false }
        }
        +group {
            +displayText(text = labels.filled, isTitle = true)
            +toggleField("toggle-on", res(Res.string.toggle_label)) { value = true }
        }
        +group {
            +displayText(text = labels.disabled, isTitle = true)
            +toggleField("toggle-disabled", res(Res.string.toggle_label)) {
                value = true
                isEnabledCondition = Condition.False
            }
        }
        +group {
            +displayText(text = labels.error, isTitle = true)
            +displayParagraph(lines = listOf(res(Res.string.toggle_error_hint)))
            +toggleField("toggle-error", res(Res.string.toggle_label)) {
                value = false
                addRule(object : ValidationRule.Field<Boolean>() {
                    override fun isValid(value: Boolean?) = mustAccept.takeUnless { value == true }
                })
            }
        }
    }
    form.validate(scrollToFirstError = false)
    return CatalogPage(
        "toggle", CatalogPage.Section.FIELDS, res(Res.string.toggle_page_title), form, labels,
        listOf(
            PageAction("reset", labels.reset) {
                form.setValues(mapOf("toggle-off" to false, "toggle-on" to true, "toggle-error" to false))
            },
        ),
    )
}
