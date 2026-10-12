/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.compose

import androidx.compose.runtime.Composable
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.google.android.material.color.MaterialColors

val orange100 = Color(0xFFFFE0B2)
val orange200 = Color(0xFFFFCC80)
val orange300 = Color(0xFFFFB74D)
val amber300 = Color(0xFFFFD54F)
val amber600 = Color(0xFFFFB300)
val amber700 = Color(0xFFFFA000)

@Composable
actual fun harmonizedWarning(
    isDarkTheme: Boolean,
) = Color(
    MaterialColors.harmonize(
        if (isDarkTheme) orange200.toArgb() else amber700.toArgb(),
        MaterialTheme.colorScheme.primary.toArgb(),
    )
)
