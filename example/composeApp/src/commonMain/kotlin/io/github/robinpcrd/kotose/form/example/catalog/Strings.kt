/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.example.catalog

import io.github.robinpcrd.kotose.resources.StrRes
import kotlinx.collections.immutable.persistentListOf
import org.jetbrains.compose.resources.StringResource

/** Deferred text: resolved by each renderer at its UI boundary. */
internal fun res(resource: StringResource, vararg args: Any): StrRes =
    StrRes(composeString = resource, formatArgs = persistentListOf(*args))
