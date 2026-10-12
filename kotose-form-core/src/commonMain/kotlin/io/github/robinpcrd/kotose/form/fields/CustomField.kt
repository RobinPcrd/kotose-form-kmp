/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.fields

import io.github.robinpcrd.kotose.form.ExperimentalKotoseFormApi
import io.github.robinpcrd.kotose.resources.StrRes

/**
 * Marks a consumer-defined [io.github.robinpcrd.kotose.form.Field] that renderers must hand to
 * the application's custom renderer. The typed value, validation and conditions stay on the
 * field itself. Renderers dispatch on [kind]; the native adapter also exposes the live field
 * so a custom SwiftUI view can cast it to the consumer's own class.
 */
@ExperimentalKotoseFormApi
interface CustomField {
    /** Stable identifier the renderer dispatches on, for example "rating". */
    val kind: String

    /** Optional display label resolved by renderers. */
    val label: StrRes? get() = null
}
