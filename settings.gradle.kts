pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        id("com.android.application") version "8.13.1" apply false
        id("com.android.library") version "8.13.1" apply false
        id("org.jetbrains.kotlin.android") version "1.9.23" apply false
        id("com.google.gms.google-services") version "4.4.1" apply false
        id("com.google.firebase.crashlytics") version "2.9.9" apply false
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "MobileNettieRebuild"
include(":app")
include(":mommastealth")
