/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.fields

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import io.github.robinpcrd.kotose.form.compose.ComposeField
import io.github.robinpcrd.kotose.form.compose.FieldColors
import io.github.robinpcrd.kotose.form.compose.FieldDefaults
import io.github.robinpcrd.kotose.form.compose.LocalKotosePaddings
import io.github.robinpcrd.kotose.form.compose.fields.SwitchField
import io.github.robinpcrd.kotose.form.utils.ComposableFun
import io.github.robinpcrd.kotose.form.utils.KFLogger
import io.github.robinpcrd.kotose.resources.StrRes

class SwitchField(
    tag: String? = null,
    defaultLabel: StrRes,
    val appearance: @Composable () -> SwitchFieldAppearance = SwitchFieldAppearance.Default
) : ToggleField(tag, defaultLabel), ComposeField {
    override var view: ComposableFun = @Composable {
        val appearance by rememberUpdatedState(appearance())

        SwitchField(
            field = this,
            appearance = appearance,
            modifier = Modifier
                .testTag(key)
                .fillMaxWidth()
                .padding(horizontal = LocalKotosePaddings.current.defaultPadding)
        )
    }

    override fun setBaseValue(value: Any?) {
        super.setBaseValue(value)
        if (value != null && value !is String && value !is Boolean) {
            KFLogger.e(
                "KF-SwitchField",
                "Invalid value: $value of type ${value::class.simpleName}"
            )
        }
    }
}

@Immutable
data class SwitchFieldAppearance(
    val colors: FieldColors,
    val labelTextStyle: TextStyle,
) {
    companion object {
        @Stable
        val Default: @Composable () -> SwitchFieldAppearance = @Composable {
            SwitchFieldAppearance(FieldDefaults.fieldColors(), LocalTextStyle.current)
        }
    }
}
