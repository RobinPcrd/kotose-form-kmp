/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.example.catalog.fields

import io.github.robinpcrd.kotose.form.Condition
import io.github.robinpcrd.kotose.form.ExperimentalKotoseFormApi
import io.github.robinpcrd.kotose.form.Field
import io.github.robinpcrd.kotose.form.ValidationError
import io.github.robinpcrd.kotose.form.ValidationRule
import io.github.robinpcrd.kotose.form.dsl.displayParagraph
import io.github.robinpcrd.kotose.form.dsl.displayText
import io.github.robinpcrd.kotose.form.dsl.form
import io.github.robinpcrd.kotose.form.dsl.group
import io.github.robinpcrd.kotose.form.example.Res
import io.github.robinpcrd.kotose.form.example.catalog.CatalogPage
import io.github.robinpcrd.kotose.form.example.catalog.PageAction
import io.github.robinpcrd.kotose.form.example.catalog.PageLabels
import io.github.robinpcrd.kotose.form.example.catalog.res
import io.github.robinpcrd.kotose.form.example.custom_hint
import io.github.robinpcrd.kotose.form.example.custom_label
import io.github.robinpcrd.kotose.form.example.custom_max_error
import io.github.robinpcrd.kotose.form.example.custom_page_title
import io.github.robinpcrd.kotose.form.fields.CustomField
import io.github.robinpcrd.kotose.resources.StrRes

/**
 * A consumer-defined typed field. Compose renders it through FormScreen.fieldView and SwiftUI
 * through the KotoseNativeForm custom callback; both call the same Kotlin methods.
 */
@OptIn(ExperimentalKotoseFormApi::class)
class StepperField(tag: String, override val label: StrRes, initial: Int = 0) : Field<Int>(tag), CustomField {
    init { value = initial }
    override val kind = "stepper"
    fun increment() { value = (value ?: 0) + 1 }
    fun decrement() { value = ((value ?: 0) - 1).coerceAtLeast(0) }
    override fun setBaseValue(value: Any?) { this.value = (value as? Int)?.coerceAtLeast(0) }
}

fun customPage(labels: PageLabels): CatalogPage {
    val tooMany = object : ValidationError() {
        override val error = res(Res.string.custom_max_error)
    }
    val form = form {
        +group {
            +displayText(text = labels.empty, isTitle = true)
            +displayParagraph(lines = listOf(res(Res.string.custom_hint)))
            +StepperField("stepper", res(Res.string.custom_label))
        }
        +group {
            +displayText(text = labels.disabled, isTitle = true)
            +StepperField("stepper-disabled", res(Res.string.custom_label), initial = 2).apply { isEnabledCondition = Condition.False }
        }
        +group {
            +displayText(text = labels.error, isTitle = true)
            +StepperField("stepper-error", res(Res.string.custom_label), initial = 6).apply {
                addRule(object : ValidationRule.Field<Int>() {
                    override fun isValid(value: Int?) = tooMany.takeIf { (value ?: 0) > 5 }
                })
            }
        }
    }
    form.validate(scrollToFirstError = false)
    return CatalogPage(
        "custom", CatalogPage.Section.FIELDS, res(Res.string.custom_page_title), form, labels,
        listOf(PageAction("reset", labels.reset) { form.setValues(mapOf("stepper" to 0, "stepper-error" to 6)) }),
    )
}
