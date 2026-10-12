/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

@file:Suppress("unused")

package io.github.robinpcrd.kotose.form

import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import io.github.robinpcrd.kotose.form.fields.validation.ValidationErrorGroupField
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/** One programmatic navigation action. Keys survive inserted validation rows. */
data class FormNavigationRequest(val id: Long, val targetKey: String)

interface FormEvent {
    fun onValueChange(form: Form, values: Map<String, Any?>)
}

sealed class SealedFormEvents {
    data class ValueChange(val form: Form, val values: Map<String, Any?>) : SealedFormEvents()
}

class Form {
    @OptIn(ExperimentalUuidApi::class)
    val key: String by lazy { Uuid.random().toString() }

    private val formCoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val lock = Mutex()

    private val _groups = mutableListOf<Group>()
    val groups get() = _groups.toList()

    private val _fields = mutableStateOf<ImmutableList<Field<*>>>(persistentListOf())
    val fields: State<ImmutableList<Field<*>>> = _fields

    private var navigationId = 0L
    private val _navigationRequest = mutableStateOf<FormNavigationRequest?>(null)
    val navigationRequest: State<FormNavigationRequest?> = _navigationRequest

    /** Compatibility view for renderers that still consume a scroll index. */
    val errorScrollIndex: State<Int> = derivedStateOf {
        _navigationRequest.value?.let { navigationIndex(it.id) } ?: -1
    }

    private val valueCache = HashMap<String, Any>()
    private val groupCache = mutableMapOf<String, Group>()
    private val fieldCache = mutableMapOf<String, Field<*>>()

    private val _visibilityMap = mutableMapOf<String, List<GenericElement>>()
    val visibilityMap: ImmutableMap<String, List<Element<*>>>
        get() = _visibilityMap.toImmutableMap()

    private val _enabledMap = mutableMapOf<String, List<GenericElement>>()
    val enabledMap: ImmutableMap<String, List<Element<*>>>
        get() = _enabledMap.toImmutableMap()

    private val _customConditionMap = mutableMapOf<String, List<GenericElement>>()
    val customConditionMap: ImmutableMap<String, List<Element<*>>>
        get() = _customConditionMap.toImmutableMap()

    private val _listeners = mutableListOf<FormEvent>()

    val events: SharedFlow<SealedFormEvents>
        field = MutableSharedFlow<SealedFormEvents>()

    @Suppress("MemberVisibilityCanBePrivate")
    var onValueChange: ((Map<String, Any?>) -> Unit)? = null

    /**
     * Validate the form and return a list of errors.
     * It also scrolls the form view to the first error.
     *
     * @return a list of errors
     */
    fun validate(
        scrollToFirstError: Boolean = true,
        includeHidden: Boolean = false,
        warningAsError: Boolean = false,
    ): List<ValidationError> = Snapshot.withMutableSnapshot {
        // One atomic apply: renderers observing snapshots must never see the focus cleared and
        // then restored on the first error, which hides and re-shows the keyboard.
        _navigationRequest.value = null
        val errors = _groups.flatMap { group ->
            group.validate(ValidationTriggerSource.FORM, includeHidden)
        }.filter {
            if (!it.asWarning) true else warningAsError
        }

        reload()
        if (scrollToFirstError && errors.isNotEmpty()) {
            navigateToFirstError(warningAsError)
        } else {
            currentFocus = null
        }
        _dismissKeyboardEvent.value = if (currentFocus == null) DismissKeyboardEvent else null
        errors
    }

    /** Moves focus straight to the first focusable error, or clears it when there is none. */
    private fun navigateToFirstError(warningAsError: Boolean) {
        fun ValidationError.canNavigate() = !isSilent && (!asWarning || warningAsError)
        for (group in groups.filter { it.isVisible }) {
            for (field in group.fields) {
                if (!field.isErrorField && field.isVisible && field.errors.any { it.canNavigate() }) {
                    currentFocus = (field as? FocusableField<*>)?.takeIf { canFocus(it) }
                    requestNavigation(field.key)
                    return
                }
            }
            if (group.groupErrors.any { it.canNavigate() }) {
                currentFocus = null
                requestNavigation(group.key)
                return
            }
        }
        currentFocus = null
    }

    /**
     * Get all the values from the form
     *
     * All the fields with a non null tag will add an entry to the result map where the key is the
     * field's [Field.tag] and the value is the field's [Field.value]
     *
     * Fields and specialized groups can customize their submitted values.
     *
     * Hidden fields and groups keep their values but are excluded by default, like
     * [validate] skips their rules. Pass [includeHidden] to submit them anyway.
     */
    fun allValues(includeHidden: Boolean = false): Map<String, Any?> = buildMap {
        for (group in _groups) putAll(group.allValues(includeHidden))
    }

    fun allNonNullValues(includeHidden: Boolean = false): Map<String, Any> {
        val values = allValues(includeHidden)
        val nonNullValues = mutableMapOf<String, Any>()
        values.forEach { (key, value) ->
            value?.let {
                nonNullValues[key] = it
            }
        }
        return nonNullValues
    }

    fun setValues(values: Map<String, Any?>) {
        for (group in _groups) {
            if (values.containsKey(group.tag)) {
                group.setBaseValue(values[group.tag])
            }
        }
        for (field in _fields.value) {
            if (values.containsKey(field.tag)) {
                field.setBaseValue(values[field.tag])
            }
        }
    }

    fun getGroupByTag(tag: String): Group? = groupCache[tag]

    fun getFieldByTag(tag: String): Field<*>? = fieldCache[tag]

    inline fun <reified FieldType : Field<*>> getTypedFieldByTag(tag: String): FieldType? =
        getFieldByTag(tag) as? FieldType

    fun getFirstErrorIndex(): Int = _fields.value.indexOfFirst(Field<*>::isErrorField)

    private fun assertSingleTag(tag: String?) {
        if (tag == null)
            return
        if (groupCache.containsKey(tag) || fieldCache.containsKey(tag))
            error("Duplicate tag found $tag")
    }

    internal fun addToCache(element: Element<*>) {
        when (element) {
            is Field<*> -> addToCache(element)
            is Group -> addToCache(element)
        }
    }

    internal fun addToCache(group: Group) {
        assertSingleTag(group.tag)
        group.tag?.let {
            groupCache[it] = group
        }
    }

    internal fun addToCache(field: Field<*>) {
        assertSingleTag(field.tag)
        field.tag?.let { tag ->
            fieldCache[tag] = field
            field.value?.let { value ->
                valueCache[tag] = value
                emitValueChange()
            }
        }
    }

    internal fun removeFromCache(group: Group) {
        group.tag?.let {
            groupCache.remove(it)
        }
    }

    internal fun removeFromCache(field: Field<*>) {
        field.tag?.let {
            fieldCache.remove(it)
            valueCache.remove(it)
            emitValueChange()
        }
    }

    internal fun addToEnabledObserver(element: GenericElement) {
        for (tag in element.isEnabledCondition.tags) {
            val list = _enabledMap[tag]?.toMutableList() ?: mutableListOf()
            if (!list.contains(element)) {
                list.add(element)
            }
            _enabledMap[tag] = list
        }
    }

    internal fun removeFromEnabledObserver(element: GenericElement) {
        for (tag in element.isEnabledCondition.tags) {
            val list = _enabledMap[tag]?.toMutableList() ?: mutableListOf()
            list.remove(element)
            _enabledMap[tag] = list
        }
    }

    internal fun addToVisibilityObserver(element: GenericElement) {
        for (tag in element.isVisibleCondition.tags) {
            val list = _visibilityMap[tag]?.toMutableList() ?: mutableListOf()
            if (!list.contains(element)) {
                list.add(element)
            }
            _visibilityMap[tag] = list
        }
    }

    internal fun removeFromVisibilityObserver(element: GenericElement) {
        for (tag in element.isVisibleCondition.tags) {
            val list = _visibilityMap[tag]?.toMutableList() ?: mutableListOf()
            list.remove(element)
            _visibilityMap[tag] = list
        }
    }

    internal fun addToCustomConditionObserver(element: GenericElement) {
        element.customCondition?.tags?.let { tags ->
            for (tag in tags) {
                val list = _customConditionMap[tag]?.toMutableList() ?: mutableListOf()
                if (!list.contains(element)) {
                    list.add(element)
                }
                _customConditionMap[tag] = list
            }
        }
    }

    internal fun removeFromCustomConditionObserver(element: GenericElement) {
        element.customCondition?.tags?.let { tags ->
            for (tag in tags) {
                val list = _customConditionMap[tag]?.toMutableList() ?: mutableListOf()
                list.remove(element)
                _customConditionMap[tag] = list
            }
        }
    }

    internal fun onFieldValueChange(tag: String, value: Any?) {
        if (value == null) {
            valueCache.remove(tag)
        } else {
            valueCache[tag] = value
        }
        emitValueChange()
    }

    fun add(group: Group, reload: Boolean = false) {
        add(index = -1, group = group, reload = reload)
    }

    fun add(index: Int = -1, group: Group, reload: Boolean = false) {
        if (group in _groups)
            return

        if (index == -1) {
            _groups.add(group)
        } else {
            _groups.add(index, group)
        }
        group.wasAddedToForm(this)

        if (reload)
            reload()
    }

    fun addAll(groups: Collection<Group>, reload: Boolean = false) {
        addAll(index = -1, groups = groups, reload = reload)
    }

    fun addAll(index: Int = -1, groups: Collection<Group>, reload: Boolean = false) {
        val filtered = groups.filter { group -> group !in _groups }
        if (index == -1) {
            _groups.addAll(filtered)
        } else {
            _groups.addAll(index, filtered)
        }
        groups.forEach { group -> group.wasAddedToForm(this) }

        if (reload)
            reload()
    }

    fun remove(group: Group, reload: Boolean = false) {
        if (!_groups.remove(group))
            return

        onRemove(group, reload)

        if (reload)
            reload()
    }

    fun removeAt(index: Int, reload: Boolean = false) {
        if (index !in _groups.indices)
            return

        _groups.removeAt(index).also {
            onRemove(it, reload)
        }

        if (reload)
            reload()
    }

    fun removeAll(groups: Collection<Group>, reload: Boolean = false) {
        if (!_groups.removeAll(groups))
            return

        groups.forEach {
            onRemove(it, reload)
        }

        if (reload)
            reload()
    }

    private fun onRemove(group: Group, reload: Boolean) {
        group.clear()
        elementWasRemoved(group, reload)
    }

    internal fun elementWasRemoved(element: Element<*>, reload: Boolean) {
        onElementAvailabilityChanged(element)
        removeFromEnabledObserver(element)
        removeFromVisibilityObserver(element)
        when (element) {
            is Group -> removeFromCache(element)
            is Field<*> -> removeFromCache(element)
        }

        // Conditions must observe the removed tag as absent, just as they observe value edits.
        element.tag?.let { tag ->
            enabledMap[tag]?.toList()?.forEach { it.evaluateEnabled() }
            visibilityMap[tag]?.toList()?.forEach { it.evaluateVisibility() }
            customConditionMap[tag]?.toList()?.forEach { it.evaluateCustomCondition() }
        }

        if (reload)
            reload()
    }

    fun reload() = runBlocking {
        lock.withLock {
            _fields.value = groups.flatMap { group -> group.fields }.toImmutableList()
        }
    }

    private fun updateFields() {

    }

    fun hasScrollToError() {
        _navigationRequest.value?.let { completeNavigation(it.id) }
    }

    // region Focus

    var currentFocus by mutableStateOf<FocusableField<*>?>(null)
        private set

    private val _dismissKeyboardEvent = mutableStateOf<DismissKeyboardEvent?>(null)
    val dismissKeyboardEvent: State<DismissKeyboardEvent?> = _dismissKeyboardEvent

    fun stopEditing(forceKeyboardDismiss: Boolean = false) {
        currentFocus = null
        _navigationRequest.value = null
        _dismissKeyboardEvent.value = if (forceKeyboardDismiss) DismissKeyboardEvent else null
    }

    fun onKeyboardDismiss() {
        _dismissKeyboardEvent.value = null
    }

    fun setFocus(field: FocusableField<*>) {
        if (!canFocus(field)) return
        currentFocus = field
        _dismissKeyboardEvent.value = null
        requestNavigation(field.key)
    }

    /** Report actual UI focus without creating another navigation request. */
    fun onFocusChanged(field: FocusableField<*>, focused: Boolean) {
        if (focused && canFocus(field)) {
            currentFocus = field
        } else if (!focused && currentFocus == field) {
            currentFocus = null
        }
    }

    fun canFocus(field: FocusableField<*>): Boolean = field.isVisible && field.isEnabled &&
        field.parent?.let { it in _groups && field in it.fields } == true

    fun nextFocusableField(field: Field<*>): FocusableField<*>? {
        val fields = _fields.value
        val index = fields.indexOf(field)
        if (index == -1) return null
        return fields.drop(index + 1).filterIsInstance<FocusableField<*>>().firstOrNull { canFocus(it) }
    }

    fun previousFocusableField(field: Field<*>): FocusableField<*>? {
        val fields = _fields.value
        val index = fields.indexOf(field)
        if (index == -1) return null
        return fields.take(index).filterIsInstance<FocusableField<*>>().lastOrNull { canFocus(it) }
    }

    fun nextFocusableFieldState(field: Field<*>): State<FocusableField<*>?> =
        derivedStateOf { nextFocusableField(field) }

    fun focusNext(field: Field<*>) {
        val next = nextFocusableField(field)
        if (next == null) stopEditing(true) else setFocus(next)
    }

    /** Resolve just before scrolling; a removed or hidden target has no current index. */
    fun navigationIndex(requestId: Long): Int {
        val request = _navigationRequest.value?.takeIf { it.id == requestId } ?: return -1
        return _fields.value.indexOfFirst { field ->
            field.isVisible && field.parent?.let { it in _groups && field in it.fields } == true &&
                (field.key == request.targetKey ||
                    field is ValidationErrorGroupField && field.elementKey == request.targetKey)
        }
    }

    /** A stale renderer must not acknowledge a newer submission. */
    fun completeNavigation(requestId: Long): Boolean {
        if (_navigationRequest.value?.id != requestId) return false
        _navigationRequest.value = null
        return true
    }

    internal fun onElementAvailabilityChanged(element: GenericElement) {
        currentFocus?.let { if (!canFocus(it)) stopEditing(true) }
        if (_navigationRequest.value?.targetKey == element.key &&
            (!element.isVisible || element is Field<*> && element !in element.parent?.fields.orEmpty() ||
                element is Group && element !in _groups)) {
            _navigationRequest.value = null
        }
    }

    private fun requestNavigation(targetKey: String) {
        _navigationRequest.value = FormNavigationRequest(++navigationId, targetKey)
    }

    object DismissKeyboardEvent

    // endregion

    fun addListener(listener: FormEvent) {
        _listeners.add(listener)
    }

    fun removeListener(listener: FormEvent) {
        _listeners.remove(listener)
    }

    fun removeAllListeners() {
        _listeners.clear()
    }

    private fun emitValueChange() {
        val values = valueCache.toMap()

        onValueChange?.invoke(values)

        _listeners.forEach { listener -> listener.onValueChange(this, values) }
        if (events.subscriptionCount.value > 0) {
            formCoroutineScope.launch {
                events.emit(SealedFormEvents.ValueChange(this@Form, values))
            }
        }
    }

    fun <T> fieldValue(tag: String): T? = valueCache[tag] as? T
}
