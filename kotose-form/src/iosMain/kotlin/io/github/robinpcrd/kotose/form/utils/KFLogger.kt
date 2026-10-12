/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.utils

actual object KFLogger {
    actual fun e(tag: String, message: String) {
        println("E/$tag: $message")
    }

    actual fun wtf(tag: String, message: String) {
        println("WTF/$tag: $message")
    }

    actual fun isDebug(): Boolean = false
}
