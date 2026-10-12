/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListLayoutInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import io.github.robinpcrd.kotose.form.Field
import io.github.robinpcrd.kotose.form.Form
import io.github.robinpcrd.kotose.form.SealedFormEvents
import io.github.robinpcrd.kotose.form.compose.fields.selectable.isCollapsed
import io.github.robinpcrd.kotose.form.fields.SelectionOptionField
import io.github.robinpcrd.kotose.form.groups.SelectionGroup
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop

val LocalForm = compositionLocalOf<Form> { error("Not in a form") }

@Composable
fun FormScreen(
    modifier: Modifier = Modifier,
    lazyListState: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    reverseLayout: Boolean = false,
    verticalArrangement: Arrangement.Vertical =
        if (!reverseLayout) Arrangement.Top else Arrangement.Bottom,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    hideKeyboardOnDrag: Boolean = true,
    onFormEvent: ((SealedFormEvents) -> Unit)? = null,
    form: Form = LocalForm.current,
    fieldView: @Composable (field: Field<*>) -> Unit = { field ->
        DefaultFieldView(field)
    }
) {
    key(form.key) {
        val fields by form.fields
        val dismissKeyboardEvent by form.dismissKeyboardEvent
        val rememberedFormEventsLambda by rememberUpdatedState(newValue = onFormEvent)

        rememberedFormEventsLambda?.let { lambda ->
            LaunchedEffect(form.events) {
                form.events.collect(lambda)
            }
        }

        DisposableEffect(form) {
            onDispose { form.stopEditing() }
        }

        LazyColumn(
            modifier = modifier,
            state = lazyListState,
            contentPadding = contentPadding,
            reverseLayout = reverseLayout,
            verticalArrangement = verticalArrangement,
            horizontalAlignment = horizontalAlignment,
        ) {
            items(fields, key = Field<*>::key) { field ->
                fieldView(field)
            }
        }

        FormNavigationEffect(form, lazyListState)

        LaunchedEffect(form, lazyListState) {
            // The keyboard shrinks the list, or adds bottom content padding when the list stays under it.
            snapshotFlow { lazyListState.layoutInfo.let { it.viewportSize.height - it.afterContentPadding } }
                .drop(1)
                .collectLatest {
                    // Re-resolve after keyboard resizing so messages below the input remain visible,
                    // but leave the list alone when the focused row is already fully in view: Compose
                    // briefly hides and re-shows the keyboard when focus moves between inputs, and
                    // scrolling on each resize would turn that into a visible jump.
                    val index = form.fields.value.indexOfFirst { it == form.currentFocus }
                    if (index < 0) return@collectLatest
                    val layout = lazyListState.layoutInfo
                    val row = layout.visibleItemsInfo.firstOrNull { it.index == index }
                    if (row == null || !isRowInView(row.offset, row.size, layout)) lazyListState.scrollToItem(index)
                }
        }

        if (hideKeyboardOnDrag) {
            HideKeyboardOnDragEffect(lazyListState, form)
        }

        DismissKeyboardEffect(dismissKeyboardEvent, form::onKeyboardDismiss)
    }
}

@Composable
internal fun FormNavigationEffect(
    form: Form,
    lazyListState: LazyListState
) {
    val request by form.navigationRequest

    LaunchedEffect(form, request?.id) {
        request?.let { navigation ->
            val index = form.rowIndex(form.navigationIndex(navigation.id))
            if (index >= 0) lazyListState.scrollToItem(index)
            form.completeNavigation(navigation.id)
        }
    }
}

/** Whether a row lies within the viewport, above the bottom content padding, which the keyboard may cover. */
internal fun isRowInView(offset: Int, size: Int, layout: LazyListLayoutInfo): Boolean =
    isRowInView(offset, size, layout.viewportStartOffset, layout.viewportEndOffset - layout.afterContentPadding)

internal fun isRowInView(offset: Int, size: Int, start: Int, end: Int): Boolean =
    offset >= start && offset + size <= end

/** An option of a collapsed group has an empty row; the group's row sits at its first visible option. */
private fun Form.rowIndex(index: Int): Int {
    val option = fields.value.getOrNull(index) as? SelectionOptionField<*>
    val group = option?.selectionGroup?.takeIf { it.isCollapsed }
    val first = group?.selectableFields?.firstOrNull { it.isVisible }
    return first?.let { fields.value.indexOf(it) }?.takeIf { it >= 0 } ?: index
}

@Composable
internal fun HideKeyboardOnDragEffect(
    lazyListState: LazyListState,
    form: Form,
) {
    val isDragged by lazyListState.interactionSource.collectIsDraggedAsState()
    LaunchedEffect(isDragged) {
        if (isDragged) {
            form.stopEditing(true)
        }
    }
}

@Composable
internal fun DismissKeyboardEffect(
    dismissKeyboardEvent: Form.DismissKeyboardEvent?,
    onDismiss: () -> Unit,
) {
    val focusManager = LocalFocusManager.current

    LaunchedEffect(dismissKeyboardEvent) {
        if (dismissKeyboardEvent != null) {
            focusManager.clearFocus()
            onDismiss()
        }
    }
}

@Composable
fun DefaultFieldView(field: Field<*>) {
    val isFieldVisible by field.isVisibleState

    AnimatedVisibility(
        isFieldVisible,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
    ) {
        key(field.tag) {
            FieldContent(field)
        }
    }
}
