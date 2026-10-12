plugins {
    kotlin("multiplatform")
    id("com.android.kotlin.multiplatform.library")
    id("publishing-convention")
}

kotlin {
    jvmToolchain(17)
    @OptIn(org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation::class)
    abiValidation()
    android {
        namespace = "io.github.robinpcrd.kotose.form.phone"
        compileSdk = 37
        minSdk = 24
        androidResources { enable = true }
        withHostTestBuilder {}.configure { isIncludeAndroidResources = true }
    }
    iosArm64()
    iosSimulatorArm64()
    sourceSets {
        commonMain.dependencies {
            api(project(":kotose-form-core"))
            api(libs.kotlinx.collections.immutable)
            api(libs.kotose.resources)
            api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
        }
        commonTest.dependencies { implementation(kotlin("test")) }
    }
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
