/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.example.catalog.behaviors

import io.github.robinpcrd.kotose.form.Condition
import io.github.robinpcrd.kotose.form.CustomCondition
import io.github.robinpcrd.kotose.form.dsl.displayParagraph
import io.github.robinpcrd.kotose.form.dsl.displayText
import io.github.robinpcrd.kotose.form.dsl.form
import io.github.robinpcrd.kotose.form.dsl.group
import io.github.robinpcrd.kotose.form.dsl.textField
import io.github.robinpcrd.kotose.form.dsl.toggleField
import io.github.robinpcrd.kotose.form.example.Res
import io.github.robinpcrd.kotose.form.example.catalog.CatalogPage
import io.github.robinpcrd.kotose.form.example.catalog.PageAction
import io.github.robinpcrd.kotose.form.example.catalog.PageLabels
import io.github.robinpcrd.kotose.form.example.catalog.res
import io.github.robinpcrd.kotose.form.example.conditions_custom
import io.github.robinpcrd.kotose.form.example.conditions_custom_hint
import io.github.robinpcrd.kotose.form.example.conditions_enabled
import io.github.robinpcrd.kotose.form.example.conditions_enabled_hint
import io.github.robinpcrd.kotose.form.example.conditions_enabled_toggle
import io.github.robinpcrd.kotose.form.example.conditions_field
import io.github.robinpcrd.kotose.form.example.conditions_mirror
import io.github.robinpcrd.kotose.form.example.conditions_page_title
import io.github.robinpcrd.kotose.form.example.conditions_source
import io.github.robinpcrd.kotose.form.example.conditions_visible
import io.github.robinpcrd.kotose.form.example.conditions_visible_hint
import io.github.robinpcrd.kotose.form.example.conditions_visible_toggle
import io.github.robinpcrd.kotose.form.fields.DisplayTextField

/** Fields react to other fields through conditions keyed by tag. */
fun conditionsPage(labels: PageLabels): CatalogPage {
    val mirror = DisplayTextField("conditions-mirror", res(Res.string.conditions_mirror, "")).apply {
        customCondition = CustomCondition("conditions-source") { element, form ->
            (element as DisplayTextField).value =
                res(Res.string.conditions_mirror, form.fieldValue<String>("conditions-source").orEmpty())
        }
    }
    val form = form {
        +group {
            +displayText(text = res(Res.string.conditions_visible), isTitle = true)
            +displayParagraph(lines = listOf(res(Res.string.conditions_visible_hint)))
            +toggleField("conditions-show", res(Res.string.conditions_visible_toggle)) { value = true }
            +textField("conditions-shown", res(Res.string.conditions_field)) {
                isVisibleCondition = Condition("conditions-show") { it.fieldValue<Boolean>("conditions-show") == true }
            }
        }
        +group {
            +displayText(text = res(Res.string.conditions_enabled), isTitle = true)
            +displayParagraph(lines = listOf(res(Res.string.conditions_enabled_hint)))
            +toggleField("conditions-enable", res(Res.string.conditions_enabled_toggle)) { value = false }
            +textField("conditions-enabled-field", res(Res.string.conditions_field)) {
                isEnabledCondition = Condition("conditions-enable") { it.fieldValue<Boolean>("conditions-enable") == true }
            }
        }
        +group {
            +displayText(text = res(Res.string.conditions_custom), isTitle = true)
            +displayParagraph(lines = listOf(res(Res.string.conditions_custom_hint)))
            +textField("conditions-source", res(Res.string.conditions_source))
            +mirror
        }
    }
    return CatalogPage(
        "conditions", CatalogPage.Section.BEHAVIORS, res(Res.string.conditions_page_title), form, labels,
        listOf(
            PageAction("reset", labels.reset) {
                form.setValues(mapOf(
                    "conditions-show" to true, "conditions-shown" to "",
                    "conditions-enable" to false, "conditions-enabled-field" to "",
                    "conditions-source" to "",
                ))
            },
        ),
    )
}
