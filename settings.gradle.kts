pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // Xposed legacy API（de.robv.android.xposed:api:82）官方仓库
        maven { url = uri("https://api.xposed.info/") }
    }
}

rootProject.name = "ColorOSClockSeconds"
include(":app")
