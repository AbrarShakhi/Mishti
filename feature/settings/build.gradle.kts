import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

group = "com.abrarshakhi.mishti"

kotlin {
    iosArm64()
    iosSimulatorArm64()
    android {
        namespace = "com.abrarshakhi.mishti.feature.settings"
        compileSdk =
            libs.versions.android.compileSdk
                .get()
                .toInt()
        minSdk =
            libs.versions.android.minSdk
                .get()
                .toInt()
        compilerOptions { jvmTarget = JvmTarget.JVM_17 }
        androidResources { enable = true }
        withHostTest { isIncludeAndroidResources = true }
    }
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:domain"))
            implementation(project(":core:designsystem"))
            implementation(project(":core:ui"))
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.jetbrains.lifecycle.viewmodel.compose)
            implementation(libs.jetbrains.lifecycle.runtime.compose)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.koin.compose.navigation3)
            implementation(libs.kotlinx.coroutines.core)
        }
        androidMain.dependencies {
            implementation(libs.koin.android)
        }
        commonTest.dependencies {
            implementation(libs.okio)
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(project(":core:testing"))
        }
    }
}

val generatedDocsDirectory = layout.buildDirectory.dir("generated/composeResources/documents")
val bundleDocs =
    tasks.register<Sync>("bundleDocs") {
        group = "build"
        description = "Stages Settings resources with the repository documents."
        listOf(
            "docs/about.md",
            "docs/credits.md",
            "docs/privacy-policy.md",
            "docs/terms-of-service.md",
            "CONTRIBUTING.md",
        ).forEachIndexed { index, path ->
            inputs
                .file(rootProject.file(path))
                .withPropertyName("requiredDocument$index")
                .withPathSensitivity(PathSensitivity.RELATIVE)
        }
        from(layout.projectDirectory.dir("src/commonMain/composeResources"))
        from(rootProject.fileTree("docs") { include("*.md") }) {
            into("files/docs")
        }
        from(rootProject.file("CONTRIBUTING.md")) {
            into("files/docs")
            rename { "contributing.md" }
        }
        into(generatedDocsDirectory)
        duplicatesStrategy = DuplicatesStrategy.FAIL
    }

compose.resources {
    generateResClass = always
    publicResClass = true
    packageOfResClass = "com.abrarshakhi.mishti.feature.settings.resources"
    customDirectory(
        sourceSetName = "commonMain",
        directoryProvider = layout.dir(bundleDocs.map { it.destinationDir }),
    )
}
