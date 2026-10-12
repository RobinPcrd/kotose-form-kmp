/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.example.catalog.behaviors

import io.github.robinpcrd.kotose.form.RequiredFieldRule
import io.github.robinpcrd.kotose.form.dsl.displayParagraph
import io.github.robinpcrd.kotose.form.dsl.displayText
import io.github.robinpcrd.kotose.form.dsl.form
import io.github.robinpcrd.kotose.form.dsl.group
import io.github.robinpcrd.kotose.form.dsl.secureTextField
import io.github.robinpcrd.kotose.form.dsl.textField
import io.github.robinpcrd.kotose.form.example.Res
import io.github.robinpcrd.kotose.form.example.catalog.CatalogPage
import io.github.robinpcrd.kotose.form.example.catalog.PageAction
import io.github.robinpcrd.kotose.form.example.catalog.PageLabels
import io.github.robinpcrd.kotose.form.example.catalog.res
import io.github.robinpcrd.kotose.form.example.keyboard_hint
import io.github.robinpcrd.kotose.form.example.keyboard_last
import io.github.robinpcrd.kotose.form.example.keyboard_note
import io.github.robinpcrd.kotose.form.example.keyboard_page_title
import io.github.robinpcrd.kotose.form.example.keyboard_password
import io.github.robinpcrd.kotose.form.example.keyboard_phone
import io.github.robinpcrd.kotose.form.example.keyboard_section
import io.github.robinpcrd.kotose.form.fields.PhoneNumberField

/** Next and Done walk through the fields; the last field sits at the bottom, under the keyboard. */
fun keyboardPage(labels: PageLabels): CatalogPage {
    val form = form {
        +group {
            +displayText(text = res(Res.string.keyboard_section), isTitle = true)
            +displayParagraph(lines = listOf(res(Res.string.keyboard_hint)))
            repeat(6) { index -> +textField("keyboard-note-${index + 1}", res(Res.string.keyboard_note, index + 1)) }
            +secureTextField("keyboard-password", res(Res.string.keyboard_password))
            +PhoneNumberField("keyboard-phone", res(Res.string.keyboard_phone))
            +textField("keyboard-last", res(Res.string.keyboard_last)) { addRule(RequiredFieldRule()) }
        }
    }
    return CatalogPage(
        "keyboard", CatalogPage.Section.BEHAVIORS, res(Res.string.keyboard_page_title), form, labels,
        listOf(
            PageAction("reset", labels.reset) {
                form.setValues(
                    (1..6).associate { "keyboard-note-$it" to "" } +
                        mapOf("keyboard-password" to null, "keyboard-phone" to null, "keyboard-last" to ""),
                )
                form.stopEditing(true)
            },
        ),
    )
}
