/*
 * @file: shared/build.gradle.kts
 * @module: shared
 */

import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.ksp)
    alias(libs.plugins.androidx.room)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    listOf(iosArm64(), iosSimulatorArm64()).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }
    android {
        namespace = "com.abrarshakhi.mishti.shared"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        compilerOptions { jvmTarget = JvmTarget.JVM_17 }
        androidResources { enable = true }
        withHostTest { isIncludeAndroidResources = true }
        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }.configure {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
    }
    sourceSets {
        commonMain.dependencies {
            // Compose Multiplatform
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            api(libs.compose.material3)
            api(libs.compose.ui)
            implementation(libs.compose.ui.tooling.preview)
            api(libs.compose.components.resources)
            api(libs.compose.material3.adaptive)
            api(libs.compose.material3.adaptive.navigation.suite)

            // Lifecycle
            implementation(libs.jetbrains.lifecycle.viewmodel.compose)
            implementation(libs.jetbrains.lifecycle.runtime.compose)

            // Navigation 3
            implementation(libs.jetbrains.navigation3.ui)
            implementation(libs.jetbrains.lifecycle.viewmodel.navigation3)

            // Koin
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.koin.compose.navigation3)

            // Data
            api(libs.androidx.datastore)
            api(libs.androidx.datastore.preferences)
            api(libs.androidx.room.runtime)
            api(libs.androidx.sqlite.bundled)
            api(libs.kotlinx.serialization.core)
            api(libs.okio)

            // Networking
            api(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)

            // UI extras
            implementation(libs.compottie)
            implementation(libs.markdown.renderer.m3)
            implementation(libs.material.kolor)
            implementation(libs.compose.shimmer)
            implementation(libs.phosphoricon.compose)
        }
        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
            implementation(libs.koin.android)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.androidx.room.ktx)
            implementation(libs.kotlinx.coroutines.android)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.okio.fakefilesystem)
            implementation(libs.ktor.client.mock)
        }
        getByName("androidHostTest").dependencies {
            // Host tests need the desktop JNI library rather than Android's native binary.
            implementation(libs.androidx.sqlite.bundled.jvm)
        }
        getByName("androidDeviceTest").dependencies {
            implementation(libs.androidx.test.runner)
        }
    }
}

val generatedDocsDirectory = layout.buildDirectory.dir("generated/composeResources/documents")
val bundleDocs = tasks.register<Sync>("bundleDocs") {
    group = "build"
    description = "Stages shared Compose resources with the repository documents."
    listOf(
        "docs/about.md",
        "docs/credits.md",
        "docs/privacy-policy.md",
        "docs/terms-of-service.md",
        "CONTRIBUTING.md",
    ).forEachIndexed { index, path ->
        inputs.file(rootProject.file(path))
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
    publicResClass = true
    packageOfResClass = "com.abrarshakhi.mishti.shared.resources"
    customDirectory(
        sourceSetName = "commonMain",
        directoryProvider = layout.dir(bundleDocs.map { it.destinationDir }),
    )
}

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    // Tooling only for debug/runtime classpath, never shipped in release
    androidRuntimeClasspath(libs.compose.ui.tooling)

    // Room compiler (KSP) per target
    add("kspAndroid", libs.androidx.room.compiler)
    add("kspIosArm64", libs.androidx.room.compiler)
    add("kspIosSimulatorArm64", libs.androidx.room.compiler)
}

// Keep the desktop SQLite implementation on the host test classpath.
configurations.matching { it.name.startsWith("androidHostTest") }.configureEach {
    exclude(group = "androidx.sqlite", module = "sqlite-bundled-android")
}
