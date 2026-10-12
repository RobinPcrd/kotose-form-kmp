plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.kotlin.multiplatform.library")
}

// Allow modules with no host tests to build without failure
tasks.withType<Test>().configureEach {
    failOnNoDiscoveredTests = false
}

kotlin {
    jvmToolchain(17)

    applyDefaultHierarchyTemplate()

    android {
        namespace = "io.github.robinpcrd.kotose.form"
        compileSdk = (findProperty("android.compileSdk") as String).toInt()
        minSdk = (findProperty("android.minSdk") as String).toInt()

        lint {
            targetSdk = (findProperty("android.targetSdk") as String).toInt()
        }

        androidResources {
            enable = true
        }

        withHostTestBuilder {

        }.configure {
            isIncludeAndroidResources = true
        }

        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }.configure {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }

        //defaultConfig {
        //    lint {
        //        targetSdk = (findProperty("android.targetSdk") as String).toInt()
        //    }
        //}
        //compileOptions {
        //    sourceCompatibility = JavaVersion.VERSION_11
        //    targetCompatibility = JavaVersion.VERSION_11
        //}
    }

    targets.all {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    progressiveMode.set(true)
                    freeCompilerArgs.addAll("-Xexpect-actual-classes")
                }
            }
        }
    }

    //androidTarget {
    //    compilations.all {
    //        compileTaskProvider.configure {
    //            compilerOptions {
    //                jvmTarget.set(JvmTarget.JVM_11)
    //            }
    //        }
    //    }
    //
    //    publishLibraryVariants("release")
    //}

    iosArm64()
    iosSimulatorArm64()
    //macosX64()
    //macosArm64()

    //jvm("desktop") {
    //    compilations.all {
    //        compileTaskProvider.configure {
    //            compilerOptions {
    //                jvmTarget.set(JvmTarget.JVM_11)
    //            }
    //        }
    //    }
    //}

    //js(IR) {
    //    browser()
    //}

    //@Suppress("OPT_IN_USAGE")
    //wasmJs {
    //    browser()
    //}

    sourceSets {

        //val desktopMain by getting

        val skikoMain by creating {
            dependsOn(commonMain.get())
            appleMain.get().dependsOn(this)
            //desktopMain.dependsOn(this)
        }
        val nonIosMain by creating {
            dependsOn(commonMain.get())
            //macosMain.get().dependsOn(this)
            androidMain.get().dependsOn(this)
            //desktopMain.dependsOn(this)
        }

        val darwinMain by creating {
            dependsOn(commonMain.get())
            iosMain.get().dependsOn(this)
            //macosMain.get().dependsOn(this)
        }

        val jvmMain by creating {
            dependsOn(commonMain.get())
            //desktopMain.dependsOn(this)
            androidMain.get().dependsOn(this)
        }
    }
}
