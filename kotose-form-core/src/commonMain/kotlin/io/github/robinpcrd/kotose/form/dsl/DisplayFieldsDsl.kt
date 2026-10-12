/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.dsl

import io.github.robinpcrd.kotose.form.fields.ActionField
import io.github.robinpcrd.kotose.form.fields.DisplayParagraphField
import io.github.robinpcrd.kotose.form.fields.DisplayTextField
import io.github.robinpcrd.kotose.resources.StrRes

fun GroupBuilder.displayText(
    tag: String? = null,
    text: StrRes? = null,
    isTitle: Boolean = false,
    init: DisplayTextField.() -> Unit = {},
): DisplayTextField = DisplayTextField(tag, text, isTitle).apply(init)

fun GroupBuilder.displayParagraph(
    tag: String? = null,
    lines: List<StrRes> = emptyList(),
    init: DisplayParagraphField.() -> Unit = {},
): DisplayParagraphField = DisplayParagraphField(tag, lines).apply(init)

/** The trailing lambda is the click handler; configure the field through [init]. */
fun GroupBuilder.actionField(
    tag: String? = null,
    title: StrRes,
    init: ActionField.() -> Unit = {},
    onClick: () -> Unit,
): ActionField = ActionField(tag, title, onClick).apply(init)
