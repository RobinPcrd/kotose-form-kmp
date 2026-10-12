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
import io.github.robinpcrd.kotose.form.dsl.secureTextField
import io.github.robinpcrd.kotose.form.example.Res
import io.github.robinpcrd.kotose.form.example.catalog.CatalogPage
import io.github.robinpcrd.kotose.form.example.catalog.PageAction
import io.github.robinpcrd.kotose.form.example.catalog.PageLabels
import io.github.robinpcrd.kotose.form.example.catalog.res
import io.github.robinpcrd.kotose.form.example.password_hint
import io.github.robinpcrd.kotose.form.example.password_label
import io.github.robinpcrd.kotose.form.example.password_page_title

/** Secure input: masked by default, reveal state belongs to each renderer. */
fun passwordPage(labels: PageLabels): CatalogPage {
    val form = form {
        +group {
            +displayText(text = labels.empty, isTitle = true)
            +displayParagraph(lines = listOf(res(Res.string.password_hint)))
            +secureTextField("password-empty", res(Res.string.password_label))
        }
        +group {
            +displayText(text = labels.filled, isTitle = true)
            +secureTextField("password-filled", res(Res.string.password_label)) { value = "DemoPass123!" }
        }
        +group {
            +displayText(text = labels.disabled, isTitle = true)
            +secureTextField("password-disabled", res(Res.string.password_label)) {
                value = "DemoPass123!"
                isEnabledCondition = Condition.False
            }
        }
        +group {
            +displayText(text = labels.error, isTitle = true)
            +secureTextField("password-error", res(Res.string.password_label)) { addRule(RequiredFieldRule()) }
        }
    }
    form.validate(scrollToFirstError = false)
    return CatalogPage(
        "password", CatalogPage.Section.FIELDS, res(Res.string.password_page_title), form, labels,
        listOf(
            PageAction("reset", labels.reset) {
                form.setValues(mapOf("password-empty" to null, "password-filled" to null, "password-error" to null))
            },
        ),
    )
}
