import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.anvil)
    alias(libs.plugins.kotlinter)
}

android {
    namespace = "dev.hossain.weatheralert"
    compileSdk = 35

    defaultConfig {
        applicationId = "dev.hossain.weatheralert"
        minSdk = 30
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        // Read API key from local.properties
        val apiKey: String =
            project.rootProject.file("local.properties").takeIf { it.exists() }?.inputStream()?.use {
                Properties().apply { load(it) }.getProperty("WEATHER_API_KEY")
            } ?: "API_KEY_FROM_local.properties"
        buildConfigField("String", "WEATHER_API_KEY", "\"$apiKey\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

kapt {
    correctErrorTypes = true
}

dependencies {
    // App dependencies
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.text.google.fonts)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.material.icons.extended)

    implementation(libs.circuit.codegen.annotations)
    implementation(libs.circuit.foundation)
    implementation(libs.circuit.overlay)
    implementation(libs.circuitx.android)
    implementation(libs.circuitx.effects)
    implementation(libs.circuitx.gestureNav)
    implementation(libs.circuitx.overlays)
    ksp(libs.circuit.codegen)

    implementation(libs.timber)

    implementation(libs.dagger)
    // Dagger KSP support is in Alpha, not available yet. Using KAPT for now.
    // https://dagger.dev/dev-guide/ksp.html
    kapt(libs.dagger.compiler)

    // Retrofit
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.retrofit.converter.moshi)

    // WorkManager
    implementation(libs.androidx.work.runtime.ktx)

    // Data Store
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.datastore.core)

    // Glance
    implementation(libs.androidx.glance)
    implementation(libs.androidx.glance.appwidget)

    // OkHttp
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)

    // Moshi
    implementation(libs.moshi.kotlin)
    ksp(libs.moshi.kotlin.codegen)

    // Navigation Compose
    implementation(libs.navigation.compose)

    implementation(libs.anvil.annotations)
    implementation(libs.anvil.annotations.optional)

    implementation(libs.eithernet)
    implementation(libs.eithernet.integration.retrofit)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.androidx.work.testing)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

ksp {
    // Anvil-KSP
    arg("anvil-ksp-extraContributingAnnotations", "com.slack.circuit.codegen.annotations.CircuitInject")
    // kotlin-inject-anvil (requires 0.0.3+)
    arg("kotlin-inject-anvil-contributing-annotations", "com.slack.circuit.codegen.annotations.CircuitInject")
}
