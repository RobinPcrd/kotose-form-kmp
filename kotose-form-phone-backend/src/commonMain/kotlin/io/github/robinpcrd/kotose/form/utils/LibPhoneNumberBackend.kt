/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.utils

import io.github.robinpcrd.kotose.form.ExperimentalKotoseFormApi

/**
 * Phone number metadata from libphonenumber on Android and PhoneNumberKit, a Swift port of it,
 * on iOS. Set it once at startup: `PhoneNumberBackend.current = LibPhoneNumberBackend`.
 */
@OptIn(ExperimentalKotoseFormApi::class)
object LibPhoneNumberBackend : PhoneNumberBackend by platformPhoneNumberBackend()

internal expect fun platformPhoneNumberBackend(): PhoneNumberBackend
