import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.androidx.room)
    alias(libs.plugins.metro)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.google.services)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.kotlinter)
    alias(libs.plugins.kotlinx.kover)
    alias(libs.plugins.ksp)
}

// Creates a variable called keystorePropertiesFile, and initializes it to the keystore.properties file.
// https://developer.android.com/build/gradle-tips#remove-private-signing-information-from-your-project
val keystorePropertiesFile = rootProject.file("keystore.properties")

// Initializes a new Properties() object called keystoreProperties.
val keystoreProperties = Properties()

// Loads the keystore.properties file into the keystoreProperties object.
if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
} else {
    // For CI builds, use debug cert to pass it.
    // https://developer.android.com/studio/publish/app-signing#debug-mode
    keystoreProperties["keyAlias"] = "androiddebugkey"
    keystoreProperties["keyPassword"] = "android"
    keystoreProperties["storeFile"] = "../keystore/debug.keystore"
    keystoreProperties["storePassword"] = "android"
}

android {
    namespace = "dev.hossain.weatheralert"
    compileSdk = 35

    defaultConfig {
        applicationId = "dev.hossain.weatheralert"
        minSdk = 30
        targetSdk = 35
        versionCode = 19
        // 🤓 FYI: Don't forget to update release notes.
        versionName = "2.10"

        // Read bundled API key from local.properties
        val localProperties = project.rootProject.file("local.properties").takeIf { it.exists() }?.inputStream()?.use {
            Properties().apply { load(it) }
        }

        val openWeatherApiKey = localProperties?.getProperty("OPEN_WEATHER_API_KEY") ?: "MISSING-KEY"
        val tomorrowIoApiKey = localProperties?.getProperty("TOMORROW_IO_API_KEY") ?: "MISSING-KEY"
        val weatherapiApiKey = localProperties?.getProperty("WEATHERAPI_API_KEY") ?: "MISSING-KEY"

        buildConfigField("String", "OPEN_WEATHER_API_KEY", "\"$openWeatherApiKey\"")
        buildConfigField("String", "TOMORROW_IO_API_KEY", "\"$tomorrowIoApiKey\"")
        buildConfigField("String", "WEATHERAPI_API_KEY", "\"$weatherapiApiKey\"")

        // Git commit hash to identify build source
        buildConfigField("String", "GIT_COMMIT_HASH", "\"${getGitCommitHash()}\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            storeFile = file(keystoreProperties["storeFile"] as String)
            storePassword = keystoreProperties["storePassword"] as String
            keyAlias = keystoreProperties["keyAlias"] as String
            keyPassword = keystoreProperties["keyPassword"] as String
        }
    }

    // https://developer.android.com/build/build-variants
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            signingConfig = signingConfigs.getByName("release")

            firebaseCrashlytics {
                // https://firebase.google.com/docs/crashlytics/get-deobfuscated-reports?platform=android
                // https://developer.android.com/studio/debug/stacktraces
                // https://developer.android.com/tools/retrace
                // https://www.guardsquare.com/manual/tools/retrace
                mappingFileUploadEnabled = true
            }
        }
    }

    // Creates flavor to have separate Application ID to install side-by-side.
    // https://developer.android.com/build/build-variants#product-flavors
    flavorDimensions += "appflavor"
    productFlavors {
        create("internal") {
            dimension = "appflavor"
            applicationIdSuffix = ".internal"
            versionNameSuffix = "-internal"
        }
        create("prod") {
            dimension = "appflavor"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    room {
        // https://developer.android.com/jetpack/androidx/releases/room#gradle-plugin
        schemaDirectory("$projectDir/schemas")
    }
}


kotlin {
    // See https://kotlinlang.org/docs/gradle-compiler-options.html
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
    }
}


// Kotlin Code Coverage - https://github.com/Kotlin/kotlinx-kover
kover {
    // Configure reports for the debug build variant
    // For now use default values, key tasks are
    // - koverHtmlReportDebug - Task to generate HTML coverage report for 'debug' Android build variant
    // - koverXmlReportDebug - Task to generate XML coverage report for 'debug' Android build variant
    reports {
        // filters for all report types of all build variants
        filters {
            excludes {
                androidGeneratedClasses()
                annotatedBy(
                    "*Composable",
                    "*Parcelize",
                    "*Preview"
                )
            }
        }

        variant("prodRelease") {
            // verification only for 'release' build variant
            verify {
                rule {
                    minBound(50)
                }
            }
        }
    }
}


dependencies {
    implementation(project(":data-model"))
    implementation(project(":service:tomorrowio"))
    implementation(project(":service:openweather"))
    implementation(project(":service:openmeteo"))
    implementation(project(":service:weatherapi"))
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.adaptive.android)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.datastore.core)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.text.google.fonts)
    implementation(libs.androidx.ui.tooling.preview)

    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)

    implementation(libs.circuit.codegen.annotations)
    implementation(libs.circuit.foundation)
    implementation(libs.circuit.overlay)
    implementation(libs.circuitx.android)
    implementation(libs.circuitx.effects)
    implementation(libs.circuitx.gestureNav)
    implementation(libs.circuitx.overlays)
    implementation(libs.androidx.junit.ktx)
    ksp(libs.circuit.codegen)

    implementation(libs.timber)

    implementation(libs.javax.inject)

    // Retrofit
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.retrofit.converter.moshi)

    // WorkManager
    implementation(libs.androidx.work.runtime.ktx)

    // Glance (disabled for now to avoid import issues)
    // implementation(libs.androidx.glance)
    // implementation(libs.androidx.glance.appwidget)

    // OkHttp
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)

    // Moshi
    implementation(libs.moshi.kotlin)
    ksp(libs.moshi.kotlin.codegen)

    // Navigation Compose
    implementation(libs.androidx.navigation.compose)



    implementation(libs.eithernet)
    implementation(libs.eithernet.integration.retrofit)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.analytics)

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    // Testing
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    debugImplementation(libs.androidx.ui.test.manifest)
    debugImplementation(libs.androidx.ui.tooling)
    testImplementation(libs.androidx.room.testing)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.androidx.test.core.ktx)
    testImplementation(libs.androidx.work.testing)
    testImplementation(libs.google.truth)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.okhttp.mock.webserver)
    testImplementation(libs.retrofit.mock.server)
    testImplementation(libs.robolectric)
}

ksp {
    // Circuit-KSP for Metro
    arg("circuit.codegen.mode", "metro")
}


// Helper function to get the current Git commit hash
fun getGitCommitHash(): String {
    val processBuilder = ProcessBuilder("git", "rev-parse", "--short", "HEAD")
    val output = File.createTempFile("git-short-commit-hash", "")
    processBuilder.redirectOutput(output)
    val process = processBuilder.start()
    process.waitFor()
    return output.readText().trim()
}