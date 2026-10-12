/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.fields

/**
 * How many lines a text input shows. Every renderer applies the same rules.
 *
 * [SingleLine] rejects newlines and uses the return key to move to the next field or finish.
 * [MultiLine] accepts newlines, inserts one on return, and grows from [MultiLine.minLines] to
 * [MultiLine.maxLines] before scrolling its content.
 */
sealed interface TextLineLimits {
    data object SingleLine : TextLineLimits

    data class MultiLine(
        val minLines: Int = 1,
        val maxLines: Int = Int.MAX_VALUE,
    ) : TextLineLimits {
        init {
            require(minLines >= 1) { "minLines must be at least 1" }
            require(maxLines >= minLines) { "maxLines must be at least minLines" }
        }
    }
}
