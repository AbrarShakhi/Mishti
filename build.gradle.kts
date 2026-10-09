// Top-level build file where you can add configuration options common to all subprojects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.androidx.room) apply false

    id("com.diffplug.spotless") version "8.10.4"
}

spotless {
    kotlin {
        target(
            "app/src/main/kotlin/**/*.kt",
            "app/src/test/kotlin/**/*.kt",
            "app/src/androidTest/kotlin/**/*.kt",
        )
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

project(":app") {
    tasks
        .matching {
            it.name.startsWith("pre") && it.name.endsWith("ReleaseBuild")
        }.configureEach {
            dependsOn(rootProject.tasks.named("spotlessCheck"))
        }
}
