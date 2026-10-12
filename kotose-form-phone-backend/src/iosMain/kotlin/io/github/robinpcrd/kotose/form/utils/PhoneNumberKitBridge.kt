/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.utils

import kotlinx.cinterop.ExperimentalForeignApi
import swiftPMImport.io.github.robinpcrd.kotose.form.phone.backend.KotosePhoneNumberKitBridge

@OptIn(ExperimentalForeignApi::class)
internal object PhoneNumberKitBridgeHolder {
    private val instance: KotosePhoneNumberKitBridge by lazy {
        KotosePhoneNumberKitBridge()
    }

    fun isPossibleNumber(number: String, region: String): Boolean {
        return try {
            instance.isPossibleNumber(number, region)
        } catch (ignored: Exception) {
            false
        }
    }

    fun getCountryCodeForRegion(region: String): Int {
        return try {
            instance.countryCodeForRegion(region)
        } catch (ignored: Exception) {
            0
        }
    }

    fun parsePhoneNumber(phone: String, defaultRegion: String): PhoneParseResult? {
        return try {
            instance.parsePhoneNumber(phone, defaultRegion)?.let { result ->
                PhoneParseResult(
                    nationalNumber = result.nationalNumber,
                    region = result.region,
                    countryCode = result.countryCode,
                )
            }
        } catch (ignored: Exception) {
            null
        }
    }

    fun parsePartialPhoneNumber(phone: String, defaultRegion: String): PhonePartialResult? {
        return try {
            instance.parsePartialPhoneNumber(phone, defaultRegion)?.let { result ->
                PhonePartialResult(
                    nationalNumber = result.nationalNumber,
                    region = result.region,
                    countryCode = result.countryCode,
                )
            }
        } catch (ignored: Exception) {
            null
        }
    }

    fun formatPartial(rawNumber: String, defaultRegion: String): String {
        return try {
            instance.formatPartial(
                rawNumber = rawNumber,
                defaultRegion = defaultRegion,
            )
        } catch (ignored: Exception) {
            rawNumber
        }
    }
}
