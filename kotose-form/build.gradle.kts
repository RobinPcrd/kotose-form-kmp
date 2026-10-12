plugins {
    id("multiplatform-module-convention")
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
    id("publishing-convention")
}

publishing {
    repositories {
        maven {
            name = "Integration"
            url = uri(providers.gradleProperty("kotoseLocalRepository").getOrElse(
                rootProject.layout.buildDirectory.dir("integration-maven").get().asFile.absolutePath
            ))
        }
    }
}

kotlin {
    @OptIn(org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation::class)
    abiValidation()
    sourceSets {
        commonMain {
            dependencies {
                api(project(":kotose-form-phone"))
                implementation(libs.kotlin.stdlib)
                implementation(libs.kotlinx.collections.immutable)
                implementation(libs.kotlinx.datetime)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.jb.compose.ui)
                implementation(libs.jb.compose.foundation)
                implementation(libs.jb.compose.runtime)
                implementation(libs.jb.compose.resources)
                implementation(libs.jb.compose.material3)
                implementation(libs.jb.compose.material.icons.core)
                api(libs.kotose.resources)
            }
        }

        androidMain {
            dependencies {
                // Compose Multiplatform 1.12.0 resolves to Jetpack Compose 1.12.0 on Android, which
                // hides and re-shows the keyboard when focus moves between text fields (fixed in
                // 1.12.1). Depending on the patch raises every consumer to it until the next CMP.
                implementation(libs.androidx.compose.ui)
                implementation(libs.androidx.compose.foundation)
                implementation(libs.androidx.compose.foundation.layout)
                implementation(libs.androidx.compose.runtime)
                implementation(libs.androidx.compose.animation)
                implementation(libs.google.android.material)
                implementation(libs.androidx.fragment)
                implementation(libs.androidx.activity.compose)
                implementation(libs.androidx.appcompat)
                implementation(libs.androidx.core.ktx)
                implementation(libs.kotose.core)
                implementation(libs.jb.compose.ui.tooling.preview)
            }
        }

        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }

        // Compose UI tests run on the iOS simulator; Android host tests have no UI runtime.
        iosTest {
            dependencies {
                implementation(libs.jb.compose.ui.test)
            }
        }

        getByName("androidDeviceTest") {
            dependencies {
                implementation(libs.androidx.test.junit)
                implementation(libs.androidx.espresso.core)
            }
        }
    }
}
