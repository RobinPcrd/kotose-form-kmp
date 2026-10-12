/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form

import io.github.robinpcrd.kotose.form.dsl.form
import io.github.robinpcrd.kotose.form.dsl.group
import io.github.robinpcrd.kotose.form.dsl.input
import io.github.robinpcrd.kotose.form.fields.InputField
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TransactionTest {
    @Test
    fun transactionAppliesAllOperationsThenReloads() {
        val form = form {
            +group {
                tag = "g"
                +input(tag = "a")
            }
        }
        val group = assertNotNull(form.getGroupByTag("g"))
        val a = assertNotNull(form.getFieldByTag("a"))
        val extra = Group("extra").apply { addField(InputField(tag = "c")) }

        form.transaction {
            add(InputField(tag = "b"), group, idx = 0)
            remove(a)
            add(extra)
        }

        assertEquals(listOf("b", "c"), form.fields.value.map { it.tag })
        assertEquals(listOf("g", "extra"), form.groups.map { it.tag })
        assertNull(form.getFieldByTag("a"))
        assertNotNull(form.getFieldByTag("c"))
    }

    @Test
    fun transactionWithoutReloadKeepsTheFieldStateUntilReload() {
        val form = form {
            +group { tag = "g" }
        }
        val group = assertNotNull(form.getGroupByTag("g"))

        form.transaction(reload = false) {
            add(InputField(tag = "a"), group)
        }

        assertTrue(form.fields.value.isEmpty())
        assertNotNull(form.getFieldByTag("a"))

        form.reload()

        assertEquals(listOf("a"), form.fields.value.map { it.tag })
    }

    @Test
    fun transactionRemovesGroups() {
        val form = form {
            +group { tag = "g1" }
            +group { tag = "g2" }
            +group { tag = "g3" }
        }
        val g1 = assertNotNull(form.getGroupByTag("g1"))
        val g3 = assertNotNull(form.getGroupByTag("g3"))

        form.transaction {
            remove(listOf(g1, g3))
        }

        assertEquals(listOf("g2"), form.groups.map { it.tag })
        assertNull(form.getGroupByTag("g1"))
    }

    @Test
    fun addingToAGroupOutsideTheFormFails() {
        val form = form {
            +group { }
        }
        val stranger = Group()

        assertFailsWith<Exception> {
            form.transaction {
                add(InputField(tag = "x"), stranger)
            }
        }
    }
}
