/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

import java.nio.file.Files
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeSimulatorTest

/**
 * Points the iOS simulator tests at the SwiftPM resource bundles, such as PhoneNumberKit's
 * metadata. Unlike an Xcode app, Kotlin's simulator test executable has no resource-copy phase,
 * so SwiftPM's debug resource accessor reads PACKAGE_RESOURCE_BUNDLE_PATH instead.
 *
 * Kotlin builds the SwiftPM products once per dependency fingerprint in a shared folder, and the
 * fingerprint only exists once its import task ran. The tests therefore read a fixed
 * `build/swiftpm-test-resources` link, which a task refreshes right before them from the plugin's
 * own import task. That task class is internal to the plugin, so its two public properties are
 * read by name. Only the tail inside the folder is Kotlin's internal xcodebuild layout.
 */
fun Project.useSwiftPMResourcesInSimulatorTests() {
    val resources = layout.buildDirectory.dir("swiftpm-test-resources").get().asFile

    val link = tasks.register("linkSwiftPMTestResources") {
        // Looked up here: the plugin registers these tasks after the kotlin block. The SwiftPM
        // products, bundles included, are built when the test binary is linked.
        val importTask = project.tasks.getByName("convertSyntheticImportProjectIntoDefFileIphonesimulator")
        dependsOn(importTask, project.tasks.getByName("linkDebugTestIosSimulatorArm64"))
        @Suppress("UNCHECKED_CAST")
        val dumpsDir = (importTask.property("fingerprintsXcodeDumpsDir") as Provider<Directory>).get().asFile
        @Suppress("UNCHECKED_CAST")
        val fingerprintFile = (importTask.property("xcodebuildFingerprint") as Provider<RegularFile>).get().asFile
        inputs.file(fingerprintFile)
        doLast {
            // The fingerprint file holds one fingerprint per line; the dump folder is named after one.
            val candidates = fingerprintFile.readLines().map(String::trim).filter(String::isNotEmpty).map {
                dumpsDir.resolve("$it/swiftImportDd/dd_iphonesimulator/Build/Products/Debug-iphonesimulator")
            }
            val products = checkNotNull(candidates.firstOrNull { it.isDirectory }) {
                "No SwiftPM products in $candidates"
            }
            resources.parentFile.mkdirs()
            Files.deleteIfExists(resources.toPath())
            Files.createSymbolicLink(resources.toPath(), products.toPath())
        }
    }

    tasks.withType<KotlinNativeSimulatorTest>().configureEach {
        dependsOn(link)
        environment("SIMCTL_CHILD_PACKAGE_RESOURCE_BUNDLE_PATH", resources.absolutePath)
    }
}
