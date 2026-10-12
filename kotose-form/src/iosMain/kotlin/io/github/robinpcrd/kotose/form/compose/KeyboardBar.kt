/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.compose

import androidx.compose.runtime.snapshotFlow
import io.github.robinpcrd.kotose.form.FocusableField
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCAction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSSelectorFromString
import platform.Foundation.setValue
import platform.Foundation.valueForKey
import platform.UIKit.UIBarButtonItem
import platform.UIKit.UIBarButtonItemStyle
import platform.UIKit.UIBarButtonSystemItem
import platform.UIKit.UIImage
import platform.UIKit.UIToolbar
import platform.UIKit.accessibilityLabel
import platform.darwin.NSObject

// region Keyboard bar

/**
 * Previous, Next and Done above the keyboard of every Compose text input. One bar serves every
 * input, so UIKit keeps it in place when focus moves; the system draws it, with Liquid Glass
 * buttons from iOS 26.
 */
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
internal object KeyboardBar {
    private var context: KeyboardBarContext? = null
    // Immediate: on the main thread, show applies the first state before it returns.
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var moves: Job? = null
    private val target = Target()
    private val previousItem by lazy { symbolItem("chevron.up", "previousTapped") }
    private val nextItem by lazy { symbolItem("chevron.down", "nextTapped") }
    private val doneItem by lazy {
        UIBarButtonItem(
            title = null,
            style = UIBarButtonItemStyle.UIBarButtonItemStyleDone,
            target = target,
            action = NSSelectorFromString("doneTapped"),
        )
    }

    val view: UIToolbar by lazy {
        UIToolbar(frame = CGRectMake(0.0, 0.0, 0.0, 44.0)).apply {
            items = listOf(
                previousItem,
                nextItem,
                UIBarButtonItem(
                    barButtonSystemItem = UIBarButtonSystemItem.UIBarButtonSystemItemFlexibleSpace,
                    target = null,
                    action = null,
                ),
                doneItem,
            )
            sizeToFit()
        }
    }

    fun show(context: KeyboardBarContext) {
        this.context = context
        // Assign only what changed: rewriting the items of a visible accessory relayouts the keyboard.
        if (doneItem.title != context.labels.done) doneItem.title = context.labels.done
        set(doneItem, true, context.id + "-done", context.labels.done)
        // Conditions can enable, show or hide the inputs around this one while it keeps focus.
        moves?.cancel()
        moves = scope.launch {
            snapshotFlow { context.hasPrevious to context.hasNext }.collect { (hasPrevious, hasNext) ->
                set(previousItem, hasPrevious, context.id + "-previous", context.labels.previous)
                set(nextItem, hasNext, context.id + "-next", context.labels.next)
            }
        }
    }

    fun release(field: FocusableField<*>) {
        if (context?.field !== field) return
        context = null
        moves?.cancel()
        moves = null
    }

    private fun set(item: UIBarButtonItem, enabled: Boolean, id: String, label: String) {
        if (item.enabled != enabled) item.enabled = enabled
        // UIBarItem declares accessibilityIdentifier in a protocol Kotlin does not see on it.
        if (item.valueForKey("accessibilityIdentifier") != id) item.setValue(id, forKey = "accessibilityIdentifier")
        if (item.accessibilityLabel != label) item.accessibilityLabel = label
    }

    private fun symbolItem(symbol: String, action: String) = UIBarButtonItem(
        image = UIImage.systemImageNamed(symbol),
        style = UIBarButtonItemStyle.UIBarButtonItemStylePlain,
        target = target,
        action = NSSelectorFromString(action),
    )

    private class Target : NSObject() {
        @ObjCAction
        fun previousTapped() {
            context?.previous()
        }

        @ObjCAction
        fun nextTapped() {
            context?.next()
        }

        @ObjCAction
        fun doneTapped() {
            context?.done?.invoke()
        }
    }
}

// endregion
