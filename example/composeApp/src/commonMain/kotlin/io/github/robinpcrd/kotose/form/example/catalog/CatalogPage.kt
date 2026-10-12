/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.example.catalog

import io.github.robinpcrd.kotose.form.Form
import io.github.robinpcrd.kotose.form.ValidationError
import io.github.robinpcrd.kotose.resources.StrRes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** A button in the page action bar. Behavior stays in Kotlin; renderers only call [run]. */
class PageAction(val id: String, val label: StrRes, val run: () -> Unit)

/** Text every page shares: action labels, state captions and the result line. */
class PageLabels(
    val validate: StrRes,
    val reset: StrRes,
    val toggleEnabled: StrRes,
    val empty: StrRes,
    val filled: StrRes,
    val disabled: StrRes,
    val error: StrRes,
    val warning: StrRes,
    val valid: StrRes,
    val errors: (count: Int) -> StrRes,
)

/**
 * One catalog page: a shared form showing a field kind or a behavior in several states, plus
 * the actions and result text both renderers display. Validate is always the first action
 * and the only writer of [result].
 */
class CatalogPage(
    val id: String,
    val section: Section,
    val title: StrRes,
    val form: Form,
    labels: PageLabels,
    actions: List<PageAction> = emptyList(),
) {
    enum class Section { FIELDS, BEHAVIORS }

    val result: StateFlow<StrRes?>
        field = MutableStateFlow(null)

    val actions: List<PageAction> = listOf(
        PageAction("validate", labels.validate) {
            val errors: List<ValidationError> = form.validate()
            result.value = if (errors.isEmpty()) labels.valid else labels.errors(errors.size)
        },
    ) + actions
}
