/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.compose.fields

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import io.github.robinpcrd.kotose.form.compose.FieldColors
import io.github.robinpcrd.kotose.form.compose.FieldDefaults
import io.github.robinpcrd.kotose.form.compose.animateBorderStrokeAsState
import io.github.robinpcrd.kotose.form.compose.fieldPadding
import io.github.robinpcrd.kotose.form.compose.kotoseFormStyle
import io.github.robinpcrd.kotose.form.fields.DateTimeField
import io.github.robinpcrd.kotose_form.generated.resources.Res
import io.github.robinpcrd.kotose_form.generated.resources.ic_schedule
import org.jetbrains.compose.resources.painterResource

/**
 * Platform-specific default picker for DateTimeField.
 * Returns a lambda to show the picker, or null if no default picker is available.
 */
@Composable
expect fun rememberDefaultDateTimePicker(field: DateTimeField): (() -> Unit)?

@Composable
fun DateTimeField(
    field: DateTimeField,
    colors: FieldColors = FieldDefaults.fieldColors(),
    interactionSource: InteractionSource = remember { MutableInteractionSource() },
    modifier: Modifier = Modifier,
) {
    val value by field.valueState
    val text = value?.let(field.formatter) ?: field.placeholder?.getStringOrEmpty().orEmpty()
    val enabled by field.isEnabledState
    val hasErrors by field.hasErrorsState
    val hasWarnings by field.hasWarningsState
    val shape = RoundedCornerShape(kotoseFormStyle().cornerRadius)
    val textColorEnabled = if (value == null) false else enabled
    val mergedTextStyle = if (value == null) {
        LocalTextStyle.current.merge(
            TextStyle(color = colors.placeholderColor(enabled).value)
        )
    } else {
        LocalTextStyle.current.merge(
            TextStyle(color = colors.textColor(textColorEnabled).value)
        )
    }

    val defaultPicker = rememberDefaultDateTimePicker(field)
    val onClickAction = field.showPicker ?: defaultPicker

    val fieldColors by rememberUpdatedState(
        newValue = if (hasErrors) colors else colors.withWarning(kotoseFormStyle().warning)
    )
    Row(
        modifier = modifier
            .fieldPadding()
            .wrapContentSize(Alignment.TopStart)
            .border(
                animateBorderStrokeAsState(
                    enabled = enabled,
                    isError = hasErrors || hasWarnings,
                    interactionSource = interactionSource,
                    colors = fieldColors,
                ).value,
                shape,
            )
            .clip(shape)
            .clickable(enabled = enabled && onClickAction != null) {
                onClickAction?.invoke()
            }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            modifier = Modifier.weight(1f),
            style = mergedTextStyle
        )
        Icon(
            painter = painterResource(Res.drawable.ic_schedule),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = fieldColors.trailingIconColor(
                enabled = enabled,
                isError = hasErrors || hasWarnings,
                interactionSource = interactionSource,
            ).value,
        )
    }
}
