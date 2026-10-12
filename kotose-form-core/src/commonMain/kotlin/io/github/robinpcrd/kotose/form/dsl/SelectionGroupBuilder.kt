/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.dsl

import io.github.robinpcrd.kotose.form.RequiredSingleSelectionGroupRule
import io.github.robinpcrd.kotose.form.ValidationTriggerSource
import io.github.robinpcrd.kotose.form.ensureSingleTags
import io.github.robinpcrd.kotose.form.fields.SelectionPresentation
import io.github.robinpcrd.kotose.form.groups.SelectionGroup
import io.github.robinpcrd.kotose.resources.StrRes

abstract class SelectableGroupBuilder<ValueType> : GroupBuilder() {
    var title: StrRes? = null
    var presentation = SelectionPresentation.Inline
    var iosPresentation: SelectionPresentation? = null
}

class SingleSelectionGroupBuilder<ValueType> :
    SelectableGroupBuilder<ValueType>() {
    var isRequired = false
    var enableDeselection = false

    override fun build(): SelectionGroup.Single<ValueType> {
        val group = SelectionGroup.Single<ValueType>(tag, title, enableDeselection, presentation, iosPresentation)
        if (isRequired) {
            group.addRule(RequiredSingleSelectionGroupRule(group))
        }
        group.isEnabledCondition = isEnabledCondition
        group.isVisibleCondition = isVisibleCondition
        fields.ensureSingleTags()
        group.addFields(fields)
        if (validateOnBuild)
            group.validate(triggerSource = ValidationTriggerSource.FORM, false)
        return group
    }
}

class MultipleSelectionGroupBuilder<ValueType> :
    SelectableGroupBuilder<ValueType>() {
    override fun build(): SelectionGroup.Multiple<ValueType> {
        val group = SelectionGroup.Multiple<ValueType>(tag, title, presentation, iosPresentation)
        group.isEnabledCondition = isEnabledCondition
        group.isVisibleCondition = isVisibleCondition
        fields.ensureSingleTags()
        group.addFields(fields)
        if (validateOnBuild)
            group.validate(triggerSource = ValidationTriggerSource.FORM, false)
        return group
    }
}
