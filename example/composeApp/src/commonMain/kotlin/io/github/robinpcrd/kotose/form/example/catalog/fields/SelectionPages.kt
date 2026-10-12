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
import io.github.robinpcrd.kotose.form.dsl.multipleSelectionGroup
import io.github.robinpcrd.kotose.form.dsl.selectionOption
import io.github.robinpcrd.kotose.form.dsl.singleSelectionGroup
import io.github.robinpcrd.kotose.form.example.Res
import io.github.robinpcrd.kotose.form.example.catalog.CatalogPage
import io.github.robinpcrd.kotose.form.example.catalog.PageAction
import io.github.robinpcrd.kotose.form.example.catalog.PageLabels
import io.github.robinpcrd.kotose.form.example.catalog.res
import io.github.robinpcrd.kotose.form.example.multiple_breakfast
import io.github.robinpcrd.kotose.form.example.multiple_group_error
import io.github.robinpcrd.kotose.form.example.multiple_hint
import io.github.robinpcrd.kotose.form.example.multiple_lounge
import io.github.robinpcrd.kotose.form.example.multiple_page_title
import io.github.robinpcrd.kotose.form.example.multiple_parking
import io.github.robinpcrd.kotose.form.example.single_basic
import io.github.robinpcrd.kotose.form.example.single_basic_subtitle
import io.github.robinpcrd.kotose.form.example.single_hint
import io.github.robinpcrd.kotose.form.example.single_page_title
import io.github.robinpcrd.kotose.form.example.single_plus
import io.github.robinpcrd.kotose.form.example.single_plus_subtitle
import io.github.robinpcrd.kotose.form.example.single_pro
import io.github.robinpcrd.kotose.form.example.single_pro_subtitle
import io.github.robinpcrd.kotose.form.example.single_title

data class Plan(val code: Int)
data class Extra(val code: Int)

/** Radio-style groups: exactly one typed value per group, required by default. */
fun singleSelectionPage(labels: PageLabels): CatalogPage {
    val form = form {
        +singleSelectionGroup<Plan> {
            tag = "plan-empty"
            +displayText(text = labels.empty, isTitle = true)
            +displayParagraph(lines = listOf(res(Res.string.single_hint)))
            +selectionOption("plan-empty-basic", Plan(1), res(Res.string.single_basic))
            +selectionOption("plan-empty-plus", Plan(2), res(Res.string.single_plus))
            +selectionOption("plan-empty-pro", Plan(3), res(Res.string.single_pro))
        }
        +singleSelectionGroup<Plan> {
            tag = "plan-filled"
            title = res(Res.string.single_title)
            +displayText(text = labels.filled, isTitle = true)
            +selectionOption("plan-filled-basic", Plan(1), res(Res.string.single_basic)) {
                subtitle = res(Res.string.single_basic_subtitle)
            }
            +selectionOption("plan-filled-plus", Plan(2), res(Res.string.single_plus)) {
                subtitle = res(Res.string.single_plus_subtitle)
                value = Plan(2)
            }
            +selectionOption("plan-filled-pro", Plan(3), res(Res.string.single_pro)) {
                subtitle = res(Res.string.single_pro_subtitle)
            }
        }
        +singleSelectionGroup<Plan> {
            tag = "plan-disabled"
            isEnabledCondition = Condition.False
            +displayText(text = labels.disabled, isTitle = true)
            +selectionOption("plan-disabled-basic", Plan(1), res(Res.string.single_basic)) { value = Plan(1) }
            +selectionOption("plan-disabled-plus", Plan(2), res(Res.string.single_plus))
        }
        +singleSelectionGroup<Plan> {
            tag = "plan-error"
            isRequired = true
            +displayText(text = labels.error, isTitle = true)
            +selectionOption("plan-error-basic", Plan(1), res(Res.string.single_basic))
            +selectionOption("plan-error-plus", Plan(2), res(Res.string.single_plus))
        }
    }
    form.validate(scrollToFirstError = false)
    return CatalogPage(
        "single-selection", CatalogPage.Section.FIELDS, res(Res.string.single_page_title), form, labels,
        listOf(
            PageAction("reset", labels.reset) {
                form.setValues(mapOf("plan-empty" to null, "plan-filled" to Plan(2), "plan-error" to null))
            },
        ),
    )
}

/** Checkbox groups: a typed collection per group, with per-option and group rules. */
fun multipleSelectionPage(labels: PageLabels): CatalogPage {
    val atLeastTwo = object : ValidationError() {
        override val error = res(Res.string.multiple_group_error)
    }
    val form = form {
        +multipleSelectionGroup<Extra> {
            tag = "extras-empty"
            +displayText(text = labels.empty, isTitle = true)
            +displayParagraph(lines = listOf(res(Res.string.multiple_hint)))
            +selectionOption("extras-empty-breakfast", Extra(10), res(Res.string.multiple_breakfast))
            +selectionOption("extras-empty-parking", Extra(20), res(Res.string.multiple_parking))
            +selectionOption("extras-empty-lounge", Extra(30), res(Res.string.multiple_lounge))
        }
        +multipleSelectionGroup<Extra> {
            tag = "extras-filled"
            +displayText(text = labels.filled, isTitle = true)
            +selectionOption("extras-filled-breakfast", Extra(10), res(Res.string.multiple_breakfast)) { value = Extra(10) }
            +selectionOption("extras-filled-parking", Extra(20), res(Res.string.multiple_parking)) { value = Extra(20) }
            +selectionOption("extras-filled-lounge", Extra(30), res(Res.string.multiple_lounge))
        }
        +multipleSelectionGroup<Extra> {
            tag = "extras-disabled"
            isEnabledCondition = Condition.False
            +displayText(text = labels.disabled, isTitle = true)
            +selectionOption("extras-disabled-breakfast", Extra(10), res(Res.string.multiple_breakfast)) { value = Extra(10) }
            +selectionOption("extras-disabled-parking", Extra(20), res(Res.string.multiple_parking))
        }
        +multipleSelectionGroup<Extra> {
            tag = "extras-error"
            +displayText(text = labels.error, isTitle = true)
            +selectionOption("extras-error-breakfast", Extra(10), res(Res.string.multiple_breakfast)) { value = Extra(10) }
            +selectionOption("extras-error-parking", Extra(20), res(Res.string.multiple_parking))
            +selectionOption("extras-error-lounge", Extra(30), res(Res.string.multiple_lounge))
        }.apply {
            addRule(object : ValidationRule.Group(atLeastTwo) {
                override fun isValid() = atLeastTwo.takeIf { selectedValues.size < 2 }
            })
        }
    }
    form.validate(scrollToFirstError = false)
    return CatalogPage(
        "multiple-selection", CatalogPage.Section.FIELDS, res(Res.string.multiple_page_title), form, labels,
        listOf(
            PageAction("reset", labels.reset) {
                form.setValues(mapOf(
                    "extras-empty" to emptyList<Extra>(),
                    "extras-filled" to listOf(Extra(10), Extra(20)),
                    "extras-error" to listOf(Extra(10)),
                ))
            },
        ),
    )
}
