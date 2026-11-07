/*
 * @file: ./build.gradle.kts
 * Top-level build
 */

plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidMultiplatformLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.androidx.room) apply false
    alias(libs.plugins.spotless)
}

spotless {
    kotlin {
        target(
            "androidApp/src/**/kotlin/**/*.kt",
            "composeApp/src/**/kotlin/**/*.kt",
            "core/**/src/**/kotlin/**/*.kt",
            "feature/**/src/**/kotlin/**/*.kt",
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
        target(
            "*.gradle.kts",
            "androidApp/*.gradle.kts",
            "iosApp/*.gradle.kts",
            "composeApp/*.gradle.kts",
            "core/*/*.gradle.kts",
            "feature/*/*.gradle.kts",
        )
        ktlint()
    }
}

project(":androidApp") {
    tasks
        .matching {
            it.name.startsWith("pre") && it.name.endsWith("ReleaseBuild")
        }.configureEach {
            dependsOn(rootProject.tasks.named("spotlessCheck"))
        }
}
