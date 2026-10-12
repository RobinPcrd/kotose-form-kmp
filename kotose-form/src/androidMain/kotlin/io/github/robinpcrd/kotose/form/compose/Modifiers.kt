/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

@file:JvmName("ModifiersAndroidKt")

package io.github.robinpcrd.kotose.form.compose

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.AutofillNode
import androidx.compose.ui.autofill.AutofillType
import androidx.compose.ui.composed
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalAutofill
import androidx.compose.ui.platform.LocalAutofillTree
import androidx.compose.ui.platform.LocalFocusManager

@Deprecated("Replace using semantics modifier", level = DeprecationLevel.ERROR)
@OptIn(ExperimentalComposeUiApi::class)
fun Modifier.autofill(
    autofillTypes: List<AutofillType>,
    onFill: ((String) -> Unit),
) = composed {
    if (autofillTypes.isEmpty())
        return@composed this
    val autofill = LocalAutofill.current
    val autofillNode = AutofillNode(onFill = onFill, autofillTypes = autofillTypes)
    val tree = LocalAutofillTree.current

    this
        .onGloballyPositioned {
            autofillNode.boundingBox = it.boundsInWindow()
        }
        .onFocusChanged { localFocusState ->
            autofill?.run {
                if (localFocusState.isFocused) {
                    tree.children.values.removeIf { node ->
                        node.autofillTypes.containsAll(autofillTypes) && node.id != autofillNode.id
                    }
                    if (!tree.children.contains(autofillNode.id))
                        tree += autofillNode
                    if (autofillNode.boundingBox != null)
                        requestAutofillForNode(autofillNode)
                } else {
                    cancelAutofillForNode(autofillNode)
                }
            }
        }
        .onKeyEvent {
            autofill?.run {
                cancelAutofillForNode(autofillNode)
                if (autofillNode.boundingBox != null)
                    requestAutofillForNode(autofillNode)
            }
            true
        }
}

@Deprecated(level = DeprecationLevel.WARNING, message = "Not used anymore")
@OptIn(ExperimentalLayoutApi::class)
fun Modifier.clearFormFocusOnKeyboardDismiss(): Modifier = composed {
    var keyboardAppearedSinceLastFocused by remember { mutableStateOf(false) }
    val imeIsVisible = WindowInsets.isImeVisible
    val form = LocalForm.current
    LaunchedEffect(imeIsVisible) {
        if (imeIsVisible) {
            keyboardAppearedSinceLastFocused = true
        } else if (keyboardAppearedSinceLastFocused) {
            form.stopEditing()
        }
    }
    return@composed this
}

@OptIn(ExperimentalLayoutApi::class)
actual fun Modifier.clearFocusOnKeyboardDismiss(): Modifier = composed {
    var isFocused by remember { mutableStateOf(false) }
    var keyboardAppearedSinceLastFocused by remember { mutableStateOf(false) }
    if (isFocused) {
        val imeIsVisible = WindowInsets.isImeVisible
        val focusManager = LocalFocusManager.current
        LaunchedEffect(imeIsVisible) {
            if (imeIsVisible) {
                keyboardAppearedSinceLastFocused = true
            } else if (keyboardAppearedSinceLastFocused) {
                focusManager.clearFocus()
            }
        }
    }
    onFocusEvent {
        if (isFocused != it.isFocused) {
            isFocused = it.isFocused
            if (isFocused) {
                keyboardAppearedSinceLastFocused = false
            }
        }
    }
}
