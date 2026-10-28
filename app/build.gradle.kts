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
    // BOMs (Platforms must be declared separately)
    implementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(platform(libs.androidx.compose.bom))

    // Core & Extras
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.datastore.preferences)

    // Bundles
    implementation(libs.bundles.compose.core)
    implementation(libs.bundles.compose.ui.extras)
    implementation(libs.bundles.lifecycle)
    implementation(libs.bundles.navigation)
    implementation(libs.bundles.ktor)
    implementation(libs.bundles.koin)
    implementation(libs.bundles.room)
    implementation(libs.bundles.coroutines.serialization)

    // Annotation Processors (Cannot be in standard implementation bundles)
    ksp(libs.androidx.room.compiler)

    // Testing Bundles
    testImplementation(libs.bundles.testing.unit)
    androidTestImplementation(libs.bundles.testing.android)

    // Debug
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}