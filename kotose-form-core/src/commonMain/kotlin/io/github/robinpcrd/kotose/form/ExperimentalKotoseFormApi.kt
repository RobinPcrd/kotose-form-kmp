/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form

/**
 * Marks an API that is available for evaluation but is not covered by the
 * library's source or behavioral compatibility guarantees yet.
 */
@RequiresOptIn(
    message = "This Kotose Form API is experimental and may change before it becomes stable.",
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
annotation class ExperimentalKotoseFormApi
