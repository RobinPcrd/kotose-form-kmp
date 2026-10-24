/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.compose.fields

import io.github.robinpcrd.kotose.form.Condition
import io.github.robinpcrd.kotose.form.ExperimentalKotoseFormApi
import io.github.robinpcrd.kotose.form.dsl.form
import io.github.robinpcrd.kotose.form.dsl.group
import io.github.robinpcrd.kotose.form.fields.PhoneCountryPicker
import io.github.robinpcrd.kotose.form.fields.PhoneField
import io.github.robinpcrd.kotose.form.utils.PhoneNumberBackend
import io.github.robinpcrd.kotose.form.utils.PhoneParseResult
import io.github.robinpcrd.kotose.form.utils.PhonePartialResult
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PhoneCountryPickerTest {
    @AfterTest
    fun reset() {
        PhoneNumberBackend.current = null
    }

    @Test
    fun aCountryIsPickedOnlyOnAnEnabledFieldWithABackendAndSelectionOn() {
        val field = PhoneField("phone")
        form { +group { +field } }
        assertFalse(field.canPickCountry)
        PhoneNumberBackend.current = FakeBackend
        assertTrue(field.canPickCountry)
        field.countryPicker = PhoneCountryPicker.Disabled
        assertFalse(field.canPickCountry)
        field.countryPicker = PhoneCountryPicker.BuiltIn()
        field.isEnabledCondition = Condition.False
        assertFalse(field.canPickCountry)
    }
}

@OptIn(ExperimentalKotoseFormApi::class)
private object FakeBackend : PhoneNumberBackend {
    override fun countryCode(region: String) = 0
    override fun isPossibleNumber(number: String, region: String) = false
    override fun parse(phone: String, defaultRegion: String): PhoneParseResult? = null
    override fun parsePartial(phone: String, defaultRegion: String): PhonePartialResult? = null
    override fun formatPartial(number: String, region: String) = number
}
