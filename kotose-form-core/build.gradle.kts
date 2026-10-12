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
        namespace = "io.github.robinpcrd.kotose.form.core"
        compileSdk = 37
        minSdk = 24
        withHostTestBuilder {}
    }
    iosArm64()
    iosSimulatorArm64()
    sourceSets {
        commonMain.dependencies {
            api(libs.jb.compose.runtime)
            api(libs.kotlinx.collections.immutable)
            api(libs.kotlinx.datetime)
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
