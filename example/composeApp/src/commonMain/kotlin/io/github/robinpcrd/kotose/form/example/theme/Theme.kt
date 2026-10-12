/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.example.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import io.github.robinpcrd.kotose.form.compose.KotoseFormTheme
import io.github.robinpcrd.kotose.form.example.catalog.Look
import io.github.robinpcrd.kotose.form.example.catalog.brandFormStyle

/** Baseline Material 3 palettes so the catalog shows the library's default look, or the brand. */
@Composable
fun ExampleTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()) {
        if (Look.brand) KotoseFormTheme(brandFormStyle(), content) else content()
    }
}
