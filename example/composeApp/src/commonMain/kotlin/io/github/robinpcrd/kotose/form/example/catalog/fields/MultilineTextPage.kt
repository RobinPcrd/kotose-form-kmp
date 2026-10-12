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
import io.github.robinpcrd.kotose.form.example.Res
import io.github.robinpcrd.kotose.form.example.catalog.CatalogPage
import io.github.robinpcrd.kotose.form.example.catalog.PageAction
import io.github.robinpcrd.kotose.form.example.catalog.PageLabels
import io.github.robinpcrd.kotose.form.example.catalog.res
import io.github.robinpcrd.kotose.form.example.multiline_error_hint
import io.github.robinpcrd.kotose.form.example.multiline_fixed_caption
import io.github.robinpcrd.kotose.form.example.multiline_fixed_hint
import io.github.robinpcrd.kotose.form.example.multiline_notes_label
import io.github.robinpcrd.kotose.form.example.multiline_page_title
import io.github.robinpcrd.kotose.form.fields.TextField
import io.github.robinpcrd.kotose.form.fields.TextLineLimits
import io.github.robinpcrd.kotose.resources.StrRes

private const val FILLED_NOTES = "First line of notes.\nSecond line, added with the return key.\n\nA third paragraph that is long enough to wrap onto the next line on a phone."

/** Multi-line text input in every state, growing between its line bounds before it scrolls. */
fun multilineTextPage(labels: PageLabels): CatalogPage {
    val inputs = mutableListOf<TextField>()
    fun input(
        tag: String,
        lineLimits: TextLineLimits = TextLineLimits.MultiLine(minLines = 3, maxLines = 6),
        init: TextField.() -> Unit = {},
    ) = TextField(tag, res(Res.string.multiline_notes_label), lineLimits = lineLimits).apply(init).also(inputs::add)
    val form = form {
        +group {
            +displayText(text = labels.empty, isTitle = true)
            +input("multiline-empty")
        }
        +group {
            +displayText(text = labels.filled, isTitle = true)
            +input("multiline-filled") { value = FILLED_NOTES }
        }
        +group {
            +displayText(text = res(Res.string.multiline_fixed_caption), isTitle = true)
            +displayParagraph(lines = listOf(res(Res.string.multiline_fixed_hint)))
            +input("multiline-fixed", TextLineLimits.MultiLine(minLines = 4, maxLines = 4)) { value = FILLED_NOTES }
        }
        +group {
            +displayText(text = labels.disabled, isTitle = true)
            +input("multiline-disabled") {
                value = FILLED_NOTES
                isEnabledCondition = Condition.False
            }
        }
        +group {
            +displayText(text = labels.error, isTitle = true)
            +displayParagraph(lines = listOf(res(Res.string.multiline_error_hint)))
            +input("multiline-error") { addRule(RequiredFieldRule()) }
        }
    }
    form.validate(scrollToFirstError = false)
    return CatalogPage(
        "multiline", CatalogPage.Section.FIELDS, res(Res.string.multiline_page_title), form, labels,
        listOf(
            PageAction("reset", labels.reset) { form.setValues(inputs.associate { it.tag.orEmpty() to "" }) },
            PageAction("enabled", labels.toggleEnabled) {
                inputs.forEach { it.isEnabledCondition = Condition(!it.isEnabled) }
            },
        ),
    )
}
