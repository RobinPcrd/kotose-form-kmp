/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.compose

import androidx.compose.runtime.Composable

/** Optional Compose view owned by a styled or custom field. */
interface ComposeField {
    var view: @Composable () -> Unit
}
