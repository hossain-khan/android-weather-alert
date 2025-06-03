plugins {
    kotlin("jvm") version "2.1.10"
    alias(libs.plugins.kover)
}

group = "dev.hossain.citydb"
version = "1.0-SNAPSHOT"

repositories {
    google()
    mavenCentral()
}


dependencies {
    // https://developer.android.com/kotlin/multiplatform/sqlite
    // https://mvnrepository.com/artifact/androidx.sqlite/sqlite-bundled
    // https://developer.android.com/jetpack/androidx/releases/sqlite
    val sqliteVersion = "2.5.1"

    // androidx.sqlite:sqlite-bundled
    implementation("androidx.sqlite:sqlite-bundled:$sqliteVersion")

    // https://github.com/jsoizo/kotlin-csv
    implementation("com.jsoizo:kotlin-csv-jvm:1.10.0")


    testImplementation(kotlin("test"))
}


tasks.test {
    useJUnitPlatform()
}

kover {
    // Configure Kover for a Kotlin JVM module.
    // An empty block uses default settings.
}