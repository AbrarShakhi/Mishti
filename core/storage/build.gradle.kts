import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.ksp)
    alias(libs.plugins.androidx.room)
}

group = "com.abrarshakhi.mishti"

kotlin {
    iosArm64()
    iosSimulatorArm64()
    android {
        namespace = "com.abrarshakhi.mishti.core.storage"
        compileSdk =
            libs.versions.android.compileSdk
                .get()
                .toInt()
        minSdk =
            libs.versions.android.minSdk
                .get()
                .toInt()
        compilerOptions { jvmTarget = JvmTarget.JVM_17 }
        withHostTest { isIncludeAndroidResources = true }
        withDeviceTestBuilder { sourceSetTreeName = "test" }.configure { instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner" }
    }
    sourceSets {
        commonMain.dependencies {
            api(project(":core:domain"))
            implementation(project(":core:device"))
            api(libs.androidx.datastore)
            api(libs.androidx.datastore.preferences)
            api(libs.androidx.room.runtime)
            api(libs.androidx.sqlite.bundled)
            api(libs.okio)
            api(libs.koin.core)
            implementation(libs.kotlinx.coroutines.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
        getByName("androidHostTest").dependencies { implementation(libs.androidx.sqlite.bundled.jvm) }
        getByName("androidDeviceTest").dependencies { implementation(libs.androidx.test.runner) }
    }
}

room { schemaDirectory("$projectDir/schemas") }
dependencies {
    add("kspAndroid", libs.androidx.room.compiler)
    add("kspIosArm64", libs.androidx.room.compiler)
    add("kspIosSimulatorArm64", libs.androidx.room.compiler)
}
configurations.matching { it.name.startsWith("androidHostTest") }.configureEach {
    exclude(group = "androidx.sqlite", module = "sqlite-bundled-android")
}
