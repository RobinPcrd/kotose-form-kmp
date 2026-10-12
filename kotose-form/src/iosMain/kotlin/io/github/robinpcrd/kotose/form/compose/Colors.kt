/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
actual fun harmonizedWarning(isDarkTheme: Boolean): Color {
    return if (isDarkTheme) Color(0xFFFFB74D) else Color(0xFFF57C00)
}
