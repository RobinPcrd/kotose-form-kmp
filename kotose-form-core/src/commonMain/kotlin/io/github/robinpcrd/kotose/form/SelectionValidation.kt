/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form

import io.github.robinpcrd.kotose.form.groups.SelectionGroup

/**
 * Exactly one visible option of the group must be selected. A selection kept on a
 * hidden option does not count, because it is neither shown nor submitted by default.
 *
 * @property group A ref to the group
 */
class RequiredSingleSelectionGroupRule(
    private val group: SelectionGroup<*>
) : ValidationRule.Group(ValidationError.RequiredSelectableSection) {
    override fun isValid(): ValidationError? {
        val isValid = group.selectableFields.count { field -> field.isVisible && field.value != null } == 1
        return if (isValid) null else error
    }
}
