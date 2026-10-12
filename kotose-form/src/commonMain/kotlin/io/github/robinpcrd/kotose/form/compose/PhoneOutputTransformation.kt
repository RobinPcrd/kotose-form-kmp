/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.compose

import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.compose.foundation.text.input.insert
import androidx.compose.runtime.Stable
import io.github.robinpcrd.kotose.form.compose.data.Country
import io.github.robinpcrd.kotose.form.compose.data.CountryRegion
import io.github.robinpcrd.kotose.form.utils.PhoneNumberBackend

/** Formats the national number as it is typed; without a [PhoneNumberBackend] it shows the digits. */
fun phoneNumberOutputTransformation(country: Country): OutputTransformation =
    PhoneNumberOutputTransformation(country)

@Stable
private data class PhoneNumberOutputTransformation(
    private val country: Country,
) : OutputTransformation {
    override fun TextFieldBuffer.transformOutput() {
        val backend = PhoneNumberBackend.current ?: return
        if (length == 0 || charAt(0) == '+' || country.region == CountryRegion.Unknown.region) {
            return
        }

        val rawNumber = asCharSequence().toString()
        val formatted = backend.formatPartial(rawNumber, country.region)
        // Only separators are inserted, so the caret keeps following the typed digits.
        if (formatted.filter(Char::isDigit) != rawNumber.filter(Char::isDigit)) {
            return
        }
        formatted.forEachIndexed { index, char ->
            if (!char.isDigit() && index < length && charAt(index) != char) {
                insert(index, char.toString())
            }
        }
    }
}
