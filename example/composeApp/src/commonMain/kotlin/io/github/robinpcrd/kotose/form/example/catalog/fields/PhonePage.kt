/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.example.catalog.fields

import io.github.robinpcrd.kotose.form.Condition
import io.github.robinpcrd.kotose.form.IsValidPhoneRule
import io.github.robinpcrd.kotose.form.RequiredFieldRule
import io.github.robinpcrd.kotose.form.compose.data.CountryRegion
import io.github.robinpcrd.kotose.form.dsl.displayParagraph
import io.github.robinpcrd.kotose.form.dsl.displayText
import io.github.robinpcrd.kotose.form.dsl.form
import io.github.robinpcrd.kotose.form.dsl.group
import io.github.robinpcrd.kotose.form.example.Res
import io.github.robinpcrd.kotose.form.example.action_toggle_phone_backend
import io.github.robinpcrd.kotose.form.example.catalog.CatalogPage
import io.github.robinpcrd.kotose.form.example.catalog.PageAction
import io.github.robinpcrd.kotose.form.example.catalog.PageLabels
import io.github.robinpcrd.kotose.form.example.catalog.res
import io.github.robinpcrd.kotose.form.example.phone_hint
import io.github.robinpcrd.kotose.form.example.phone_label
import io.github.robinpcrd.kotose.form.example.phone_page_title
import io.github.robinpcrd.kotose.form.example.phone_warning_hint
import io.github.robinpcrd.kotose.form.fields.PhoneNumberField
import io.github.robinpcrd.kotose.form.utils.LibPhoneNumberBackend
import io.github.robinpcrd.kotose.form.utils.PhoneNumberBackend
import kotlinx.collections.immutable.persistentSetOf

/**
 * Phone input with a country picker; parsing and country policy stay in Kotlin. The toggle
 * switches every phone field to the basic dial code and number input, and back.
 */
fun phonePage(labels: PageLabels): CatalogPage {
    fun phone(tag: String, init: PhoneNumberField.() -> Unit = {}) =
        PhoneNumberField(tag, res(Res.string.phone_label)).apply {
            setOfAllowedRegion = persistentSetOf("FR", "BE", "US", "GB")
            selectCountry(CountryRegion.France)
            init()
        }
    val form = form {
        +group {
            +displayText(text = labels.empty, isTitle = true)
            +displayParagraph(lines = listOf(res(Res.string.phone_hint)))
            +phone("phone-empty")
        }
        +group {
            +displayText(text = labels.filled, isTitle = true)
            +phone("phone-filled") { setPhone("+33612345678") }
        }
        +group {
            +displayText(text = labels.disabled, isTitle = true)
            +phone("phone-disabled") {
                setPhone("+33612345678")
                isEnabledCondition = Condition.False
            }
        }
        +group {
            +displayText(text = labels.error, isTitle = true)
            +phone("phone-error") { addRule(RequiredFieldRule()) }
        }
        +group {
            +displayText(text = labels.warning, isTitle = true)
            +displayParagraph(lines = listOf(res(Res.string.phone_warning_hint)))
            +phone("phone-warning") {
                setPhone("12")
                addRule(IsValidPhoneRule)
            }
        }
    }
    form.validate(scrollToFirstError = false)
    return CatalogPage(
        "phone", CatalogPage.Section.FIELDS, res(Res.string.phone_page_title), form, labels,
        listOf(
            PageAction("reset", labels.reset) {
                form.setValues(mapOf("phone-empty" to null, "phone-filled" to null, "phone-error" to null, "phone-warning" to null))
            },
            PageAction("phone-backend", res(Res.string.action_toggle_phone_backend)) {
                PhoneNumberBackend.current = if (PhoneNumberBackend.current == null) LibPhoneNumberBackend else null
            },
        ),
    )
}
