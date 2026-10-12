/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.fields

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import io.github.robinpcrd.kotose.form.compose.ComposeField
import io.github.robinpcrd.kotose.form.compose.fields.ButtonField
import io.github.robinpcrd.kotose.form.utils.ComposableFun
import io.github.robinpcrd.kotose.form.utils.PainterRes
import io.github.robinpcrd.kotose.resources.StrRes

class ButtonField(
    tag: String? = null,
    override val title: StrRes,
    val appearance: @Composable () -> ButtonFieldAppearance = ButtonFieldAppearance.Default,
    onClick: () -> Unit
) : ActionField(tag, title, onClick), ComposeField {
    override var view: ComposableFun = @Composable {
        val appearance by rememberUpdatedState(appearance())

        ButtonField(field = this, appearance = appearance)
    }
}

/**
 * Button field appearance
 *
 * @property textStyle If null, the [LocalTextStyle.current] will be used in the default [ButtonField(StrRes, ButtonFieldAppearance, () -> Unit, Boolean)]
 * @property buttonColors If null, the [ButtonDefaults.buttonColors()], [ButtonDefaults.outlinedButtonColors()] or [ButtonDefaults.textButtonColors()] (depends on [ButtonStyle])  will be used in the default [ButtonField(StrRes, ButtonFieldAppearance, () -> Unit, Boolean)]
 * @property padding If null, the [PaddingValues(horizontal = LocalPaddings.current.defaultPadding)] will be used in the default [ButtonField(StrRes, ButtonFieldAppearance, () -> Unit, Boolean)]
 * @property shape If null, the [RectangleShape] will be used if [width] is [ButtonWidth.FULL] in other cases [MaterialTheme.shapes.small] will be used in the default [ButtonField(StrRes, ButtonFieldAppearance, () -> Unit, Boolean)]
 * @property iconSize is not handled if [icon] is null
 * @property spacerWidth is not handled if [icon] is null
 */
@Suppress("KDocUnresolvedReference")
@Immutable
data class ButtonFieldAppearance(
    val buttonStyle: ButtonStyle = ButtonStyle.BUTTON,
    val buttonColors: ButtonColors? = null,
    val textStyle: TextStyle? = null,
    val padding: PaddingValues? = null,
    val horizontalArrangement: Arrangement.Horizontal = Arrangement.End,
    val verticalAlignment: Alignment.Vertical = Alignment.Top,
    val width: ButtonWidth = ButtonWidth.WRAP_CONTENT,
    val shape: Shape? = null,
    val icon: PainterRes? = null,
    val iconSize: DpSize = DpSize(width = 24.dp, height = 24.dp),
    val spacerWidth: Dp = 8.dp,
) {
    companion object {
        @Stable
        val Default: @Composable () -> ButtonFieldAppearance =
            @Composable { ButtonFieldAppearance() }
    }

    enum class ButtonStyle { BUTTON, OUTLINE, TEXT }
    enum class ButtonWidth { WRAP_CONTENT, WIDE, FULL }
}
