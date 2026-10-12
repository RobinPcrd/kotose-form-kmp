/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.fields

/**
 * How a single selection shows its options. Each renderer maps a value to its own platform
 * control; an `iosPresentation` set next to `presentation` replaces it on iOS.
 */
enum class SelectionPresentation {
    /** Every option is a row in the form. */
    Inline,

    /** One row that opens a menu anchored to it. */
    Menu,

    /** One row that opens the options in a bottom sheet. */
    Sheet,

    /** One row that opens the options on a separate page. */
    Page,

    /** A segmented control, for a few short options; tapping the selected segment keeps it. */
    Segmented,
}
