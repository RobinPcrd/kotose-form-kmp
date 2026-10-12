/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

@file:OptIn(
    io.github.robinpcrd.kotose.form.ExperimentalKotoseFormApi::class,
    ExperimentalKotoseSwiftUIApi::class,
)

package io.github.robinpcrd.kotose.form.swiftui

import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.Snapshot
import io.github.robinpcrd.kotose.form.Field
import io.github.robinpcrd.kotose.form.FocusableField
import io.github.robinpcrd.kotose.form.Form
import io.github.robinpcrd.kotose.form.FormNavigationRequest
import io.github.robinpcrd.kotose.form.fields.CustomField
import io.github.robinpcrd.kotose.form.fields.ActionField
import io.github.robinpcrd.kotose.form.fields.DisplayParagraphField
import io.github.robinpcrd.kotose.form.fields.DisplayTextField
import io.github.robinpcrd.kotose.form.fields.IosDatePickerMode
import io.github.robinpcrd.kotose.form.fields.LocalDateField
import io.github.robinpcrd.kotose.form.fields.DateTimeField
import io.github.robinpcrd.kotose.form.fields.TextField
import io.github.robinpcrd.kotose.form.fields.TextLineLimits
import io.github.robinpcrd.kotose.form.fields.SecureTextField
import io.github.robinpcrd.kotose.form.fields.PhoneNumberField
import io.github.robinpcrd.kotose.form.compose.data.Country
import io.github.robinpcrd.kotose.form.compose.data.CountryConfiguration
import io.github.robinpcrd.kotose.form.compose.data.getCountryForRegion
import io.github.robinpcrd.kotose.form.fields.SingleChoiceField
import io.github.robinpcrd.kotose.form.fields.SelectionOptionField
import io.github.robinpcrd.kotose.form.fields.SelectionPresentation
import io.github.robinpcrd.kotose.form.groups.SelectionGroup
import io.github.robinpcrd.kotose.form.fields.ToggleField
import io.github.robinpcrd.kotose.form.fields.validation.ValidationErrorField
import io.github.robinpcrd.kotose.form.fields.validation.ValidationErrorGroupField
import io.github.robinpcrd.kotose.resources.StrRes
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.number

/** Resolved text at the native UI boundary. These are snapshots, not independent form state. */
@ExperimentalKotoseSwiftUIApi
class NativeValidationMessage internal constructor(
    val text: String,
    val asWarning: Boolean,
)

@ExperimentalKotoseSwiftUIApi
class NativeChoiceOption internal constructor(
    val id: String,
    val label: String,
)

@ExperimentalKotoseSwiftUIApi
enum class NativeFieldKind {
    TEXT,
    PASSWORD,
    PHONE,
    TOGGLE,
    SINGLE_CHOICE,
    MULTIPLE_SELECTION,
    SINGLE_SELECTION,
    DATE,
    DATE_TIME,
    LABEL,
    PARAGRAPH,
    ACTION,
    CUSTOM,
}

/**
 * The domain value stays in Kotlin. A missing selected ID represents no selection.
 * [presentation] is the field's presentation on iOS.
 */
@ExperimentalKotoseSwiftUIApi
class NativeChoiceSnapshot internal constructor(
    val options: List<NativeChoiceOption>,
    val selectedId: String?,
    val isClearable: Boolean,
    val presentation: SelectionPresentation = SelectionPresentation.Menu,
)

/**
 * Without a phone number backend, [hasCountryPicker] is false: the row takes the dial code as
 * typed digits ([dialCodeDigits]) through `setPhoneDialCode`, and [countries] is empty.
 */
@ExperimentalKotoseSwiftUIApi
class NativePhoneSnapshot internal constructor(
    val region: String,
    val dialCode: String,
    val countrySelectionEnabled: Boolean,
    val countries: List<NativeChoiceOption>,
    val hasCountryPicker: Boolean,
    val dialCodeDigits: String,
    /** Emoji flag of [region]. */
    val flag: String,
)

/**
 * Calendar components of the Kotlin value, without a time zone. When [hasValue] is false the
 * components are zero and the renderer offers to pick a date. Time components stay zero for
 * date-only fields.
 */
@ExperimentalKotoseSwiftUIApi
class NativeDateSnapshot internal constructor(
    val year: Int,
    val month: Int,
    val day: Int,
    val hour: Int,
    val minute: Int,
    val hasValue: Boolean,
    val includesTime: Boolean,
    val pickerMode: IosDatePickerMode = IosDatePickerMode.Compact,
)

/** Read-only text rows. A label has one line; a paragraph has several. */
/** Line bounds of a multi-line text input. A text field without one is single line. */
@ExperimentalKotoseSwiftUIApi
class NativeLineLimits internal constructor(
    val minLines: Int,
    val maxLines: Int,
)

@ExperimentalKotoseSwiftUIApi
class NativeDisplaySnapshot internal constructor(
    val lines: List<String>,
    val isTitle: Boolean,
)

@ExperimentalKotoseSwiftUIApi
class NativeFieldSnapshot internal constructor(
    val key: String,
    val kind: NativeFieldKind,
    val customKind: String?,
    val label: String,
    val textValue: String,
    val toggleValue: Boolean,
    val enabled: Boolean,
    val validationMessages: List<NativeValidationMessage>,
    val nextFocusKey: String? = null,
    val choice: NativeChoiceSnapshot? = null,
    val selected: Boolean = false,
    val phone: NativePhoneSnapshot? = null,
    val date: NativeDateSnapshot? = null,
    val display: NativeDisplaySnapshot? = null,
    val lines: NativeLineLimits? = null,
    /** Text shown in an empty text or password input: the field's placeholder, else its label. */
    val placeholder: String = label,
    /** Secondary text of a selection option, shown under its label. */
    val subtitle: String? = null,
    val previousFocusKey: String? = null,
) {
    /** Text-only compatibility view. Renderers should use [validationMessages] for severity. */
    val messages: List<String> get() = validationMessages.map { it.text }
}

/** Field messages follow their control; group messages follow all controls in the group. */
@ExperimentalKotoseSwiftUIApi
class NativeGroupSnapshot internal constructor(
    val key: String,
    val fields: List<NativeFieldSnapshot>,
    val validationMessages: List<NativeValidationMessage>,
    /** Title of a selection group, shown as the header of its options. */
    val title: String? = null,
    /** A selection group's presentation on iOS; [SelectionPresentation.Inline] for other groups. */
    val presentation: SelectionPresentation = SelectionPresentation.Inline,
)

@ExperimentalKotoseSwiftUIApi
class NativeFormSnapshot internal constructor(
    val groups: List<NativeGroupSnapshot>,
    val focusedFieldKey: String? = null,
    val navigationRequest: FormNavigationRequest? = null,
) {
    val fields: List<NativeFieldSnapshot> get() = groups.flatMap { it.fields }
}

private data class MessageReading(val text: StrRes, val asWarning: Boolean) {
    suspend fun resolve(): NativeValidationMessage? = text.getStringSuspend()?.let {
        NativeValidationMessage(it, asWarning)
    }
}

private data class GroupReading(
    val key: String,
    val fields: List<FieldReading>,
    val messages: List<MessageReading>,
    val title: StrRes? = null,
    val presentation: SelectionPresentation = SelectionPresentation.Inline,
)

private data class FieldReading(
    val key: String,
    val kind: NativeFieldKind,
    val customKind: String?,
    val label: StrRes?,
    val textValue: String,
    val toggleValue: Boolean,
    val enabled: Boolean,
    val messages: List<MessageReading>,
    val nextFocusKey: String?,
    val choice: ChoiceReading?,
    val selected: Boolean,
    val phone: PhoneReading?,
    val date: NativeDateSnapshot?,
    val display: DisplayReading?,
    val lines: NativeLineLimits? = null,
    val placeholder: StrRes? = null,
    val subtitle: StrRes? = null,
    val previousFocusKey: String? = null,
)

private data class DisplayReading(val lines: List<StrRes>, val isTitle: Boolean) {
    suspend fun resolve() = NativeDisplaySnapshot(lines.map { it.getStringSuspend().orEmpty() }, isTitle)
}

private data class PhoneReading(
    val country: Country,
    val selectionEnabled: Boolean,
    val countries: List<Country>,
    val hasCountryPicker: Boolean,
    val dialCodeDigits: String,
) {
    suspend fun resolve() = NativePhoneSnapshot(
        country.region,
        "+${country.code}",
        selectionEnabled,
        countries.map {
            NativeChoiceOption(it.region, "${it.emojiFlag} ${it.name.getStringSuspend().orEmpty()} (+${it.code})")
        },
        hasCountryPicker,
        dialCodeDigits,
        country.emojiFlag,
    )
}

private data class ChoiceReading(
    val options: List<Pair<String, StrRes>>,
    val selectedId: String?,
    val isClearable: Boolean,
    val presentation: SelectionPresentation,
) {
    suspend fun resolve() = NativeChoiceSnapshot(
        options.map { NativeChoiceOption(it.first, it.second.getStringSuspend().orEmpty()) },
        selectedId, isClearable, presentation,
    )
}

private data class FormReading(
    val groups: List<GroupReading>,
    val focusedFieldKey: String?,
    val navigationRequest: FormNavigationRequest?,
)

/**
 * Experimental adapter over the existing Compose-backed form engine.
 * Collect and mutate on the main thread. No Compose view or recomposer is required.
 * Each collector owns and disposes its snapshot observer when collection is cancelled.
 */
@ExperimentalKotoseSwiftUIApi
class NativeFormAdapter(private val form: Form) {
    val snapshots: Flow<NativeFormSnapshot> = flow {
        coroutineScope {
            val writes = Channel<Unit>(Channel.CONFLATED)
            val observer = Snapshot.registerGlobalWriteObserver { writes.trySend(Unit) }
            val notifier = launch {
                for (ignored in writes) Snapshot.sendApplyNotifications()
            }
            try {
                snapshotFlow {
                    FormReading(readGroups(), form.currentFocus?.key, form.navigationRequest.value)
                }.collect { readings ->
                    val groups = readings.groups.map { group ->
                        NativeGroupSnapshot(
                            key = group.key,
                            fields = group.fields.map { reading ->
                                val label = reading.label?.getStringSuspend().orEmpty()
                                NativeFieldSnapshot(
                                    key = reading.key,
                                    kind = reading.kind,
                                    customKind = reading.customKind,
                                    label = label,
                                    textValue = reading.textValue,
                                    toggleValue = reading.toggleValue,
                                    enabled = reading.enabled,
                                    validationMessages = reading.messages.mapNotNull { it.resolve() },
                                    nextFocusKey = reading.nextFocusKey,
                                    choice = reading.choice?.resolve(),
                                    selected = reading.selected,
                                    phone = reading.phone?.resolve(),
                                    date = reading.date,
                                    display = reading.display?.resolve(),
                                    lines = reading.lines,
                                    placeholder = reading.placeholder?.getStringSuspend() ?: label,
                                    subtitle = reading.subtitle?.getStringSuspend(),
                                    previousFocusKey = reading.previousFocusKey,
                                )
                            },
                            validationMessages = group.messages.mapNotNull { it.resolve() },
                            title = group.title?.getStringSuspend(),
                            presentation = group.presentation,
                        )
                    }
                    emit(NativeFormSnapshot(groups, readings.focusedFieldKey, readings.navigationRequest))
                }
            } finally {
                observer.dispose()
                notifier.cancel()
                writes.close()
            }
        }
    }

    fun setText(key: String, value: String): Boolean {
        val field = editableField(key) ?: return false
        when (field) {
            is PhoneNumberField -> field.setPhone(value)
            is SecureTextField -> field.setValue(value)
            is TextField -> field.value = value
            else -> return false
        }
        return true
    }

    /**
     * Sets the typed dial code of a phone row without a country picker and returns the dial code
     * the row now holds, which the input shows instead of [dialCode]: a rejected digit leaves the
     * value, and so the snapshots, unchanged. Null when the row has a country picker or is not
     * editable.
     */
    fun setPhoneDialCode(key: String, dialCode: String): String? {
        val field = editableField(key) as? PhoneNumberField ?: return null
        if (field.hasCountryPicker) return null
        field.setDialCode(dialCode)
        return field.dialCode
    }

    fun selectPhoneCountry(key: String, region: String): Boolean {
        val field = editableField(key) as? PhoneNumberField ?: return false
        if (!field.isCountrySelectionEnabled) return false
        val country = field.selectableCountries().firstOrNull { it.region == region } ?: return false
        field.selectCountry(country)
        return true
    }

    /**
     * Applies calendar components to a date or date-time field. Time components are ignored
     * for date-only fields. Impossible components such as February 30 are rejected.
     */
    fun setDate(key: String, year: Int, month: Int, day: Int, hour: Int, minute: Int): Boolean {
        val field = editableField(key) ?: return false
        return try {
            when (field) {
                is LocalDateField -> field.value = LocalDate(year, month, day)
                is DateTimeField -> field.value = LocalDateTime(year, month, day, hour, minute)
                else -> return false
            }
            true
        } catch (error: IllegalArgumentException) {
            false
        }
    }

    /**
     * The live Kotlin field behind a CUSTOM snapshot, for the application's own renderer to
     * cast to its class and read or update typed state. Null when the key is not a visible
     * custom field. The renderer must honor the snapshot's enabled state itself.
     */
    fun customField(key: String): Field<*>? = form.fields.value
        .firstOrNull { it.key == key && it.isVisible && it is CustomField }

    /** Runs the Kotlin callback of a visible, enabled action field. */
    fun performAction(key: String): Boolean =
        (editableField(key) as? ActionField)?.perform() ?: false

    fun setToggle(key: String, value: Boolean): Boolean {
        val field = editableField(key) as? ToggleField ?: return false
        field.value = value
        return true
    }

    /** Null requests user clearing. The model enforces clearing and option membership. */
    fun selectOption(key: String, optionId: String?): Boolean {
        val field = editableField(key) as? SingleChoiceField<*> ?: return false
        return field.selectOption(optionId)
    }

    /**
     * The option key identifies the control; its typed value stays in Kotlin. In a single
     * selection group, selecting moves the selection and deselecting follows the group's
     * deselection policy.
     */
    fun setSelected(key: String, selected: Boolean): Boolean {
        val field = editableField(key) as? SelectionOptionField<*> ?: return false
        return field.setSelected(selected)
    }

    fun validate(): Boolean = validate(includeHidden = false, warningAsError = false)

    /** The same submission and navigation policies as [Form.validate]. */
    fun validate(includeHidden: Boolean, warningAsError: Boolean): Boolean = form.validate(
        includeHidden = includeHidden,
        warningAsError = warningAsError,
    ).isEmpty()

    fun focusNext(key: String) {
        editableField(key)?.let { form.focusNext(it) }
    }

    fun focusPrevious(key: String) {
        editableField(key)?.let { form.previousFocusableField(it) }?.let { form.setFocus(it) }
    }

    fun onFocusChanged(key: String, focused: Boolean) {
        val field = form.fields.value.firstOrNull { it.key == key } as? FocusableField<*> ?: return
        form.onFocusChanged(field, focused)
    }

    /** Cancels pending navigation when this renderer is detached. Form values remain shared. */
    fun stopEditing() = form.stopEditing(true)

    fun completeNavigation(requestId: Long): Boolean = form.completeNavigation(requestId)

    fun isNavigationCurrent(requestId: Long): Boolean = form.navigationIndex(requestId) >= 0

    private fun editableField(key: String): Field<*>? = form.fields.value
        .firstOrNull { it.key == key && it.isEnabled && it.isVisible }

    // The visible rows Compose draws, validation message rows included, so no rule is re-derived here.
    private fun readGroups(): List<GroupReading> = form.fields.value
        .filter { it.isVisibleState.value }
        .groupBy { requireNotNull(it.parent) }
        .map { (group, rows) ->
            val fieldMessages = rows.filterIsInstance<ValidationErrorField>().groupBy { it.elementKey }
            GroupReading(
                key = group.key,
                fields = readFields(rows.filterNot { it.isErrorField }, fieldMessages),
                messages = rows.filterIsInstance<ValidationErrorGroupField>().map {
                    MessageReading(it.error, it.asWarning)
                },
                title = (group as? SelectionGroup<*>)?.title,
                // The adapter feeds the SwiftUI renderer, so it resolves for iOS on every host.
                presentation = (group as? SelectionGroup<*>)?.presentationFor(ios = true)
                    ?: SelectionPresentation.Inline,
            )
        }

    private fun readFields(
        fields: List<Field<*>>,
        messages: Map<String?, List<ValidationErrorField>>,
    ): List<FieldReading> = fields
        .map { field ->
            val kind: NativeFieldKind
            var customKind: String? = null
            val label: StrRes?
            val text: String
            val toggle: Boolean
            var choice: ChoiceReading? = null
            var selected = false
            var phone: PhoneReading? = null
            var date: NativeDateSnapshot? = null
            var display: DisplayReading? = null
            var lines: NativeLineLimits? = null
            var placeholder: StrRes? = null
            var subtitle: StrRes? = null
            when (field) {
                is PhoneNumberField -> {
                    kind = NativeFieldKind.PHONE
                    label = field.placeholder
                    text = field.valueState.value?.phone.orEmpty()
                    toggle = false
                    phone = PhoneReading(
                        field.displayCountry,
                        field.isCountrySelectionEnabled,
                        field.selectableCountries(),
                        field.hasCountryPicker,
                        field.dialCode,
                    )
                }
                is SelectionOptionField<*> -> {
                    kind = when (field.selectionGroup) {
                        is SelectionGroup.Single<*> -> NativeFieldKind.SINGLE_SELECTION
                        else -> NativeFieldKind.MULTIPLE_SELECTION
                    }
                    label = field.labelForValueState.value ?: StrRes(text = field.selectableValue.toString())
                    subtitle = field.subtitle
                    text = ""
                    toggle = false
                    selected = field.valueState.value != null
                }
                is SingleChoiceField<*> -> {
                    kind = NativeFieldKind.SINGLE_CHOICE
                    label = field.placeholder
                    text = ""
                    toggle = false
                    choice = ChoiceReading(
                        field.possibleValuesState.value.map { it.id to it.label },
                        field.selectedOption.value?.id,
                        field.isClearable,
                        field.presentationFor(ios = true),
                    )
                }
                is SecureTextField -> {
                    kind = NativeFieldKind.PASSWORD
                    label = field.label ?: field.placeholder
                    placeholder = field.placeholder
                    text = field.valueState.value.orEmpty()
                    toggle = false
                }
                is TextField -> {
                    kind = NativeFieldKind.TEXT
                    label = field.label
                    placeholder = field.placeholder
                    text = field.valueState.value.orEmpty()
                    toggle = false
                    lines = (field.lineLimits as? TextLineLimits.MultiLine)?.let {
                        NativeLineLimits(it.minLines, it.maxLines)
                    }
                }
                is ToggleField -> {
                    kind = NativeFieldKind.TOGGLE
                    label = field.labelState.value
                    text = ""
                    toggle = field.valueState.value ?: false
                }
                is LocalDateField -> {
                    kind = NativeFieldKind.DATE
                    label = field.placeholder
                    val value = field.valueState.value
                    text = value?.let(field.formatter).orEmpty()
                    toggle = false
                    date = NativeDateSnapshot(
                        value?.year ?: 0, value?.month?.number ?: 0, value?.day ?: 0, 0, 0,
                        hasValue = value != null, includesTime = false, pickerMode = field.iosPickerMode,
                    )
                }
                is DateTimeField -> {
                    kind = NativeFieldKind.DATE_TIME
                    label = field.placeholder
                    val value = field.valueState.value
                    text = value?.let(field.formatter).orEmpty()
                    toggle = false
                    date = NativeDateSnapshot(
                        value?.year ?: 0, value?.month?.number ?: 0, value?.day ?: 0,
                        value?.hour ?: 0, value?.minute ?: 0,
                        hasValue = value != null, includesTime = true, pickerMode = field.iosPickerMode,
                    )
                }
                is DisplayTextField -> {
                    kind = NativeFieldKind.LABEL
                    val current = field.valueState.value
                    label = current
                    text = ""
                    toggle = false
                    display = DisplayReading(listOfNotNull(current), field.isTitle)
                }
                is DisplayParagraphField -> {
                    kind = NativeFieldKind.PARAGRAPH
                    label = null
                    text = ""
                    toggle = false
                    display = DisplayReading(field.valueState.value.orEmpty(), isTitle = false)
                }
                is ActionField -> {
                    kind = NativeFieldKind.ACTION
                    label = field.title
                    text = ""
                    toggle = false
                }
                is CustomField -> {
                    kind = NativeFieldKind.CUSTOM
                    customKind = field.kind
                    label = field.label
                    // Reading the value keeps the snapshot flow reactive to typed custom edits.
                    text = field.valueState.value?.toString().orEmpty()
                    toggle = false
                }
                else -> error("No native renderer contract for field ${field.key}")
            }
            FieldReading(
                field.key, kind, customKind, label, text, toggle,
                field.isEnabledState.value,
                messages[field.key].orEmpty().map { MessageReading(it.error, it.asWarning) },
                form.nextFocusableField(field)?.key,
                choice,
                selected,
                phone,
                date,
                display,
                lines,
                placeholder,
                subtitle,
                form.previousFocusableField(field)?.key,
            )
        }
}
