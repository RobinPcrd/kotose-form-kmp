/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.example.catalog.fields

import io.github.robinpcrd.kotose.form.Condition
import io.github.robinpcrd.kotose.form.dsl.actionField
import io.github.robinpcrd.kotose.form.dsl.displayParagraph
import io.github.robinpcrd.kotose.form.dsl.displayText
import io.github.robinpcrd.kotose.form.dsl.form
import io.github.robinpcrd.kotose.form.dsl.group
import io.github.robinpcrd.kotose.form.example.Res
import io.github.robinpcrd.kotose.form.example.action_hint
import io.github.robinpcrd.kotose.form.example.action_page_title
import io.github.robinpcrd.kotose.form.example.action_taps
import io.github.robinpcrd.kotose.form.example.action_tap_me
import io.github.robinpcrd.kotose.form.example.caption_body
import io.github.robinpcrd.kotose.form.example.caption_live
import io.github.robinpcrd.kotose.form.example.caption_multi_line
import io.github.robinpcrd.kotose.form.example.caption_single_line
import io.github.robinpcrd.kotose.form.example.caption_title
import io.github.robinpcrd.kotose.form.example.catalog.CatalogPage
import io.github.robinpcrd.kotose.form.example.catalog.PageAction
import io.github.robinpcrd.kotose.form.example.catalog.PageLabels
import io.github.robinpcrd.kotose.form.example.catalog.res
import io.github.robinpcrd.kotose.form.example.label_body_text
import io.github.robinpcrd.kotose.form.example.label_live_hint
import io.github.robinpcrd.kotose.form.example.label_live_value
import io.github.robinpcrd.kotose.form.example.label_page_title
import io.github.robinpcrd.kotose.form.example.label_title_text
import io.github.robinpcrd.kotose.form.example.paragraph_line
import io.github.robinpcrd.kotose.form.example.paragraph_page_title
import io.github.robinpcrd.kotose.form.example.paragraph_single
import io.github.robinpcrd.kotose.form.fields.DisplayTextField

/** Read-only text: title emphasis, body text, and a label whose value changes at runtime. */
fun labelPage(labels: PageLabels): CatalogPage {
    var ticks = 0
    val live = DisplayTextField("label-live", res(Res.string.label_live_value, 0))
    val form = form {
        +group {
            +displayText(text = res(Res.string.caption_title), isTitle = true)
            +displayText("label-title", res(Res.string.label_title_text), isTitle = true)
        }
        +group {
            +displayText(text = res(Res.string.caption_body), isTitle = true)
            +displayText("label-body", res(Res.string.label_body_text))
        }
        +group {
            +displayText(text = res(Res.string.caption_live), isTitle = true)
            +displayParagraph(lines = listOf(res(Res.string.label_live_hint)))
            +live
        }
    }
    return CatalogPage(
        "label", CatalogPage.Section.FIELDS, res(Res.string.label_page_title), form, labels,
        listOf(
            PageAction("tick", res(Res.string.action_tap_me)) { ticks++; live.value = res(Res.string.label_live_value, ticks) },
            PageAction("reset", labels.reset) { ticks = 0; live.value = res(Res.string.label_live_value, 0) },
        ),
    )
}

/** Read-only paragraphs: one line and several lines. */
fun paragraphPage(labels: PageLabels): CatalogPage {
    val form = form {
        +group {
            +displayText(text = res(Res.string.caption_single_line), isTitle = true)
            +displayParagraph("paragraph-single", listOf(res(Res.string.paragraph_single)))
        }
        +group {
            +displayText(text = res(Res.string.caption_multi_line), isTitle = true)
            +displayParagraph("paragraph-multi", List(4) { res(Res.string.paragraph_line, it + 1) })
        }
    }
    return CatalogPage("paragraph", CatalogPage.Section.FIELDS, res(Res.string.paragraph_page_title), form, labels)
}

/** A button whose behavior lives in Kotlin; both renderers only report the tap. */
fun actionPage(labels: PageLabels): CatalogPage {
    var taps = 0
    val counter = DisplayTextField("action-count", res(Res.string.action_taps, 0))
    val form = form {
        +group {
            +displayText(text = labels.empty, isTitle = true)
            +displayParagraph(lines = listOf(res(Res.string.action_hint)))
            +actionField("action-enabled", res(Res.string.action_tap_me)) { taps++; counter.value = res(Res.string.action_taps, taps) }
            +counter
        }
        +group {
            +displayText(text = labels.disabled, isTitle = true)
            +actionField("action-disabled", res(Res.string.action_tap_me), init = { isEnabledCondition = Condition.False }) {}
        }
    }
    return CatalogPage(
        "action", CatalogPage.Section.FIELDS, res(Res.string.action_page_title), form, labels,
        listOf(PageAction("reset", labels.reset) { taps = 0; counter.value = res(Res.string.action_taps, 0) }),
    )
}
