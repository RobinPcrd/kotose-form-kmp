/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.fields

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.robinpcrd.kotose.form.compose.ComposeField
import io.github.robinpcrd.kotose.form.compose.FieldColors
import io.github.robinpcrd.kotose.form.compose.FieldColorsToken
import io.github.robinpcrd.kotose.form.compose.FieldDefaults
import io.github.robinpcrd.kotose.form.compose.toColor
import io.github.robinpcrd.kotose.form.dsl.SelectionGroupDslMarker
import io.github.robinpcrd.kotose.form.utils.ComposableFun
import io.github.robinpcrd.kotose.resources.StrRes

/**
 * A field that can be selected
 *
 * @param tag Field's tag
 * @param ValueType Type associated to this field
 * @param selectableValue Value to set to this field when it is selected
 * @param defaultLabelForValue The text to display, if null the displayed text is
 * [selectableValue.toString()]
 */
@SelectionGroupDslMarker
open class SelectableField<ValueType>(
    tag: String? = null,
    selectableValue: ValueType,
    defaultLabelForValue: StrRes? = null,
) : SelectionOptionField<ValueType>(tag, selectableValue, defaultLabelForValue), ComposeField {

    private val _appearance = mutableStateOf(SelectableFieldAppearance())
    val appearanceState: State<SelectableFieldAppearance> = _appearance

    var appearance: SelectableFieldAppearance
        get() = _appearance.value
        set(value) {
            _appearance.value = value
        }

    override var view: ComposableFun = @Composable {
        error("No composable view returned")
    }

}

@Immutable
data class SelectableFieldAppearance(
    val textColor: Color = Color.Unspecified,
    val labelTextStyle: TextStyle? = null,
    val tonalElevation: Dp = 0.dp,
    val startPadding: Dp = 0.dp,
)

@Composable
fun SelectableFieldAppearance.toColors(): FieldColors {
    val containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(tonalElevation)
    val textColor = if (this.textColor == Color.Unspecified) {
        FieldColorsToken.TextColor.toColor()
    } else {
        this.textColor
    }

    return FieldDefaults.fieldColors(
        containerColor = containerColor,
        textColor = textColor,
    )
}
