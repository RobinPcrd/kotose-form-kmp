/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.input.PlatformImeOptions
import androidx.compose.ui.window.DialogProperties
import io.github.robinpcrd.kotose.form.FocusableField

/** Selects the iOS override of per-platform settings such as a selection's presentation. */
internal expect val isIosPlatform: Boolean

/** A dialog drawn behind the system bars; its content pads itself with the safe drawing insets. */
internal expect fun fullScreenDialogProperties(): DialogProperties

/** Keeps system bar icons readable over a full-screen dialog; [lightBars] means dark icons. */
@Composable
internal expect fun FullScreenDialogBars(lightBars: Boolean)

/** Keyboard options that carry the iOS keyboard bar; null on Android, which has none. */
internal expect val keyboardBarImeOptions: PlatformImeOptions?

/** Makes the iOS keyboard bar act for the input that just took focus. */
internal expect fun showKeyboardBar(context: KeyboardBarContext)

/** Makes the iOS keyboard bar drop [field] when its view leaves the composition, so it keeps no closed form. */
internal expect fun releaseKeyboardBar(field: FocusableField<*>)
