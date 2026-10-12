/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form

import androidx.compose.runtime.Stable
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toImmutableSet

data class Condition(
    val tags: ImmutableSet<String> = persistentSetOf(),
    val condition: (Form) -> Boolean = { true }
) {
    constructor(boolean: Boolean) : this(
        condition = { boolean }
    )

    constructor(vararg tags: String, condition: (Form) -> Boolean) : this(
        tags = tags.asSequence().toImmutableSet(),
        condition = condition,
    )

    companion object {
        val True = Condition { true }
        val False = Condition { false }
    }
}

/**
 * A custom condition that can be attached to an [Element].
 *
 * @param tags The tags that this condition is associated with.
 * @param onEval The function that is invoked when the condition is evaluated.
 */
@Stable
class CustomCondition(
    val tags: ImmutableSet<String> = persistentSetOf(),
    val onEval: (Element<*>, Form) -> Unit,
) {
    /**
     * Constructs a custom condition with the specified tags and evaluation function.
     *
     * @param tags The tags that this condition is associated with.
     * @param onEval The function that is invoked when the condition is evaluated.
     */
    constructor(vararg tags: String, onEval: (Element<*>, Form) -> Unit) : this(
        tags = tags.asSequence().toImmutableSet(),
        onEval = onEval,
    )
}
