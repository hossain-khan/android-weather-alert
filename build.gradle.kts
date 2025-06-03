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

    // Applies the Anvil plugin for Dagger dependency injection.
    // Project: https://github.com/square/anvil
    // Also see: https://github.com/ZacSweers/anvil/blob/main/FORK.md
    alias(libs.plugins.anvil) apply false

    // Room Gradle Plugin
    // https://developer.android.com/jetpack/androidx/releases/room#gradle-plugin
    alias(libs.plugins.androidx.room) apply false

    alias(libs.plugins.google.services) apply false

    // Firebase Crashlytics Gradle plugin
    // https://firebase.google.com/docs/crashlytics/get-started?platform=android#add-sdk
    alias(libs.plugins.firebase.crashlytics) apply false

    // Kover coverage for Kotlin projects
    // Project: https://github.com/Kotlin/kotlinx-kover
    alias(libs.plugins.kotlinx.kover) apply true
}

// Configure Kover for project-wide HTML and XML reports.
// https://github.com/Kotlin/kotlinx-kover?tab=readme-ov-file#reports
kover {
    // https://github.com/Kotlin/kotlinx-kover?tab=readme-ov-file#merging-reports
    merge {
        // Enable project-wide report generation
        html.set(true)
        xml.set(true)

        // Specifies directory to generate HTML reports.
        htmlDir.set(layout.buildDirectory.dir("reports/kover/html"))
        // Specifies file to generate XML report.
        xmlFile.set(layout.buildDirectory.file("reports/kover/xml/report.xml"))
    }
}

// Optional: Task to print coverage to console
// https://github.com/Kotlin/kotlinx-kover?tab=readme-ov-file#common-configuration
tasks.register("koverLog", org.jetbrains.kotlinx.kover.gradle.plugin.tasks.KoverLogTask::class) {
    coverageEngine.set(org.jetbrains.kotlinx.kover.gradle.plugin.dsl.CoverageEngine.INTELLIJ)
    level.set(org.jetbrains.kotlinx.kover.gradle.plugin.dsl.CoverageLevel.PROJECT)

    filters {
        // Default Kover filters for Android and common exclusions
        // https://github.com/Kotlin/kotlinx-kover?tab=readme-ov-file#filtering
        androidGeneratedClasses()
        annotatedBy(
            "*Generated",
            "androidx.compose.runtime.Composable",
            "androidx.compose.ui.tooling.preview.Preview"
        )
        packages(
            "*.databinding.*",
            "*.BuildConfig"
        )
    }

    // Log coverage of the whole project to console
    htmlPath.set(layout.buildDirectory.dir("reports/kover/html"))
    xmlPath.set(layout.buildDirectory.file("reports/kover/xml/report.xml"))
}
