import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

group = "com.abrarshakhi.mishti"

kotlin {
    listOf(iosArm64(), iosSimulatorArm64()).forEach { target ->
        target.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
            export(project(":core:llm"))
            export(project(":core:domain"))
        }
    }
    android {
        namespace = "com.abrarshakhi.mishti.composeapp"
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
            api(project(":core:domain"))
            implementation(libs.compose.ui.tooling.preview)
            implementation(project(":core:designsystem"))
            implementation(project(":core:ui"))
            implementation(project(":core:storage"))
            api(project(":core:llm"))
            implementation(project(":core:device"))
            implementation(project(":core:session"))
            implementation(project(":feature:chat"))
            implementation(project(":feature:model"))
            implementation(project(":feature:settings"))
            implementation(project(":feature:onboarding"))
            implementation(project(":feature:splash"))
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.jetbrains.navigation3.ui)
            implementation(libs.jetbrains.lifecycle.viewmodel.navigation3)
            implementation(libs.jetbrains.lifecycle.runtime.compose)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.koin.compose.navigation3)
            implementation(libs.kotlinx.coroutines.core)
        }
        androidMain.dependencies {
            implementation(libs.koin.android)
            implementation(libs.kotlinx.coroutines.android)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(project(":core:testing"))
        }
    }
}

compose.resources {
    generateResClass = always
    publicResClass = true
    packageOfResClass = "com.abrarshakhi.mishti.composeapp.resources"
}

tasks.withType<Test>().configureEach {
    inputs.files(rootProject.file("settings.gradle.kts"))
    inputs.files(rootProject.fileTree("core") { include("*/build.gradle.kts") })
    inputs.files(rootProject.fileTree("feature") { include("*/build.gradle.kts") })
    inputs.files(rootProject.file("androidApp/build.gradle.kts"), file("build.gradle.kts"))
}
