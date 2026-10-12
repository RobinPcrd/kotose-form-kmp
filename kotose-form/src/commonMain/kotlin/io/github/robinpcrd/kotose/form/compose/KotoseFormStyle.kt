/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.compose

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// region Style

/**
 * Visual tokens for the Compose renderer, named like the SwiftUI `KotoseFormStyle` so one brand
 * maps to both renderers. Kotlin owns every semantic (enabled, error, warning, focus, labels);
 * the style only decides how the renderer draws them. Defaults come from [MaterialTheme], so a
 * form inside the app's theme already follows it; use [KotoseFormTheme] to override tokens.
 */
@Immutable
data class KotoseFormStyle(
    val accent: Color,
    val text: Color,
    val placeholder: Color,
    val inputBackground: Color,
    val inputBorder: Color,
    val focusedBorder: Color,
    val disabledText: Color,
    val disabledBackground: Color,
    val error: Color,
    val warning: Color,
    val paragraphBackground: Color,
    val cornerRadius: Dp,
    val borderWidth: Dp,
    val inputTextStyle: TextStyle,
    val messageTextStyle: TextStyle,
    val titleTextStyle: TextStyle,
    val showsMessageIcons: Boolean,
    /** Previous, Next and Done above the iOS keyboard of every text input; Android has none. */
    val showsKeyboardBar: Boolean = true,
) {
    companion object {
        /** The Material look every token defaults to. */
        @Composable
        fun fromMaterial(): KotoseFormStyle {
            val colors = MaterialTheme.colorScheme
            val typography = MaterialTheme.typography
            return KotoseFormStyle(
                accent = colors.primary,
                text = colors.onSurface,
                // Lighter than the text so an empty input reads as empty.
                placeholder = colors.onSurfaceVariant.copy(alpha = 0.6f),
                inputBackground = Color.Transparent,
                inputBorder = colors.outline,
                focusedBorder = colors.primary,
                // Disabled keeps readable text on a greyed container instead of fading like a placeholder.
                disabledText = colors.onSurface.copy(alpha = 0.6f),
                disabledBackground = colors.onSurface.copy(alpha = 0.06f),
                error = colors.error,
                warning = harmonizedWarning(isSystemInDarkTheme()),
                paragraphBackground = colors.primary.copy(alpha = 0.1f),
                cornerRadius = 4.dp,
                borderWidth = 1.dp,
                inputTextStyle = typography.bodyLarge,
                messageTextStyle = typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                titleTextStyle = typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                showsMessageIcons = true,
            )
        }
    }
}

/**
 * Set by [KotoseFormTheme]; null means "follow MaterialTheme", resolved by [kotoseFormStyle].
 * Internal: SKIE cannot export a composition local of a nullable class to Swift.
 */
internal val LocalKotoseFormStyle = staticCompositionLocalOf<KotoseFormStyle?> { null }

/** The style in effect: the override from [KotoseFormTheme], or the current Material look. */
@Composable
fun kotoseFormStyle(): KotoseFormStyle = LocalKotoseFormStyle.current ?: KotoseFormStyle.fromMaterial()

/** Applies [style] to every form renderer below it. */
@Composable
fun KotoseFormTheme(style: KotoseFormStyle, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalKotoseFormStyle provides style, content)
}

// endregion
