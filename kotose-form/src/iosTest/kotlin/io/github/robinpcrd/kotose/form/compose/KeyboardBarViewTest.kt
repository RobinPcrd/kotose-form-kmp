/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.compose

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runComposeUiTest
import io.github.robinpcrd.kotose.form.Condition
import io.github.robinpcrd.kotose.form.Form
import io.github.robinpcrd.kotose.form.dsl.form
import io.github.robinpcrd.kotose.form.dsl.group
import io.github.robinpcrd.kotose.form.dsl.toggleField
import io.github.robinpcrd.kotose.form.fields.TextField
import io.github.robinpcrd.kotose.resources.StrRes
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.ref.WeakReference
import kotlin.native.runtime.GC
import kotlin.native.runtime.NativeRuntimeApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import platform.Foundation.valueForKey
import platform.UIKit.UIBarButtonItem

@OptIn(ExperimentalTestApi::class, ExperimentalNativeApi::class, NativeRuntimeApi::class)
class KeyboardBarViewTest {
    @Test
    fun previousFollowsAConditionChangeWhileTheInputKeepsFocus() {
        val dependent = TextField("dependent").apply {
            isEnabledCondition = Condition("enable") { it.fieldValue<Boolean>("enable") == true }
        }
        val probe = TextField("probe")
        val form = form {
            +group {
                +toggleField("enable", StrRes(text = "Enable"))
                +dependent
                +probe
            }
        }
        val labels = KeyboardBarLabels("Previous", "Next", "Done")
        KeyboardBar.show(KeyboardBarContext(form, probe, "probe", labels, done = {}))
        val previous = KeyboardBar.view.items?.firstOrNull() as? UIBarButtonItem
        assertEquals(false, previous?.enabled)

        form.setValues(mapOf("enable" to true))
        Snapshot.sendApplyNotifications()

        assertTrue(dependent.isEnabled)
        assertEquals(true, previous?.enabled)
    }

    @Test
    fun aClosedFormScreenIsReleased() = runComposeUiTest {
        val shown = mutableStateOf(true)
        val form = showForm(shown, focus = false)

        closeAndCollect(shown)

        assertNull(form.value)
    }

    @Test
    fun aClosedFormScreenIsReleasedAfterTheKeyboardBarServedItsInput() = runComposeUiTest {
        val shown = mutableStateOf(true)
        val form = showForm(shown, focus = true)
        assertEquals("released-name-done", barDoneId())

        closeAndCollect(shown)

        assertNull(form.value)
    }

    private class Screen(var form: Form?)

    private val screen = Screen(null)

    /** Shows a form only [screen] references, so a weak reference tells whether anything else keeps it. */
    private fun ComposeUiTest.showForm(shown: MutableState<Boolean>, focus: Boolean): WeakReference<Form> {
        val name = TextField("released-name", StrRes(text = "Name"))
        val form = form { +group { +name } }
        screen.form = form
        setContent { if (shown.value) screen.form?.let { FormScreen(form = it) } }
        waitForIdle()
        if (focus) {
            form.setFocus(name)
            // A focused input's blinking cursor keeps the test clock from going idle.
            waitUntil { barDoneId() == "released-name-done" }
        }
        return WeakReference(form)
    }

    private fun barDoneId() =
        (KeyboardBar.view.items?.lastOrNull() as? UIBarButtonItem)?.valueForKey("accessibilityIdentifier")

    private fun ComposeUiTest.closeAndCollect(shown: MutableState<Boolean>) {
        shown.value = false
        screen.form = null
        waitForIdle()
        GC.collect()
    }
}
