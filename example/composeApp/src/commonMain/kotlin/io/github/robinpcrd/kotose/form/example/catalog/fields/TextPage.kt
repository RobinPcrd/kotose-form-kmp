/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.example.catalog.fields

import io.github.robinpcrd.kotose.form.Condition
import io.github.robinpcrd.kotose.form.RequiredFieldRule
import io.github.robinpcrd.kotose.form.ValidationError
import io.github.robinpcrd.kotose.form.ValidationRule
import io.github.robinpcrd.kotose.form.dsl.displayParagraph
import io.github.robinpcrd.kotose.form.dsl.displayText
import io.github.robinpcrd.kotose.form.dsl.form
import io.github.robinpcrd.kotose.form.dsl.group
import io.github.robinpcrd.kotose.form.dsl.textField
import io.github.robinpcrd.kotose.form.example.Res
import io.github.robinpcrd.kotose.form.example.catalog.CatalogPage
import io.github.robinpcrd.kotose.form.example.catalog.PageAction
import io.github.robinpcrd.kotose.form.example.catalog.PageLabels
import io.github.robinpcrd.kotose.form.example.catalog.res
import io.github.robinpcrd.kotose.form.example.text_email_label
import io.github.robinpcrd.kotose.form.example.text_error_hint
import io.github.robinpcrd.kotose.form.example.text_name_label
import io.github.robinpcrd.kotose.form.example.text_page_title
import io.github.robinpcrd.kotose.form.example.text_warning_hint
import io.github.robinpcrd.kotose.form.example.text_warning_message
import io.github.robinpcrd.kotose.form.fields.TextField
import io.github.robinpcrd.kotose.resources.StrRes

/** Single-line text input in every state. Error and warning groups are validated on open. */
fun textPage(labels: PageLabels): CatalogPage {
    val warning = object : ValidationError() {
        override val error = res(Res.string.text_warning_message)
        override val asWarning = true
    }
    val inputs = mutableListOf<TextField>()
    fun input(tag: String, label: StrRes, init: TextField.() -> Unit = {}) =
        TextField(tag, label).apply(init).also(inputs::add)
    val form = form {
        +group {
            +displayText(text = labels.empty, isTitle = true)
            +input("text-empty", res(Res.string.text_name_label))
        }
        +group {
            +displayText(text = labels.filled, isTitle = true)
            +input("text-filled", res(Res.string.text_name_label)) { value = "Ada Lovelace" }
        }
        +group {
            +displayText(text = labels.disabled, isTitle = true)
            +input("text-disabled", res(Res.string.text_name_label)) {
                value = "Read but not edited"
                isEnabledCondition = Condition.False
            }
        }
        +group {
            +displayText(text = labels.error, isTitle = true)
            +displayParagraph(lines = listOf(res(Res.string.text_error_hint)))
            +input("text-error", res(Res.string.text_name_label)) { addRule(RequiredFieldRule()) }
        }
        +group {
            +displayText(text = labels.warning, isTitle = true)
            +displayParagraph(lines = listOf(res(Res.string.text_warning_hint)))
            +input("text-warning", res(Res.string.text_email_label)) {
                value = "ada@example.com"
                addRule(object : ValidationRule.Field<String>() {
                    override fun isValid(value: String?) = warning.takeIf { value?.endsWith("@example.com") == true }
                })
            }
        }
    }
    form.validate(scrollToFirstError = false)
    return CatalogPage(
        "text", CatalogPage.Section.FIELDS, res(Res.string.text_page_title), form, labels,
        listOf(
            PageAction("reset", labels.reset) { form.setValues(inputs.associate { it.tag.orEmpty() to "" }) },
            PageAction("enabled", labels.toggleEnabled) {
                inputs.forEach { it.isEnabledCondition = Condition(!it.isEnabled) }
            },
        ),
    )
}
