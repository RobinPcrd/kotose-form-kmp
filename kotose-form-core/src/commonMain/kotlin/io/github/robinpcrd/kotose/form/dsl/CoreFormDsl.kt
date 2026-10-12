/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.dsl

@DslMarker
annotation class FormDslMarker

@DslMarker
annotation class GroupDslMarker

@DslMarker
annotation class SelectionGroupDslMarker

@DslMarker
annotation class FieldDslMarker

fun form(init: FormBuilder.() -> Unit) = FormBuilder().apply(init).build()

fun FormBuilder.group(init: GroupBuilder.() -> Unit) = GroupBuilder().apply(init).build()
