/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.utils

internal inline fun <reified T> tryCast(obj: Any?, block: T.() -> Unit) {
    if (obj is T) {
        block(obj)
    }
}
