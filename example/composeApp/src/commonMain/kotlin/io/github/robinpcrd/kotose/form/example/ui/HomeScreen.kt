/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

@file:OptIn(ExperimentalMaterial3Api::class)

package io.github.robinpcrd.kotose.form.example.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import io.github.robinpcrd.kotose.form.example.Res
import io.github.robinpcrd.kotose.form.example.app_title
import io.github.robinpcrd.kotose.form.example.catalog.Catalog
import io.github.robinpcrd.kotose.form.example.catalog.CatalogPage
import io.github.robinpcrd.kotose.form.example.catalog.Look
import io.github.robinpcrd.kotose.form.example.section_behaviors
import io.github.robinpcrd.kotose.form.example.section_fields
import org.jetbrains.compose.resources.stringResource

/** Catalog home: one row per page, grouped by section. */
@Composable
fun HomeScreen(catalog: Catalog, onOpen: (CatalogPage) -> Unit) {
    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(Res.string.app_title)) }) },
        contentWindowInsets = WindowInsets.safeDrawing,
    ) { padding ->
        LazyColumn(Modifier.fillMaxSize(), contentPadding = padding) {
            item(key = "look") {
                ListItem(
                    headlineContent = { Text(Look.label.getStringOrEmpty()) },
                    trailingContent = {
                        Switch(
                            checked = Look.brand,
                            onCheckedChange = { Look.brand = it },
                            modifier = Modifier.testTag("look-brand"),
                        )
                    },
                )
                HorizontalDivider()
            }
            CatalogPage.Section.entries.forEach { section ->
                val pages = catalog.pages.filter { it.section == section }
                if (pages.isNotEmpty()) {
                    item(key = section.name) {
                        Text(
                            text = stringResource(
                                when (section) {
                                    CatalogPage.Section.FIELDS -> Res.string.section_fields
                                    CatalogPage.Section.BEHAVIORS -> Res.string.section_behaviors
                                },
                            ),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp),
                        )
                    }
                    items(pages, key = CatalogPage::id) { page ->
                        ListItem(
                            headlineContent = { Text(page.title.getStringOrEmpty()) },
                            modifier = Modifier.clickable { onOpen(page) }.testTag("page-${page.id}"),
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}
