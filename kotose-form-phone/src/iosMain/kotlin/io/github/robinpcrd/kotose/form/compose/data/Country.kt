/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.compose.data

import io.github.robinpcrd.kotose.form.utils.getDefaultLocaleCountry
import io.github.robinpcrd.kotose.resources.StrRes
import platform.Foundation.NSLocale
import platform.Foundation.NSLocaleCountryCode
import platform.Foundation.currentLocale

actual object CountryConfiguration {
    actual val defaultRegion: CountryRegion =
        CountryRegion.getCountryRegion(getDefaultLocaleCountry())
}

actual fun getCountryNameForRegion(region: String): StrRes {
    val name = NSLocale.currentLocale.displayNameForKey(NSLocaleCountryCode, region) ?: region
    return StrRes(text = name)
}

