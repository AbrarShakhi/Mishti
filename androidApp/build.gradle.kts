import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    compilerOptions { jvmTarget = JvmTarget.JVM_17 }
}

android {
    namespace = "com.abrarshakhi.mishti"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    ndkVersion = "28.2.13676358"

    defaultConfig {
        applicationId = "com.abrarshakhi.mishti"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()

        versionCode = 5
        versionName = "1.2.0-beta.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        externalNativeBuild {
            cmake {
                arguments += "-DCMAKE_BUILD_TYPE=Release"
                arguments += "-DBUILD_SHARED_LIBS=ON"
            }
        }
    }

    // NOTE: path is relative to androidApp/ now -> move app/src/main/cpp to androidApp/src/main/cpp
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
    implementation(project(":shared"))

    // BOM: kept only so the version-less androidx test/manifest artifacts below still resolve.
    // Can be removed once those are replaced.
    implementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(platform(libs.androidx.compose.bom))

    // Core & Activity
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Android-only libraries (not usable from commonMain)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.lottie.compose)
    implementation(libs.phosphoricon.compose)

    // TODO: move to shared/commonMain once KMP-ready replacements are in the catalog
    implementation(libs.bundles.navigation)
    implementation(libs.bundles.koin)

    // Compose preview / tooling
    implementation(libs.compose.ui.tooling.preview)
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Testing
    testImplementation(libs.bundles.testing.unit)
    androidTestImplementation(libs.bundles.testing.android)
}