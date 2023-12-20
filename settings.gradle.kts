pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        id("com.android.application") version "7.4.0"
        id("com.android.library") version "7.4.0"
        id("org.jetbrains.kotlin.android") version "1.8.10"
        id("org.jetbrains.kotlin.jvm") version "1.8.10"
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_PROJECT)
    repositories {
        google()
        mavenCentral()
        maven { setUrl("https://jitpack.io") }
        maven {
            setUrl("https://s3.amazonaws.com/repo.commonsware.com")
        }
        jcenter()
    }
}