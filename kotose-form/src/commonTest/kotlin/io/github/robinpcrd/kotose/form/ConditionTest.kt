/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form

import io.github.robinpcrd.kotose.form.dsl.form
import io.github.robinpcrd.kotose.form.dsl.group
import io.github.robinpcrd.kotose.form.dsl.input
import io.github.robinpcrd.kotose.form.dsl.label
import io.github.robinpcrd.kotose.form.dsl.selectableField
import io.github.robinpcrd.kotose.form.dsl.singleSelectionGroup
import io.github.robinpcrd.kotose.form.dsl.switch
import io.github.robinpcrd.kotose.form.fields.InputField
import io.github.robinpcrd.kotose.form.fields.SelectableField
import io.github.robinpcrd.kotose.form.fields.SwitchField
import io.github.robinpcrd.kotose.resources.StrRes
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ConditionTest {
    @Test
    fun visibilityFollowsAnotherFieldValue() {
        val form = form {
            +group {
                +switch(tag = "hasPet", label = StrRes(text = "Pet?"))
                +input(tag = "petName") {
                    isVisibleCondition = Condition("hasPet") { it.fieldValue<Boolean>("hasPet") == true }
                }
            }
        }
        val petName = assertNotNull(form.getFieldByTag("petName"))
        assertFalse(petName.isVisible)

        form.getTypedFieldByTag<SwitchField>("hasPet")?.value = true
        assertTrue(petName.isVisible)

        form.getTypedFieldByTag<SwitchField>("hasPet")?.value = false
        assertFalse(petName.isVisible)
    }

    @Test
    fun enabledFollowsAnotherFieldValue() {
        val form = form {
            +group {
                +input(tag = "name")
                +input(tag = "nickname") {
                    isEnabledCondition = Condition("name") { !it.fieldValue<String>("name").isNullOrBlank() }
                }
            }
        }
        val nickname = assertNotNull(form.getFieldByTag("nickname"))
        assertFalse(nickname.isEnabled)

        form.getTypedFieldByTag<InputField>("name")?.value = "Spock"

        assertTrue(nickname.isEnabled)
    }

    @Test
    fun hiddenGroupHidesItsFields() {
        val form = form {
            +group {
                tag = "g"
                isVisibleCondition = Condition.False
                +input(tag = "a")
            }
        }
        val group = assertNotNull(form.getGroupByTag("g"))
        val field = assertNotNull(form.getFieldByTag("a"))
        assertFalse(group.isVisible)
        assertFalse(field.isVisible)

        group.isVisibleCondition = Condition.True

        assertTrue(group.isVisible)
        assertTrue(field.isVisible)
    }

    @Test
    fun customConditionRunsOnAddAndWhenTheDependencyChanges() {
        val evaluations = mutableListOf<String?>()
        val form = form {
            +group {
                +input(tag = "name")
                +label(tag = "greeting") {
                    customCondition = CustomCondition("name") { _, form ->
                        evaluations.add(form.fieldValue<String>("name"))
                    }
                }
            }
        }
        assertEquals(listOf<String?>(null), evaluations)

        form.getTypedFieldByTag<InputField>("name")?.value = "Spock"

        assertEquals(listOf(null, "Spock"), evaluations)
    }

    @Test
    fun selectionGroupValueDrivesConditionsWhenSelectingAField() {
        val form = form {
            +singleSelectionGroup<String> {
                tag = "plan"
                +selectableField("free", "free")
                +selectableField("pro", "pro")
            }
            +group {
                +input(tag = "card") {
                    isVisibleCondition = Condition("plan") { it.fieldValue<String>("plan") == "pro" }
                }
            }
        }
        val card = assertNotNull(form.getFieldByTag("card"))
        assertFalse(card.isVisible)

        form.getTypedFieldByTag<SelectableField<*>>("pro")?.select()

        assertTrue(card.isVisible)

        form.getTypedFieldByTag<SelectableField<*>>("free")?.select()

        assertFalse(card.isVisible)
    }
}
