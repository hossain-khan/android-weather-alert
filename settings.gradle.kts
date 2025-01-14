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

rootProject.name = "Weather Alert"
include(":data-model")
include(":service:tomorrowio")
include(":service:openweather")
include(":service:openmeteo")
include(":app")
