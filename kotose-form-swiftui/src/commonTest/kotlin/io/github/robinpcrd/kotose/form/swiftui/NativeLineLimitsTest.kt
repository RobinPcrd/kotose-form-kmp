/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.swiftui

import io.github.robinpcrd.kotose.form.dsl.form
import io.github.robinpcrd.kotose.form.dsl.group
import io.github.robinpcrd.kotose.form.fields.SecureTextField
import io.github.robinpcrd.kotose.form.fields.TextField
import io.github.robinpcrd.kotose.form.fields.TextLineLimits
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalKotoseSwiftUIApi::class)
class NativeLineLimitsTest {
    @Test
    fun snapshotsCarryLineLimitsOnlyForMultiLineText() = runBlocking {
        val name = TextField("name")
        val notes = TextField("notes", lineLimits = TextLineLimits.MultiLine(minLines = 3, maxLines = 6))
        val password = SecureTextField("password")
        val form = form { +group { +name; +notes; +password } }
        val adapter = NativeFormAdapter(form)
        val fields = adapter.snapshots.first().fields
        assertNull(fields[0].lines)
        assertEquals(3, fields[1].lines?.minLines)
        assertEquals(6, fields[1].lines?.maxLines)
        assertNull(fields[2].lines)
        assertTrue(adapter.setText("notes", "first\nsecond"))
        assertEquals("first\nsecond", notes.value)
    }
}
