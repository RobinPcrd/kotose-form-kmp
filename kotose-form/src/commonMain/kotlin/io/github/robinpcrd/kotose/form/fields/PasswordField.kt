/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.fields

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import io.github.robinpcrd.kotose.form.compose.ComposeField
import io.github.robinpcrd.kotose.form.compose.fieldPadding
import io.github.robinpcrd.kotose.form.compose.fields.PasswordField
import io.github.robinpcrd.kotose.form.utils.ComposableFun
import io.github.robinpcrd.kotose.resources.StrRes

class PasswordField(
    tag: String? = null,
    override val label: StrRes? = null,
    override val placeholder: StrRes? = label,
    val appearance: @Composable () -> InputFieldAppearance = InputFieldAppearance.DefaultPassword
) : SecureTextField(tag, label, placeholder), ComposeField {
    override var view: ComposableFun = @Composable {
        val appearance by rememberUpdatedState(appearance())

        PasswordField(
            field = this,
            appearance = appearance,
            modifier = Modifier
                .testTag(key)
                .fillMaxWidth()
                .fieldPadding()
        )
    }

    override fun setBaseValue(value: Any?) = super.setBaseValue(value)

    override fun setValue(value: String?) = super.setValue(value)
}
