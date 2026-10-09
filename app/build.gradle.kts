import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.androidx.room)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.abrarshakhi.mishti"
    compileSdk { version = release(37) }

    ndkVersion = "28.2.13676358"

    defaultConfig {
        applicationId = "com.abrarshakhi.mishti"
        minSdk = 30
        targetSdk = 37

        versionCode = 4
        versionName = "1.2.0-alpha.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        externalNativeBuild {
            cmake {
                arguments += "-DCMAKE_BUILD_TYPE=Release"
                arguments += "-DBUILD_SHARED_LIBS=ON"
            }
        }
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    buildTypes {
        debug {
            ndk { abiFilters += listOf("arm64-v8a") }
        }
        release {
            optimization { enable = true }
            ndk { abiFilters += listOf("arm64-v8a", "x86_64") }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    sourceSets {
        getByName("main") {
            assets.directories.add(rootProject.file("hub").absolutePath)
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

room {
    schemaDirectory("$projectDir/schemas")
}

kotlin {
    compilerOptions { jvmTarget = JvmTarget.JVM_17 }
}
abstract class BundleDocsTask : DefaultTask() {

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.NAME_ONLY)
    abstract val documents: ConfigurableFileCollection

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun bundle() {
        val target = outputDir.get().asFile.resolve("docs")
        target.deleteRecursively()
        target.mkdirs()
        documents.files.filter { it.isFile }.forEach { file ->
            file.copyTo(target.resolve(file.name.lowercase()), overwrite = true)
        }
    }
}

val bundleDocs = tasks.register<BundleDocsTask>("bundleDocs") {
    group = "build"
    description = "Copies the repository's docs into the app's assets."
    documents.from(
        rootProject.fileTree("docs") { include("*.md") },
        rootProject.file("CONTRIBUTING.md"),
    )
}

androidComponents {
    onVariants { variant ->
        variant.sources.assets?.addGeneratedSourceDirectory(bundleDocs, BundleDocsTask::outputDir)
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.lifecycle.viewmodel.navigation3)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)
    implementation(libs.koin.compose.navigation3)
    implementation(libs.koin.ktor)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)

    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    implementation(libs.androidx.core.splashscreen)
    implementation(libs.lottie.compose)
    implementation(libs.markdown.renderer.m3)
    implementation(libs.material.kolor)
    implementation(libs.compose.shimmer)
}