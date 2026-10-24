/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.compose

import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.ui.graphics.Color
import kotlin.test.Test
import kotlin.test.assertEquals

class FieldColorsTest {
    @Test
    fun aWarningKeepsTheCallerColorsAndOnlyRecolorsTheErrorState() {
        assertEquals(colors(errorState = Color.Yellow), colors(errorState = Color.Red).withWarning(Color.Yellow))
    }

    /** Distinct colors everywhere, so a swapped or dropped token fails the comparison. */
    private fun colors(errorState: Color) = FieldColors(
        textColor = Color(0xFF000001),
        disabledTextColor = Color(0xFF000002),
        containerColor = Color(0xFF000003),
        cursorColor = Color(0xFF000004),
        errorCursorColor = errorState,
        textSelectionColors = TextSelectionColors(Color(0xFF000005), Color(0xFF000006)),
        focusedIndicatorColor = Color(0xFF000007),
        unfocusedIndicatorColor = Color(0xFF000008),
        errorIndicatorColor = errorState,
        disabledIndicatorColor = Color(0xFF000009),
        focusedLeadingIconColor = Color(0xFF00000A),
        unfocusedLeadingIconColor = Color(0xFF00000B),
        disabledLeadingIconColor = Color(0xFF00000C),
        errorLeadingIconColor = Color(0xFF00000D),
        focusedTrailingIconColor = Color(0xFF00000E),
        unfocusedTrailingIconColor = Color(0xFF00000F),
        disabledTrailingIconColor = Color(0xFF000010),
        errorTrailingIconColor = errorState,
        focusedLabelColor = Color(0xFF000011),
        unfocusedLabelColor = Color(0xFF000012),
        disabledLabelColor = Color(0xFF000013),
        errorLabelColor = errorState,
        placeholderColor = Color(0xFF000014),
        disabledPlaceholderColor = Color(0xFF000015),
    )
}
