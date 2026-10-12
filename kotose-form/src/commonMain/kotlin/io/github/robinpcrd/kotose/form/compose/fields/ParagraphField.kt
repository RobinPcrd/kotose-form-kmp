/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.compose.fields

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.isUnspecified
import io.github.robinpcrd.kotose.form.compose.FieldDefaults
import io.github.robinpcrd.kotose.form.compose.kotoseFormStyle
import io.github.robinpcrd.kotose.form.compose.fieldPadding
import io.github.robinpcrd.kotose.form.fields.DisplayParagraphField
import io.github.robinpcrd.kotose.form.fields.ParagraphFieldAppearance
import io.github.robinpcrd.kotose.resources.StrRes

@Composable
fun ParagraphField(
    field: DisplayParagraphField,
    appearance: ParagraphFieldAppearance = FieldDefaults.paragraphFieldAppearance()
) {
    val value by field.valueState

    ParagraphField(
        lines = value ?: emptyList(),
        appearance = appearance,
        modifier = Modifier.testTag(field.key).fieldPadding(),
    )
}

@Composable
fun ParagraphField(
    lines: List<StrRes>,
    appearance: ParagraphFieldAppearance,
    modifier: Modifier = Modifier,
) {
    val textStyle = appearance.textStyle.merge(
        TextStyle(color = appearance.colors.textColor(enabled = true).value)
    )

    val style = kotoseFormStyle()
    val backgroundColor = appearance.backgroundColor?.takeOrElse { style.paragraphBackground } ?: Color.Unspecified
    val borderColor = appearance.borderColor.takeOrElse { style.accent }
    val shape = appearance.cornerShape ?: RoundedCornerShape(style.cornerRadius)
    val boxModifier = modifier
        .then(
            when (appearance.width) {
                ParagraphFieldAppearance.Width.FillMaxWidth -> Modifier.fillMaxWidth()
                ParagraphFieldAppearance.Width.WrapContent -> Modifier
            }
        )
        .then(
            if (appearance.borderSize.isUnspecified) {
                Modifier.background(backgroundColor, shape)
            } else {
                Modifier.background(backgroundColor, shape).border(appearance.borderSize, borderColor, shape)
            }
        )
        .padding(appearance.padding)

    Box(boxModifier) {
        Column {
            lines.forEach { line ->
                var text = line.getString() ?: return
                text = if (appearance.linePrefix != null) {
                    "${appearance.linePrefix} $text"
                } else {
                    text
                }
                Text(text = text, style = textStyle)
            }
        }
    }
}
