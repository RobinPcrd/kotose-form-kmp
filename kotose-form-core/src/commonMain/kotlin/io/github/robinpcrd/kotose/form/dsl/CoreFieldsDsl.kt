/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.dsl

import io.github.robinpcrd.kotose.form.fields.TextField
import io.github.robinpcrd.kotose.form.fields.TextLineLimits
import io.github.robinpcrd.kotose.form.fields.SecureTextField
import io.github.robinpcrd.kotose.form.fields.DropdownOption
import io.github.robinpcrd.kotose.form.fields.SelectionPresentation
import io.github.robinpcrd.kotose.form.fields.SingleChoiceField
import io.github.robinpcrd.kotose.form.fields.ToggleField
import io.github.robinpcrd.kotose.resources.StrRes

fun GroupBuilder.textField(
    tag: String? = null,
    label: StrRes? = null,
    placeholder: StrRes? = label,
    lineLimits: TextLineLimits = TextLineLimits.SingleLine,
    init: TextField.() -> Unit = {},
): TextField = TextField(tag, label, placeholder, lineLimits).apply(init)

fun GroupBuilder.toggleField(
    tag: String? = null,
    label: StrRes,
    init: ToggleField.() -> Unit = {},
): ToggleField = ToggleField(tag, label).apply(init)

fun GroupBuilder.secureTextField(
    tag: String? = null,
    label: StrRes? = null,
    placeholder: StrRes? = label,
    init: SecureTextField.() -> Unit = {},
): SecureTextField = SecureTextField(tag, label, placeholder).apply(init)

fun <T> GroupBuilder.singleChoiceField(
    tag: String? = null,
    possibleValues: List<DropdownOption<T>>,
    placeholder: StrRes? = null,
    isClearable: Boolean = false,
    presentation: SelectionPresentation = SelectionPresentation.Menu,
    iosPresentation: SelectionPresentation? = null,
    init: SingleChoiceField<T>.() -> Unit = {},
): SingleChoiceField<T> =
    SingleChoiceField(tag, possibleValues, placeholder, isClearable, presentation, iosPresentation).apply(init)
