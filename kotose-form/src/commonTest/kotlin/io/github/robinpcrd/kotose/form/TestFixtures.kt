/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form

import io.github.robinpcrd.kotose.form.compose.data.Country
import io.github.robinpcrd.kotose.form.compose.data.CountryRegion
import io.github.robinpcrd.kotose.resources.StrRes

internal enum class YesNo(val tag: String) {
    YES("yes"),
    NO("no"),
}

internal val belgium = Country(region = CountryRegion.Belgium.region, code = 32)

internal object NoLetterAWarning : ValidationError() {
    override val asWarning: Boolean = true
    override val error: StrRes = StrRes(text = "Text without A")
}

internal object NoLetterARule : ValidationRule.Field<String>() {
    override fun isValid(value: String?): ValidationError? =
        if (value == null || value.contains("a", ignoreCase = true)) NoLetterAWarning else null
}
