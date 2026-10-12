/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

@file:Suppress("unused")

package io.github.robinpcrd.kotose.form.fields

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import io.github.robinpcrd.kotose.form.compose.ComposeField
import io.github.robinpcrd.kotose.form.compose.FieldColors
import io.github.robinpcrd.kotose.form.compose.FieldDefaults
import io.github.robinpcrd.kotose.form.compose.fieldPadding
import io.github.robinpcrd.kotose.form.compose.fields.InputField
import io.github.robinpcrd.kotose.form.utils.ComposableFun
import io.github.robinpcrd.kotose.form.utils.PainterRes
import io.github.robinpcrd.kotose.resources.StrRes

/**
 * Input field which can be used to get user input.
 *
 * @property tag The tag of the field
 * @property label The label of the field
 * @property placeholder The placeholder of the field which is shown when the input field is empty.
 * If not specified, the label is used as the placeholder.
 * @property lineLimits Single or multi-line behaviour shared with every renderer, see [TextLineLimits].
 * @property appearance The appearance of the field which can be used to change its colors,
 * text style and other properties.
 * @property trailingIconSlot The slot which is used to put the trailing icon to the field.
 * If not specified, the clear icon will be shown if the field is clearable.
 * Otherwise the [InputFieldAppearance.trailingIconRes] will be shown.
 */
class InputField(
    tag: String? = null,
    label: StrRes? = null,
    placeholder: StrRes? = label,
    lineLimits: TextLineLimits = TextLineLimits.SingleLine,
    val appearance: @Composable () -> InputFieldAppearance = InputFieldAppearance.Default,
    private val trailingIconSlot: (@Composable () -> Unit)? = null,
) : TextField(tag, label, placeholder, lineLimits), ComposeField {

    override var view: ComposableFun = @Composable {
        val appearance by rememberUpdatedState(appearance())

        InputField(
            field = this,
            modifier = Modifier
                .testTag(key)
                .fillMaxWidth()
                .fieldPadding(),
            appearance = appearance,
            trailingIconSlot = trailingIconSlot,
        )
    }

}

@Immutable
@OptIn(ExperimentalComposeUiApi::class)
data class InputFieldAppearance(
    val colors: FieldColors,
    val textStyle: TextStyle,
    val clearable: Boolean = false,
    val readOnly: Boolean = false,
    val visualTransformation: VisualTransformation = VisualTransformation.None,
    val keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    val keyboardActions: KeyboardActions = KeyboardActions.Default,
    val leadingIconRes: PainterRes? = null,
    val trailingIconRes: PainterRes? = null,
    val autofillType: ContentType? = null,
) {
    companion object {
        @Stable
        val Default: @Composable () -> InputFieldAppearance = @Composable {
            InputFieldAppearance(
                colors = FieldDefaults.fieldColors(),
                textStyle = LocalTextStyle.current
            )
        }

        @Stable
        val DefaultInt: @Composable () -> InputFieldAppearance = @Composable {
            Default().copy(
                keyboardOptions = KeyboardOptions(
                    autoCorrectEnabled = false,
                    keyboardType = KeyboardType.Number
                )
            )
        }

        @Stable
        val DefaultUsername: @Composable () -> InputFieldAppearance = @Composable {
            Default().copy(
                keyboardOptions = KeyboardOptions(
                    autoCorrectEnabled = false,
                    keyboardType = KeyboardType.Text
                ),
                autofillType = ContentType.Username
            )
        }

        @Stable
        val DefaultPassword: @Composable () -> InputFieldAppearance = @Composable {
            Default().copy(
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    autoCorrectEnabled = false,
                    keyboardType = KeyboardType.Password
                ),
                autofillType = ContentType.Password
            )
        }

        @Stable
        val DefaultEmail: @Composable () -> InputFieldAppearance = @Composable {
            Default().copy(
                keyboardOptions = KeyboardOptions(
                    autoCorrectEnabled = false,
                    keyboardType = KeyboardType.Email
                ),
                autofillType = ContentType.EmailAddress
            )
        }

        @Stable
        val DefaultPhone: @Composable () -> InputFieldAppearance = @Composable {
            Default().copy(
                keyboardOptions = KeyboardOptions(
                    autoCorrectEnabled = false,
                    keyboardType = KeyboardType.Phone
                ),
                autofillType = ContentType.PhoneNumber
            )
        }
    }
}
