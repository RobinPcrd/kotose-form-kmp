/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.swiftui

import io.github.robinpcrd.kotose.form.dsl.form
import io.github.robinpcrd.kotose.form.dsl.group
import io.github.robinpcrd.kotose.form.fields.SecureTextField
import io.github.robinpcrd.kotose.form.fields.TextField
import io.github.robinpcrd.kotose.resources.StrRes
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalKotoseSwiftUIApi::class)
class NativePlaceholderTest {
    @Test
    fun snapshotsCarryThePlaceholderAndFallBackToTheLabel() = runBlocking {
        val form = form {
            +group {
                +TextField("lastName", StrRes(text = "Nom"), StrRes(text = "Nom de famille du patient"))
                +TextField("firstName", StrRes(text = "Prénom"))
                +TextField("free")
                +SecureTextField("password", StrRes(text = "Mot de passe"), StrRes(text = "8 caractères"))
            }
        }
        val fields = NativeFormAdapter(form).snapshots.first().fields.associateBy { it.key }

        assertEquals("Nom", fields.getValue("lastName").label)
        assertEquals("Nom de famille du patient", fields.getValue("lastName").placeholder)
        assertEquals("Prénom", fields.getValue("firstName").placeholder)
        assertEquals("", fields.getValue("free").placeholder)
        assertEquals("Mot de passe", fields.getValue("password").label)
        assertEquals("8 caractères", fields.getValue("password").placeholder)
    }
}
