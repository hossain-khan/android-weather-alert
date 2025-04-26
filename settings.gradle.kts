pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // Adds JitPack repository
        maven("https://jitpack.io")
    }
}

plugins {
    // Develocity - for Gradle build scan publishing
    // https://scans.gradle.com/
    // https://docs.gradle.com/develocity/gradle-plugin/current/
    id("com.gradle.develocity") version("4.0.1")
}

develocity {
    // configurations - https://scans.gradle.com/
    buildScan {
        termsOfUseAgree = "yes"
        termsOfUseUrl = "https://gradle.com/help/legal-terms-of-use"
    }
}

rootProject.name = "Weather Alert"
include(":data-model")
include(":service:tomorrowio")
include(":service:openweather")
include(":service:openmeteo")
include(":service:weatherapi")
include(":app")
