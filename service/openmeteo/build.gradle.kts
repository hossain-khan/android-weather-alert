plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlinter)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kover)
}

android {
    namespace = "com.openmeteo.api"
    compileSdk = 35

    defaultConfig {
        minSdk = 30

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
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
}

kover {
    reports {
        filters {
            androidGeneratedClasses()
            annotatedBy("*Parcelize")
        }
    }
}

dependencies {
    implementation(project(":data-model"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.timber)

    // https://github.com/open-meteo/open-meteo-api-kotlin/wiki/Examples
    implementation(libs.openmeteo.api.kotlin)

    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.serialization.properties)

    testImplementation(libs.junit)
    testImplementation(libs.google.truth)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}