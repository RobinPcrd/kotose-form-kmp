/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.example.catalog.behaviors

import io.github.robinpcrd.kotose.form.RequiredFieldRule
import io.github.robinpcrd.kotose.form.ValidationOption
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
import io.github.robinpcrd.kotose.form.example.timing_always
import io.github.robinpcrd.kotose.form.example.timing_always_hint
import io.github.robinpcrd.kotose.form.example.timing_label
import io.github.robinpcrd.kotose.form.example.timing_on_change
import io.github.robinpcrd.kotose.form.example.timing_on_change_hint
import io.github.robinpcrd.kotose.form.example.timing_on_demand
import io.github.robinpcrd.kotose.form.example.timing_on_demand_hint
import io.github.robinpcrd.kotose.form.example.timing_page_title

/** The same required rule under the three validation timings. Type, clear, then Validate. */
fun validationTimingPage(labels: PageLabels): CatalogPage {
    val form = form {
        +group {
            +displayText(text = res(Res.string.timing_on_demand), isTitle = true)
            +displayParagraph(lines = listOf(res(Res.string.timing_on_demand_hint)))
            +textField("timing-on-demand", res(Res.string.timing_label)) {
                validationOption = ValidationOption.ON_DEMAND
                addRule(RequiredFieldRule())
            }
        }
        +group {
            +displayText(text = res(Res.string.timing_on_change), isTitle = true)
            +displayParagraph(lines = listOf(res(Res.string.timing_on_change_hint)))
            +textField("timing-on-change", res(Res.string.timing_label)) {
                validationOption = ValidationOption.ON_VALUE_CHANGE
                addRule(RequiredFieldRule())
            }
        }
        +group {
            +displayText(text = res(Res.string.timing_always), isTitle = true)
            +displayParagraph(lines = listOf(res(Res.string.timing_always_hint)))
            +textField("timing-always", res(Res.string.timing_label)) {
                validationOption = ValidationOption.ALWAYS
                addRule(RequiredFieldRule())
            }
        }
    }
    return CatalogPage(
        "validation-timing", CatalogPage.Section.BEHAVIORS, res(Res.string.timing_page_title), form, labels,
        listOf(
            PageAction("reset", labels.reset) {
                form.setValues(mapOf("timing-on-demand" to "", "timing-on-change" to "", "timing-always" to ""))
            },
        ),
    )
}
