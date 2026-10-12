/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.fields

import io.github.robinpcrd.kotose.form.IsValidPhoneRule
import io.github.robinpcrd.kotose.form.RequiredFieldRule
import io.github.robinpcrd.kotose.form.ValidationError
import io.github.robinpcrd.kotose.form.utils.LibPhoneNumberBackend
import io.github.robinpcrd.kotose.form.utils.PhoneNumberBackend
import io.github.robinpcrd.kotose.form.compose.data.CountryRegion
import io.github.robinpcrd.kotose.form.dsl.form
import io.github.robinpcrd.kotose.form.dsl.group
import kotlinx.collections.immutable.persistentSetOf
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PhoneNumberFieldTest {
    @BeforeTest
    fun useLibraries() {
        PhoneNumberBackend.current = LibPhoneNumberBackend
    }

    @AfterTest
    fun reset() {
        PhoneNumberBackend.current = null
    }

    @Test
    fun possibilityChecksAgreeOnNationalInternationalAndBorderlineNumbers() {
        val cases = listOf(
            Triple("612345678", "BE", true),
            Triple("612345678", "FR", true),
            Triple("0612345678", "FR", true),
            Triple("+33612345678", "BE", true),
            Triple("6502530000", "US", true),
            Triple("7400123456", "GB", true),
            Triple("123", "FR", false),
            Triple("abc", "FR", false),
            Triple("", "BE", false),
        )
        for ((number, region, expected) in cases) {
            assertEquals(expected, LibPhoneNumberBackend.isPossibleNumber(number, region), "$region: $number")
        }
    }

    @Test
    fun internationalPasteAndCountryChangesPreserveSharedValue() {
        val field = PhoneNumberField("phone")
        field.selectCountry(CountryRegion.Belgium)
        field.setPhone("+33 6 12 34 56 78")
        assertEquals("FR", field.value?.country?.region)
        assertEquals("612345678", field.value?.phone)
        assertEquals("+33612345678", field.submittedValue)
        field.selectCountry(CountryRegion.Belgium)
        assertEquals("612345678", field.value?.phone)
        assertEquals("+32612345678", field.submittedValue)
    }

    @Test
    fun partialNumbersAndAllowedCountriesKeepExistingParsingContract() {
        val field = PhoneNumberField()
        field.selectCountry(CountryRegion.Belgium)
        field.setPhone("+3")
        assertEquals("BE", field.value?.country?.region)
        assertEquals("+3", field.value?.phone)
        field.setPhone("+33")
        assertEquals("FR", field.value?.country?.region)
        assertEquals("", field.value?.phone)
        field.setOfAllowedRegion = persistentSetOf("FR", "BE")
        field.selectCountry(CountryRegion.UnitedStatesOfAmerica)
        assertEquals("FR", field.value?.country?.region)
        assertEquals(listOf("FR"), field.selectableCountries("+33").map { it.region })
        field.setOfAllowedRegion = persistentSetOf()
        assertEquals("ZZ", field.displayCountry.region)
        assertTrue(field.selectableCountries().isEmpty())
    }

    @Test
    fun validationAndPresetsUseTheSharedForm() {
        val field = PhoneNumberField("phone").apply {
            addRule(RequiredFieldRule())
            addRule(IsValidPhoneRule)
        }
        val form = form { +group { +field } }
        assertTrue(form.validate().isNotEmpty())
        field.selectCountry(CountryRegion.France)
        field.setPhone("abc")
        assertEquals(ValidationError.PhoneIsValid, IsValidPhoneRule.isValid(field.value))
        assertTrue(form.validate().isEmpty()) // Existing phone validity is a warning, not a submission blocker.
        form.setValues(mapOf("phone" to "+33612345678"))
        assertEquals(emptyList(), form.validate())
        assertEquals("+33612345678", form.allValues()["phone"])
        form.setValues(mapOf("phone" to null))
        assertEquals(null, field.value)
    }
}
