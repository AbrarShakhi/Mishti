import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompileCommon

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlin.serialization)
}

group = "com.abrarshakhi.mishti"

kotlin {
    iosArm64 { compilerOptions.moduleName.set("mishti_core_ui") }
    iosSimulatorArm64 { compilerOptions.moduleName.set("mishti_core_ui") }
    android {
        namespace = "com.abrarshakhi.mishti.core.ui"
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
            api(project(":core:designsystem"))
            api(libs.compose.foundation)
            api(libs.compose.material3)
            api(libs.compose.components.resources)
            api(libs.jetbrains.lifecycle.viewmodel.compose)
            api(libs.jetbrains.lifecycle.runtime.compose)
            api(libs.jetbrains.navigation3.ui)
            api(libs.kotlinx.serialization.core)
            implementation(libs.markdown.renderer.m3)
            implementation(libs.kotlinx.coroutines.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}

compose.resources {
    generateResClass = always
    publicResClass = true
    packageOfResClass = "com.abrarshakhi.mishti.core.ui.resources"
}

kotlin.targets.named("metadata") {
    compilations.configureEach {
        val metadataModuleName = "mishti_core_ui_$name"
        compileTaskProvider.configure {
            if (this is KotlinCompileCommon) {
                @Suppress("DEPRECATION")
                moduleName.set(metadataModuleName)
            }
        }
    }
}
