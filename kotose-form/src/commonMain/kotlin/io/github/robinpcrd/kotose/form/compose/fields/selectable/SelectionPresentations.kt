/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.compose.fields.selectable

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import io.github.robinpcrd.kotose.form.compose.FieldDefaults
import io.github.robinpcrd.kotose.form.compose.FullScreenDialogBars
import io.github.robinpcrd.kotose.form.compose.fieldPadding
import io.github.robinpcrd.kotose.form.compose.fullScreenDialogProperties
import io.github.robinpcrd.kotose.form.compose.fields.DropdownBox
import io.github.robinpcrd.kotose.form.compose.fields.DropdownField
import io.github.robinpcrd.kotose.form.compose.fields.LabelField
import io.github.robinpcrd.kotose.form.compose.fields.popupTestTags
import io.github.robinpcrd.kotose.form.compose.isIosPlatform
import io.github.robinpcrd.kotose.form.fields.SelectableFieldAppearance
import io.github.robinpcrd.kotose.form.fields.SelectionPresentation
import io.github.robinpcrd.kotose.form.fields.SingleChoiceField
import io.github.robinpcrd.kotose.form.groups.SelectionGroup
import io.github.robinpcrd.kotose_form.generated.resources.Res
import io.github.robinpcrd.kotose_form.generated.resources.close
import io.github.robinpcrd.kotose_form.generated.resources.ic_clear
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** One option of a collapsed selection. [id] is the option key or the choice option ID. */
@Immutable
internal data class SelectionChoice(
    val id: String,
    val tag: String,
    val label: String,
    val subtitle: String?,
    val selected: Boolean,
    val enabled: Boolean,
)

internal val SelectionGroup<*>.isCollapsed: Boolean
    get() = presentationFor(isIosPlatform) != SelectionPresentation.Inline

/** A collapsed selection group, drawn once at its first visible option. */
@Composable
internal fun CollapsedSelectionGroup(group: SelectionGroup<*>) {
    val options = group.selectableFields.filter { it.isVisibleState.value }
    val choices = options.map { option ->
        SelectionChoice(
            id = option.key,
            tag = option.key,
            label = option.labelForValueState.value?.getString() ?: option.selectableValue.toString(),
            subtitle = option.subtitle?.getString(),
            selected = option.valueState.value != null,
            enabled = option.isEnabledState.value,
        )
    }.toImmutableList()
    val errors = group.errorsState.value + options.flatMap { it.errorsState.value }
    val presentation = group.presentationFor(isIosPlatform)
    val onPick: (SelectionChoice) -> Unit = { choice -> options.firstOrNull { it.key == choice.id }?.select() }
    val modifier = Modifier.testTag(group.key).fillMaxWidth().fieldPadding()
    if (presentation == SelectionPresentation.Segmented) {
        SegmentedSelection(choices, onPick, modifier)
    } else {
        CollapsedSelection(
            key = group.key,
            title = group.title?.getString().orEmpty(),
            text = choices.filter { it.selected }.joinToString { it.label },
            isPlaceholder = false,
            choices = choices,
            presentation = presentation,
            multiple = group is SelectionGroup.Multiple,
            enabled = choices.any { it.enabled },
            hasErrors = errors.any { !it.asWarning },
            hasWarnings = errors.any { it.asWarning },
            onPick = onPick,
            modifier = modifier,
        )
    }
}

/** A single choice field in its presentation; [SelectionPresentation.Menu] keeps the dropdown. */
@Composable
internal fun SingleChoiceContent(field: SingleChoiceField<*>, modifier: Modifier) {
    when (val presentation = field.presentationFor(isIosPlatform)) {
        SelectionPresentation.Menu -> DropdownField(field, modifier)
        SelectionPresentation.Inline -> InlineSingleChoice(field)
        else -> {
            val options by field.possibleValuesState
            val selected by field.selectedOption
            val enabled by field.isEnabledState
            val placeholder = field.placeholder?.getString().orEmpty()
            val choices = options.map { option ->
                SelectionChoice(
                    id = option.id,
                    tag = "${field.key}-option-${option.id}",
                    label = option.label.getString().orEmpty(),
                    subtitle = null,
                    selected = option.id == selected?.id,
                    enabled = enabled,
                )
            }.toImmutableList()
            val onPick: (SelectionChoice) -> Unit = { choice ->
                field.selectOption(if (choice.selected && field.isClearable) null else choice.id)
            }
            if (presentation == SelectionPresentation.Segmented) {
                Column {
                    field.placeholder?.let {
                        LabelField(it, FieldDefaults.labelFieldAppearance(), enabled, Modifier.testTag("${field.key}-title"))
                    }
                    SegmentedSelection(choices, onPick, modifier)
                }
            } else {
                CollapsedSelection(
                    key = field.key,
                    title = placeholder,
                    text = selected?.label?.getString() ?: placeholder,
                    isPlaceholder = selected == null,
                    choices = choices,
                    presentation = presentation,
                    enabled = enabled,
                    hasErrors = field.hasErrorsState.value,
                    hasWarnings = field.hasWarningsState.value,
                    onPick = onPick,
                    modifier = modifier,
                )
            }
        }
    }
}

/** A Material 3 segmented row; like the platform controls, tapping the selected segment keeps it. */
@Composable
private fun SegmentedSelection(
    choices: ImmutableList<SelectionChoice>,
    onPick: (SelectionChoice) -> Unit,
    modifier: Modifier,
) {
    SingleChoiceSegmentedButtonRow(modifier) {
        choices.forEachIndexed { index, choice ->
            SegmentedButton(
                selected = choice.selected,
                onClick = { if (!choice.selected) onPick(choice) },
                shape = SegmentedButtonDefaults.itemShape(index, choices.size),
                modifier = Modifier.testTag(choice.tag),
                enabled = choice.enabled,
                label = { Text(choice.label, maxLines = 1) },
            )
        }
    }
}

/** Options of a single choice field as radio rows under its placeholder, like an inline group. */
@Composable
private fun InlineSingleChoice(field: SingleChoiceField<*>) {
    val options by field.possibleValuesState
    val selected by field.selectedOption
    val enabled by field.isEnabledState
    val hasErrors by field.hasErrorsState
    val hasWarnings by field.hasWarningsState
    Column(Modifier.testTag(field.key)) {
        field.placeholder?.let {
            LabelField(it, FieldDefaults.labelFieldAppearance(), enabled, Modifier.testTag("${field.key}-title"))
        }
        options.forEach { option ->
            val isSelected = option.id == selected?.id
            SingleSelectionField(
                isSelected = isSelected,
                label = option.label.getString().orEmpty(),
                appearance = SelectableFieldAppearance(),
                modifier = Modifier.testTag("${field.key}-option-${option.id}"),
                enabled = enabled,
                isError = hasErrors,
                hasWarning = hasWarnings,
                toggleRow = { field.selectOption(if (isSelected && field.isClearable) null else option.id) },
            )
        }
    }
}

/**
 * A single selection collapsed into the dropdown row. A menu anchors to it, a sheet or a
 * full-screen page opens over the form. Picking an option or losing availability closes them.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CollapsedSelection(
    key: String,
    title: String,
    text: String,
    isPlaceholder: Boolean,
    choices: ImmutableList<SelectionChoice>,
    presentation: SelectionPresentation,
    enabled: Boolean,
    hasErrors: Boolean,
    hasWarnings: Boolean,
    onPick: (SelectionChoice) -> Unit,
    modifier: Modifier,
    multiple: Boolean = false,
) {
    var open by remember { mutableStateOf(false) }
    LaunchedEffect(enabled) {
        if (!enabled) open = false
    }
    // A multiple selection stays open to toggle several options.
    val pick = { choice: SelectionChoice ->
        onPick(choice)
        if (!multiple) open = false
    }
    DropdownBox(
        text = text,
        isPlaceholder = isPlaceholder,
        expanded = open,
        enabled = enabled,
        hasErrors = hasErrors,
        hasWarnings = hasWarnings,
        onOpen = { open = true },
        modifier = modifier,
    ) {
        if (presentation == SelectionPresentation.Menu) {
            DropdownMenu(expanded = open, onDismissRequest = { open = false }, modifier = Modifier.popupTestTags()) {
                choices.forEach { choice ->
                    DropdownMenuItem(
                        text = { ChoiceText(choice) },
                        onClick = { pick(choice) },
                        leadingIcon = { RadioButton(selected = choice.selected, onClick = null, enabled = choice.enabled) },
                        enabled = choice.enabled,
                        modifier = Modifier.testTag(choice.tag),
                    )
                }
            }
        }
    }
    if (open && presentation == SelectionPresentation.Sheet) {
        ModalBottomSheet(onDismissRequest = { open = false }) {
            Column(Modifier.popupTestTags().padding(bottom = 16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f).padding(horizontal = 24.dp, vertical = 8.dp),
                    )
                    if (multiple) {
                        IconButton(onClick = { open = false }, modifier = Modifier.testTag("$key-close")) {
                            Icon(painterResource(Res.drawable.ic_clear), contentDescription = stringResource(Res.string.close))
                        }
                    }
                }
                ChoiceRows(choices, pick, multiple)
            }
        }
    }
    if (open && presentation == SelectionPresentation.Page) {
        Dialog(onDismissRequest = { open = false }, properties = fullScreenDialogProperties()) {
            FullScreenDialogBars(lightBars = MaterialTheme.colorScheme.surface.luminance() > 0.5f)
            Surface(Modifier.fillMaxSize().popupTestTags()) {
                Column(Modifier.windowInsetsPadding(WindowInsets.safeDrawing)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { open = false }, modifier = Modifier.testTag("$key-close")) {
                            Icon(painterResource(Res.drawable.ic_clear), contentDescription = stringResource(Res.string.close))
                        }
                        Text(title, style = MaterialTheme.typography.titleLarge)
                    }
                    ChoiceRows(choices, pick, multiple)
                }
            }
        }
    }
}

@Composable
private fun ChoiceRows(choices: ImmutableList<SelectionChoice>, pick: (SelectionChoice) -> Unit, multiple: Boolean) {
    Column(Modifier.verticalScroll(rememberScrollState())) {
        choices.forEach { choice ->
            val modifier = Modifier.testTag(choice.tag).padding(horizontal = 8.dp)
            if (multiple) {
                MultipleSelectionField(
                    isSelected = choice.selected,
                    label = choice.label,
                    appearance = SelectableFieldAppearance(),
                    modifier = modifier,
                    enabled = choice.enabled,
                    hasWarnin = false,
                    subtitle = choice.subtitle,
                    toggleRow = { pick(choice) },
                )
            } else {
                SingleSelectionField(
                    isSelected = choice.selected,
                    label = choice.label,
                    appearance = SelectableFieldAppearance(),
                    modifier = modifier,
                    enabled = choice.enabled,
                    subtitle = choice.subtitle,
                    toggleRow = { pick(choice) },
                )
            }
        }
    }
}

@Composable
private fun ChoiceText(choice: SelectionChoice) {
    Column {
        Text(choice.label)
        choice.subtitle?.let {
            Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
