/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.compose.fields

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import io.github.robinpcrd.kotose.form.ValidationError
import io.github.robinpcrd.kotose.form.compose.LocalForm
import io.github.robinpcrd.kotose.form.fields.InputFieldAppearance
import io.github.robinpcrd.kotose.form.fields.PasswordField
import io.github.robinpcrd.kotose.form.fields.SecureTextField
import io.github.robinpcrd.kotose_form.generated.resources.Res
import io.github.robinpcrd.kotose_form.generated.resources.ic_visibility
import io.github.robinpcrd.kotose_form.generated.resources.ic_visibility_off
import io.github.robinpcrd.kotose_form.generated.resources.show_password
import io.github.robinpcrd.kotose_form.generated.resources.hide_password
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun PasswordField(
    field: PasswordField,
    appearance: InputFieldAppearance,
    modifier: Modifier = Modifier,
) = PasswordField(field as SecureTextField, appearance, modifier)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PasswordField(
    field: SecureTextField,
    appearance: InputFieldAppearance = InputFieldAppearance.DefaultPassword(),
    modifier: Modifier = Modifier,
) {
    val fieldValue by field.valueState
    val errors by field.errorsState
    val placeHolder = field.placeholder
    val isEnabled by field.isEnabledState

    var passwordHidden by remember(field) { mutableStateOf(true) }

    val focus = rememberFieldFocus(field)
    val focusRequester = focus.requester
    val form = field.form ?: LocalForm.current

    val focusState by focus.state
    val isFieldFocused = remember(focusState) { focusState?.isFocused ?: false }
    val bringIntoViewRequester = remember { BringIntoViewRequester() }

    FocusableInputField(
        focus = focus,
        field = field,
        appearance = appearance
    ) { keyboardActions, keyboardOptions ->
        InputField(
            value = fieldValue,
            placeHolder = placeHolder,
            isEnabled = isEnabled,
            modifier = modifier.bringIntoViewRequester(bringIntoViewRequester),
            isError = errors.isNotEmpty() && errors.any { !it.asWarning },
            hasWarning = errors.any(ValidationError::asWarning),
            visualTransformation = if (passwordHidden) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = keyboardOptions.copy(
                autoCorrectEnabled = false,
                keyboardType = KeyboardType.Password
            ),
            keyboardActions = keyboardActions,
            appearance = appearance,
            focusRequester = focusRequester,
            onValueChanged = field::setValue,
            onFocusChanged = {
                if (focus.update(it)) form.onFocusChanged(field, it.isFocused)
            },
            trailingIcon = {
                IconButton(
                    onClick = { passwordHidden = !passwordHidden },
                    enabled = isEnabled,
                    modifier = Modifier.testTag(field.key + "-reveal"),
                ) {
                    if (passwordHidden)
                        Icon(painter = painterResource(Res.drawable.ic_visibility), contentDescription = stringResource(Res.string.show_password))
                    else
                        Icon(painter = painterResource(Res.drawable.ic_visibility_off), contentDescription = stringResource(Res.string.hide_password))
                }
            }
        )
    }
}
