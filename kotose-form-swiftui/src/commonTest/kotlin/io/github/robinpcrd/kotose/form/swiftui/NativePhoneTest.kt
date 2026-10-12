/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

@file:OptIn(ExperimentalKotoseSwiftUIApi::class)

package io.github.robinpcrd.kotose.form.swiftui

import io.github.robinpcrd.kotose.form.Condition
import io.github.robinpcrd.kotose.form.ExperimentalKotoseFormApi
import io.github.robinpcrd.kotose.form.dsl.form
import io.github.robinpcrd.kotose.form.dsl.group
import io.github.robinpcrd.kotose.form.fields.PhoneNumberField
import io.github.robinpcrd.kotose.form.utils.PhoneNumberBackend
import io.github.robinpcrd.kotose.form.utils.PhoneParseResult
import io.github.robinpcrd.kotose.form.utils.PhonePartialResult
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class NativePhoneTest {
    @AfterTest
    fun reset() {
        PhoneNumberBackend.current = null
    }

    @Test
    fun adaptersSharePhoneEditsAndRejectUnavailableCountryActions() = runBlocking {
        PhoneNumberBackend.current = FakeBackend
        val field = PhoneNumberField("phone").apply { setOfAllowedRegion = persistentSetOf("FR", "BE") }
        val form = form { +group { +field } }
        val native = NativeFormAdapter(form)
        assertTrue(native.setText("phone", "+33612345678"))
        val snapshot = NativeFormAdapter(form).snapshots.first().fields.single()
        assertEquals(NativeFieldKind.PHONE, snapshot.kind)
        assertEquals("612345678", snapshot.textValue)
        assertEquals("FR", snapshot.phone?.region)
        assertEquals("+33", snapshot.phone?.dialCode)
        assertEquals("\uD83C\uDDEB\uD83C\uDDF7", snapshot.phone?.flag)
        assertEquals(true, snapshot.phone?.hasCountryPicker)
        assertNull(native.setPhoneDialCode("phone", "44"))
        assertFalse(native.selectPhoneCountry("phone", "US"))
        assertTrue(native.selectPhoneCountry("phone", "BE"))
        assertEquals("612345678", field.value?.phone)
        field.isCountrySelectionEnabled = false
        assertFalse(native.selectPhoneCountry("phone", "FR"))
        field.isEnabledCondition = Condition.False
        assertFalse(native.setText("phone", "123"))
        field.isEnabledCondition = Condition.True
        field.isVisibleCondition = Condition.False
        assertFalse(native.setText("phone", "123"))
        assertEquals("+32612345678", form.allValues(includeHidden = true)["phone"])
        assertFalse(form.allValues().containsKey("phone"))
    }

    @Test
    fun rowsWithoutBackendTakeTheTypedDialCode() = runBlocking {
        val field = PhoneNumberField("phone")
        val form = form { +group { +field } }
        val native = NativeFormAdapter(form)
        assertEquals("33", native.setPhoneDialCode("phone", "033"))
        assertEquals("331", native.setPhoneDialCode("phone", "3312"))
        assertEquals("33", native.setPhoneDialCode("phone", "33"))
        assertTrue(native.setText("phone", "06 12 34 56 78"))
        val phone = native.snapshots.first().fields.single().phone
        assertEquals(false, phone?.hasCountryPicker)
        assertEquals("33", phone?.dialCodeDigits)
        assertEquals(emptyList(), phone?.countries)
        assertFalse(native.selectPhoneCountry("phone", "FR"))
        assertEquals("+330612345678", form.allValues()["phone"])
        PhoneNumberBackend.current = FakeBackend
        assertEquals(true, native.snapshots.first().fields.single().phone?.hasCountryPicker)
    }
}

@OptIn(ExperimentalKotoseFormApi::class)
private object FakeBackend : PhoneNumberBackend {
    private val codes = mapOf("FR" to 33, "BE" to 32, "US" to 1)

    override fun countryCode(region: String) = codes[region] ?: 0
    override fun isPossibleNumber(number: String, region: String) = true
    override fun parse(phone: String, defaultRegion: String): PhoneParseResult? {
        val digits = phone.filter(Char::isDigit)
        val (region, code) = codes.entries.firstOrNull { phone.startsWith("+") && digits.startsWith(it.value.toString()) }
            ?.toPair() ?: return null
        return PhoneParseResult(digits.removePrefix(code.toString()), region, code)
    }
    override fun parsePartial(phone: String, defaultRegion: String): PhonePartialResult? = null
    override fun formatPartial(number: String, region: String) = number
}
