/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.dsl

import io.github.robinpcrd.kotose.form.Form
import io.github.robinpcrd.kotose.form.Group

@FormDslMarker
class FormBuilder {

    private val groups = mutableListOf<Group>()

    fun build(): Form {
        val form = Form()
        form.addAll(groups, reload = true)
        return form
    }

    operator fun Group.unaryPlus() {
        this@FormBuilder.groups.add(this)
    }

    operator fun Collection<Group>.unaryPlus() {
        this@FormBuilder.groups.addAll(this)
    }

    operator fun Iterable<Group>.unaryPlus() {
        this@FormBuilder.groups.addAll(this)
    }

    operator fun Sequence<Group>.unaryPlus() {
        this@FormBuilder.groups.addAll(this)
    }
}
