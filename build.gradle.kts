// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    // Applies the Android application plugin.
    // Project: https://developer.android.com/build
    alias(libs.plugins.android.application) apply false

    // Applies the Android library plugin.
    // Project: https://developer.android.com/build
    // https://developer.android.com/studio/projects/android-library
    alias(libs.plugins.android.library) apply false

    // Applies the Kotlin Android plugin.
    // Project: https://kotlinlang.org/docs/android-overview.html
    alias(libs.plugins.kotlin.android) apply false

    // Applies the Kotlin Compose plugin.
    // Project: https://developer.android.com/jetpack/compose
    alias(libs.plugins.kotlin.compose) apply false

    // Applies the Kotlin Parcelize plugin.
    // Project: https://developer.android.com/kotlin/parcelize
    alias(libs.plugins.kotlin.parcelize) apply false

    // Applies Kotlin serialization plugin.
    // Project: https://github.com/Kotlin/kotlinx.serialization
    alias(libs.plugins.kotlin.serialization) apply false

    // Applies the Kotlin KAPT (Kotlin Annotation Processing Tool) plugin.
    // Project: https://kotlinlang.org/docs/kapt.html
    alias(libs.plugins.kotlin.kapt) apply false

    // Applies the Kotlin Symbol Processing (KSP) plugin.
    // Project: https://github.com/google/ksp
    alias(libs.plugins.ksp) apply false

    // Room Gradle Plugin
    // https://developer.android.com/jetpack/androidx/releases/room#gradle-plugin
    alias(libs.plugins.androidx.room) apply false

    alias(libs.plugins.google.services) apply false

    // Firebase Crashlytics Gradle plugin
    // https://firebase.google.com/docs/crashlytics/get-started?platform=android#add-sdk
    alias(libs.plugins.firebase.crashlytics) apply false

    // Kover coverage for Kotlin projects
    // Project: https://github.com/Kotlin/kotlinx-kover
    alias(libs.plugins.kotlinx.kover) apply false
}
