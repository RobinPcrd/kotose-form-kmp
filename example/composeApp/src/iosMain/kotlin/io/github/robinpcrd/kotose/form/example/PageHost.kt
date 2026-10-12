/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.example

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.uikit.OnFocusBehavior
import androidx.compose.ui.window.ComposeUIViewController
import io.github.robinpcrd.kotose.form.example.catalog.CatalogPage
import io.github.robinpcrd.kotose.form.example.theme.ExampleTheme
import io.github.robinpcrd.kotose.form.example.ui.LocalActionBarRidesKeyboard
import io.github.robinpcrd.kotose.form.example.ui.PageScreen
import platform.UIKit.UIViewController

/** Compose iOS renderer for one catalog page, embedded below the SwiftUI renderer switch. */
fun makePageViewController(page: CatalogPage): UIViewController =
    ComposeUIViewController(configure = { onFocusBehavior = OnFocusBehavior.DoNothing }) {
        CompositionLocalProvider(LocalActionBarRidesKeyboard provides false) {
            ExampleTheme { PageScreen(page, showTopBar = false) }
        }
    }
