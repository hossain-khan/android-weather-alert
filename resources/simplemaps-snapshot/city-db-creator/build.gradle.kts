plugins {
    kotlin("jvm") version "2.0.21"
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
    // https://developer.android.com/jetpack/androidx/releases/sqlite#2.5.0-alpha12
    val sqliteVersion = "2.5.0-alpha12"

    // androidx.sqlite:sqlite-bundled
    implementation("androidx.sqlite:sqlite-bundled:$sqliteVersion")


    testImplementation(kotlin("test"))
}


tasks.test {
    useJUnitPlatform()
}