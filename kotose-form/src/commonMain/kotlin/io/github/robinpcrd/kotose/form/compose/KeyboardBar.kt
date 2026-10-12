/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.robinpcrd.kotose.form.FocusableField
import io.github.robinpcrd.kotose.form.Form
import io.github.robinpcrd.kotose_form.generated.resources.Res
import io.github.robinpcrd.kotose_form.generated.resources.keyboard_done
import io.github.robinpcrd.kotose_form.generated.resources.keyboard_next
import io.github.robinpcrd.kotose_form.generated.resources.keyboard_previous
import io.github.robinpcrd.kotose.resources.StrRes

// region Keyboard bar

/** Titles of the iOS keyboard bar buttons, resolved in composition. */
internal class KeyboardBarLabels(val previous: String, val next: String, val done: String)

@Composable
internal fun keyboardBarLabels() = KeyboardBarLabels(
    previous = StrRes(Res.string.keyboard_previous).getStringOrEmpty(),
    next = StrRes(Res.string.keyboard_next).getStringOrEmpty(),
    done = StrRes(Res.string.keyboard_done).getStringOrEmpty(),
)

/**
 * The iOS keyboard bar of the focused input. Previous and Next move between the two inputs of a
 * phone row with a typed dial code, otherwise through the form in Kotlin's order; Done stops
 * editing. [id] prefixes the buttons' test identifiers, like the SwiftUI bar.
 */
internal class KeyboardBarContext(
    val form: Form,
    val field: FocusableField<*>,
    val id: String,
    val labels: KeyboardBarLabels,
    val done: () -> Unit,
    private val previousInput: (() -> Unit)? = null,
    private val nextInput: (() -> Unit)? = null,
) {
    val hasPrevious: Boolean get() = previousInput != null || form.previousFocusableField(this.field) != null
    val hasNext: Boolean get() = nextInput != null || form.nextFocusableField(this.field) != null

    fun previous() {
        val input = previousInput
        if (input != null) input() else form.previousFocusableField(field)?.let { enter(it, forward = false) }
    }

    fun next() {
        val input = nextInput
        if (input != null) input() else form.nextFocusableField(field)?.let { enter(it, forward = true) }
    }

    private fun enter(target: FocusableField<*>, forward: Boolean) {
        KeyboardBarEntry.forward = forward
        form.setFocus(target)
    }
}

/** How the keyboard bar entered the field taking focus; null once an input has it. */
internal object KeyboardBarEntry {
    var forward: Boolean? by mutableStateOf(null)
}

/**
 * Whether form focus lands on the number of a phone row rather than on its typed [dialCode]
 * (null with a country picker). The keyboard bar enters the row on the dial code going forward
 * and on the number going back; other focus starts on the dial code while it is empty.
 */
internal fun focusesNumber(dialCode: String?, forward: Boolean?): Boolean =
    dialCode == null || (forward?.not() ?: dialCode.isNotEmpty())

/** Called when an input takes focus: the bar now acts for it. */
internal fun focusKeyboardBar(context: KeyboardBarContext) {
    KeyboardBarEntry.forward = null
    showKeyboardBar(context)
}

// endregion
