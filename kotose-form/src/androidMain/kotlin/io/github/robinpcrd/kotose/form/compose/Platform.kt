/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.input.PlatformImeOptions
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowCompat
import io.github.robinpcrd.kotose.form.FocusableField

internal actual val isIosPlatform: Boolean = false

internal actual val keyboardBarImeOptions: PlatformImeOptions? = null

internal actual fun showKeyboardBar(context: KeyboardBarContext) = Unit

internal actual fun releaseKeyboardBar(field: FocusableField<*>) = Unit

internal actual fun fullScreenDialogProperties() =
    DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)

@Composable
internal actual fun FullScreenDialogBars(lightBars: Boolean) {
    val view = LocalView.current
    SideEffect {
        (view.parent as? DialogWindowProvider)?.window?.let { window ->
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = lightBars
                isAppearanceLightNavigationBars = lightBars
            }
        }
    }
}
