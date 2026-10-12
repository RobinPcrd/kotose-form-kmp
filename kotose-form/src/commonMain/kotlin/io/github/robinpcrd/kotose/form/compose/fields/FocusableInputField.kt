/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.compose.fields

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.KeyboardActionHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import io.github.robinpcrd.kotose.form.FocusableField
import io.github.robinpcrd.kotose.form.compose.LocalForm
import io.github.robinpcrd.kotose.form.compose.KeyboardBarContext
import io.github.robinpcrd.kotose.form.compose.keyboardBarImeOptions
import io.github.robinpcrd.kotose.form.compose.keyboardBarLabels
import io.github.robinpcrd.kotose.form.compose.kotoseFormStyle
import io.github.robinpcrd.kotose.form.fields.InputFieldAppearance
import io.github.robinpcrd.kotose.form.fields.TextField
import io.github.robinpcrd.kotose.form.fields.TextLineLimits
import io.github.robinpcrd.kotose.form.utils.KFLogger

/**
 * Connects a text input to the form's focus and keyboard navigation: [content] receives the
 * keyboard actions and options that move to the next field or finish.
 *
 * @param lineLimits Multi-line inputs keep the default IME action so return inserts a newline.
 */
@Composable
fun FocusableInputField(
    field: FocusableField<*>,
    appearance: InputFieldAppearance,
    focus: ComposeFieldFocus = rememberFieldFocus(field),
    lineLimits: TextLineLimits = (field as? TextField)?.lineLimits ?: TextLineLimits.SingleLine,
    content: @Composable (keyboardActions: KeyboardActions, keyboardOptions: KeyboardOptions) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val focusRequester = focus.requester
    val form = field.form ?: LocalForm.current
    val focusState by focus.state
    val nextField by form.nextFocusableFieldState(field)

    // Only follow a focus request. A cleared form focus is not a dismissal: moving between two
    // inputs reports the loss before the gain, and clearing here would hide and re-show the keyboard.
    // Explicit dismissals arrive through Form.dismissKeyboardEvent in FormScreen.
    // Requesting focus on the already focused input restarts its text input session on Android,
    // which dips the keyboard, so only request it when the input does not have it yet.
    LaunchedEffect(form.currentFocus) {
        if (form.currentFocus == field && focusState?.isFocused != true) focusRequester.requestFocus()
    }
    val barLabels = keyboardBarLabels()
    SideEffect {
        focus.keyboardBar = KeyboardBarContext(form, field, field.key, barLabels, done = {
            form.stopEditing()
            focusManager.clearFocus()
        })
    }

    val imeAction = remember(lineLimits, nextField) {
        when {
            lineLimits !is TextLineLimits.SingleLine -> ImeAction.Default
            nextField == null -> ImeAction.Done
            else -> ImeAction.Next
        }
    }
    val keyboardActions by rememberUpdatedState(
        KeyboardActions(
            onNext = {
                nextField.let { f ->
                    if (f != null) {
                        form.setFocus(f)
                    } else if (KFLogger.isDebug()) {
                        KFLogger.wtf("InputField", "Next field is null")
                    }
                }
            },
            onDone = {
                form.stopEditing()
                focusManager.clearFocus()
            }
        )
    )

    val platformImeOptions = appearance.keyboardOptions.platformImeOptions
        ?: keyboardBarImeOptions.takeIf { kotoseFormStyle().showsKeyboardBar }
    content(keyboardActions, appearance.keyboardOptions.copy(imeAction = imeAction, platformImeOptions = platformImeOptions))
}

/** [FocusableInputField] for a `BasicTextField` on a `TextFieldState`, which takes a [KeyboardActionHandler]. */
@Composable
fun FocusableInputField2(
    field: FocusableField<*>,
    appearance: InputFieldAppearance,
    focus: ComposeFieldFocus = rememberFieldFocus(field),
    content: @Composable (keyboardAction: KeyboardActionHandler, keyboardOptions: KeyboardOptions) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val focusRequester = focus.requester
    val form = field.form ?: LocalForm.current
    val focusState by focus.state
    val nextField by form.nextFocusableFieldState(field)

    // Only follow a focus request. A cleared form focus is not a dismissal: moving between two
    // inputs reports the loss before the gain, and clearing here would hide and re-show the keyboard.
    // Explicit dismissals arrive through Form.dismissKeyboardEvent in FormScreen.
    // Requesting focus on the already focused input restarts its text input session on Android,
    // which dips the keyboard, so only request it when the input does not have it yet.
    LaunchedEffect(form.currentFocus) {
        if (form.currentFocus == field && focusState?.isFocused != true) focusRequester.requestFocus()
    }
    val barLabels = keyboardBarLabels()
    SideEffect {
        focus.keyboardBar = KeyboardBarContext(form, field, field.key, barLabels, done = {
            form.stopEditing()
            focusManager.clearFocus()
        })
    }

    val imeAction = remember(nextField) {
        when {
            nextField == null -> ImeAction.Done
            else -> ImeAction.Next
        }
    }
    val keyboardActions by rememberUpdatedState(
        KeyboardActionHandler {
            when (imeAction) {
                ImeAction.Done -> {
                    form.stopEditing()
                    focusManager.clearFocus()
                }

                ImeAction.Next -> {
                    nextField.let { f ->
                        if (f != null) {
                            form.setFocus(f)
                        } else if (KFLogger.isDebug()) {
                            KFLogger.wtf("InputField", "Next field is null")
                        }
                    }
                }
            }
        }
    )

    val platformImeOptions = appearance.keyboardOptions.platformImeOptions
        ?: keyboardBarImeOptions.takeIf { kotoseFormStyle().showsKeyboardBar }
    content(keyboardActions, appearance.keyboardOptions.copy(imeAction = imeAction, platformImeOptions = platformImeOptions))
}
