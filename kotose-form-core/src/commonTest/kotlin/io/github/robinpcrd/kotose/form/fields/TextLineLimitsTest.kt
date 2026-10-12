/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.fields

import io.github.robinpcrd.kotose.form.dsl.form
import io.github.robinpcrd.kotose.form.dsl.group
import io.github.robinpcrd.kotose.form.dsl.textField
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TextLineLimitsTest {
    @Test
    fun textFieldsAreSingleLineUnlessConfigured() {
        lateinit var name: TextField
        lateinit var notes: TextField
        form {
            +group {
                name = textField("name")
                +name
                notes = textField("notes", lineLimits = TextLineLimits.MultiLine(minLines = 3, maxLines = 6))
                +notes
            }
        }
        assertEquals(TextLineLimits.SingleLine, name.lineLimits)
        assertEquals(TextLineLimits.MultiLine(3, 6), notes.lineLimits)
        assertEquals(Int.MAX_VALUE, TextLineLimits.MultiLine().maxLines)
    }

    @Test
    fun multiLineBoundsAreValidated() {
        assertFailsWith<IllegalArgumentException> { TextLineLimits.MultiLine(minLines = 0) }
        assertFailsWith<IllegalArgumentException> { TextLineLimits.MultiLine(minLines = 4, maxLines = 3) }
    }
}
