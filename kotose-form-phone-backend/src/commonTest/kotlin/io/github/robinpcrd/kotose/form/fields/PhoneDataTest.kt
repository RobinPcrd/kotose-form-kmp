/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.fields

import io.github.robinpcrd.kotose.form.compose.data.CountryRegion
import io.github.robinpcrd.kotose.form.compose.data.getCountryForRegion
import io.github.robinpcrd.kotose.form.utils.LibPhoneNumberBackend
import io.github.robinpcrd.kotose.form.utils.PhoneNumberBackend
import kotlinx.collections.immutable.persistentSetOf
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PhoneDataTest {
    @BeforeTest
    fun useLibraries() {
        PhoneNumberBackend.current = LibPhoneNumberBackend
    }

    @AfterTest
    fun reset() {
        PhoneNumberBackend.current = null
    }

    @Test
    fun parseKeepsDefaultRegionWhenInputCannotBeParsed() {
        val data = PhoneData.parse(
            phone = "abc",
            defaultRegion = CountryRegion.Belgium.region
        )

        assertEquals(CountryRegion.Belgium.region, data.country.region)
        assertEquals("abc", data.phone)
    }

    @Test
    fun parseDetectsInternationalCountryCodeBeforeFullNumberIsValid() {
        val data = PhoneData.parse(
            phone = "+33",
            defaultRegion = CountryRegion.Belgium.region
        )

        assertEquals(CountryRegion.France.region, data.country.region)
        assertEquals("", data.phone)
    }

    @Test
    fun parseDoesNotSwitchCountryBeforeInternationalCountryCodeIsComplete() {
        val data = PhoneData.parse(
            phone = "+3",
            defaultRegion = CountryRegion.Belgium.region
        )

        assertEquals(CountryRegion.Belgium.region, data.country.region)
        assertEquals("+3", data.phone)
    }

    @Test
    fun selectCountryPreservesCurrentNationalNumber() {
        val field = PhoneNumberField()
        field.value = PhoneData(
            country = getCountryForRegion(CountryRegion.Belgium),
            phone = "471234567"
        )

        field.selectCountry(CountryRegion.France)

        assertEquals(CountryRegion.France.region, field.value?.country?.region)
        assertEquals("471234567", field.value?.phone)
    }

    @Test
    fun selectCountryIgnoresDisallowedRegion() {
        val field = PhoneNumberField()
        field.setOfAllowedRegion = persistentSetOf(CountryRegion.Belgium.region)
        field.value = PhoneData(
            country = getCountryForRegion(CountryRegion.Belgium),
            phone = "471234567"
        )

        field.selectCountry(CountryRegion.France)

        assertEquals(CountryRegion.Belgium.region, field.value?.country?.region)
        assertEquals("471234567", field.value?.phone)
    }

    @Test
    fun selectableCountriesExcludeUnknownRegion() {
        val field = PhoneNumberField()

        val countries = field.selectableCountries()

        assertFalse(countries.any { it.region == CountryRegion.Unknown.region })
    }

    @Test
    fun selectableCountriesSearchMatchesDialCode() {
        val field = PhoneNumberField()
        field.setOfAllowedRegion = persistentSetOf(
            CountryRegion.Belgium.region,
            CountryRegion.France.region
        )

        val countries = field.selectableCountries("33")

        assertEquals(1, countries.size)
        assertEquals(CountryRegion.France.region, countries.first().region)
    }

    @Test
    fun selectableCountriesPinsSelectedCountryFirst() {
        val field = PhoneNumberField()
        field.setOfAllowedRegion = persistentSetOf(
            CountryRegion.Andorra.region,
            CountryRegion.Belgium.region,
            CountryRegion.France.region
        )
        field.value = PhoneData(
            country = getCountryForRegion(CountryRegion.France),
            phone = ""
        )

        val countries = field.selectableCountries()

        assertEquals(CountryRegion.France.region, countries.first().region)
    }

    @Test
    fun selectableCountriesKeepsSelectedCountryFirstWhenItMatchesSearch() {
        val field = PhoneNumberField()
        field.setOfAllowedRegion = persistentSetOf(
            CountryRegion.Belgium.region,
            CountryRegion.Belize.region
        )
        field.value = PhoneData(
            country = getCountryForRegion(CountryRegion.Belize),
            phone = ""
        )

        val countries = field.selectableCountries("b")

        assertEquals(CountryRegion.Belize.region, countries.first().region)
        assertEquals(CountryRegion.Belgium.region, countries[1].region)
    }

    @Test
    fun selectableCountriesSearchMatchesRegionCode() {
        val field = PhoneNumberField()
        field.setOfAllowedRegion = persistentSetOf(
            CountryRegion.Belgium.region,
            CountryRegion.France.region
        )

        val countries = field.selectableCountries("be")

        assertTrue(countries.any { it.region == CountryRegion.Belgium.region })
        assertFalse(countries.any { it.region == CountryRegion.France.region })
    }
}
