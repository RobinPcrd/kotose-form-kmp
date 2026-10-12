/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.fields

import io.github.robinpcrd.kotose.form.ExperimentalKotoseFormApi
import io.github.robinpcrd.kotose.form.IsValidPhoneRule
import io.github.robinpcrd.kotose.form.RequiredFieldRule
import io.github.robinpcrd.kotose.form.ValidationError
import io.github.robinpcrd.kotose.form.compose.data.Country
import io.github.robinpcrd.kotose.form.compose.data.CountryRegion
import io.github.robinpcrd.kotose.form.compose.data.availableCountries
import io.github.robinpcrd.kotose.form.dsl.form
import io.github.robinpcrd.kotose.form.dsl.group
import io.github.robinpcrd.kotose.form.utils.PhoneNumberBackend
import io.github.robinpcrd.kotose.form.utils.PhoneParseResult
import io.github.robinpcrd.kotose.form.utils.PhonePartialResult
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class BasicPhoneNumberFieldTest {
    @AfterTest
    fun reset() {
        PhoneNumberBackend.current = null
    }

    @Test
    fun dialCodeAndNumberAreTypedSeparatelyAsDigits() {
        val field = PhoneNumberField("phone")
        assertFalse(field.hasCountryPicker)
        field.setDialCode("0+3 3")
        assertEquals("33", field.dialCode)
        field.setDialCode("1234")
        assertEquals("123", field.dialCode)
        field.setDialCode("٣٣")
        assertEquals("33", field.dialCode)
        field.setPhone("06 12-34.56 78 90 12 34")
        assertEquals("0612345678901234", field.value?.phone)
        assertEquals("33", field.dialCode)
        assertEquals("+330612345678901234", field.submittedValue)
        field.setPhone("")
        assertNull(field.submittedValue)
        assertEquals("33", field.dialCode)
    }

    @Test
    fun presetsAreNeverSplitAndSubmitBackUnchanged() {
        val field = PhoneNumberField("phone").apply {
            addRule(RequiredFieldRule())
            addRule(IsValidPhoneRule)
        }
        val form = form { +group { +field } }
        form.setValues(mapOf("phone" to "+33612345678"))
        assertEquals("", field.dialCode)
        assertEquals("33612345678", field.value?.phone)
        assertEquals("+33612345678", form.allValues()["phone"])
        assertEquals(ValidationError.PhoneIsValid, IsValidPhoneRule.isValid(field.value))
        field.setDialCode("33")
        field.setPhone("612345678")
        assertNull(IsValidPhoneRule.isValid(field.value))
        assertEquals("+33612345678", form.allValues()["phone"])
        field.setBaseValue(PhoneData(Country(CountryRegion.Unknown.region, 32), "471234567"))
        assertEquals("32", field.dialCode)
        assertEquals("+32471234567", field.submittedValue)
    }

    @Test
    fun countryPolicyHasNoEffectWithoutBackend() {
        val field = PhoneNumberField("phone")
        field.setDialCode("32")
        field.selectCountry(CountryRegion.France)
        assertEquals("32", field.dialCode)
        assertTrue(field.selectableCountries().isEmpty())
        assertTrue(availableCountries.isEmpty())
    }

    @Test
    fun backendSwitchesTheFieldToCountries() {
        val field = PhoneNumberField("phone")
        PhoneNumberBackend.current = FakeBackend
        assertTrue(field.hasCountryPicker)
        assertEquals(listOf("BE", "FR"), availableCountries.map { it.region }.sorted())
        field.setDialCode("44")
        assertEquals("", field.dialCode)
        field.setPhone("+33612345678")
        assertEquals("FR", field.value?.country?.region)
        assertEquals("33", field.dialCode)
        PhoneNumberBackend.current = null
        assertFalse(field.hasCountryPicker)
        assertTrue(availableCountries.isEmpty())
    }
}

@OptIn(ExperimentalKotoseFormApi::class)
private object FakeBackend : PhoneNumberBackend {
    private val codes = mapOf("FR" to 33, "BE" to 32)

    override fun countryCode(region: String) = codes[region] ?: 0
    override fun isPossibleNumber(number: String, region: String) = number.length == 9
    override fun parse(phone: String, defaultRegion: String): PhoneParseResult? {
        val digits = phone.filter(Char::isDigit)
        val (region, code) = codes.entries.firstOrNull { phone.startsWith("+") && digits.startsWith(it.value.toString()) }
            ?.toPair() ?: return null
        return PhoneParseResult(digits.removePrefix(code.toString()), region, code)
    }
    override fun parsePartial(phone: String, defaultRegion: String): PhonePartialResult? = null
    override fun formatPartial(number: String, region: String) = number
}
