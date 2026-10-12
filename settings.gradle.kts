rootProject.name = "kotose-form-kmp"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

plugins {
    id("de.fayard.refreshVersions") version "0.60.6"
}

// Skip the example app when building the libraries
if (gradle.startParameter.taskNames.none { it.startsWith(":example:") }) {
    startParameter.excludedTaskNames.addAll(
        listOf(
            ":example:composeApp:build",
            ":example:composeApp:assemble",
            ":example:androidApp:build",
            ":example:androidApp:assembleDebug",
            ":example:androidApp:assembleDebugUnitTest",
            ":example:androidApp:assembleDebugAndroidTest",
            ":example:androidApp:assemble",
        ),
    )
}

include(
    ":kotose-form-core",
    ":kotose-form-phone",
    ":kotose-form-phone-backend",
    ":kotose-form",
    ":kotose-form-swiftui",
    ":example:composeApp",
    ":example:androidApp",
)
