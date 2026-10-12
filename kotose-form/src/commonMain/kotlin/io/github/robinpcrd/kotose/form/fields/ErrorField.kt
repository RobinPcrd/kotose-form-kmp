/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.fields

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.robinpcrd.kotose.form.Field
import io.github.robinpcrd.kotose.form.compose.ComposeField
import io.github.robinpcrd.kotose.form.compose.LocalKotosePaddings
import io.github.robinpcrd.kotose.form.compose.fields.validation.ValidationErrorField
import io.github.robinpcrd.kotose.form.utils.ComposableFun
import io.github.robinpcrd.kotose.resources.StrRes
import io.github.robinpcrd.kotose.resources.getStringOrEmpty

open class ErrorField(
    tag: String? = null,
    defaultValue: StrRes? = null,
    asWarning: Boolean = false,
) : Field<StrRes>(tag), ComposeField {
    override var view: ComposableFun = @Composable {
        ValidationErrorField(
            error = this.value.getStringOrEmpty(),
            asWarning = asWarning,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = LocalKotosePaddings.current.defaultPadding,
                    end = LocalKotosePaddings.current.defaultPadding,
                    bottom = LocalKotosePaddings.current.tinyPadding,
                )
        )
    }

    init {
        this.value = defaultValue
    }

    override fun setBaseValue(value: Any?) = Unit
}
