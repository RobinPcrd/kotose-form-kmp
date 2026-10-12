plugins {
    kotlin("multiplatform")
    id("com.android.kotlin.multiplatform.library")
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.skie)
}

kotlin {
    jvmToolchain(17)

    android {
        namespace = "io.github.robinpcrd.kotose.form.example.shared"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        androidResources {
            enable = true
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
            binaryOptions["bundleId"] = "io.github.robinpcrd.kotose.form.example"
            // The SwiftUI shell renders catalog pages natively and reads StrRes labels by name.
            // Exporting only the SwiftUI module compiles the bundled renderer like a minimal consumer.
            export(projects.kotoseFormSwiftui)
            export(libs.kotose.resources)
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.jb.compose.ui.tooling.preview)
        }
        commonMain.dependencies {
            api(projects.kotoseFormCore)
            api(projects.kotoseFormPhoneBackend)
            api(projects.kotoseForm)
            api(projects.kotoseFormSwiftui)
            api(libs.kotose.resources)

            implementation(libs.jb.compose.runtime)
            implementation(libs.jb.compose.foundation)
            implementation(libs.jb.compose.material3)
            implementation(libs.jb.compose.ui)
            implementation(libs.jb.compose.ui.tooling.preview)
            implementation(libs.jb.compose.resources)
            implementation(libs.kotlinx.collections.immutable)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.serialization.json)

            // navigation3-ui brings the runtime (NavKey, entryProvider, rememberNavBackStack) transitively.
            implementation(libs.androidx.navigation3.ui)
        }
    }
}

skie {
    analytics { enabled.set(false) }
}

compose.resources {
    packageOfResClass = "io.github.robinpcrd.kotose.form.example"
    generateResClass = always
}
