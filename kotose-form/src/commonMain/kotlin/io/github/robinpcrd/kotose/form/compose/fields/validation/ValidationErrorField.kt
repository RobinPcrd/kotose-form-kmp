/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.compose.fields.validation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Icon
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import io.github.robinpcrd.kotose.form.compose.kotoseFormStyle
import io.github.robinpcrd.kotose.form.fields.validation.ValidationErrorField
import io.github.robinpcrd.kotose.form.fields.validation.ValidationErrorGroupField

@Composable
fun ValidationErrorField(field: ValidationErrorGroupField, modifier: Modifier = Modifier) {
    field.error.getString()?.let {
        ValidationErrorField(it, field.asWarning, modifier.testTag("${if (field.asWarning) "warning" else "error"}-${field.elementKey}"))
    }
}

@Composable
fun ValidationErrorField(field: ValidationErrorField, modifier: Modifier = Modifier) {
    field.error.getString()?.let {
        ValidationErrorField(it, field.asWarning, modifier.testTag("${if (field.asWarning) "warning" else "error"}-${field.elementKey}"))
    }
}

@Composable
fun ValidationErrorField(error: String, asWarning: Boolean, modifier: Modifier = Modifier) {
    val style = kotoseFormStyle()
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
        val color = if (asWarning) style.warning else style.error
        CompositionLocalProvider(LocalContentColor provides color) {
            if (style.showsMessageIcons) {
                Icon(
                    if (asWarning) Icons.Outlined.Warning else Icons.Outlined.Info,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
            }
            Text(error, style = style.messageTextStyle)
        }
    }
}
