/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.fields

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import io.github.robinpcrd.kotose.form.compose.ComposeField
import io.github.robinpcrd.kotose.form.compose.fieldPadding
import io.github.robinpcrd.kotose.form.compose.fields.DropdownField
import io.github.robinpcrd.kotose.form.utils.ComposableFun
import io.github.robinpcrd.kotose.resources.StrRes

class DropDownField<T>(
    tag: String? = null,
    possibleValues: List<DropdownOption<T>>,
    placeholder: StrRes? = null,
    isClearable: Boolean = false
) : SingleChoiceField<T>(tag, possibleValues, placeholder, isClearable), ComposeField {

    override var view: ComposableFun = @Composable {
        DropdownField(
            field = this,
            modifier = Modifier
                .testTag(key)
                .fillMaxWidth()
                .fieldPadding()
        )
    }

}
