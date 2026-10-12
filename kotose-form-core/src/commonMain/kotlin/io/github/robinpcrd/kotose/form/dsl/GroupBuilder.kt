/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.dsl

import io.github.robinpcrd.kotose.form.Condition
import io.github.robinpcrd.kotose.form.Field
import io.github.robinpcrd.kotose.form.Form
import io.github.robinpcrd.kotose.form.Group
import io.github.robinpcrd.kotose.form.ValidationTriggerSource
import io.github.robinpcrd.kotose.form.ensureSingleTags

interface IGroupBuilder {
    val form: Form
    var tag: String?
    val fields: MutableList<Field<*>>
}

open class GroupBuilder {
    var tag: String? = null
    var isEnabledCondition = Condition()
    var isVisibleCondition = Condition()

    /**
     * If true, the group will be validated on build.
     */
    var validateOnBuild = false

    protected val fields = mutableListOf<Field<*>>()

    operator fun Field<*>.unaryPlus() {
        this@GroupBuilder.fields.add(this)
    }

    operator fun Collection<Field<*>>.unaryPlus() {
        this@GroupBuilder.fields.addAll(this)
    }

    operator fun Iterable<Field<*>>.unaryPlus() {
        this@GroupBuilder.fields.addAll(this)
    }

    operator fun Sequence<Field<*>>.unaryPlus() {
        this@GroupBuilder.fields.addAll(this)
    }

    open fun build(): Group {
        val group = Group(tag)
        group.isEnabledCondition = isEnabledCondition
        group.isVisibleCondition = isVisibleCondition
        fields.ensureSingleTags()
        group.addFields(fields)
        if (validateOnBuild)
            group.validate(triggerSource = ValidationTriggerSource.FORM, false)
        return group
    }
}
