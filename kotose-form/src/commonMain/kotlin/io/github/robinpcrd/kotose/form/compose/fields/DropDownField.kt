/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.compose.fields

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import io.github.robinpcrd.kotose.form.compose.FieldColors
import io.github.robinpcrd.kotose.form.compose.FieldDefaults
import io.github.robinpcrd.kotose.form.compose.animateBorderStrokeAsState
import io.github.robinpcrd.kotose.form.compose.kotoseFormStyle
import io.github.robinpcrd.kotose.form.fields.SingleChoiceField
import io.github.robinpcrd.kotose_form.generated.resources.Res
import io.github.robinpcrd.kotose_form.generated.resources.ic_clear
import io.github.robinpcrd.kotose_form.generated.resources.ic_expand_less
import io.github.robinpcrd.kotose_form.generated.resources.clear_selection
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun <T> DropdownField(
    field: SingleChoiceField<T>,
    modifier: Modifier = Modifier,
    colors: FieldColors = FieldDefaults.fieldColors(),
    interactionSource: InteractionSource = remember { MutableInteractionSource() }
) {
    val possibleValues by field.possibleValuesState
    var expanded by remember { mutableStateOf(false) }
    val selectedOption by field.selectedOption
    val text = selectedOption?.label?.getString() ?: field.placeholder?.getStringOrEmpty() ?: ""
    val enabled by field.isEnabledState
    val visible by field.isVisibleState
    LaunchedEffect(enabled, visible) {
        if (!enabled || !visible) expanded = false
    }
    val hasErrors by field.hasErrorsState
    val hasWarnings by field.hasWarningsState
    DropdownBox(
        text = text,
        isPlaceholder = selectedOption == null,
        expanded = expanded,
        enabled = enabled,
        hasErrors = hasErrors,
        hasWarnings = hasWarnings,
        onOpen = { expanded = true },
        modifier = modifier,
        colors = colors,
        interactionSource = interactionSource,
        trailing = { fieldColors ->
            AnimatedVisibility(field.isClearable && selectedOption != null) {
                IconButton(
                    onClick = { field.selectOption(null) }, enabled = enabled,
                    modifier = Modifier.testTag("${field.key}-clear"),
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_clear),
                        contentDescription = stringResource(Res.string.clear_selection),
                        tint = fieldColors.trailingIconColor(
                            enabled = enabled,
                            isError = hasErrors || hasWarnings,
                            interactionSource = interactionSource,
                        ).value
                    )
                }
            }
        },
    ) {
        DropdownMenu(
            expanded = expanded, onDismissRequest = { expanded = false },
            modifier = Modifier.popupTestTags(),
        ) {
            possibleValues.forEach { opt ->
                DropdownMenuItem(
                    modifier = Modifier.testTag("${field.key}-option-${opt.id}"),
                    text = {
                        Text(text = opt.label.getStringOrEmpty())
                    },
                    onClick = {
                        field.selectOption(opt.id)
                        expanded = false
                    }
                )
            }
        }
    }
}

/** The dropdown's bordered row: the value or placeholder and an expand icon. [menu] anchors to it. */
@Composable
internal fun DropdownBox(
    text: String,
    isPlaceholder: Boolean,
    expanded: Boolean,
    enabled: Boolean,
    hasErrors: Boolean,
    hasWarnings: Boolean,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
    colors: FieldColors = FieldDefaults.fieldColors(),
    interactionSource: InteractionSource = remember { MutableInteractionSource() },
    trailing: @Composable RowScope.(FieldColors) -> Unit = {},
    menu: @Composable () -> Unit = {},
) {
    val iconOrientation =
        animateFloatAsState(if (expanded) 0f else 180f, label = "animateIconOrientation")
    val textColorEnabled = if (isPlaceholder) false else enabled
    val mergedTextStyle = if (isPlaceholder) {
        LocalTextStyle.current.merge(
            TextStyle(color = colors.placeholderColor(enabled).value)
        )
    } else {
        LocalTextStyle.current.merge(
            TextStyle(color = colors.textColor(textColorEnabled).value)
        )
    }
    val shape = RoundedCornerShape(kotoseFormStyle().cornerRadius)

    val fieldColors by rememberUpdatedState(
        newValue = if (hasErrors) colors else colors.withWarning(kotoseFormStyle().warning)
    )
    Box(
        modifier = modifier
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
            .clickable(enabled = enabled, onClick = onOpen)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                modifier = Modifier.weight(1f),
                style = mergedTextStyle,
            )
            IconButton(onClick = onOpen, enabled = enabled) {
                Icon(
                    painter = painterResource(Res.drawable.ic_expand_less),
                    contentDescription = null,
                    modifier = Modifier.rotate(iconOrientation.value),
                    tint = fieldColors.trailingIconColor(
                        enabled = enabled,
                        isError = hasErrors || hasWarnings,
                        interactionSource = interactionSource,
                    ).value
                )
            }
            trailing(fieldColors)
        }
        menu()
    }
}

/** Android popups have a separate semantics root; iOS already exports test tags. */
internal expect fun Modifier.popupTestTags(): Modifier
