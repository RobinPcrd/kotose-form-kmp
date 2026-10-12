/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.compose

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.unit.dp

@Deprecated(
    message = "Padding is set on error fields to avoid view jump",
    replaceWith = ReplaceWith("fieldPadding()"),
    level = DeprecationLevel.ERROR
)
fun Modifier.fieldPadding(isError: Boolean = false) = composed {
    padding(
        start = LocalKotosePaddings.current.defaultPadding,
        top = LocalKotosePaddings.current.smallPadding,
        end = LocalKotosePaddings.current.defaultPadding,
        bottom = if (isError) 0.dp else LocalKotosePaddings.current.smallPadding
    )
}

expect fun Modifier.clearFocusOnKeyboardDismiss(): Modifier

fun Modifier.fieldPadding() = composed {
    padding(
        start = LocalKotosePaddings.current.defaultPadding,
        top = LocalKotosePaddings.current.smallPadding,
        end = LocalKotosePaddings.current.defaultPadding,
        bottom = LocalKotosePaddings.current.smallPadding
    )
}
