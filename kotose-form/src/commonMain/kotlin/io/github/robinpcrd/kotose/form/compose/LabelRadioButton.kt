/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Checkbox
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

@Composable
fun LabelRadioButton(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    colors: FieldColors = FieldDefaults.fieldColors(),
    textStyle: TextStyle = LocalTextStyle.current,
    enabled: Boolean = true,
    subtitle: String? = null,
) {
    val mergedStyle = textStyle.merge(
        TextStyle(color = colors.textColor(enabled = enabled).value)
    )

    Row(
        modifier = modifier.clickable(enabled = enabled, onClick = onClick),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick, enabled = enabled)
        OptionText(label, subtitle, mergedStyle, enabled, colors)
    }
}

@Composable
fun LabelCheckBox(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    colors: FieldColors = FieldDefaults.fieldColors(),
    textStyle: TextStyle = LocalTextStyle.current,
    enabled: Boolean = true,
    subtitle: String? = null,
) {
    val mergedStyle = textStyle.merge(
        TextStyle(color = colors.textColor(enabled = enabled).value)
    )

    Row(
        modifier = modifier.clickable(enabled = enabled, onClick = onClick),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = selected, onCheckedChange = { onClick() }, enabled = enabled)
        OptionText(label, subtitle, mergedStyle, enabled, colors)
    }
}

@Composable
private fun OptionText(label: String, subtitle: String?, style: TextStyle, enabled: Boolean, colors: FieldColors) {
    Column {
        Text(label, style = style)
        if (subtitle != null) {
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else colors.textColor(enabled = false).value,
            )
        }
    }
}
