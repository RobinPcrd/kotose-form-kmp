/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.example.catalog

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.robinpcrd.kotose.form.compose.KotoseFormStyle
import io.github.robinpcrd.kotose.form.example.Res
import io.github.robinpcrd.kotose.form.example.look_brand
import io.github.robinpcrd.kotose.resources.StrRes

// region Look

/**
 * Which look the catalog renders: the platform defaults, or one brand applied to every
 * renderer so their chrome can be compared. The Swift shell reads the same values.
 */
object Look {
    var brand by mutableStateOf(false)

    val label: StrRes = res(Res.string.look_brand)

    /** Brand tokens as ARGB, the single source for Compose and Swift. */
    val accentArgb: Long = 0xFF0F766E
    val errorArgb: Long = 0xFFC62828
    val cornerRadiusDp: Int = 14
}

/** The brand on Compose: same values as the Swift `Brand.style`. */
@Composable
fun brandFormStyle(): KotoseFormStyle {
    val accent = Color(Look.accentArgb)
    return KotoseFormStyle.fromMaterial().copy(
        accent = accent,
        focusedBorder = accent,
        inputBorder = Color.Transparent,
        inputBackground = MaterialTheme.colorScheme.surfaceVariant,
        error = Color(Look.errorArgb),
        paragraphBackground = accent.copy(alpha = 0.12f),
        cornerRadius = Look.cornerRadiusDp.dp,
    )
}

// endregion
