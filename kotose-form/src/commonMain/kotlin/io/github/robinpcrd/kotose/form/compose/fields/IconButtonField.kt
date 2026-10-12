/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.compose.fields

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import io.github.robinpcrd.kotose.form.compose.fieldPadding
import io.github.robinpcrd.kotose.form.fields.IconButtonField
import io.github.robinpcrd.kotose.form.fields.IconButtonFieldAppearance

@Composable
fun IconButtonField(
    field: IconButtonField,
    appearance: IconButtonFieldAppearance,
) {
    val painter = field.icon.getPainter() ?: return
    val contentDescription = field.contentDescription?.getStringOrEmpty()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .fieldPadding(),
        horizontalArrangement = appearance.horizontalArrangement,
        verticalAlignment = appearance.verticalAlignment
    ) {
        Surface(
            color = appearance.backgroundColor,
            contentColor = appearance.contentColor,
            shape = appearance.shape,
            border = appearance.border,
        ) {
            IconButton(
                onClick = { field.perform() },
                modifier = Modifier
                    .let { mod ->
                        appearance.buttonPadding?.let { mod.padding(it) } ?: mod
                    }
            ) {
                Icon(
                    painter = painter,
                    contentDescription = contentDescription,
                    modifier = Modifier
                        .size(appearance.iconSize)
                        .let { mod ->
                            appearance.iconPadding?.let { mod.padding(it) } ?: mod
                        },
                    tint = if (appearance.contentColor != Color.Unspecified) {
                        appearance.contentColor
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
            }
        }
    }
}
