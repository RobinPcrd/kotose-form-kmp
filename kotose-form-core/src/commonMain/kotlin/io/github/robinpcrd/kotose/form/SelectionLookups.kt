/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form

import io.github.robinpcrd.kotose.form.groups.SelectionGroup

@Suppress("UNCHECKED_CAST")
inline fun <reified ValueType> Form.getSelectionGroupByTag(tag: String): SelectionGroup<ValueType>? =
    getGroupByTag(tag) as? SelectionGroup<ValueType>?

@Suppress("UNCHECKED_CAST")
inline fun <reified ValueType> Form.getSingleSelectionGroupByTag(tag: String): SelectionGroup.Single<ValueType>? =
    getGroupByTag(tag) as? SelectionGroup.Single<ValueType>

@Suppress("UNCHECKED_CAST")
inline fun <reified ValueType> Form.getMultipleSelectionGroupByTag(tag: String): SelectionGroup.Multiple<ValueType>? =
    getGroupByTag(tag) as? SelectionGroup.Multiple<ValueType>
