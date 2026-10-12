/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.utils

import android.util.Log

actual object KFLogger {
    actual fun e(tag: String, message: String) {
        Log.e(tag, message)
    }

    actual fun wtf(tag: String, message: String) {
        Log.wtf(tag, message)
    }

    actual fun isDebug(): Boolean = false
}
