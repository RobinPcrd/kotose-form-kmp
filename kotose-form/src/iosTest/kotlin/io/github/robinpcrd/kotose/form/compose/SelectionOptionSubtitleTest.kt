/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.compose

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runComposeUiTest
import io.github.robinpcrd.kotose.form.dsl.form
import io.github.robinpcrd.kotose.form.dsl.multipleSelectionGroup
import io.github.robinpcrd.kotose.form.dsl.selectionOption
import io.github.robinpcrd.kotose.form.dsl.singleSelectionGroup
import io.github.robinpcrd.kotose.resources.StrRes
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class SelectionOptionSubtitleTest {
    @Test
    fun singleAndMultipleOptionsShowTheirSubtitle() = runComposeUiTest {
        val form = form {
            +singleSelectionGroup<Int> {
                +selectionOption("plan", 1, StrRes(text = "Basic")) { subtitle = StrRes(text = "1 user") }
            }
            +multipleSelectionGroup<Int> {
                +selectionOption("extra", 10, StrRes(text = "Breakfast")) { subtitle = StrRes(text = "7 to 10 am") }
            }
        }
        setContent { FormScreen(form = form) }

        onNodeWithText("1 user").assertExists()
        onNodeWithText("7 to 10 am").assertExists()
    }
}
