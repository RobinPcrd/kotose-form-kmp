/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.example.catalog.behaviors

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
import io.github.robinpcrd.kotose.form.example.transactions_add
import io.github.robinpcrd.kotose.form.example.transactions_hint
import io.github.robinpcrd.kotose.form.example.transactions_note
import io.github.robinpcrd.kotose.form.example.transactions_page_title
import io.github.robinpcrd.kotose.form.example.transactions_remove
import io.github.robinpcrd.kotose.form.example.transactions_section
import io.github.robinpcrd.kotose.form.fields.TextField
import io.github.robinpcrd.kotose.form.transaction

/** Fields are added and removed at runtime in one transaction, so the UI updates once. */
fun transactionsPage(labels: PageLabels): CatalogPage {
    val notes = mutableListOf<TextField>()
    val form = form {
        +group {
            +displayText(text = res(Res.string.transactions_section), isTitle = true)
            +displayParagraph(lines = listOf(res(Res.string.transactions_hint)))
        }
    }
    val group = form.groups.single()
    fun add() {
        val note = TextField("note-${notes.size + 1}", res(Res.string.transactions_note, notes.size + 1))
            .apply { if (notes.isEmpty()) addRule(RequiredFieldRule()) }
        notes += note
        transaction(form) { add(note, group) }
    }
    fun removeLast() {
        val note = notes.removeLastOrNull() ?: return
        transaction(form) { remove(note, group) }
    }
    add()
    return CatalogPage(
        "transactions", CatalogPage.Section.BEHAVIORS, res(Res.string.transactions_page_title), form, labels,
        listOf(
            PageAction("add", res(Res.string.transactions_add)) { add() },
            PageAction("remove", res(Res.string.transactions_remove)) { removeLast() },
        ),
    )
}
