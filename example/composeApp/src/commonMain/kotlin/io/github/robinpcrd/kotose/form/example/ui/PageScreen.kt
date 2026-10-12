/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package io.github.robinpcrd.kotose.form.example.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.robinpcrd.kotose.form.compose.DefaultFieldView
import io.github.robinpcrd.kotose.form.compose.FormScreen
import io.github.robinpcrd.kotose.form.example.Res
import io.github.robinpcrd.kotose.form.example.back
import io.github.robinpcrd.kotose.form.example.catalog.CatalogPage
import io.github.robinpcrd.kotose.form.example.catalog.fields.StepperField
import io.github.robinpcrd.kotose.form.example.ic_arrow_back
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * Whether the action bar rides above the keyboard. The iOS hosts turn it off: the keyboard bar of
 * the form takes over there, and the action bar stays under the keyboard like the SwiftUI page.
 */
val LocalActionBarRidesKeyboard = staticCompositionLocalOf { true }

/**
 * One catalog page: the shared form above a bottom action bar. The form viewport ends at the bar,
 * or at the keyboard when it is higher.
 */
@Composable
fun PageScreen(page: CatalogPage, showTopBar: Boolean, onBack: () -> Unit = {}) {
    val result by page.result.collectAsState()
    val ridesKeyboard = LocalActionBarRidesKeyboard.current
    val barInsets = if (ridesKeyboard) WindowInsets.safeDrawing else WindowInsets.safeDrawing.exclude(WindowInsets.ime)
    Scaffold(
        // Without its top bar the page sits below the host's own bars, which take the top inset.
        contentWindowInsets = if (showTopBar) {
            WindowInsets.safeDrawing
        } else {
            WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)
        },
        topBar = {
            if (showTopBar) {
                TopAppBar(
                    title = { Text(page.title.getStringOrEmpty()) },
                    navigationIcon = {
                        IconButton(onClick = onBack, modifier = Modifier.testTag("back")) {
                            Icon(painterResource(Res.drawable.ic_arrow_back), contentDescription = stringResource(Res.string.back))
                        }
                    },
                )
            }
        },
        bottomBar = {
            Column(
                Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(barInsets.only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                result?.let { Text(it.getStringOrEmpty(), Modifier.testTag("result")) }
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    page.actions.forEach { action ->
                        OutlinedButton(onClick = action.run, modifier = Modifier.testTag("action-${action.id}")) {
                            Text(action.label.getStringOrEmpty())
                        }
                    }
                }
            }
        },
    ) { padding ->
        FormScreen(
            form = page.form,
            modifier = Modifier.fillMaxSize().padding(padding),
            // Under the keyboard the form keeps its height, so it shows behind the keyboard bar,
            // and only gains room at its end.
            contentPadding = PaddingValues(top = 8.dp, bottom = 8.dp + if (ridesKeyboard) 0.dp else keyboardOverlap(padding)),
        ) { field -> if (field is StepperField) StepperRow(field) else DefaultFieldView(field) }
    }
}

/** How far the keyboard rises above the bottom padding of the page content. */
@Composable
private fun keyboardOverlap(padding: PaddingValues): Dp {
    val density = LocalDensity.current
    val overlap = WindowInsets.ime.getBottom(density) - with(density) { padding.calculateBottomPadding().roundToPx() }
    return with(density) { overlap.coerceAtLeast(0).toDp() }
}

/** Consumer-owned rendering of the custom field; behavior stays on the Kotlin class. */
@Composable
private fun StepperRow(field: StepperField) {
    val value by field.valueState
    val enabled by field.isEnabledState
    val visible by field.isVisibleState
    if (visible) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp).testTag(field.key),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(field.label.getStringOrEmpty(), Modifier.weight(1f))
            OutlinedButton(onClick = field::decrement, enabled = enabled, modifier = Modifier.testTag("${field.key}-minus")) { Text("-") }
            Text((value ?: 0).toString(), Modifier.testTag("${field.key}-value"))
            OutlinedButton(onClick = field::increment, enabled = enabled, modifier = Modifier.testTag("${field.key}-plus")) { Text("+") }
        }
    }
}
