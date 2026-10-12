/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.compose.fields

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import io.github.robinpcrd.kotose.form.compose.FieldDefaults
import io.github.robinpcrd.kotose.form.compose.LocalKotosePaddings
import io.github.robinpcrd.kotose.form.fields.ActionField
import io.github.robinpcrd.kotose.form.fields.ButtonFieldAppearance
import io.github.robinpcrd.kotose.resources.StrRes

@Composable
fun ButtonField(
    field: ActionField,
    appearance: ButtonFieldAppearance = FieldDefaults.buttonFieldAppearance(),
    modifier: Modifier = Modifier.testTag(field.key),
) {
    val title = field.title
    // Kotlin gates the callback on visibility and enabled state, like the native renderer.
    val onClick: () -> Unit = { field.perform() }

    val isEnabled by field.isEnabledState

    ButtonField(title = title, appearance = appearance, onClick = onClick, isEnabled, modifier)
}

@Composable
fun ButtonField(
    title: StrRes,
    appearance: ButtonFieldAppearance,
    onClick: () -> Unit,
    isEnabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val padding =
        appearance.padding ?: PaddingValues(horizontal = LocalKotosePaddings.current.defaultPadding)

    val buttonModifier = when (appearance.width) {
        ButtonFieldAppearance.ButtonWidth.WRAP_CONTENT -> modifier.padding(padding)
        ButtonFieldAppearance.ButtonWidth.WIDE -> modifier
            .fillMaxWidth()
            .padding(padding)

        ButtonFieldAppearance.ButtonWidth.FULL -> modifier.fillMaxWidth()
    }

    val buttonShape = appearance.shape ?: ButtonDefaults.shape
    /*when (appearance.width) {
        ButtonFieldAppearance.ButtonWidth.FULL -> RectangleShape
        ButtonFieldAppearance.ButtonWidth.WIDE, ButtonFieldAppearance.ButtonWidth.WRAP_CONTENT ->
            MaterialTheme.shapes.small
    }*/

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = appearance.horizontalArrangement,
        verticalAlignment = appearance.verticalAlignment
    ) {
        when (appearance.buttonStyle) {
            ButtonFieldAppearance.ButtonStyle.BUTTON -> Button(
                onClick = onClick,
                modifier = buttonModifier,
                enabled = isEnabled,
                shape = buttonShape,
                colors = appearance.buttonColors ?: ButtonDefaults.buttonColors()
            ) { ButtonFieldInner(title, appearance) }

            ButtonFieldAppearance.ButtonStyle.OUTLINE -> OutlinedButton(
                onClick = onClick,
                modifier = buttonModifier,
                enabled = isEnabled,
                shape = buttonShape,
                colors = appearance.buttonColors ?: ButtonDefaults.outlinedButtonColors()
            ) { ButtonFieldInner(title, appearance) }

            ButtonFieldAppearance.ButtonStyle.TEXT -> TextButton(
                onClick = onClick,
                modifier = buttonModifier,
                enabled = isEnabled,
                shape = buttonShape,
                colors = appearance.buttonColors ?: ButtonDefaults.textButtonColors()
            ) { ButtonFieldInner(title, appearance) }
        }
    }
}

@Composable
private fun ButtonFieldInner(
    title: StrRes,
    appearance: ButtonFieldAppearance
) {
    appearance.icon?.getPainter()?.let {
        Icon(
            it,
            contentDescription = title.getStringOrEmpty(),
            Modifier.size(appearance.iconSize)
        )
        if (appearance.spacerWidth != Dp.Unspecified)
            Spacer(Modifier.width(appearance.spacerWidth))
    }
    Text(title.getStringOrEmpty(), style = appearance.textStyle ?: LocalTextStyle.current)
}
