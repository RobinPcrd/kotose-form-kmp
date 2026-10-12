/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.utils

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.robinpcrd.kotose.form.ExperimentalKotoseFormApi

data class PhoneParseResult(
    val nationalNumber: String,
    val region: String,
    val countryCode: Int,
)

data class PhonePartialResult(
    val nationalNumber: String,
    val region: String,
    val countryCode: Int,
)

/**
 * Phone number metadata for phone fields: dial codes, parsing, validation and formatting.
 *
 * Without a [current] backend, phone fields take a typed dial code and a number, and
 * `IsValidPhoneRule` only checks that a dial code is present. The `kotose-form-phone-backend`
 * module provides `LibPhoneNumberBackend`.
 */
@SubclassOptInRequired(ExperimentalKotoseFormApi::class)
interface PhoneNumberBackend {
    /** The dial code of [region], or 0 when it has none. */
    fun countryCode(region: String): Int

    fun isPossibleNumber(number: String, region: String): Boolean

    /** A national or international number, or null when it cannot be parsed. */
    fun parse(phone: String, defaultRegion: String): PhoneParseResult?

    /** An international number typed so far, from its dial code on, or null. */
    fun parsePartial(phone: String, defaultRegion: String): PhonePartialResult?

    /** The national [number] formatted as the user types it. */
    fun formatPartial(number: String, region: String): String

    companion object {
        /** The backend used by every phone field. Renderers follow a change. */
        var current: PhoneNumberBackend? by mutableStateOf(null)
    }
}
