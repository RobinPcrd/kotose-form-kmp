/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

@file:OptIn(io.github.robinpcrd.kotose.form.swiftui.ExperimentalKotoseSwiftUIApi::class)

package io.github.robinpcrd.kotose.form.example.catalog

import io.github.robinpcrd.kotose.form.example.Res
import io.github.robinpcrd.kotose.form.example.action_reset
import io.github.robinpcrd.kotose.form.example.action_toggle_enabled
import io.github.robinpcrd.kotose.form.example.action_validate
import io.github.robinpcrd.kotose.form.example.caption_disabled
import io.github.robinpcrd.kotose.form.example.caption_empty
import io.github.robinpcrd.kotose.form.example.caption_error
import io.github.robinpcrd.kotose.form.example.caption_filled
import io.github.robinpcrd.kotose.form.example.caption_warning
import io.github.robinpcrd.kotose.form.example.catalog.behaviors.conditionsPage
import io.github.robinpcrd.kotose.form.example.catalog.behaviors.keyboardPage
import io.github.robinpcrd.kotose.form.example.catalog.behaviors.scrollToErrorPage
import io.github.robinpcrd.kotose.form.example.catalog.behaviors.transactionsPage
import io.github.robinpcrd.kotose.form.example.catalog.behaviors.validationTimingPage
import io.github.robinpcrd.kotose.form.example.catalog.fields.actionPage
import io.github.robinpcrd.kotose.form.example.catalog.fields.customPage
import io.github.robinpcrd.kotose.form.example.catalog.fields.datePage
import io.github.robinpcrd.kotose.form.example.catalog.fields.dateTimePage
import io.github.robinpcrd.kotose.form.example.catalog.fields.dropdownPage
import io.github.robinpcrd.kotose.form.example.catalog.fields.labelPage
import io.github.robinpcrd.kotose.form.example.catalog.fields.multipleSelectionPage
import io.github.robinpcrd.kotose.form.example.catalog.fields.paragraphPage
import io.github.robinpcrd.kotose.form.example.catalog.fields.multilineTextPage
import io.github.robinpcrd.kotose.form.example.catalog.fields.passwordPage
import io.github.robinpcrd.kotose.form.example.catalog.fields.phonePage
import io.github.robinpcrd.kotose.form.example.catalog.fields.selectionPresentationPage
import io.github.robinpcrd.kotose.form.example.catalog.fields.singleSelectionPage
import io.github.robinpcrd.kotose.form.example.catalog.fields.textPage
import io.github.robinpcrd.kotose.form.example.catalog.fields.togglePage
import io.github.robinpcrd.kotose.form.example.result_errors
import io.github.robinpcrd.kotose.form.example.result_valid
import io.github.robinpcrd.kotose.form.swiftui.NativeFormAdapter
import io.github.robinpcrd.kotose.form.utils.LibPhoneNumberBackend
import io.github.robinpcrd.kotose.form.utils.PhoneNumberBackend
import io.github.robinpcrd.kotose.resources.StrRes

/** The single registry both renderers read. */
class Catalog {
    init {
        PhoneNumberBackend.current = LibPhoneNumberBackend
    }

    private val labels = PageLabels(
        validate = res(Res.string.action_validate),
        reset = res(Res.string.action_reset),
        toggleEnabled = res(Res.string.action_toggle_enabled),
        empty = res(Res.string.caption_empty),
        filled = res(Res.string.caption_filled),
        disabled = res(Res.string.caption_disabled),
        error = res(Res.string.caption_error),
        warning = res(Res.string.caption_warning),
        valid = res(Res.string.result_valid),
        errors = { res(Res.string.result_errors, it) },
    )

    val pages: List<CatalogPage> = listOf(
        textPage(labels), multilineTextPage(labels), passwordPage(labels), phonePage(labels), togglePage(labels),
        dropdownPage(labels), singleSelectionPage(labels), selectionPresentationPage(labels), multipleSelectionPage(labels),
        datePage(labels), dateTimePage(labels), labelPage(labels), paragraphPage(labels),
        actionPage(labels), customPage(labels),
        validationTimingPage(labels), conditionsPage(labels), transactionsPage(labels),
        keyboardPage(labels), scrollToErrorPage(labels),
    )

    private val adapters = pages.associate { it.id to NativeFormAdapter(it.form) }

    fun byId(id: String): CatalogPage = pages.first { it.id == id }

    fun adapter(page: CatalogPage): NativeFormAdapter = adapters.getValue(page.id)

    /** Resolves display text for the SwiftUI renderer. */
    suspend fun text(res: StrRes): String = res.getStringSuspendOrEmpty()
}
