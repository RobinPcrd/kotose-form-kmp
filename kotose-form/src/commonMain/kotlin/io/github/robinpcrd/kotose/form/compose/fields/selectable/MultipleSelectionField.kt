/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.compose.fields.selectable

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import io.github.robinpcrd.kotose.form.ValidationError
import io.github.robinpcrd.kotose.form.compose.LabelCheckBox
import io.github.robinpcrd.kotose.form.compose.harmonizedWarning
import io.github.robinpcrd.kotose.form.fields.SelectableField
import io.github.robinpcrd.kotose.form.fields.SelectionOptionField
import io.github.robinpcrd.kotose.form.fields.SelectableFieldAppearance
import io.github.robinpcrd.kotose.form.fields.toColors

@Composable
fun MultipleSelectionField(field: SelectableField<*>) {
    val appearance by field.appearanceState
    MultipleSelectionField(field, appearance)
}

@Composable
fun MultipleSelectionField(
    field: SelectionOptionField<*>,
    appearance: SelectableFieldAppearance = SelectableFieldAppearance(),
) {
    val value by field.valueState
    val errors by field.errorsState
    val parentErrors by field.parentErrorState
    val enabled by field.isEnabledState
    val labelState by field.labelForValueState

    TextStyle.Default.merge(TextStyle.Default)

    MultipleSelectionField(
        isSelected = value != null,
        label = labelState?.getString() ?: field.selectableValue.toString(),
        subtitle = field.subtitle?.getString(),
        appearance = appearance,
        modifier = Modifier.testTag(field.key),
        enabled = enabled,
        isError = (errors.isNotEmpty() && errors.any { !it.asWarning }) || (parentErrors.isNotEmpty() && parentErrors.any { !it.asWarning }),
        hasWarnin = errors.any(ValidationError::asWarning) || parentErrors.any(ValidationError::asWarning),
        toggleRow = { field.select() }
    )
}

@Composable
fun MultipleSelectionField(
    isSelected: Boolean,
    label: String,
    appearance: SelectableFieldAppearance,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isError: Boolean = false,
    hasWarnin: Boolean,
    subtitle: String? = null,
    toggleRow: () -> Unit
) {
    val colors by rememberUpdatedState(appearance.toColors())

    LabelCheckBox(
        selected = isSelected,
        onClick = toggleRow,
        label = label,
        modifier = modifier
            .padding(start = appearance.startPadding, top = 2.dp, bottom = 2.dp)
            .then(
                if (isError) {
                    Modifier.border(1.dp, color = MaterialTheme.colorScheme.error)
                } else if (hasWarnin) {
                    Modifier.border(1.dp, color = harmonizedWarning())
                } else {
                    Modifier
                }
            )
            .fillMaxWidth(),
        colors = colors,
        textStyle = appearance.labelTextStyle ?: LocalTextStyle.current,
        enabled = enabled,
        subtitle = subtitle,
    )
}
