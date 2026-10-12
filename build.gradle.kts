plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    //alias(libs.plugins.androidLibrary) apply false
    id(libs.plugins.kotlinMultiplatform.get().pluginId) apply false
    id(libs.plugins.androidApplication.get().pluginId) apply false
    //alias(libs.plugins.androidApplication) apply false
    //alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    //alias(libs.plugins.kotlinMultiplatform) apply false
    id(libs.plugins.androidKotlinMultiplatformLibrary.get().pluginId) apply false
    //alias(libs.plugins.androidLint) apply false
    alias(libs.plugins.kotlinSerialization) apply false
    id(libs.plugins.dokka.get().pluginId)
}

dependencies {
    dokka(project(":kotose-form-core"))
    dokka(project(":kotose-form-phone"))
    dokka(project(":kotose-form-phone-backend"))
    dokka(project(":kotose-form"))
    dokka(project(":kotose-form-swiftui"))
}
