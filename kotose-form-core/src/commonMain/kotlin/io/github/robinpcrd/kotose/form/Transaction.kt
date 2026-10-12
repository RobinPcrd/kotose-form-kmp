/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form

import kotlin.jvm.JvmName

internal sealed class TransactionEvent

/**
 * Add a [Field] to a [Group]
 *
 * @throws Exception If the [Group] does not belong to the [Form]
 */
internal data class AddField(
    val field: Field<*>?,
    val toGroup: Group,
    val atIdx: Int?,
    val skipGroupValidation: Boolean = true,
) : TransactionEvent()

/**
 * Add multiple [Field] to a [Group]
 *
 * @throws Exception If the [Group] does not belong to the [Form]
 */
internal data class AddFields(
    val fields: Collection<Field<*>>,
    val toGroup: Group,
    val atIdx: Int?,
    val skipGroupValidation: Boolean = true,
) : TransactionEvent()

internal data class RemoveField(
    val field: Field<*>,
    val ofGroup: Group?,
    val idx: Int?
) : TransactionEvent()

internal data class RemoveFields(
    val fields: Collection<Field<*>>,
    val ofGroup: Group?,
    val idx: Int?
) : TransactionEvent()

internal data class AddGroup(
    val group: Group,
    val idx: Int?
) : TransactionEvent()

internal data class AddGroups(
    val groups: Collection<Group>,
    val idx: Int?
) : TransactionEvent()

internal data class RemoveGroup(
    val group: Group,
    val idx: Int?
) : TransactionEvent()

internal data class RemoveGroups(
    val groups: Collection<Group>,
    val idx: Int?
) : TransactionEvent()

class Transaction internal constructor(
    private val form: Form,
    private val reload: Boolean = true,
) {
    private val operations = mutableListOf<TransactionEvent>()

    fun add(field: Field<*>, group: Group, idx: Int? = null, skipGroupValidation: Boolean = true) =
        operations.add(
            AddField(field, group, idx, skipGroupValidation)
        )

    fun add(
        fields: Collection<Field<*>>,
        group: Group,
        idx: Int? = null,
        skipGroupValidation: Boolean = true
    ) = operations.add(
        AddFields(fields, group, idx, skipGroupValidation)
    )

    fun remove(field: Field<*>?, group: Group? = field?.parent, idx: Int? = null) {
        if (field != null) {
            operations.add(RemoveField(field, group, idx))
        }
    }

    fun remove(fields: Collection<Field<*>>, group: Group?, idx: Int? = null) {
        if (fields.isNotEmpty()) {
            operations.add(RemoveFields(fields, group, idx))
        }
    }

    fun add(group: Group, idx: Int? = null) = operations.add(
        AddGroup(group, idx)
    )

    fun add(groups: Collection<Group>, idx: Int? = null) = operations.add(
        AddGroups(groups, idx)
    )

    fun remove(group: Group?, idx: Int? = null) {
        if (group != null) {
            operations.add(RemoveGroup(group, idx))
        }
    }

    fun remove(groups: Collection<Group>, idx: Int? = null) {
        if (groups.isNotEmpty()) {
            operations.add(RemoveGroups(groups, idx))
        }
    }

    /**
     * Apply all pending operations immediately without reloading the form, then clear the queue
     */
    fun commit() {
        runOperations()
        operations.clear()
    }

    private fun runOperations() {
        for (operation in operations) {
            form.apply {
                when (operation) {
                    is AddField -> {
                        if (operation.field != null) {
                            if (operation.atIdx != null) {
                                operation.toGroup.addField(
                                    idx = operation.atIdx,
                                    field = operation.field,
                                    skipGroupValidation = operation.skipGroupValidation,
                                )
                            } else {
                                operation.toGroup.addField(
                                    field = operation.field,
                                    skipGroupValidation = operation.skipGroupValidation,
                                )
                            }
                        }
                        if (operation.toGroup !in form.groups)
                            throw Exception("Group ${operation.toGroup} does not belong to form.")
                    }

                    is AddFields -> {
                        if (operation.atIdx != null) {
                            operation.toGroup.addFields(
                                idx = operation.atIdx,
                                fields = operation.fields,
                                skipGroupValidation = operation.skipGroupValidation,
                            )
                        } else {
                            operation.toGroup.addFields(
                                fields = operation.fields,
                                skipGroupValidation = operation.skipGroupValidation,
                            )
                        }
                        if (operation.toGroup !in form.groups)
                            throw Exception("Group ${operation.toGroup} does not belong to form.")
                    }

                    is RemoveField -> {
                        (operation.ofGroup ?: operation.field.parent)
                            ?.removeField(operation.field)
                    }

                    is RemoveFields -> {
                        if (operation.ofGroup != null) {
                            operation.ofGroup.removeFields(operation.fields)
                        } else {
                            operation.fields.forEach(Field<*>::remove)
                        }
                    }

                    is AddGroup -> {
                        if (operation.idx != null)
                            form.add(operation.idx, operation.group)
                        else
                            form.add(operation.group)
                    }

                    is AddGroups -> {
                        if (operation.idx != null)
                            form.addAll(operation.idx, operation.groups)
                        else
                            form.addAll(operation.groups)
                    }

                    is RemoveGroup -> {
                        form.remove(operation.group)
                    }

                    is RemoveGroups -> {
                        form.removeAll(operation.groups)
                    }
                }
            }
        }
    }

    internal fun execute() {
        // val pendingGroups = mutableListOf<Group>()
        commit()

        if (reload)
            form.reload()
    }
}

fun transaction(form: Form, reload: Boolean = true, init: Transaction.() -> Unit) =
    Transaction(form, reload).apply(init).execute()

@JvmName("formTransaction")
fun Form.transaction(reload: Boolean = true, init: Transaction.() -> Unit) =
    transaction(form = this, reload = reload, init = init)
