/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.utils

import com.google.i18n.phonenumbers.PhoneNumberUtil
import io.github.robinpcrd.kotose.form.ExperimentalKotoseFormApi
import io.github.robinpcrd.kotose.form.compose.data.CountryRegion

internal actual fun platformPhoneNumberBackend(): PhoneNumberBackend = AndroidPhoneNumberBackend

@OptIn(ExperimentalKotoseFormApi::class)
private object AndroidPhoneNumberBackend : PhoneNumberBackend {
    private val instance = PhoneNumberUtil.getInstance()

    override fun isPossibleNumber(number: String, region: String): Boolean {
        return try {
            instance.isPossibleNumber(number, region)
        } catch (e: Exception) {
            false
        }
    }

    override fun countryCode(region: String): Int {
        return instance.getCountryCodeForRegion(region)
    }

    override fun parse(phone: String, defaultRegion: String): PhoneParseResult? {
        return try {
            val phoneNumber = instance.parse(phone, defaultRegion)
            val region = instance.getRegionCodeForNumber(phoneNumber)
            val code = instance.getCountryCodeForRegion(region)
            PhoneParseResult(
                nationalNumber = phoneNumber.nationalNumber.toString(),
                region = region,
                countryCode = code
            )
        } catch (e: Exception) {
            null
        }
    }

    override fun parsePartial(phone: String, defaultRegion: String): PhonePartialResult? {
        val trimmedPhone = phone.trimStart()
        if (!trimmedPhone.startsWith("+")) {
            return null
        }

        val digits = trimmedPhone.filter(Char::isDigit)
        for (codeLength in 1..minOf(digits.length, MAX_COUNTRY_CODE_LENGTH)) {
            val code = digits.take(codeLength).toIntOrNull() ?: continue
            val region = instance.getRegionCodeForCountryCode(code)
            if (CountryRegion.getCountryRegion(region) != CountryRegion.Unknown) {
                return PhonePartialResult(
                    nationalNumber = digits.drop(codeLength),
                    region = region,
                    countryCode = code,
                )
            }
        }

        return null
    }

    override fun formatPartial(number: String, region: String): String {
        // The as-you-type formatter follows the main region of the dial code, like the country picker.
        val code = instance.getCountryCodeForRegion(region)
        val prefix = "+$code"
        val formatter = instance.getAsYouTypeFormatter(instance.getRegionCodeForCountryCode(code))
        prefix.forEach(formatter::inputDigit)
        var formatted = ""
        for (char in number) {
            formatted = formatter.inputDigit(char)
        }
        return formatted.removePrefix(prefix).trimStart()
    }

    private const val MAX_COUNTRY_CODE_LENGTH = 3
}
