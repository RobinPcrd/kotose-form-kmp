/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.compose.fields

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import io.github.robinpcrd.kotose.form.fields.ToggleField
import io.github.robinpcrd.kotose.form.fields.SwitchFieldAppearance

@Composable
fun SwitchField(
    field: ToggleField,
    appearance: SwitchFieldAppearance,
    modifier: Modifier = Modifier
) {
    val fieldValue by field.valueState
    val enabled by field.isEnabledState
    val labelState by field.labelState

    SwitchField(
        value = fieldValue,
        label = labelState.getStringOrEmpty(),
        appearance = appearance,
        modifier = modifier,
        enabled = enabled,
    ) {
        field.value = it
    }
}

@Composable
fun SwitchField(
    value: Boolean?,
    label: String,
    appearance: SwitchFieldAppearance,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onChange: (Boolean) -> Unit
) {
    val nonNullValue = value ?: false
    val toggleRow: () -> Unit = { onChange(!nonNullValue) }
    val mergedStyle = appearance.labelTextStyle.merge(
        TextStyle(color = appearance.colors.textColor(enabled = enabled).value)
    )

    Row(
        modifier = Modifier
            .clickable(enabled = enabled, onClick = toggleRow)
            .then(modifier),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = mergedStyle
        )
        Switch(
            checked = nonNullValue,
            onCheckedChange = { toggleRow() },
            enabled = enabled,
        )
    }
}
