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
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.byValue
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.testTag
import io.github.robinpcrd.kotose_form.generated.resources.phone_dial_code
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.layout.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.offset
import io.github.robinpcrd.kotose.form.Form
import io.github.robinpcrd.kotose.form.compose.KeyboardBarContext
import io.github.robinpcrd.kotose.form.compose.KeyboardBarEntry
import io.github.robinpcrd.kotose.form.compose.focusKeyboardBar
import io.github.robinpcrd.kotose.form.compose.focusesNumber
import io.github.robinpcrd.kotose.form.compose.keyboardBarImeOptions
import io.github.robinpcrd.kotose.form.compose.keyboardBarLabels
import io.github.robinpcrd.kotose.form.compose.kotoseFormStyle
import io.github.robinpcrd.kotose.form.compose.FieldColors
import io.github.robinpcrd.kotose.form.compose.FieldDefaults
import io.github.robinpcrd.kotose.form.compose.LocalForm
import io.github.robinpcrd.kotose.form.compose.animateBorderStrokeAsState
import io.github.robinpcrd.kotose.form.compose.data.Country
import io.github.robinpcrd.kotose.form.compose.data.CountryConfiguration
import io.github.robinpcrd.kotose.form.compose.data.CountryRegion
import io.github.robinpcrd.kotose.form.compose.data.getCountryForRegion
import io.github.robinpcrd.kotose.form.compose.phoneNumberOutputTransformation
import io.github.robinpcrd.kotose.form.fields.InputFieldAppearance
import io.github.robinpcrd.kotose.form.fields.PhoneCountryPicker
import io.github.robinpcrd.kotose.form.fields.PhoneData
import io.github.robinpcrd.kotose.form.fields.PhoneNumberField
import io.github.robinpcrd.kotose.form.fields.PhoneField
import io.github.robinpcrd.kotose.form.utils.KFLogger
import io.github.robinpcrd.kotose.resources.StrRes
import io.github.robinpcrd.kotose_form.generated.resources.Res
import io.github.robinpcrd.kotose_form.generated.resources.ic_clear
import org.jetbrains.compose.resources.painterResource
import kotlin.math.max
import kotlin.math.roundToInt

@Composable
fun PhoneField(
    field: PhoneField,
    appearance: InputFieldAppearance,
    modifier: Modifier = Modifier,
    colors: FieldColors = FieldDefaults.fieldColors(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    PhoneField(field as PhoneNumberField, appearance, modifier, colors, interactionSource)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PhoneField(
    field: PhoneNumberField,
    appearance: InputFieldAppearance,
    modifier: Modifier = Modifier,
    colors: FieldColors = FieldDefaults.fieldColors(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    val data by field.valueState
    val textFieldState = rememberTextFieldState(data?.phone.orEmpty())
    val country = field.displayCountry
    val hasErrors by field.hasErrorsState
    val hasWarnings by field.hasWarningsState
    val enabled by field.isEnabledState
    val focus = rememberFieldFocus(field)
    val requester = focus.requester
    val form = field.form ?: LocalForm.current
    val hasCountryPicker = field.hasCountryPicker
    val dialCodeState = rememberTextFieldState(field.dialCode)
    val numberRequester = remember { FocusRequester() }

    val buttonTextStyle = LocalTextStyle.current.merge(
        TextStyle(color = colors.textColor(enabled).value)
    )
    val textColorEnabled = if (data == null) false else enabled
    val mergedTextStyle = LocalTextStyle.current.merge(
        TextStyle(color = colors.textColor(textColorEnabled).value)
    )

    val focusState by focus.state
    val isFieldFocused = remember(focusState) { focusState?.isFocused ?: false }
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val showCountryPicker = rememberPhoneCountryPicker(field)

    LaunchedEffect(data) {
        if (textFieldState.text != data?.phone) {
            textFieldState.edit {
                replace(0, length, data?.phone.orEmpty())
            }
        }
    }
    LaunchedEffect(textFieldState) {
        snapshotFlow { textFieldState.text.toString() }.collect {
            val text = textFieldState.text.toString()
            field.setPhone(text)
            val phone = field.value?.phone.orEmpty()
            if (phone != text) {
                textFieldState.edit { replace(0, length, phone) }
            }
        }
    }
    if (!hasCountryPicker) {
        LaunchedEffect(data) {
            if (dialCodeState.text.toString() != field.dialCode) {
                dialCodeState.edit { replace(0, length, field.dialCode) }
            }
        }
        LaunchedEffect(dialCodeState) {
            snapshotFlow { dialCodeState.text.toString() }.collect { field.setDialCode(it) }
        }
    }

    val fieldColors by rememberUpdatedState(
        newValue = if (hasErrors) colors else colors.withWarning(kotoseFormStyle().warning)
    )

    InternalPhoneField(
        modifier = modifier,
        enabled = enabled,
        placeHolder = field.placeholder,
        hasErrors = hasErrors,
        hasWarnings = hasWarnings,
        interactionSource = interactionSource,
        colors = fieldColors,
        field = field,
        country = country,
        buttonTextStyle = buttonTextStyle,
        appearance = appearance,
        textFieldState = textFieldState,
        bringIntoViewRequester = bringIntoViewRequester,
        requester = requester,
        focus = focus,
        form = form,
        mergedTextStyle = mergedTextStyle,
        data = data,
        countryPickerEnabled = field.isCountrySelectionEnabled,
        shape = RoundedCornerShape(kotoseFormStyle().cornerRadius),
        dialCode = if (hasCountryPicker) null else dialCodeState,
        numberRequester = numberRequester,
        onFlagClicked = showCountryPicker,
    )
}

/**
 * Opens the country picker of [field] from a custom phone input: the [PhoneField.countryPicker] of a
 * [PhoneField], the built-in one otherwise. The picked country goes through
 * [PhoneNumberField.selectCountry]. Does nothing while the field is disabled, has no phone backend
 * or has country selection off.
 */
@Composable
fun rememberPhoneCountryPicker(field: PhoneNumberField): () -> Unit {
    val defaultPresenter = rememberDefaultPhoneCountryPickerPresenter()
    return remember(field, defaultPresenter) {
        {
            if (field.canPickCountry) {
                when (val countryPicker = (field as? PhoneField)?.countryPicker ?: PhoneCountryPicker.BuiltIn()) {
                    is PhoneCountryPicker.BuiltIn -> {
                        val presenter = countryPicker.presenter ?: defaultPresenter
                        if (presenter != null) {
                            showPhoneCountryPicker(field, presenter)
                        } else {
                            KFLogger.e(
                                "KF-PhoneField",
                                "No presenter found for built-in phone country picker"
                            )
                        }
                    }

                    is PhoneCountryPicker.Custom -> (field as? PhoneField)?.let { countryPicker.show(it) }
                    PhoneCountryPicker.Disabled -> Unit
                }
            }
        }
    }
}

/** Whether the field takes a country now: enabled, with a phone backend and country selection on. */
internal val PhoneNumberField.canPickCountry: Boolean
    get() = isEnabled && hasCountryPicker && isCountrySelectionEnabled

@Composable
@OptIn(ExperimentalFoundationApi::class)
internal fun InternalPhoneField(
    modifier: Modifier,
    placeHolder: StrRes?,
    enabled: Boolean,
    hasErrors: Boolean,
    hasWarnings: Boolean,
    interactionSource: MutableInteractionSource,
    colors: FieldColors,
    shape: CornerBasedShape = MaterialTheme.shapes.extraSmall,
    phoneButtonShape: CornerBasedShape = shape.copy(
        topEnd = CornerSize(0f),
        bottomEnd = CornerSize(0f)
    ),
    field: PhoneNumberField,
    country: Country,
    buttonTextStyle: TextStyle,
    appearance: InputFieldAppearance,
    textFieldState: TextFieldState,
    bringIntoViewRequester: BringIntoViewRequester,
    requester: FocusRequester,
    focus: ComposeFieldFocus,
    form: Form,
    mergedTextStyle: TextStyle,
    data: PhoneData?,
    countryPickerEnabled: Boolean,
    dialCode: TextFieldState? = null,
    numberRequester: FocusRequester = remember { FocusRequester() },
    onFlagClicked: () -> Unit,
) {
    val placeholder: @Composable (() -> Unit)? = if (placeHolder != null) {
        @Composable {
            Text(
                text = placeHolder.getStringOrEmpty(),
                color = colors.placeholderColor(enabled).value,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
        }
    } else null

    Row(
        modifier = Modifier
            .height(IntrinsicSize.Min)
            .then(modifier)
            .border(
                animateBorderStrokeAsState(
                    enabled = enabled,
                    isError = hasErrors || hasWarnings,
                    interactionSource = interactionSource,
                    colors = colors,
                ).value,
                shape,
            )
    ) {
        val focusesNumber = focusesNumber(dialCode?.text?.toString(), KeyboardBarEntry.forward)
        val barLabels = keyboardBarLabels()
        val barImeOptions = keyboardBarImeOptions.takeIf { kotoseFormStyle().showsKeyboardBar }
        val focusManager = LocalFocusManager.current
        val dialCodeRequester = remember { FocusRequester() }
        val stopEditing = {
            form.stopEditing()
            focusManager.clearFocus()
        }
        if (dialCode == null) {
            Box(
                modifier = Modifier
                    .testTag(field.key + "-country")
                    .clip(phoneButtonShape)
                    .clickable(enabled = enabled && countryPickerEnabled, onClick = onFlagClicked)
                    .padding(8.dp)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "${country.emojiFlag} +${country.code}", style = buttonTextStyle)
            }
        } else {
            val dialCodeLabel = StrRes(Res.string.phone_dial_code).getStringOrEmpty()
            val textMeasurer = rememberTextMeasurer()
            val density = LocalDensity.current
            val dialCodeWidth = remember(mergedTextStyle, density) {
                with(density) { textMeasurer.measure("+999", mergedTextStyle).size.width.toDp() + 2.dp }
            }
            Box(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                BasicTextField(
                    state = dialCode,
                    modifier = Modifier
                        .width(dialCodeWidth)
                        .testTag(field.key + "-dial-code")
                        .semantics { contentDescription = dialCodeLabel }
                        .focusRequester(dialCodeRequester)
                        .then(if (focusesNumber) Modifier else Modifier.focusRequester(requester))
                        .onFocusChanged {
                            if (focus.update(it)) form.onFocusChanged(field, it.isFocused)
                            if (it.isFocused) {
                                focusKeyboardBar(
                                    KeyboardBarContext(
                                        form, field, field.key + "-dial-code", barLabels, stopEditing,
                                        nextInput = { numberRequester.requestFocus() },
                                    )
                                )
                            }
                        },
                    enabled = enabled,
                    inputTransformation = DialCodeInputTransformation,
                    textStyle = mergedTextStyle,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next,
                        platformImeOptions = barImeOptions,
                    ),
                    onKeyboardAction = { numberRequester.requestFocus() },
                    lineLimits = TextFieldLineLimits.SingleLine,
                    cursorBrush = SolidColor(colors.cursorColor(hasErrors || hasWarnings).value),
                    decorator = { innerTextField ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "+", style = mergedTextStyle)
                            innerTextField()
                        }
                    },
                )
            }
        }
        VerticalDivider(
            Modifier
                .fillMaxHeight()
                .width(1.dp)
        )

        FocusableInputField2(
            focus = focus,
            field = field,
            appearance = appearance
        ) { keyboardActions, keyboardOptions ->
            BasicTextField(
                state = textFieldState,
                modifier = Modifier
                    .weight(1f)
                    //.clearFocusOnKeyboardDismiss()
                    .bringIntoViewRequester(bringIntoViewRequester)
                    .focusRequester(numberRequester)
                    .then(if (focusesNumber) Modifier.focusRequester(requester) else Modifier)
                    .onFocusChanged {
                        if (focus.update(it)) form.onFocusChanged(field, it.isFocused)
                        if (it.isFocused && dialCode != null) {
                            focusKeyboardBar(
                                KeyboardBarContext(
                                    form, field, field.key, barLabels, stopEditing,
                                    previousInput = { dialCodeRequester.requestFocus() },
                                )
                            )
                        }
                    },
                enabled = enabled,
                inputTransformation = if (dialCode == null) null else NumberInputTransformation,
                //inputTransformation = InputTransformation.then {
                //if (!TextUtils.isDigitsOnly(asCharSequence()))
                //    revertAllChanges()
                //if (!PhoneNumberUtil.getInstance().isPossibleNumber(
                //        asCharSequence(),
                //        PhoneNumberUtil.getInstance().getRegionCodeForCountryCode(country.code)
                //    )
                //) revertAllChanges()
                //},
                textStyle = mergedTextStyle,
                keyboardOptions = keyboardOptions.copy(keyboardType = if (dialCode == null) KeyboardType.Phone else KeyboardType.Number),
                onKeyboardAction = keyboardActions,
                lineLimits = TextFieldLineLimits.SingleLine,
                interactionSource = interactionSource,
                cursorBrush = SolidColor(colors.cursorColor(hasErrors || hasWarnings).value),
                decorator = { innerTextField ->
                    PhoneFieldDecorationBox(
                        value = textFieldState.text.toString(),
                        innerTextField = innerTextField,
                        visualTransformation = VisualTransformation.None,
                        placeholder = placeholder,
                        trailingIcon = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (appearance.clearable) {
                                    AnimatedVisibility(
                                        visible = data?.phone.isNullOrEmpty().not(),
                                        modifier = Modifier
                                            .defaultMinSize(48.dp, 48.dp)
                                            .align(Alignment.CenterVertically),
                                        enter = fadeIn() + expandIn(expandFrom = Alignment.CenterStart),
                                        exit = shrinkOut(shrinkTowards = Alignment.CenterStart) + fadeOut(),
                                    ) {
                                        IconButton(
                                            onClick = {
                                                textFieldState.clearText()
                                                field.setPhone("")
                                                focus.requester.requestFocus()
                                            },
                                            enabled = enabled
                                        ) {
                                            Icon(
                                                painter = painterResource(Res.drawable.ic_clear),
                                                contentDescription = null,
                                                tint = colors.trailingIconColor(
                                                    enabled = enabled,
                                                    isError = hasErrors || hasWarnings,
                                                    interactionSource = interactionSource
                                                ).value
                                            )
                                        }
                                    }
                                }
                            }
                        },
                        interactionSource = interactionSource,
                        contentPadding = PaddingValues(16.dp),
                        isError = hasErrors || hasWarnings,
                        colors = colors,
                    )
                },
                outputTransformation = phoneNumberOutputTransformation(country)
            )
        }
    }
}

// PhoneFieldDecorationBox, PhoneFieldLayout and PhoneFieldMeasurePolicy are adapted from the Jetpack
// Compose Material text field, Copyright The Android Open Source Project, Apache License 2.0.
@Composable
internal fun PhoneFieldDecorationBox(
    value: String,
    innerTextField: @Composable () -> Unit,
    visualTransformation: VisualTransformation,
    placeholder: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    singleLine: Boolean = false,
    enabled: Boolean = true,
    isError: Boolean = false,
    interactionSource: InteractionSource,
    contentPadding: PaddingValues,
    colors: FieldColors,
    border: @Composable (() -> Unit)? = null
) {
    val transformedText = remember(value, visualTransformation) {
        visualTransformation.filter(AnnotatedString(value))
    }.text.text

    val isFocused = interactionSource.collectIsFocusedAsState().value
    val placeholderAlphaProgress = if (transformedText.isEmpty()) 1f else 0f

    val typography = MaterialTheme.typography
    val subtitle1 = typography.titleMedium
    val caption = typography.labelMedium
    val shouldOverrideTextStyleColor =
        (subtitle1.color == Color.Unspecified && caption.color != Color.Unspecified) ||
            (subtitle1.color != Color.Unspecified && caption.color == Color.Unspecified)

    val decoratedPlaceholder: @Composable ((Modifier) -> Unit)? =
        if (placeholder != null && transformedText.isEmpty()) {
            @Composable { modifier ->
                Box(modifier.alpha(placeholderAlphaProgress)) {
                    placeholder()
                }
            }
        } else null

    val trailingIconColor = colors.trailingIconColor(enabled, isError, interactionSource).value
    val decoratedTrailing: @Composable (() -> Unit)? = trailingIcon

    PhoneFieldLayout(
        modifier = Modifier,
        textField = innerTextField,
        placeholder = decoratedPlaceholder,
        trailing = decoratedTrailing,
        singleLine = singleLine,
        paddingValues = PaddingValues(16.dp),
    )

}

// Typed text is filtered inside the edit: rewriting it afterwards races the keyboard.
private val DialCodeInputTransformation = InputTransformation.byValue { _, proposed ->
    proposed.asciiDigits().trimStart('0').take(3)
}

private val NumberInputTransformation = InputTransformation.byValue { _, proposed -> proposed.asciiDigits() }

private fun CharSequence.asciiDigits(): String = buildString {
    for (char in this@asciiDigits) char.digitToIntOrNull()?.let { append('0' + it) }
}

internal const val TextFieldId = "TextField"
internal const val PlaceholderId = "Hint"
internal const val TrailingId = "Trailing"

internal val HorizontalIconPadding = 12.dp
internal val IconDefaultSizeModifier = Modifier.defaultMinSize(48.dp, 48.dp)
internal val ZeroConstraints = Constraints(0, 0, 0, 0)

@Composable
internal fun PhoneFieldLayout(
    modifier: Modifier,
    textField: @Composable () -> Unit,
    placeholder: @Composable ((Modifier) -> Unit)?,
    trailing: @Composable (() -> Unit)? = null,
    singleLine: Boolean,
    paddingValues: PaddingValues
) {
    val measurePolicy = remember(singleLine, paddingValues) {
        PhoneFieldMeasurePolicy(singleLine, paddingValues)
    }
    val layoutDirection = LocalLayoutDirection.current
    Layout(
        modifier = modifier,
        content = {
            val startTextFieldPadding = paddingValues.calculateStartPadding(layoutDirection)
            val endTextFieldPadding = paddingValues.calculateEndPadding(layoutDirection)
            val padding = Modifier.padding(
                start = startTextFieldPadding,
                end = if (trailing != null) {
                    (endTextFieldPadding - HorizontalIconPadding).coerceAtLeast(0.dp)
                } else {
                    endTextFieldPadding
                }
            )
            if (trailing != null) {
                Box(
                    modifier = Modifier
                        .layoutId(TrailingId)
                        .then(IconDefaultSizeModifier),
                    contentAlignment = Alignment.Center
                ) {
                    trailing()
                }
            }
            if (placeholder != null) {
                placeholder(
                    Modifier
                        .layoutId(PlaceholderId)
                        .then(padding)
                )
            }
            Box(
                modifier = Modifier
                    .layoutId(TextFieldId)
                    .then(padding),
                propagateMinConstraints = true,
            ) {
                textField()
            }
        },
        measurePolicy = measurePolicy
    )
}

private class PhoneFieldMeasurePolicy(
    private val singleLine: Boolean,
    private val paddingValues: PaddingValues
) : MeasurePolicy {
    override fun MeasureScope.measure(
        measurables: List<Measurable>,
        constraints: Constraints
    ): MeasureResult {
        // used to calculate the constraints for measuring elements that will be placed in a row
        var occupiedSpaceHorizontally = 0
        val bottomPadding = paddingValues.calculateBottomPadding().roundToPx()

        val relaxedConstraints = constraints.copy(minWidth = 0, minHeight = 0)

        // measure trailing icon
        val trailingPlaceable = measurables.find { it.layoutId == TrailingId }
            ?.measure(relaxedConstraints)
        occupiedSpaceHorizontally += widthOrZero(trailingPlaceable)

        // measure text field
        // on top we offset either by default padding or by label's half height if its too big
        // minHeight must not be set to 0 due to how foundation TextField treats zero minHeight
        val textConstraints = constraints.offset(
            horizontal = -occupiedSpaceHorizontally,
            vertical = -bottomPadding - bottomPadding
        ).copy(minHeight = 0)
        val textFieldPlaceable =
            measurables.first { it.layoutId == TextFieldId }.measure(textConstraints)

        // measure placeholder
        val placeholderConstraints = textConstraints.copy(minWidth = 0)
        val placeholderPlaceable =
            measurables.find { it.layoutId == PlaceholderId }?.measure(placeholderConstraints)

        val width =
            calculateWidth(
                widthOrZero(trailingPlaceable),
                textFieldPlaceable.width,
                widthOrZero(placeholderPlaceable),
                constraints
            )
        val height =
            calculateHeight(
                heightOrZero(trailingPlaceable),
                textFieldPlaceable.height,
                heightOrZero(placeholderPlaceable),
                constraints,
                density,
                paddingValues
            )

        return layout(width, height) {
            place(
                height,
                width,
                trailingPlaceable,
                textFieldPlaceable,
                placeholderPlaceable,
                singleLine,
                density,
                layoutDirection,
                paddingValues
            )
        }
    }

    override fun IntrinsicMeasureScope.maxIntrinsicHeight(
        measurables: List<IntrinsicMeasurable>,
        width: Int
    ): Int {
        return intrinsicHeight(measurables, width) { intrinsicMeasurable, w ->
            intrinsicMeasurable.maxIntrinsicHeight(w)
        }
    }

    override fun IntrinsicMeasureScope.minIntrinsicHeight(
        measurables: List<IntrinsicMeasurable>,
        width: Int
    ): Int {
        return intrinsicHeight(measurables, width) { intrinsicMeasurable, w ->
            intrinsicMeasurable.minIntrinsicHeight(w)
        }
    }

    override fun IntrinsicMeasureScope.maxIntrinsicWidth(
        measurables: List<IntrinsicMeasurable>,
        height: Int
    ): Int {
        return intrinsicWidth(measurables, height) { intrinsicMeasurable, h ->
            intrinsicMeasurable.maxIntrinsicWidth(h)
        }
    }

    override fun IntrinsicMeasureScope.minIntrinsicWidth(
        measurables: List<IntrinsicMeasurable>,
        height: Int
    ): Int {
        return intrinsicWidth(measurables, height) { intrinsicMeasurable, h ->
            intrinsicMeasurable.minIntrinsicWidth(h)
        }
    }

    private fun intrinsicWidth(
        measurables: List<IntrinsicMeasurable>,
        height: Int,
        intrinsicMeasurer: (IntrinsicMeasurable, Int) -> Int
    ): Int {
        val textFieldWidth =
            intrinsicMeasurer(measurables.first { it.layoutId == TextFieldId }, height)
        val trailingWidth = measurables.find { it.layoutId == TrailingId }?.let {
            intrinsicMeasurer(it, height)
        } ?: 0
        val placeholderWidth = measurables.find { it.layoutId == PlaceholderId }?.let {
            intrinsicMeasurer(it, height)
        } ?: 0
        return calculateWidth(
            trailingPlaceableWidth = trailingWidth,
            textFieldPlaceableWidth = textFieldWidth,
            placeholderPlaceableWidth = placeholderWidth,
            constraints = ZeroConstraints
        )
    }

    private fun IntrinsicMeasureScope.intrinsicHeight(
        measurables: List<IntrinsicMeasurable>,
        width: Int,
        intrinsicMeasurer: (IntrinsicMeasurable, Int) -> Int
    ): Int {
        val textFieldHeight =
            intrinsicMeasurer(measurables.first { it.layoutId == TextFieldId }, width)
        val trailingHeight = measurables.find { it.layoutId == TrailingId }?.let {
            intrinsicMeasurer(it, width)
        } ?: 0
        val placeholderHeight = measurables.find { it.layoutId == PlaceholderId }?.let {
            intrinsicMeasurer(it, width)
        } ?: 0
        return calculateHeight(
            trailingPlaceableHeight = trailingHeight,
            textFieldPlaceableHeight = textFieldHeight,
            placeholderPlaceableHeight = placeholderHeight,
            constraints = ZeroConstraints,
            density = density,
            paddingValues = paddingValues
        )
    }
}

internal val IntrinsicMeasurable.layoutId: Any?
    get() = (parentData as? LayoutIdParentData)?.layoutId

private fun Placeable.PlacementScope.place(
    height: Int,
    width: Int,
    trailingPlaceable: Placeable?,
    textFieldPlaceable: Placeable,
    placeholderPlaceable: Placeable?,
    singleLine: Boolean,
    density: Float,
    layoutDirection: LayoutDirection,
    paddingValues: PaddingValues
) {
    val topPadding = (paddingValues.calculateTopPadding().value * density).roundToInt()
    val startPadding =
        (paddingValues.calculateStartPadding(layoutDirection).value * density).roundToInt()
    val iconPadding = HorizontalIconPadding.value * density

    // placed center vertically and to the end edge horizontally
    trailingPlaceable?.placeRelative(
        width - trailingPlaceable.width,
        Alignment.CenterVertically.align(trailingPlaceable.height, height)
    )

    // placed center vertically and after the leading icon horizontally if single line text field
    // placed to the top with padding for multi line text field
    val textVerticalPosition = if (singleLine) {
        Alignment.CenterVertically.align(textFieldPlaceable.height, height)
    } else {
        topPadding
    }
    textFieldPlaceable.placeRelative(0, textVerticalPosition)

    // placed similar to the input text above
    placeholderPlaceable?.let {
        val placeholderVerticalPosition = if (singleLine) {
            Alignment.CenterVertically.align(it.height, height)
        } else {
            topPadding
        }
        it.placeRelative(0, placeholderVerticalPosition)
    }
}

internal fun widthOrZero(placeable: Placeable?) = placeable?.width ?: 0
internal fun heightOrZero(placeable: Placeable?) = placeable?.height ?: 0

private fun calculateWidth(
    trailingPlaceableWidth: Int,
    textFieldPlaceableWidth: Int,
    placeholderPlaceableWidth: Int,
    constraints: Constraints
): Int {
    val middleSection = maxOf(
        textFieldPlaceableWidth,
        placeholderPlaceableWidth
    )
    val wrappedWidth = middleSection + trailingPlaceableWidth
    return max(wrappedWidth, constraints.minWidth)
}

private fun calculateHeight(
    trailingPlaceableHeight: Int,
    textFieldPlaceableHeight: Int,
    placeholderPlaceableHeight: Int,
    constraints: Constraints,
    density: Float,
    paddingValues: PaddingValues
): Int {
    // middle section is defined as a height of the text field or placeholder ( whichever is
    // taller) plus 16.dp or half height of the label if it is taller, given that the label
    // is vertically centered to the top edge of the resulting text field's container
    val inputFieldHeight = max(
        textFieldPlaceableHeight,
        placeholderPlaceableHeight
    )
    val topPadding = paddingValues.calculateTopPadding().value * density
    val bottomPadding = paddingValues.calculateBottomPadding().value * density
    val middleSectionHeight = inputFieldHeight + bottomPadding + topPadding
    return max(
        constraints.minHeight,
        maxOf(
            trailingPlaceableHeight,
            middleSectionHeight.roundToInt()
        )
    )
}
