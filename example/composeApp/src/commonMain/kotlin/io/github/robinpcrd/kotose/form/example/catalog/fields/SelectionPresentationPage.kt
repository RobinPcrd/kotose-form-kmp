/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.example.catalog.fields

import io.github.robinpcrd.kotose.form.Condition
import io.github.robinpcrd.kotose.form.RequiredFieldRule
import io.github.robinpcrd.kotose.form.dsl.SingleSelectionGroupBuilder
import io.github.robinpcrd.kotose.form.dsl.displayParagraph
import io.github.robinpcrd.kotose.form.dsl.displayText
import io.github.robinpcrd.kotose.form.dsl.form
import io.github.robinpcrd.kotose.form.dsl.group
import io.github.robinpcrd.kotose.form.dsl.selectionOption
import io.github.robinpcrd.kotose.form.dsl.singleChoiceField
import io.github.robinpcrd.kotose.form.dsl.singleSelectionGroup
import io.github.robinpcrd.kotose.form.example.Res
import io.github.robinpcrd.kotose.form.example.billing_lifetime
import io.github.robinpcrd.kotose.form.example.billing_monthly
import io.github.robinpcrd.kotose.form.example.billing_title
import io.github.robinpcrd.kotose.form.example.billing_yearly
import io.github.robinpcrd.kotose.form.example.caption_choice_field
import io.github.robinpcrd.kotose.form.example.caption_ios_page
import io.github.robinpcrd.kotose.form.example.caption_menu
import io.github.robinpcrd.kotose.form.example.caption_page
import io.github.robinpcrd.kotose.form.example.caption_segmented
import io.github.robinpcrd.kotose.form.example.caption_sheet
import io.github.robinpcrd.kotose.form.example.catalog.CatalogPage
import io.github.robinpcrd.kotose.form.example.catalog.PageAction
import io.github.robinpcrd.kotose.form.example.catalog.PageLabels
import io.github.robinpcrd.kotose.form.example.catalog.res
import io.github.robinpcrd.kotose.form.example.dropdown_express
import io.github.robinpcrd.kotose.form.example.dropdown_pickup
import io.github.robinpcrd.kotose.form.example.dropdown_placeholder
import io.github.robinpcrd.kotose.form.example.dropdown_standard
import io.github.robinpcrd.kotose.form.example.presentation_hint
import io.github.robinpcrd.kotose.form.example.presentation_page_title
import io.github.robinpcrd.kotose.form.example.single_basic
import io.github.robinpcrd.kotose.form.example.single_basic_subtitle
import io.github.robinpcrd.kotose.form.example.single_plus
import io.github.robinpcrd.kotose.form.example.single_plus_subtitle
import io.github.robinpcrd.kotose.form.example.single_pro
import io.github.robinpcrd.kotose.form.example.single_pro_subtitle
import io.github.robinpcrd.kotose.form.example.single_title
import io.github.robinpcrd.kotose.form.example.size_large
import io.github.robinpcrd.kotose.form.example.size_medium
import io.github.robinpcrd.kotose.form.example.size_placeholder
import io.github.robinpcrd.kotose.form.example.size_small
import io.github.robinpcrd.kotose.form.fields.DropdownOption
import io.github.robinpcrd.kotose.form.fields.SelectionPresentation
import io.github.robinpcrd.kotose.form.dsl.MultipleSelectionGroupBuilder
import io.github.robinpcrd.kotose.form.dsl.multipleSelectionGroup
import io.github.robinpcrd.kotose.form.example.caption_multiple_page
import io.github.robinpcrd.kotose.form.example.caption_multiple_sheet
import io.github.robinpcrd.kotose.form.example.extras_title
import io.github.robinpcrd.kotose.form.example.multiple_breakfast
import io.github.robinpcrd.kotose.form.example.multiple_lounge
import io.github.robinpcrd.kotose.form.example.multiple_parking

/** The same single selections under each presentation, including an iOS-only override. */
fun selectionPresentationPage(labels: PageLabels): CatalogPage {
    fun SingleSelectionGroupBuilder<Plan>.plans(prefix: String) {
        +selectionOption("$prefix-basic", Plan(1), res(Res.string.single_basic)) {
            subtitle = res(Res.string.single_basic_subtitle)
        }
        +selectionOption("$prefix-plus", Plan(2), res(Res.string.single_plus)) {
            subtitle = res(Res.string.single_plus_subtitle)
        }
        +selectionOption("$prefix-pro", Plan(3), res(Res.string.single_pro)) {
            subtitle = res(Res.string.single_pro_subtitle)
        }
    }
    fun MultipleSelectionGroupBuilder<Extra>.extras(prefix: String) {
        +selectionOption("$prefix-breakfast", Extra(10), res(Res.string.multiple_breakfast))
        +selectionOption("$prefix-parking", Extra(20), res(Res.string.multiple_parking))
        +selectionOption("$prefix-lounge", Extra(30), res(Res.string.multiple_lounge))
    }
    fun deliveries() = listOf(
        DropdownOption(Delivery(10), res(Res.string.dropdown_standard), "standard"),
        DropdownOption(Delivery(20), res(Res.string.dropdown_express), "express"),
        DropdownOption(Delivery(30), res(Res.string.dropdown_pickup), "pickup"),
    )
    val form = form {
        +singleSelectionGroup<Plan> {
            tag = "plan-menu"
            title = res(Res.string.single_title)
            presentation = SelectionPresentation.Menu
            +displayText(text = res(Res.string.caption_menu), isTitle = true)
            +displayParagraph(lines = listOf(res(Res.string.presentation_hint)))
            plans("plan-menu")
        }
        +singleSelectionGroup<Plan> {
            tag = "plan-sheet"
            title = res(Res.string.single_title)
            presentation = SelectionPresentation.Sheet
            isRequired = true
            +displayText(text = res(Res.string.caption_sheet), isTitle = true)
            plans("plan-sheet")
        }
        +singleSelectionGroup<Plan> {
            tag = "plan-page"
            title = res(Res.string.single_title)
            presentation = SelectionPresentation.Page
            enableDeselection = true
            +displayText(text = res(Res.string.caption_page), isTitle = true)
            plans("plan-page")
        }
        +singleSelectionGroup<Plan> {
            tag = "billing"
            title = res(Res.string.billing_title)
            presentation = SelectionPresentation.Segmented
            +displayText(text = res(Res.string.caption_segmented), isTitle = true)
            +selectionOption("billing-monthly", Plan(1), res(Res.string.billing_monthly))
            +selectionOption("billing-yearly", Plan(2), res(Res.string.billing_yearly))
            +selectionOption("billing-lifetime", Plan(3), res(Res.string.billing_lifetime)) {
                isEnabledCondition = Condition.False
            }
        }
        +multipleSelectionGroup<Extra> {
            tag = "extras-sheet"
            title = res(Res.string.extras_title)
            presentation = SelectionPresentation.Sheet
            +displayText(text = res(Res.string.caption_multiple_sheet), isTitle = true)
            extras("extras-sheet")
        }
        +multipleSelectionGroup<Extra> {
            tag = "extras-page"
            title = res(Res.string.extras_title)
            presentation = SelectionPresentation.Page
            +displayText(text = res(Res.string.caption_multiple_page), isTitle = true)
            extras("extras-page")
        }
        +singleSelectionGroup<Plan> {
            tag = "plan-ios-page"
            title = res(Res.string.single_title)
            iosPresentation = SelectionPresentation.Page
            +displayText(text = res(Res.string.caption_ios_page), isTitle = true)
            plans("plan-ios-page")
        }
        +singleSelectionGroup<Plan> {
            tag = "plan-page-disabled"
            title = res(Res.string.single_title)
            presentation = SelectionPresentation.Page
            isEnabledCondition = Condition.False
            +displayText(text = labels.disabled, isTitle = true)
            plans("plan-page-disabled")
        }
        +group {
            +displayText(text = res(Res.string.caption_choice_field), isTitle = true)
            +singleChoiceField(
                "delivery-inline", deliveries(), res(Res.string.dropdown_placeholder),
                presentation = SelectionPresentation.Inline,
            )
            +singleChoiceField(
                "delivery-sheet", deliveries(), res(Res.string.dropdown_placeholder), isClearable = true,
                presentation = SelectionPresentation.Sheet,
            ) { value = Delivery(20) }
            +singleChoiceField(
                "delivery-page", deliveries(), res(Res.string.dropdown_placeholder),
                presentation = SelectionPresentation.Page,
            ) { addRule(RequiredFieldRule()) }
            +singleChoiceField(
                "size",
                listOf(
                    DropdownOption(Delivery(1), res(Res.string.size_small), "small"),
                    DropdownOption(Delivery(2), res(Res.string.size_medium), "medium"),
                    DropdownOption(Delivery(3), res(Res.string.size_large), "large"),
                ),
                res(Res.string.size_placeholder),
                presentation = SelectionPresentation.Segmented,
            )
        }
    }
    form.setValues(mapOf("plan-menu" to Plan(2), "plan-page-disabled" to Plan(1)))
    form.validate(scrollToFirstError = false)
    return CatalogPage(
        "presentation", CatalogPage.Section.FIELDS, res(Res.string.presentation_page_title), form, labels,
        listOf(
            PageAction("reset", labels.reset) {
                form.setValues(mapOf(
                    "plan-menu" to Plan(2), "plan-sheet" to null, "plan-page" to null, "plan-ios-page" to null,
                    "delivery-inline" to null, "delivery-sheet" to Delivery(20), "delivery-page" to null,
                    "billing" to null, "size" to null,
                    "extras-sheet" to emptyList<Extra>(), "extras-page" to emptyList<Extra>(),
                ))
            },
        ),
    )
}
