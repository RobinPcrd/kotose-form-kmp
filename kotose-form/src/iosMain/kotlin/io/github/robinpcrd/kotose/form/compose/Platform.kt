/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.text.input.PlatformImeOptions
import androidx.compose.ui.window.DialogProperties
import io.github.robinpcrd.kotose.form.FocusableField

internal actual val isIosPlatform: Boolean = true

@OptIn(ExperimentalComposeUiApi::class)
private val keyboardBarOptions by lazy { PlatformImeOptions { inputAccessoryView(KeyboardBar.view) } }

internal actual val keyboardBarImeOptions: PlatformImeOptions? get() = keyboardBarOptions

internal actual fun showKeyboardBar(context: KeyboardBarContext) = KeyboardBar.show(context)

internal actual fun releaseKeyboardBar(field: FocusableField<*>) = KeyboardBar.release(field)

internal actual fun fullScreenDialogProperties() =
    DialogProperties(usePlatformDefaultWidth = false, usePlatformInsets = false)

/** The status bar follows the app's appearance on iOS. */
@Composable
internal actual fun FullScreenDialogBars(lightBars: Boolean) = Unit
