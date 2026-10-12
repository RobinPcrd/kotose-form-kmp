/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.example

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import io.github.robinpcrd.kotose.form.example.catalog.Catalog
import io.github.robinpcrd.kotose.form.example.theme.ExampleTheme
import io.github.robinpcrd.kotose.form.example.ui.HomeScreen
import io.github.robinpcrd.kotose.form.example.ui.PageScreen
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

@Serializable
data object HomeKey : NavKey

@Serializable
data class PageKey(val id: String) : NavKey

/** Non-JVM targets have no reflection, so the back stack needs explicit key serializers. */
private val navConfiguration = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(HomeKey::class, HomeKey.serializer())
            subclass(PageKey::class, PageKey.serializer())
        }
    }
}

/** Compose entry for Android and for the full-Compose iOS check: catalog home, then one page. */
@Composable
fun App(modifier: Modifier = Modifier, catalog: Catalog = remember { Catalog() }) {
    val backStack = rememberNavBackStack(navConfiguration, HomeKey)
    ExampleTheme {
        Surface(modifier) {
            NavDisplay(
                backStack = backStack,
                onBack = { backStack.removeLastOrNull() },
                entryProvider = entryProvider {
                    entry<HomeKey> { HomeScreen(catalog) { backStack.add(PageKey(it.id)) } }
                    entry<PageKey> { key ->
                        PageScreen(catalog.byId(key.id), showTopBar = true) { backStack.removeLastOrNull() }
                    }
                },
            )
        }
    }
}
