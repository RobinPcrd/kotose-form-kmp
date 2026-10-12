plugins {
    kotlin("multiplatform")
    id("com.android.kotlin.multiplatform.library")
    id("publishing-convention")
}

// Local development can import uncommitted Swift sources. Published artifacts must
// instead identify a Git package that can be resolved without this checkout.
val bridgeRepositoryOverride = providers.gradleProperty("kotosePhoneBridgeRepository").orNull
val bridgeVersionOverride = providers.gradleProperty("kotosePhoneBridgeVersion").orNull
require((bridgeRepositoryOverride == null) == (bridgeVersionOverride == null)) {
    "Set kotosePhoneBridgeRepository and kotosePhoneBridgeVersion together."
}
val usesLocalBridge = version.toString().endsWith("-LOCAL") && bridgeRepositoryOverride == null

tasks.withType<org.gradle.api.publish.maven.tasks.AbstractPublishToMaven>().configureEach {
    // Locals keep the action free of build script references, which the configuration cache rejects.
    val localBridge = usesLocalBridge
    val overridden = bridgeRepositoryOverride != null
    doFirst {
        check(!localBridge) {
            "Cannot publish local SwiftPM paths. Set both kotosePhoneBridge properties for a local fixture, or VERSION for a release."
        }
        if (overridden) {
            check(this is org.gradle.api.publish.maven.tasks.PublishToMavenLocal ||
                (this is org.gradle.api.publish.maven.tasks.PublishToMavenRepository && repository.url.scheme == "file")) {
                "Native fixture overrides are only allowed for local Maven publication."
            }
        }
    }
}

useSwiftPMResourcesInSimulatorTests()

kotlin {
    swiftPMDependencies {
        if (usesLocalBridge) {
            localSwiftPackage(
                directory = rootProject.layout.projectDirectory.dir("kotose-form/spm/KotosePhoneNumberKitBridge"),
                products = listOf("KotosePhoneNumberKitBridge"),
            )
        } else {
            swiftPackage(
                url = url(bridgeRepositoryOverride ?: "https://github.com/$githubRepository.git"),
                version = exact(bridgeVersionOverride ?: project.version.toString().removePrefix("v")),
                products = listOf(product("KotosePhoneNumberKitBridge")),
            )
        }
    }


    jvmToolchain(17)
    @OptIn(org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation::class)
    abiValidation()
    android {
        namespace = "io.github.robinpcrd.kotose.form.phone.backend"
        compileSdk = 37
        minSdk = 24
        withHostTestBuilder {}.configure { isIncludeAndroidResources = true }
    }
    iosArm64()
    iosSimulatorArm64()
    sourceSets {
        commonMain.dependencies {
            api(project(":kotose-form-phone"))
        }
        androidMain.dependencies { implementation(libs.libphonenumber) }
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
