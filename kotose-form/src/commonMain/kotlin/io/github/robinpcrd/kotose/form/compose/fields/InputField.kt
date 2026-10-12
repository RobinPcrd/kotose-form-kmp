/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.compose.fields

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import io.github.robinpcrd.kotose.form.ValidationError
import io.github.robinpcrd.kotose.form.compose.LocalForm
import io.github.robinpcrd.kotose.form.compose.clearFocusOnKeyboardDismiss
import io.github.robinpcrd.kotose.form.compose.FieldDefaults
import io.github.robinpcrd.kotose.form.compose.KotoseFormStyle
import io.github.robinpcrd.kotose.form.compose.kotoseFormStyle
import androidx.compose.foundation.shape.RoundedCornerShape
import io.github.robinpcrd.kotose.form.fields.InputField
import io.github.robinpcrd.kotose.form.fields.TextField
import io.github.robinpcrd.kotose.form.fields.TextLineLimits
import io.github.robinpcrd.kotose.form.fields.InputFieldAppearance
import io.github.robinpcrd.kotose.resources.StrRes
import io.github.robinpcrd.kotose_form.generated.resources.Res
import io.github.robinpcrd.kotose_form.generated.resources.ic_clear
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun InputField(
    field: TextField,
    modifier: Modifier = Modifier,
    appearance: InputFieldAppearance = (field as? InputField)?.appearance?.invoke()
        ?: InputFieldAppearance.Default(),
    trailingIconSlot: (@Composable () -> Unit)? = null,
) {
    val fieldValue by field.valueState
    val errors by field.errorsState
    val placeHolder = field.placeholder
    val isEnabled by field.isEnabledState

    val focus = rememberFieldFocus(field)
    val focusRequester = focus.requester
    val form = field.form ?: LocalForm.current
    val focusState by focus.state
    val isFieldFocused = remember(focusState) { focusState?.isFocused ?: false }
    val bringIntoViewRequester = remember { BringIntoViewRequester() }

    val trailingIcon: @Composable (() -> Unit)? = trailingIconSlot ?: if (appearance.clearable) {
        @Composable {
            AnimatedVisibility(
                visible = fieldValue.isNullOrEmpty().not(),
                enter = fadeIn() + expandIn(expandFrom = Alignment.CenterStart),
                exit = shrinkOut(shrinkTowards = Alignment.CenterStart) + fadeOut(),
            ) {
                IconButton(
                    onClick = {
                        field.setValue(null)
                        focusRequester.requestFocus()
                    },
                    enabled = isEnabled
                ) {
                    Icon(painter = painterResource(Res.drawable.ic_clear), contentDescription = null)
                }
            }
        }
    } else appearance.trailingIconRes?.getPainter()?.let {
        @Composable { Icon(painter = it, null) }
    }

    FocusableInputField(
        focus = focus,
        field = field,
        appearance = appearance,
    ) { keyboardActions, keyboardOptions ->
        InputField(
            value = fieldValue,
            placeHolder = placeHolder,
            isEnabled = isEnabled,
            modifier = modifier.bringIntoViewRequester(bringIntoViewRequester),
            isError = errors.isNotEmpty() && errors.any { !it.asWarning },
            hasWarning = errors.any(ValidationError::asWarning),
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            appearance = appearance,
            lineLimits = field.lineLimits,
            focusState = focusState,
            focusRequester = focusRequester,
            onValueChanged = field::setValue,
            onFocusChanged = {
                if (focus.update(it)) form.onFocusChanged(field, it.isFocused)
            },
            trailingIcon = trailingIcon,
        )
    }
}

@Composable
@OptIn(ExperimentalComposeUiApi::class)
fun InputField(
    value: String?,
    placeHolder: StrRes?,
    isEnabled: Boolean,
    appearance: InputFieldAppearance,
    modifier: Modifier = Modifier,
    lineLimits: TextLineLimits = TextLineLimits.SingleLine,
    isError: Boolean = false,
    hasWarning: Boolean = false,
    visualTransformation: VisualTransformation? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    focusState: FocusState? = null,
    focusRequester: FocusRequester = remember { FocusRequester() },
    onValueChanged: (String?) -> Unit,
    onFocusChanged: (FocusState) -> Unit,
    leadingIcon: @Composable (() -> Unit)? = appearance.leadingIconRes?.getPainter()?.let {
        @Composable { Icon(painter = it, null) }
    },
    trailingIcon: @Composable (() -> Unit)?,
) {
    var tfValue by remember { mutableStateOf(TextFieldValue(value ?: "")) }
    val singleLine = lineLimits is TextLineLimits.SingleLine
    val minLines = (lineLimits as? TextLineLimits.MultiLine)?.minLines ?: 1
    val maxLines = (lineLimits as? TextLineLimits.MultiLine)?.maxLines ?: 1

    val placeholder: @Composable (() -> Unit)? = if (placeHolder != null) {
        @Composable {
            Text(
                text = placeHolder.getStringOrEmpty(),
                overflow = TextOverflow.Ellipsis,
                maxLines = maxLines
            )
        }
    } else null

    LaunchedEffect(value) {
        if (value != tfValue.text) {
            tfValue = TextFieldValue(value ?: "", TextRange(value?.length ?: 0))
        }
    }

    LaunchedEffect(focusState) {
        if (focusState?.isFocused == true) {
            tfValue = tfValue.copy(selection = TextRange(tfValue.text.length))
        }
    }

    val style = kotoseFormStyle()
    OutlinedTextField(
        value = tfValue,
        onValueChange = {
            if (singleLine && it.text.contains("\n")) {
                tfValue = it.copy(text = it.text.replace("\n", " "))
            } else {
                tfValue = it
                onValueChanged(it.text)
            }
        },
        modifier = modifier
            .focusRequester(focusRequester)
            .clearFocusOnKeyboardDismiss()
            .onFocusChanged { state ->
                onFocusChanged(state)
            }
            .semantics {
                appearance.autofillType?.let {
                    contentType = it
                }
            }
        /*.autofill(
            autofillTypes = if (appearance.autofillType != null) listOf(appearance.autofillType) else emptyList(),
            onFill = {
                tfValue = TextFieldValue(text = it, selection = TextRange(index = it.length))
                onValueChanged(it)
            }
        )*/,
        enabled = isEnabled,
        readOnly = appearance.readOnly,
        textStyle = appearance.textStyle,
        placeholder = placeholder,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        isError = isError || hasWarning,
        visualTransformation = visualTransformation ?: appearance.visualTransformation,
        keyboardOptions = if (keyboardOptions == KeyboardOptions.Default) appearance.keyboardOptions else keyboardOptions,
        keyboardActions = if (keyboardActions == KeyboardActions.Default) appearance.keyboardActions else keyboardActions,
        singleLine = singleLine,
        minLines = minLines,
        maxLines = maxLines,
        shape = RoundedCornerShape(style.cornerRadius),
        colors = outlinedTextFieldColors(style, hasError = isError, hasWarnings = hasWarning)
    )
}

/** Material colors with the style tokens applied: borders, text, placeholder, caret and container. */
@Composable
private fun outlinedTextFieldColors(
    style: KotoseFormStyle,
    hasError: Boolean,
    hasWarnings: Boolean,
): TextFieldColors {
    val colors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = style.text,
        unfocusedTextColor = style.text,
        errorTextColor = style.text,
        focusedContainerColor = style.inputBackground,
        unfocusedContainerColor = style.inputBackground,
        errorContainerColor = style.inputBackground,
        cursorColor = style.accent,
        errorCursorColor = style.error,
        focusedBorderColor = style.focusedBorder,
        unfocusedBorderColor = style.inputBorder,
        errorBorderColor = style.error,
        focusedPlaceholderColor = style.placeholder,
        unfocusedPlaceholderColor = style.placeholder,
        errorPlaceholderColor = style.placeholder,
        // Disabled keeps the container and border so a filled brand input does not turn into an outline.
        disabledContainerColor = style.disabledBackground,
        disabledBorderColor = style.inputBorder,
        disabledTextColor = style.disabledText,
        disabledPlaceholderColor = style.placeholder.copy(alpha = FieldDefaults.DisabledInputOpacity),
    )
    if (hasWarnings && !hasError) {
        val warningColor = style.warning
        return colors.copy(
            errorCursorColor = warningColor,
            errorIndicatorColor = warningColor,
            errorLeadingIconColor = warningColor,
            errorTrailingIconColor = warningColor,
            errorLabelColor = warningColor,
            errorPrefixColor = warningColor,
            errorSuffixColor = warningColor,
        )
    }
    return colors
}
