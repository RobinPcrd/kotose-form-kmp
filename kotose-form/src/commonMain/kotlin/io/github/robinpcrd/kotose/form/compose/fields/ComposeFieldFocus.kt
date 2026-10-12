/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.compose.fields

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.FocusState
import io.github.robinpcrd.kotose.form.FocusableField
import io.github.robinpcrd.kotose.form.compose.KeyboardBarContext
import io.github.robinpcrd.kotose.form.compose.focusKeyboardBar
import io.github.robinpcrd.kotose.form.compose.releaseKeyboardBar

/** Focus objects belong to one mounted Compose field view. */
class ComposeFieldFocus {
    val requester = FocusRequester()
    val state: State<FocusState?>
        field = mutableStateOf(null)

    /** The iOS keyboard bar of the field's input, shown when the input takes focus. */
    internal var keyboardBar: KeyboardBarContext? = null

    fun update(state: FocusState): Boolean {
        val focusChanged = this.state.value?.isFocused == true || state.isFocused
        this.state.value = state
        if (state.isFocused) keyboardBar?.let { focusKeyboardBar(it) }
        return focusChanged
    }
}

@Composable
fun rememberFieldFocus(field: FocusableField<*>): ComposeFieldFocus =
    remember(field) { ComposeFieldFocus() }.also {
        DisposableEffect(field) { onDispose { releaseKeyboardBar(field) } }
    }
