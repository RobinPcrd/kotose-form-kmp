/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.fields

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import io.github.robinpcrd.kotose.form.RequiredValue
import io.github.robinpcrd.kotose.form.FocusableField
import io.github.robinpcrd.kotose.form.compose.ComposeField
import io.github.robinpcrd.kotose.form.compose.data.Country
import io.github.robinpcrd.kotose.form.compose.data.CountryConfiguration
import io.github.robinpcrd.kotose.form.compose.data.CountryRegion
import io.github.robinpcrd.kotose.form.compose.data.UnknownCountry
import io.github.robinpcrd.kotose.form.compose.data.availableCountries
import io.github.robinpcrd.kotose.form.compose.data.getCountryForRegion
import io.github.robinpcrd.kotose.form.compose.fieldPadding
import io.github.robinpcrd.kotose.form.compose.fields.PhoneField
import io.github.robinpcrd.kotose.form.utils.ComposableFun
import io.github.robinpcrd.kotose.form.utils.KFLogger
import io.github.robinpcrd.kotose.resources.StrRes
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.toImmutableList

class PhoneField(
    tag: String? = null,
    override val placeholder: StrRes? = null,
    val appearance: @Composable () -> InputFieldAppearance = InputFieldAppearance.DefaultPhone,
) : PhoneNumberField(tag, placeholder), ComposeField {
    override val submittedValue: Any? get() = super.submittedValue
    override var setOfAllowedRegion: ImmutableSet<String>?
        get() = super.setOfAllowedRegion
        set(value) { super.setOfAllowedRegion = value }

    var countryPicker: PhoneCountryPicker by mutableStateOf(PhoneCountryPicker.BuiltIn())

    override var view: ComposableFun = @Composable {
        val appearance by rememberUpdatedState(appearance())

        PhoneField(
            field = this,
            appearance = appearance,
            modifier = Modifier
                .testTag(key)
                .fillMaxWidth()
                .fieldPadding(),
        )
    }

    override var isCountrySelectionEnabled: Boolean
        get() = countryPicker !is PhoneCountryPicker.Disabled
        set(value) { countryPicker = if (value) PhoneCountryPicker.BuiltIn() else PhoneCountryPicker.Disabled }

    override fun setBaseValue(value: Any?) = super.setBaseValue(value)
    override fun setPhone(phone: String) = super.setPhone(phone)
    override fun selectCountry(region: CountryRegion) = super.selectCountry(region)
    override fun selectCountry(country: Country) = super.selectCountry(country)
    override fun selectableCountries(query: String): ImmutableList<Country> = super.selectableCountries(query)
}
