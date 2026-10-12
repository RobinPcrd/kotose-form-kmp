/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.swiftui

/**
 * Marks the native SwiftUI bridge while its snapshots and rendering extension
 * points are still evolving toward the first stable release.
 */
@RequiresOptIn(
    message = "The Kotose Form SwiftUI bridge is experimental and may change before its first stable release.",
    level = RequiresOptIn.Level.WARNING,
)
@Retention(AnnotationRetention.BINARY)
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.CONSTRUCTOR,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.TYPEALIAS,
)
annotation class ExperimentalKotoseSwiftUIApi
