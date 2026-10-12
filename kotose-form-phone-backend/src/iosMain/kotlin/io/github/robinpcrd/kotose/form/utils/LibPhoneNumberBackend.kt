/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.utils

import io.github.robinpcrd.kotose.form.ExperimentalKotoseFormApi

internal actual fun platformPhoneNumberBackend(): PhoneNumberBackend = PhoneNumberKitBackend

@OptIn(ExperimentalKotoseFormApi::class)
private object PhoneNumberKitBackend : PhoneNumberBackend {
    override fun isPossibleNumber(number: String, region: String): Boolean {
        return PhoneNumberKitBridgeHolder.isPossibleNumber(number, region)
    }

    override fun countryCode(region: String): Int {
        return PhoneNumberKitBridgeHolder.getCountryCodeForRegion(region)
    }

    override fun parse(phone: String, defaultRegion: String): PhoneParseResult? {
        return PhoneNumberKitBridgeHolder.parsePhoneNumber(phone, defaultRegion)
    }

    override fun parsePartial(phone: String, defaultRegion: String): PhonePartialResult? {
        return PhoneNumberKitBridgeHolder.parsePartialPhoneNumber(phone, defaultRegion)
    }

    override fun formatPartial(number: String, region: String): String {
        return PhoneNumberKitBridgeHolder.formatPartial(number, region)
    }
}
