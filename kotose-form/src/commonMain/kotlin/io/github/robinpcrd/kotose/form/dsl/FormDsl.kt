/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

@file:Suppress("unused")

package io.github.robinpcrd.kotose.form.dsl

import androidx.compose.runtime.Composable
import io.github.robinpcrd.kotose.form.compose.fields.selectable.MultipleSelectionField
import io.github.robinpcrd.kotose.form.compose.fields.selectable.SingleSelectionField
import io.github.robinpcrd.kotose.form.fields.ButtonField
import io.github.robinpcrd.kotose.form.fields.ButtonFieldAppearance
import io.github.robinpcrd.kotose.form.fields.DropDownField
import io.github.robinpcrd.kotose.form.fields.DropdownOption
import io.github.robinpcrd.kotose.form.fields.ErrorField
import io.github.robinpcrd.kotose.form.fields.IconButtonField
import io.github.robinpcrd.kotose.form.fields.IconButtonFieldAppearance
import io.github.robinpcrd.kotose.form.fields.InputField
import io.github.robinpcrd.kotose.form.fields.InputFieldAppearance
import io.github.robinpcrd.kotose.form.fields.TextLineLimits
import io.github.robinpcrd.kotose.form.fields.LabelField
import io.github.robinpcrd.kotose.form.fields.LabelFieldAppearance
import io.github.robinpcrd.kotose.form.fields.ParagraphField
import io.github.robinpcrd.kotose.form.fields.ParagraphFieldAppearance
import io.github.robinpcrd.kotose.form.fields.PasswordField
import io.github.robinpcrd.kotose.form.fields.PhoneField
import io.github.robinpcrd.kotose.form.fields.SelectableField
import io.github.robinpcrd.kotose.form.fields.SelectableFieldAppearance
import io.github.robinpcrd.kotose.form.fields.SwitchField
import io.github.robinpcrd.kotose.form.fields.SwitchFieldAppearance
import io.github.robinpcrd.kotose.form.utils.PainterRes
import io.github.robinpcrd.kotose.resources.StrRes


fun GroupBuilder.title(
    tag: String? = null,
    title: StrRes? = null,
    appearance: @Composable () -> LabelFieldAppearance = LabelFieldAppearance.Title,
    init: LabelField.() -> Unit = {}
): LabelField = LabelField(tag, defaultValue = title, appearance = appearance, isTitle = true).apply(init)

fun GroupBuilder.label(
    tag: String? = null,
    label: StrRes? = null,
    appearance: @Composable () -> LabelFieldAppearance = LabelFieldAppearance.Default,
    init: LabelField.() -> Unit = {}
): LabelField = LabelField(tag, defaultValue = label, appearance = appearance).apply(init)

fun GroupBuilder.input(
    tag: String? = null,
    label: StrRes? = null,
    placeholder: StrRes? = label,
    lineLimits: TextLineLimits = TextLineLimits.SingleLine,
    appearance: @Composable () -> InputFieldAppearance = InputFieldAppearance.Default,
    trailingIconSlot: (@Composable () -> Unit)? = null,
    init: InputField.() -> Unit = {}
): InputField = InputField(tag, label, placeholder, lineLimits, appearance, trailingIconSlot).apply(init)

fun GroupBuilder.password(
    tag: String? = null,
    label: StrRes? = null,
    placeholder: StrRes? = label,
    appearance: @Composable () -> InputFieldAppearance = InputFieldAppearance.DefaultPassword,
    init: PasswordField.() -> Unit = {}
): PasswordField = PasswordField(tag, label, placeholder, appearance).apply(init)

@Deprecated("Deprecated", replaceWith = ReplaceWith("buttonField"), level = DeprecationLevel.ERROR)
fun GroupBuilder.button(
    tag: String? = null,
    title: StrRes,
    appearance: @Composable () -> ButtonFieldAppearance = ButtonFieldAppearance.Default,
    onClick: () -> Unit = {}
): ButtonField = ButtonField(tag, title, appearance, onClick)

fun GroupBuilder.buttonField(
    tag: String? = null,
    title: StrRes,
    appearance: @Composable () -> ButtonFieldAppearance = ButtonFieldAppearance.Default,
    onClick: () -> Unit = {},
    init: ButtonField.() -> Unit = {}
): ButtonField = ButtonField(tag, title, appearance, onClick).apply(init)

internal fun GroupBuilder.iconButton(
    tag: String? = null,
    icon: PainterRes,
    description: StrRes? = null,
    appearance: @Composable () -> IconButtonFieldAppearance = IconButtonFieldAppearance.Default,
    onClick: () -> Unit,
    init: IconButtonField.() -> Unit = {}
): IconButtonField = IconButtonField(tag, icon, description, appearance, onClick).apply(init)



fun <T> GroupBuilder.dropDown(
    tag: String? = null,
    possibleValues: List<DropdownOption<T>>,
    placeholder: StrRes? = null,
    isClearable: Boolean = false,
    init: DropDownField<T>.() -> Unit = {}
): DropDownField<T> = DropDownField(tag, possibleValues, placeholder, isClearable).apply(init)

fun GroupBuilder.switch(
    tag: String? = null,
    label: StrRes,
    appearance: @Composable () -> SwitchFieldAppearance = SwitchFieldAppearance.Default,
    init: SwitchField.() -> Unit = {}
): SwitchField = SwitchField(tag, label, appearance).apply(init)

fun GroupBuilder.paragraph(
    tag: String? = null,
    appearance: @Composable () -> ParagraphFieldAppearance = ParagraphFieldAppearance.Default,
    init: ParagraphField.() -> Unit = {}
): ParagraphField = ParagraphField(tag, appearance).apply(init)

fun GroupBuilder.phone(
    tag: String? = null,
    placeholder: StrRes? = null,
    appearance: @Composable () -> InputFieldAppearance = InputFieldAppearance.DefaultPhone,
    init: PhoneField.() -> Unit = {}
): PhoneField = PhoneField(tag, placeholder, appearance).apply(init)

@Deprecated("Deprecated", replaceWith = ReplaceWith("phone()"), level = DeprecationLevel.ERROR)
fun GroupBuilder.phoneDropdown(
    tag: String? = null,
    placeholder: StrRes? = null,
    appearance: @Composable () -> InputFieldAppearance = InputFieldAppearance.DefaultPhone,
    init: PhoneField.() -> Unit = {}
): PhoneField = phone(tag, placeholder, appearance, init)

fun GroupBuilder.error(
    tag: String? = null,
    error: StrRes? = null,
    asWarning: Boolean = false,
    init: ErrorField.() -> Unit = {}
) = ErrorField(tag, error, asWarning).apply(init)

fun <ValueType> SelectableGroupBuilder<ValueType>.selectableField(
    tag: String? = null,
    selectableValue: ValueType,
    labelForValue: StrRes? = null,
    view: @Composable (SelectableField<ValueType>) -> Unit = {
        when (this@selectableField) {
            is SingleSelectionGroupBuilder -> SingleSelectionField(field = it)
            is MultipleSelectionGroupBuilder -> MultipleSelectionField(field = it)
        }
    },
    appearance: SelectableFieldAppearance = SelectableFieldAppearance(),
    init: SelectableField<ValueType>.() -> Unit = {}
): SelectableField<ValueType> =
    SelectableField(tag, selectableValue, labelForValue).apply {
        init()
        this.appearance = appearance
        this.view = { view(this) }
    }
