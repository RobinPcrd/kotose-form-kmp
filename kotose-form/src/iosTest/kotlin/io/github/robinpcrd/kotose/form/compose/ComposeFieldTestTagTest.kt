/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.compose

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.uikit.LocalUIViewController
import io.github.robinpcrd.kotose.form.dsl.dropDown
import io.github.robinpcrd.kotose.form.dsl.form
import io.github.robinpcrd.kotose.form.dsl.group
import io.github.robinpcrd.kotose.form.dsl.input
import io.github.robinpcrd.kotose.form.dsl.password
import io.github.robinpcrd.kotose.form.dsl.phone
import io.github.robinpcrd.kotose.form.dsl.switch
import io.github.robinpcrd.kotose.form.fields.DropdownOption
import io.github.robinpcrd.kotose.resources.StrRes
import kotlin.test.Test
import platform.UIKit.UIViewController

@OptIn(ExperimentalTestApi::class)
class ComposeFieldTestTagTest {
    @Test
    fun composeFieldViewsExposeTheirKeyAsTestTag() = runComposeUiTest {
        val form = form {
            +group {
                +input("name", StrRes(text = "Name"))
                +switch("hidden", StrRes(text = "Hidden"))
                +password("secret", StrRes(text = "Secret"))
                +phone("phone")
                +dropDown("choice", listOf(DropdownOption(1, StrRes(text = "One"))))
            }
        }
        // The phone field's country picker presenter needs a presenting controller.
        setContent {
            CompositionLocalProvider(LocalUIViewController provides UIViewController()) {
                FormScreen(form = form)
            }
        }

        for (tag in listOf("name", "hidden", "secret", "phone", "choice")) {
            onNodeWithTag(tag).assertExists()
        }
    }
}
