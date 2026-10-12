/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.fields

/** The date picker of both iOS renderers. Android always shows its Material dialog. */
enum class IosDatePickerMode {
    /** A pill showing the value; tapping it opens the system calendar. */
    Compact,

    /** Wheels in a panel at the bottom of the screen, applied with Done. */
    Wheels,
}
