/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.fields

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.robinpcrd.kotose.form.compose.ComposeField
import io.github.robinpcrd.kotose.form.compose.FieldColors
import io.github.robinpcrd.kotose.form.compose.FieldDefaults
import io.github.robinpcrd.kotose.form.compose.fields.ParagraphField
import io.github.robinpcrd.kotose.form.utils.ComposableFun
import io.github.robinpcrd.kotose.resources.StrRes

class ParagraphField(
    tag: String? = null,
    val appearance: @Composable () -> ParagraphFieldAppearance = ParagraphFieldAppearance.Default
) : DisplayParagraphField(tag), ComposeField {
    override var view: ComposableFun = @Composable {
        val appearance by rememberUpdatedState(appearance())

        ParagraphField(field = this, appearance = appearance)
    }
}

/**
 * Paragraph field appearance
 *
 * @property textStyle If null, the [LocalTextStyle.current] will be used in the default [ParagraphField(List<StrRes>, Modifier, ParagraphFieldAppearance)]
 */
@Suppress("KDocUnresolvedReference")
@Immutable
data class ParagraphFieldAppearance(
    val colors: FieldColors,
    val textStyle: TextStyle,
    val linePrefix: String? = null,
    val backgroundColor: Color? = Color.Unspecified,
    val borderSize: Dp = 1.dp,
    val borderColor: Color = Color.Unspecified,
    val padding: PaddingValues = PaddingValues(8.dp),
    val cornerShape: Shape? = null,
    val width: Width = Width.WrapContent,
) {
    companion object {
        @Stable
        val Default: @Composable () -> ParagraphFieldAppearance = @Composable {
            ParagraphFieldAppearance(
                FieldDefaults.fieldColors(),
                LocalTextStyle.current
            )
        }
    }

    enum class Width {
        FillMaxWidth,
        WrapContent;
    }
}
