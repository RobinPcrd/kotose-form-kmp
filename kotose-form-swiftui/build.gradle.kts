plugins {
    kotlin("multiplatform")
    id("com.android.kotlin.multiplatform.library")
    alias(libs.plugins.composeCompiler)
    id("co.touchlab.skie") version "0.10.15"
    id("publishing-convention")
}

kotlin {
    jvmToolchain(17)
    @OptIn(org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation::class)
    abiValidation()
    android {
        namespace = "io.github.robinpcrd.kotose.form.swiftui"
        compileSdk = 37
        minSdk = 24
        withHostTestBuilder {}.configure { isIncludeAndroidResources = true }
    }
    iosArm64()
    iosSimulatorArm64()
    sourceSets {
        commonMain.dependencies {
            api(project(":kotose-form-phone"))
            implementation(libs.jb.compose.runtime)
            implementation(libs.kotlinx.collections.immutable)
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
        }
        commonTest.dependencies { implementation(kotlin("test")) }
    }
}

skie { analytics { enabled.set(false) } }

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
