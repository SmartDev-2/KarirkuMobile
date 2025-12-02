pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        // ✅ Tambahkan repository JitPack di sini
        maven("https://jitpack.io")
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // ✅ Tambahkan ini juga
        maven("https://jitpack.io")
    }
}

rootProject.name = "KARIRKU"
include(":app")
