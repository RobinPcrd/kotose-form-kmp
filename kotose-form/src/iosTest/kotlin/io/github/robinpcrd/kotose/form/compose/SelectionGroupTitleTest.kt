/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.compose

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.v2.runComposeUiTest
import io.github.robinpcrd.kotose.form.Condition
import io.github.robinpcrd.kotose.form.dsl.form
import io.github.robinpcrd.kotose.form.dsl.selectionOption
import io.github.robinpcrd.kotose.form.dsl.singleSelectionGroup
import io.github.robinpcrd.kotose.resources.StrRes
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class SelectionGroupTitleTest {
    @Test
    fun groupTitleHeadsTheFirstVisibleOptionOnce() = runComposeUiTest {
        val form = form {
            +singleSelectionGroup<Int> {
                tag = "plan"
                title = StrRes(text = "Plan")
                +selectionOption("hidden", 0, StrRes(text = "Hidden")) { isVisibleCondition = Condition.False }
                +selectionOption("basic", 1, StrRes(text = "Basic"))
                +selectionOption("plus", 2, StrRes(text = "Plus"))
            }
        }
        setContent { FormScreen(form = form) }

        onNodeWithTag("plan-title").assertExists()
        onAllNodesWithText("Plan").assertCountEquals(1)
    }
}
