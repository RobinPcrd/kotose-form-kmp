/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.compose

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FormScreenTest {
    @Test
    fun rowUnderTheBottomContentPaddingIsNotInView() {
        // Viewport of 1000 with a keyboard-sized bottom padding of 400: rows end at 600.
        val end = 1000 - 400
        assertTrue(isRowInView(offset = 500, size = 100, start = 0, end = end))
        assertFalse(isRowInView(offset = 550, size = 100, start = 0, end = end))
        assertFalse(isRowInView(offset = -10, size = 100, start = 0, end = end))
    }
}
