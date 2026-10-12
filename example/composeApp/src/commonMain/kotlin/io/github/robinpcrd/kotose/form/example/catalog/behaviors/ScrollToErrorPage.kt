/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.example.catalog.behaviors

import io.github.robinpcrd.kotose.form.RequiredFieldRule
import io.github.robinpcrd.kotose.form.ValidationError
import io.github.robinpcrd.kotose.form.ValidationRule
import io.github.robinpcrd.kotose.form.dsl.displayParagraph
import io.github.robinpcrd.kotose.form.dsl.displayText
import io.github.robinpcrd.kotose.form.dsl.form
import io.github.robinpcrd.kotose.form.dsl.group
import io.github.robinpcrd.kotose.form.dsl.textField
import io.github.robinpcrd.kotose.form.example.Res
import io.github.robinpcrd.kotose.form.example.action_preset_fill
import io.github.robinpcrd.kotose.form.example.catalog.CatalogPage
import io.github.robinpcrd.kotose.form.example.catalog.PageAction
import io.github.robinpcrd.kotose.form.example.catalog.PageLabels
import io.github.robinpcrd.kotose.form.example.catalog.res
import io.github.robinpcrd.kotose.form.example.scroll_approve
import io.github.robinpcrd.kotose.form.example.scroll_group_error
import io.github.robinpcrd.kotose.form.example.scroll_hint
import io.github.robinpcrd.kotose.form.example.scroll_last
import io.github.robinpcrd.kotose.form.example.scroll_note
import io.github.robinpcrd.kotose.form.example.scroll_page_title
import io.github.robinpcrd.kotose.form.example.scroll_section
import io.github.robinpcrd.kotose.form.fields.ToggleField

/** Validate from the top reveals the first error at the end of a long form. */
fun scrollToErrorPage(labels: PageLabels): CatalogPage {
    val approved = ToggleField("scroll-approved", res(Res.string.scroll_approve)).apply { value = false }
    val groupError = object : ValidationError() {
        override val error = res(Res.string.scroll_group_error)
    }
    val form = form {
        +group {
            +displayText(text = res(Res.string.scroll_section), isTitle = true)
            +displayParagraph(lines = listOf(res(Res.string.scroll_hint)))
            repeat(24) { index -> +textField("scroll-note-${index + 1}", res(Res.string.scroll_note, index + 1)) }
            +textField("scroll-last", res(Res.string.scroll_last)) { addRule(RequiredFieldRule()) }
        }
        +group {
            tag = "scroll-group"
            +approved
        }.apply {
            addRule(object : ValidationRule.Group(groupError) {
                override fun isValid() = groupError.takeUnless { approved.value == true }
            })
        }
    }
    return CatalogPage(
        "scroll-to-error", CatalogPage.Section.BEHAVIORS, res(Res.string.scroll_page_title), form, labels,
        listOf(
            PageAction("preset", res(Res.string.action_preset_fill)) {
                form.setValues(mapOf("scroll-last" to "Filled from Kotlin", "scroll-approved" to true))
            },
            PageAction("reset", labels.reset) {
                form.setValues(mapOf("scroll-last" to "", "scroll-approved" to false))
                form.stopEditing(true)
            },
        ),
    )
}
