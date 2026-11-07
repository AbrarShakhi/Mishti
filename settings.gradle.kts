rootProject.name = "Mishti"

pluginManagement {
    repositories {
        google {
            content {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

include(":androidApp")
include(":iosApp")
include(":composeApp")
include(":core:domain")
include(":core:designsystem")
include(":core:device")
include(":core:llm")
include(":core:storage")
include(":core:ui")
include(":core:session")
include(":core:testing")
include(":feature:chat")
include(":feature:settings")
include(":feature:model")
include(":feature:onboarding")
include(":feature:splash")
