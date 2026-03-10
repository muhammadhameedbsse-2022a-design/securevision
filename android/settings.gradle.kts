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
    }
}

rootProject.name = "SecureVision"

include(":app")
include(":core-ui")
include(":core-data")
include(":core-domain")
include(":feature-dashboard")
include(":feature-live")
include(":feature-profiles")
include(":feature-history")
include(":feature-settings")
include(":feature-alerts")
include(":ml-common")
include(":ml-face")
include(":ml-weapon")
include(":ml-attributes")
