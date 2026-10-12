/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.compose

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import io.github.robinpcrd.kotose.form.fields.ButtonFieldAppearance
import io.github.robinpcrd.kotose.form.fields.IconButtonFieldAppearance
import io.github.robinpcrd.kotose.form.fields.LabelFieldAppearance
import io.github.robinpcrd.kotose.form.fields.ParagraphFieldAppearance
import io.github.robinpcrd.kotose.form.utils.PainterRes

internal const val AnimationDuration = 150

@Immutable
class FieldColors internal constructor(
    private val textColor: Color,
    private val disabledTextColor: Color,
    private val containerColor: Color,
    private val cursorColor: Color,
    private val errorCursorColor: Color,
    private val textSelectionColors: TextSelectionColors,
    private val focusedIndicatorColor: Color,
    private val unfocusedIndicatorColor: Color,
    private val errorIndicatorColor: Color,
    private val disabledIndicatorColor: Color,
    private val focusedLeadingIconColor: Color,
    private val unfocusedLeadingIconColor: Color,
    private val disabledLeadingIconColor: Color,
    private val errorLeadingIconColor: Color,
    private val focusedTrailingIconColor: Color,
    private val unfocusedTrailingIconColor: Color,
    private val disabledTrailingIconColor: Color,
    private val errorTrailingIconColor: Color,
    private val focusedLabelColor: Color,
    private val unfocusedLabelColor: Color,
    private val disabledLabelColor: Color,
    private val errorLabelColor: Color,
    private val placeholderColor: Color,
    private val disabledPlaceholderColor: Color
) {

    @Composable
    fun textColor(enabled: Boolean): State<Color> {
        return rememberUpdatedState(if (enabled) textColor else disabledTextColor)
    }

    @Composable
    fun cursorColor(isError: Boolean): State<Color> {
        return rememberUpdatedState(if (isError) errorCursorColor else cursorColor)
    }

    @Composable
    fun placeholderColor(enabled: Boolean): State<Color> {
        return rememberUpdatedState(if (enabled) placeholderColor else disabledPlaceholderColor)
    }

    @Composable
    fun containerColor(): Color = containerColor

    @Composable
    fun surfaceColorAtElevation(elevation: Dp): Color {
        return if (containerColor == MaterialTheme.colorScheme.surface) {
            MaterialTheme.colorScheme.surfaceColorAtElevation(elevation)
        } else {
            containerColor
        }
    }

    @Composable
    fun indicatorColor(
        enabled: Boolean,
        isError: Boolean,
        interactionSource: InteractionSource
    ): State<Color> {
        val focused by interactionSource.collectIsFocusedAsState()

        val targetValue = when {
            !enabled -> disabledIndicatorColor
            isError -> errorIndicatorColor
            focused -> focusedIndicatorColor
            else -> unfocusedIndicatorColor
        }
        return if (enabled) {
            animateColorAsState(
                targetValue,
                tween(durationMillis = AnimationDuration),
                label = "animateIndicatorColor"
            )
        } else {
            rememberUpdatedState(targetValue)
        }
    }

    @Composable
    fun leadingIconColor(
        enabled: Boolean,
        isError: Boolean,
        interactionSource: InteractionSource
    ): State<Color> {
        val focused by interactionSource.collectIsFocusedAsState()

        return rememberUpdatedState(
            when {
                !enabled -> disabledLeadingIconColor
                isError -> errorLeadingIconColor
                focused -> focusedLeadingIconColor
                else -> unfocusedLeadingIconColor
            }
        )
    }

    @Composable
    fun trailingIconColor(
        enabled: Boolean,
        isError: Boolean,
        interactionSource: InteractionSource
    ): State<Color> {
        val focused by interactionSource.collectIsFocusedAsState()

        return rememberUpdatedState(
            when {
                !enabled -> disabledTrailingIconColor
                isError -> errorTrailingIconColor
                focused -> focusedTrailingIconColor
                else -> unfocusedTrailingIconColor
            }
        )
    }

    /*
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun toTextFieldColors() = TextFieldDefaults.outlinedTextFieldColors(
        textColor = textColor,
        disabledTextColor = disabledTextColor,
        containerColor = containerColor,
        cursorColor = cursorColor,
        errorCursorColor = errorCursorColor,
        selectionColors = textSelectionColors,
        focusedBorderColor = focusedIndicatorColor,
        unfocusedBorderColor = unfocusedIndicatorColor,
        disabledBorderColor = errorIndicatorColor,
        errorBorderColor = disabledIndicatorColor,
        focusedLeadingIconColor = focusedLeadingIconColor,
        unfocusedLeadingIconColor = unfocusedLeadingIconColor,
        disabledLeadingIconColor = disabledLeadingIconColor,
        errorLeadingIconColor = errorLeadingIconColor,
        focusedTrailingIconColor = focusedTrailingIconColor,
        unfocusedTrailingIconColor = unfocusedTrailingIconColor,
        disabledTrailingIconColor = disabledTrailingIconColor,
        errorTrailingIconColor = errorTrailingIconColor,
        focusedLabelColor = focusedLabelColor,
        unfocusedLabelColor = unfocusedLabelColor,
        disabledLabelColor = disabledLabelColor,
        errorLabelColor = errorLabelColor,
        placeholderColor = placeholderColor,
        disabledPlaceholderColor = disabledPlaceholderColor,
    )
    */

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || other !is FieldColors) return false

        if (textColor != other.textColor) return false
        if (disabledTextColor != other.disabledTextColor) return false
        if (cursorColor != other.cursorColor) return false
        if (errorCursorColor != other.errorCursorColor) return false
        if (textSelectionColors != other.textSelectionColors) return false
        if (focusedIndicatorColor != other.focusedIndicatorColor) return false
        if (unfocusedIndicatorColor != other.unfocusedIndicatorColor) return false
        if (errorIndicatorColor != other.errorIndicatorColor) return false
        if (disabledIndicatorColor != other.disabledIndicatorColor) return false
        if (focusedLeadingIconColor != other.focusedLeadingIconColor) return false
        if (unfocusedLeadingIconColor != other.unfocusedLeadingIconColor) return false
        if (disabledLeadingIconColor != other.disabledLeadingIconColor) return false
        if (errorLeadingIconColor != other.errorLeadingIconColor) return false
        if (focusedTrailingIconColor != other.focusedTrailingIconColor) return false
        if (unfocusedTrailingIconColor != other.unfocusedTrailingIconColor) return false
        if (disabledTrailingIconColor != other.disabledTrailingIconColor) return false
        if (errorTrailingIconColor != other.errorTrailingIconColor) return false
        if (containerColor != other.containerColor) return false
        if (focusedLabelColor != other.focusedLabelColor) return false
        if (unfocusedLabelColor != other.unfocusedLabelColor) return false
        if (disabledLabelColor != other.disabledLabelColor) return false
        if (errorLabelColor != other.errorLabelColor) return false
        if (placeholderColor != other.placeholderColor) return false
        if (disabledPlaceholderColor != other.disabledPlaceholderColor) return false

        return true
    }

    override fun hashCode(): Int {
        var result = textColor.hashCode()
        result = 31 * result + disabledTextColor.hashCode()
        result = 31 * result + cursorColor.hashCode()
        result = 31 * result + errorCursorColor.hashCode()
        result = 31 * result + textSelectionColors.hashCode()
        result = 31 * result + focusedIndicatorColor.hashCode()
        result = 31 * result + unfocusedIndicatorColor.hashCode()
        result = 31 * result + errorIndicatorColor.hashCode()
        result = 31 * result + disabledIndicatorColor.hashCode()
        result = 31 * result + focusedLeadingIconColor.hashCode()
        result = 31 * result + unfocusedLeadingIconColor.hashCode()
        result = 31 * result + disabledLeadingIconColor.hashCode()
        result = 31 * result + errorLeadingIconColor.hashCode()
        result = 31 * result + focusedTrailingIconColor.hashCode()
        result = 31 * result + unfocusedTrailingIconColor.hashCode()
        result = 31 * result + disabledTrailingIconColor.hashCode()
        result = 31 * result + errorTrailingIconColor.hashCode()
        result = 31 * result + containerColor.hashCode()
        result = 31 * result + focusedLabelColor.hashCode()
        result = 31 * result + unfocusedLabelColor.hashCode()
        result = 31 * result + disabledLabelColor.hashCode()
        result = 31 * result + errorLabelColor.hashCode()
        result = 31 * result + placeholderColor.hashCode()
        result = 31 * result + disabledPlaceholderColor.hashCode()
        return result
    }
}

enum class ColorSchemeKeyTokens {
    OnSurface;
}

object FieldColorsToken {
    val TextColor = ColorSchemeKeyTokens.OnSurface
}

fun ColorScheme.fromToken(value: ColorSchemeKeyTokens): Color {
    return when (value) {
        ColorSchemeKeyTokens.OnSurface -> onSurface
    }
}

@Composable
@ReadOnlyComposable
fun ColorSchemeKeyTokens.toColor(): Color {
    return MaterialTheme.colorScheme.fromToken(this)
}

object FieldDefaults {
    const val DisabledInputOpacity = 0.38f
    const val DisabledOutlineOpacity = 0.12f
    const val DisabledLeadingIconOpacity = 0.38f
    const val DisabledTrailingIconOpacity = 0.38f
    const val DisabledLabelOpacity = 0.38f

    @Composable
    fun fieldColors(
        textColor: Color = MaterialTheme.colorScheme.onSurface,
        disabledTextColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = DisabledInputOpacity),
        containerColor: Color = Color.Transparent,
        cursorColor: Color = MaterialTheme.colorScheme.primary,
        errorCursorColor: Color = MaterialTheme.colorScheme.error,
        selectionColors: TextSelectionColors = LocalTextSelectionColors.current,
        focusedBorderColor: Color = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor: Color = MaterialTheme.colorScheme.outline,
        disabledBorderColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = DisabledOutlineOpacity),
        errorBorderColor: Color = MaterialTheme.colorScheme.error,
        focusedLeadingIconColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
        unfocusedLeadingIconColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
        disabledLeadingIconColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = DisabledLeadingIconOpacity),
        errorLeadingIconColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
        focusedTrailingIconColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
        unfocusedTrailingIconColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
        disabledTrailingIconColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = DisabledTrailingIconOpacity),
        errorTrailingIconColor: Color = MaterialTheme.colorScheme.error,
        focusedLabelColor: Color = MaterialTheme.colorScheme.primary,
        unfocusedLabelColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
        disabledLabelColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = DisabledLabelOpacity),
        errorLabelColor: Color = MaterialTheme.colorScheme.error,
        placeholderColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
        disabledPlaceholderColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = DisabledInputOpacity)
    ): FieldColors = FieldColors(
        textColor = textColor,
        disabledTextColor = disabledTextColor,
        cursorColor = cursorColor,
        errorCursorColor = errorCursorColor,
        textSelectionColors = selectionColors,
        focusedIndicatorColor = focusedBorderColor,
        unfocusedIndicatorColor = unfocusedBorderColor,
        errorIndicatorColor = errorBorderColor,
        disabledIndicatorColor = disabledBorderColor,
        focusedLeadingIconColor = focusedLeadingIconColor,
        unfocusedLeadingIconColor = unfocusedLeadingIconColor,
        disabledLeadingIconColor = disabledLeadingIconColor,
        errorLeadingIconColor = errorLeadingIconColor,
        focusedTrailingIconColor = focusedTrailingIconColor,
        unfocusedTrailingIconColor = unfocusedTrailingIconColor,
        disabledTrailingIconColor = disabledTrailingIconColor,
        errorTrailingIconColor = errorTrailingIconColor,
        containerColor = containerColor,
        focusedLabelColor = focusedLabelColor,
        unfocusedLabelColor = unfocusedLabelColor,
        disabledLabelColor = disabledLabelColor,
        errorLabelColor = errorLabelColor,
        placeholderColor = placeholderColor,
        disabledPlaceholderColor = disabledPlaceholderColor
    )

    @Composable
    fun warningFieldColors(): FieldColors {
        val harmonizedWarning = harmonizedWarning()

        return fieldColors(
            errorCursorColor = harmonizedWarning,
            errorBorderColor = harmonizedWarning,
            errorTrailingIconColor = harmonizedWarning,
            errorLabelColor = harmonizedWarning,
        )
    }

    @Composable
    fun buttonFieldAppearance(
        buttonStyle: ButtonFieldAppearance.ButtonStyle = ButtonFieldAppearance.ButtonStyle.BUTTON,
        buttonColors: ButtonColors? = null,
        textStyle: TextStyle? = null,
        padding: PaddingValues? = null,
        horizontalArrangement: Arrangement.Horizontal = Arrangement.End,
        verticalAlignment: Alignment.Vertical = Alignment.Top,
        width: ButtonFieldAppearance.ButtonWidth = ButtonFieldAppearance.ButtonWidth.WRAP_CONTENT,
        shape: Shape? = null,
        icon: PainterRes? = null,
        iconSize: DpSize = DpSize(width = 24.dp, height = 24.dp),
        spacerWidth: Dp = 8.dp,
    ) = ButtonFieldAppearance(
        buttonStyle = buttonStyle,
        buttonColors = buttonColors,
        textStyle = textStyle,
        padding = padding,
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = verticalAlignment,
        width = width,
        shape = shape,
        icon = icon,
        iconSize = iconSize,
        spacerWidth = spacerWidth,
    )

    @Composable
    fun iconButtonFieldAppearance(
        iconSize: DpSize = DpSize(width = 24.dp, height = 24.dp),
        iconPadding: PaddingValues? = null,
        buttonPadding: PaddingValues? = null,
        horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
        verticalAlignment: Alignment.Vertical = Alignment.Top,
        backgroundColor: Color = Color.Unspecified,
        contentColor: Color = Color.Unspecified,
        shape: Shape = RectangleShape,
        border: BorderStroke? = null
    ) = IconButtonFieldAppearance(
        iconSize = iconSize,
        iconPadding = iconPadding,
        buttonPadding = buttonPadding,
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = verticalAlignment,
        backgroundColor = backgroundColor,
        contentColor = contentColor,
        shape = shape,
        border = border,
    )

    @Composable
    fun labelFieldAppearance(
        colors: FieldColors = fieldColors(),
        textStyle: TextStyle = LocalTextStyle.current
    ) = LabelFieldAppearance(
        colors = colors,
        textStyle = textStyle
    )

    @Composable
    fun paragraphFieldAppearance(
        colors: FieldColors = fieldColors(),
        textStyle: TextStyle = LocalTextStyle.current,
        linePrefix: String? = null,
        backgroundColor: Color? = Color.Unspecified,
        borderSize: Dp = Dp.Unspecified,
        borderColor: Color = Color.Unspecified,
        padding: PaddingValues = PaddingValues(8.dp),
        cornerShape: Shape? = null,
        width: ParagraphFieldAppearance.Width = ParagraphFieldAppearance.Width.WrapContent,
    ) = ParagraphFieldAppearance(
        colors = colors,
        textStyle = textStyle,
        linePrefix = linePrefix,
        backgroundColor = backgroundColor,
        borderSize = borderSize,
        borderColor = borderColor,
        padding = padding,
        cornerShape = cornerShape,
        width = width,
    )
}

private val UnfocusedBorderThickness = 1.dp
private val FocusedBorderThickness = 2.dp

@Composable
fun animateBorderStrokeAsState(
    enabled: Boolean,
    isError: Boolean,
    interactionSource: InteractionSource,
    colors: FieldColors,
    focusedBorderThickness: Dp = FocusedBorderThickness,
    unfocusedBorderThickness: Dp = UnfocusedBorderThickness,
): State<BorderStroke> {
    val focused by interactionSource.collectIsFocusedAsState()
    val indicatorColor = colors.indicatorColor(enabled, isError, interactionSource)
    val targetThickness = if (focused) focusedBorderThickness else unfocusedBorderThickness
    val animatedThickness = if (enabled) {
        animateDpAsState(
            targetThickness,
            tween(durationMillis = AnimationDuration),
            label = "animateBorderStrokeAsState"
        )
    } else {
        rememberUpdatedState(unfocusedBorderThickness)
    }
    return rememberUpdatedState(
        BorderStroke(animatedThickness.value, SolidColor(indicatorColor.value))
    )
}
