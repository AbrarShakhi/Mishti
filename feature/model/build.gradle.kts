import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlin.serialization)
}

group = "com.abrarshakhi.mishti"

kotlin {
    iosArm64()
    iosSimulatorArm64()
    android {
        namespace = "com.abrarshakhi.mishti.feature.model"
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
            implementation(project(":core:storage"))
            implementation(project(":core:device"))
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.jetbrains.lifecycle.viewmodel.compose)
            implementation(libs.jetbrains.lifecycle.runtime.compose)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.koin.compose.navigation3)
            implementation(libs.ktor.client.core)
            implementation(libs.okio)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.compose.shimmer)
            implementation(libs.kotlinx.coroutines.core)
        }
        androidMain.dependencies {
            implementation(libs.koin.android)
            implementation(libs.kotlinx.coroutines.android)
            implementation(libs.androidx.activity.compose)
            implementation(libs.ktor.client.okhttp)
        }
        iosMain.dependencies { implementation(libs.ktor.client.darwin) }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(project(":core:testing"))
            implementation(libs.okio.fakefilesystem)
            implementation(libs.ktor.client.mock)
        }
    }
}

compose.resources {
    generateResClass = always
    publicResClass = true
    packageOfResClass = "com.abrarshakhi.mishti.feature.model.resources"
}
