/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.fields

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import io.github.robinpcrd.kotose.form.compose.kotoseFormStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.text.TextStyle
import io.github.robinpcrd.kotose.form.compose.ComposeField
import io.github.robinpcrd.kotose.form.compose.FieldColors
import io.github.robinpcrd.kotose.form.compose.FieldDefaults
import io.github.robinpcrd.kotose.form.compose.fields.LabelField
import io.github.robinpcrd.kotose.form.utils.ComposableFun
import io.github.robinpcrd.kotose.resources.StrRes

class LabelField(
    tag: String? = null,
    val defaultValue: StrRes? = null,
    val appearance: @Composable () -> LabelFieldAppearance = LabelFieldAppearance.Default,
    isTitle: Boolean = false,
) : DisplayTextField(tag, defaultValue, isTitle), ComposeField {
    override var view: ComposableFun = @Composable {
        val appearance by rememberUpdatedState(appearance())

        LabelField(field = this, appearance = appearance)
    }
}

/**
 * Label field appearance
 *
 * @property textStyle If null, the [LocalTextStyle.current] will be used in the default [LabelField(String, LabelFieldAppearance)]
 */
@Suppress("KDocUnresolvedReference")
@Immutable
data class LabelFieldAppearance(
    val colors: FieldColors,
    val textStyle: TextStyle,
) {
    companion object {
        @Stable
        val Default: @Composable () -> LabelFieldAppearance = @Composable {
            LabelFieldAppearance(
                colors = FieldDefaults.fieldColors(),
                textStyle = LocalTextStyle.current
            )
        }

        @Stable
        val Title: @Composable () -> LabelFieldAppearance = @Composable {
            LabelFieldAppearance(
                colors = FieldDefaults.fieldColors(),
                textStyle = LocalTextStyle.current.merge(kotoseFormStyle().titleTextStyle)
            )
        }
    }
}
