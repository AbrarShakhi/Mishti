// Top-level build file where you can add configuration options common to all subprojects/modules.
plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidMultiplatformLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.androidx.room) apply false

    id("com.diffplug.spotless") version "8.10.4"
}

spotless {
    kotlin {
        target(
            "androidApp/src/**/kotlin/**/*.kt",
            "shared/src/**/kotlin/**/*.kt",
        )
        targetExclude("**/cpp/**")
        ktlint().editorConfigOverride(
            mapOf(
                "max_line_length" to "120",
                "ktlint_function_naming_ignore_when_annotated_with" to "Composable",
                "ktlint_standard_property-naming" to "disabled",
            ),
        )
    }

    kotlinGradle {
        target("*.gradle.kts")
        ktlint()
    }
}

tasks.named("spotlessKotlin") {
    mustRunAfter(syncLlamaCpp)
}

project(":androidApp") {
    tasks
        .matching {
            it.name.startsWith("pre") && it.name.endsWith("ReleaseBuild")
        }.configureEach {
            dependsOn(rootProject.tasks.named("spotlessCheck"))
        }
}

val syncLlamaCpp =
    tasks.register<Sync>("syncLlamaCpp") {
        group = "build"
        description = "Synchronizes llama.cpp into Android C++ sources."

        from(rootProject.file("llama.cpp")) {
            exclude(".git/**")
        }

        into(rootProject.file("androidApp/src/main/cpp/llama.cpp"))
    }

project(":androidApp") {
    tasks.configureEach {
        if (name.startsWith("configureCMake")) {
            dependsOn(syncLlamaCpp)
        }
    }
}
