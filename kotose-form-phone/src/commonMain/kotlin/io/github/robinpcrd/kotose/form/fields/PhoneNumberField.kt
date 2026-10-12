/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.fields

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.robinpcrd.kotose.form.RequiredValue
import io.github.robinpcrd.kotose.form.FocusableField
import io.github.robinpcrd.kotose.form.compose.data.*
import io.github.robinpcrd.kotose.form.utils.PhoneNumberBackend
import io.github.robinpcrd.kotose.resources.StrRes
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.toImmutableList

@Immutable
data class PhoneData(
    val country: Country = getCountryForRegion(CountryConfiguration.defaultRegion.region),
    val phone: String = "",
) : RequiredValue {
    override val isEmptyForRequired: Boolean get() = phone.isBlank()

    /**
     * Returns the phone number in international format, without a dial code when there is none.
     *
     * returns null if the phone number is empty
     */
    val formatted: String? = if (phone.isNotBlank()) "+${country.code.takeIf { it > 0 } ?: ""}$phone" else null

    companion object {
        fun parse(
            phone: String,
            defaultRegion: String = CountryConfiguration.defaultRegion.region,
            setOfAllowedRegion: ImmutableSet<String>? = null,
        ): PhoneData {
            // Without metadata the dial code cannot be told apart from the number.
            val backend = PhoneNumberBackend.current ?: return PhoneData(UnknownCountry, phone.phoneDigits())
            try {
                val parsed = backend.parse(phone, defaultRegion)
                if (parsed != null) {
                    val region = parsed.region.let { region ->
                        if (setOfAllowedRegion == null || region in setOfAllowedRegion) {
                            region
                        } else {
                            CountryRegion.Unknown.region
                        }
                    }
                    val country = Country(region, parsed.countryCode)
                    return PhoneData(country, parsed.nationalNumber)
                }
            } catch (ignored: Exception) {
                //Log.e("PhoneData", null, ignored)
            }

            val partial = backend.parsePartial(phone, defaultRegion)
            if (partial != null) {
                val region = partial.region.let { region ->
                    if (setOfAllowedRegion == null || region in setOfAllowedRegion) {
                        region
                    } else {
                        CountryRegion.Unknown.region
                    }
                }
                val country = Country(region, partial.countryCode)
                return PhoneData(country, partial.nationalNumber)
            }

            return PhoneData(getCountryForRegion(defaultRegion), phone = phone)
        }
    }
}

/**
 * Shared phone state; country-picker presentation belongs to the renderer.
 *
 * With a [PhoneNumberBackend] the country is picked and the number parsed. Without one, the
 * dial code and the number are typed separately: see [setDialCode].
 */
open class PhoneNumberField(
    tag: String? = null,
    open val placeholder: StrRes? = null,
) : FocusableField<PhoneData>(tag) {
    override val submittedValue: Any? get() = value?.formatted
    open var setOfAllowedRegion: ImmutableSet<String>? by mutableStateOf(null)
    open var isCountrySelectionEnabled: Boolean by mutableStateOf(true)

    /** Whether renderers show the country picker, which needs a [PhoneNumberBackend]. */
    val hasCountryPicker: Boolean get() = PhoneNumberBackend.current != null

    /** The digits of the value's dial code, empty when there is none. */
    val dialCode: String get() = value?.country?.code?.takeIf { it > 0 }?.toString().orEmpty()

    val displayCountry: Country
        get() {
            val current = value?.country
            val allowed = setOfAllowedRegion
            val default = CountryConfiguration.defaultRegion.region
            return when {
                allowed == null -> current ?: getCountryForRegion(default)
                current != null && current.region in allowed -> current
                default in allowed -> getCountryForRegion(default)
                else -> UnknownCountry
            }
        }

    override fun setBaseValue(value: Any?) {
        when (value) {
            null -> this.value = null
            is String -> if (hasCountryPicker) setPhone(value) else this.value = PhoneData.parse(value)
            is PhoneData -> this.value = value
            else -> Unit
        }
    }

    /** Parses [phone] with a backend; without one, keeps its digits as the number. */
    open fun setPhone(phone: String) {
        if (!hasCountryPicker) {
            val digits = phone.phoneDigits()
            value = value?.copy(phone = digits) ?: PhoneData(UnknownCountry, digits)
            return
        }
        val defaultRegion = value?.country?.region ?: CountryConfiguration.defaultRegion.region
        val data = PhoneData.parse(
            phone = phone,
            defaultRegion = defaultRegion,
            setOfAllowedRegion = setOfAllowedRegion
        )
        value = value?.copy(country = data.country, phone = data.phone) ?: data
    }

    open fun selectCountry(region: CountryRegion) {
        selectCountry(getCountryForRegion(region))
    }

    /**
     * Sets the typed dial code when there is no [PhoneNumberBackend]: digits only, at most 3,
     * without a leading 0. Does nothing with a backend, where the country sets it.
     */
    fun setDialCode(dialCode: String) {
        if (hasCountryPicker) return
        val code = dialCode.phoneDigits().trimStart('0').take(MAX_DIAL_CODE_LENGTH).toIntOrNull() ?: 0
        val country = Country(CountryRegion.Unknown.region, code)
        value = value?.copy(country = country) ?: PhoneData(country)
    }

    open fun selectCountry(country: Country) {
        if (!hasCountryPicker) return
        if (!isRegionAllowed(country.region) || country.region == UnknownCountry.region) {
            return
        }

        val phone = value?.phone.orEmpty()
        val selectedCountry = getCountryForRegion(country.region)
        value = value?.copy(country = selectedCountry, phone = phone)
            ?: PhoneData(country = selectedCountry, phone = phone)
    }

    open fun selectableCountries(query: String = ""): ImmutableList<Country> {
        val selectedRegion = value?.country?.region
        return availableCountries
            .asSequence()
            .filter { it.region != UnknownCountry.region }
            .filter { isRegionAllowed(it.region) }
            .filter { it.matchesCountryQuery(query) }
            .sortedWith(
                compareBy<Country> { country ->
                    if (country.region == selectedRegion) 0 else 1
                }
                    .thenBy { it.region }
            )
            .toImmutableList()
    }

    private fun isRegionAllowed(region: String): Boolean {
        return setOfAllowedRegion?.contains(region) ?: true
    }
}

private const val MAX_DIAL_CODE_LENGTH = 3

/** Decimal digits of any script, as ASCII. */
private fun String.phoneDigits(): String = buildString {
    for (char in this@phoneDigits) char.digitToIntOrNull()?.let { append('0' + it) }
}

private fun Country.matchesCountryQuery(query: String): Boolean {
    val normalizedQuery = query.trim().lowercase()
    if (normalizedQuery.isBlank()) {
        return true
    }

    val dialCode = "+$code"
    val dialCodeWithoutPrefix = code.toString()
    return region.lowercase().contains(normalizedQuery) ||
        dialCode.contains(normalizedQuery) ||
        dialCodeWithoutPrefix.contains(normalizedQuery.removePrefix("+"))
}
