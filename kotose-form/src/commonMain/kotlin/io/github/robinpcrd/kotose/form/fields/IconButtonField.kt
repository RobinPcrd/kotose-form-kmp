/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.fields

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import io.github.robinpcrd.kotose.form.compose.ComposeField
import io.github.robinpcrd.kotose.form.compose.fields.IconButtonField
import io.github.robinpcrd.kotose.form.utils.ComposableFun
import io.github.robinpcrd.kotose.form.utils.PainterRes
import io.github.robinpcrd.kotose.resources.StrRes

/** Natively the icon is not available, so the description becomes the button title. */
class IconButtonField(
    tag: String? = null,
    val icon: PainterRes,
    val contentDescription: StrRes? = null,
    val appearance: @Composable () -> IconButtonFieldAppearance = IconButtonFieldAppearance.Default,
    onClick: () -> Unit
) : ActionField(tag, contentDescription ?: StrRes.Empty, onClick), ComposeField {
    override var view: ComposableFun = @Composable {
        val appearance by rememberUpdatedState(appearance())

        IconButtonField(field = this, appearance = appearance)
    }
}

@Immutable
data class IconButtonFieldAppearance(
    val iconSize: DpSize = DpSize(width = 24.dp, height = 24.dp),
    val iconPadding: PaddingValues? = null,
    val buttonPadding: PaddingValues? = null,
    val horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    val verticalAlignment: Alignment.Vertical = Alignment.Top,
    val backgroundColor: Color = Color.Unspecified,
    val contentColor: Color = Color.Unspecified,
    val shape: Shape = RectangleShape,
    val border: BorderStroke? = null
) {
    companion object {
        @Stable
        val Default: @Composable () -> IconButtonFieldAppearance = @Composable {
            IconButtonFieldAppearance()
        }
    }
}
