/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.compose.fields

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import io.github.robinpcrd.kotose.form.compose.FieldDefaults
import io.github.robinpcrd.kotose.form.compose.fieldPadding
import io.github.robinpcrd.kotose.form.fields.DisplayTextField
import io.github.robinpcrd.kotose.form.fields.LabelFieldAppearance
import io.github.robinpcrd.kotose.resources.StrRes

@Composable
fun LabelField(
    field: DisplayTextField,
    appearance: LabelFieldAppearance =
        if (field.isTitle) LabelFieldAppearance.Title() else FieldDefaults.labelFieldAppearance(),
) {
    val value by field.valueState
    val text = value ?: field.text ?: StrRes.Empty
    val enabled by field.isEnabledState

    LabelField(text, appearance, enabled, Modifier.testTag(field.key))
}

@Composable
fun LabelField(
    text: StrRes,
    appearance: LabelFieldAppearance,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val mergedStyle = appearance.textStyle.merge(
        TextStyle(color = appearance.colors.textColor(enabled = enabled).value)
    )
    Row(
        modifier
            .fillMaxWidth()
            .fieldPadding()
    ) {
        Text(
            text = text.getStringOrEmpty(),
            modifier = Modifier.fillMaxWidth(),
            style = mergedStyle
        )
    }
}
