/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import io.github.robinpcrd.kotose.form.Field
import io.github.robinpcrd.kotose.form.groups.SelectionGroup
import io.github.robinpcrd.kotose.form.fields.SelectionOptionField
import io.github.robinpcrd.kotose.form.compose.fields.selectable.MultipleSelectionField
import io.github.robinpcrd.kotose.form.compose.fields.selectable.CollapsedSelectionGroup
import io.github.robinpcrd.kotose.form.compose.fields.selectable.SingleChoiceContent
import io.github.robinpcrd.kotose.form.compose.fields.selectable.SingleSelectionField
import io.github.robinpcrd.kotose.form.compose.fields.selectable.isCollapsed
import io.github.robinpcrd.kotose.form.compose.fields.InputField
import io.github.robinpcrd.kotose.form.compose.fields.ButtonField
import io.github.robinpcrd.kotose.form.compose.fields.LabelField
import io.github.robinpcrd.kotose.form.compose.fields.ParagraphField
import io.github.robinpcrd.kotose.form.fields.ActionField
import io.github.robinpcrd.kotose.form.fields.DisplayParagraphField
import io.github.robinpcrd.kotose.form.fields.DisplayTextField
import io.github.robinpcrd.kotose.form.compose.fields.DateTimeField
import io.github.robinpcrd.kotose.form.compose.fields.LocalDateField
import io.github.robinpcrd.kotose.form.fields.DateTimeField as DateTimeFieldModel
import io.github.robinpcrd.kotose.form.fields.LocalDateField as LocalDateFieldModel
import io.github.robinpcrd.kotose.form.fields.SingleChoiceField
import io.github.robinpcrd.kotose.form.compose.fields.SwitchField
import io.github.robinpcrd.kotose.form.compose.fields.validation.ValidationErrorField
import io.github.robinpcrd.kotose.form.fields.SwitchFieldAppearance
import io.github.robinpcrd.kotose.form.fields.TextField
import io.github.robinpcrd.kotose.form.fields.SecureTextField
import io.github.robinpcrd.kotose.form.fields.InputFieldAppearance
import io.github.robinpcrd.kotose.form.compose.fields.PasswordField
import io.github.robinpcrd.kotose.form.fields.ToggleField
import io.github.robinpcrd.kotose.form.fields.validation.ValidationErrorField as ErrorFieldModel
import io.github.robinpcrd.kotose.form.fields.validation.ValidationErrorGroupField
import io.github.robinpcrd.kotose.resources.StrRes

@Composable
internal fun FieldContent(field: Field<*>) {
    // A selection group's title heads its first visible option, so the list keeps one row per field.
    // A collapsed group draws its single row there too, and its other options draw nothing.
    val group = (field as? SelectionOptionField<*>)?.selectionGroup
    val isFirst = group != null && group.selectableFields.firstOrNull { it.isVisibleState.value } == field
    val title = group?.title?.takeIf { isFirst }
    val collapsed = group?.takeIf { it.isCollapsed }
    when {
        collapsed != null -> if (isFirst) {
            Column {
                title?.let { SelectionGroupTitle(collapsed, it) }
                CollapsedSelectionGroup(collapsed)
            }
        }
        group != null && title != null -> Column {
            SelectionGroupTitle(group, title)
            FieldBody(field)
        }
        else -> FieldBody(field)
    }
}

@Composable
private fun SelectionGroupTitle(group: SelectionGroup<*>, title: StrRes) {
    val enabled by group.isEnabledState
    LabelField(title, FieldDefaults.labelFieldAppearance(), enabled, Modifier.testTag(group.key + "-title"))
}

@Composable
private fun FieldBody(field: Field<*>) {
    when (field) {
        is ComposeField -> field.view()
        is SelectionOptionField<*> -> when (field.selectionGroup) {
            is SelectionGroup.Single -> SingleSelectionField(field)
            else -> MultipleSelectionField(field)
        }
        is SingleChoiceField<*> -> SingleChoiceContent(field, Modifier.testTag(field.key).fillMaxWidth().fieldPadding())
        is SecureTextField -> PasswordField(field, InputFieldAppearance.DefaultPassword(), Modifier.testTag(field.key).fillMaxWidth().fieldPadding())
        is io.github.robinpcrd.kotose.form.fields.PhoneNumberField -> io.github.robinpcrd.kotose.form.compose.fields.PhoneField(field, InputFieldAppearance.DefaultPhone(), Modifier.testTag(field.key).fillMaxWidth().fieldPadding())
        is DisplayTextField -> LabelField(field)
        is DisplayParagraphField -> ParagraphField(field)
        is ActionField -> ButtonField(field)
        is LocalDateFieldModel -> LocalDateField(field, modifier = Modifier.testTag(field.key))
        is DateTimeFieldModel -> DateTimeField(field, modifier = Modifier.testTag(field.key))
        is TextField -> InputField(field, Modifier.testTag(field.key).fillMaxWidth().fieldPadding())
        is ToggleField -> SwitchField(
            field, SwitchFieldAppearance.Default(),
            Modifier.testTag(field.key).fillMaxWidth().padding(horizontal = LocalKotosePaddings.current.defaultPadding),
        )
        is ErrorFieldModel -> ValidationErrorField(
            field,
            Modifier.fillMaxWidth().padding(
                start = LocalKotosePaddings.current.defaultPadding,
                end = LocalKotosePaddings.current.defaultPadding,
                bottom = LocalKotosePaddings.current.tinyPadding,
            ),
        )
        is ValidationErrorGroupField -> ValidationErrorField(
            field,
            Modifier.fillMaxWidth().padding(
                start = LocalKotosePaddings.current.defaultPadding,
                end = LocalKotosePaddings.current.defaultPadding,
                top = LocalKotosePaddings.current.smallPadding,
                bottom = LocalKotosePaddings.current.tinyPadding,
            ),
        )
        else -> error("Supply FormScreen.fieldView for custom field ${field.key}")
    }
}
