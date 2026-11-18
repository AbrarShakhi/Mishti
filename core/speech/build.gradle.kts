import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import java.net.URI
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.security.MessageDigest

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

abstract class PrepareWhistleFiles : DefaultTask() {
    @get:Input
    abstract val baseUrl: Property<String>

    @get:Input
    abstract val hashes: MapProperty<String, String>

    @get:OutputDirectory
    abstract val destination: DirectoryProperty

    @get:Input
    abstract val outputSubdirectory: Property<String>

    init {
        outputSubdirectory.convention("")
    }

    @TaskAction
    fun prepare() {
        val directory = destination.get().asFile.resolve(outputSubdirectory.get())
        directory.mkdirs()
        for ((name, expectedHash) in hashes.get()) {
            val output = directory.resolve(name)
            if (output.isFile && sha256(output) == expectedHash) continue
            check(!projectOffline.get()) {
                "Missing verified Whistle artifact $name. Run :core:speech:prepareWhistleArtifacts online."
            }
            val partial = Files.createTempFile(temporaryDir.toPath(), "$name-", ".part").toFile()
            try {
                val connection = URI("${baseUrl.get()}/$name").toURL().openConnection()
                connection.connectTimeout = 30_000
                connection.readTimeout = 60_000
                connection.getInputStream().use { source ->
                    partial.outputStream().use { target -> source.copyTo(target) }
                }
                check(sha256(partial) == expectedHash) { "Whistle checksum mismatch for $name" }
                Files.move(
                    partial.toPath(),
                    output.toPath(),
                    StandardCopyOption.REPLACE_EXISTING,
                )
            } finally {
                partial.delete()
            }
        }
    }

    @get:Internal
    abstract val projectOffline: Property<Boolean>

    private fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(64 * 1024)
            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                digest.update(buffer, 0, count)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
}

group = "com.abrarshakhi.mishti"

val runtimeRevision = "1a028f5a3972b67dd8d8752f882113d475e7ec4c"
val runtimeUrl = "https://huggingface.co/Cactus-Compute/cactus/resolve/$runtimeRevision/releases/4.0.0"
val modelRevision = "ad73f1468dd5203791e0cc81d31544c553563645"
val modelUrl = "https://huggingface.co/Cactus-Compute/whistle/resolve/$modelRevision"
val headerHash = "facefca17a0d5e715bd92a8b0ea272c1ac34a20a798003b1e691c8fba5ace6e2"
val offline = gradle.startParameter.isOffline
val iosDeploymentTarget = providers.environmentVariable("IPHONEOS_DEPLOYMENT_TARGET").orNull.orEmpty()
val iosMajorVersion = iosDeploymentTarget.substringBefore('.').toIntOrNull() ?: 0
val iosMinorVersion = iosDeploymentTarget.substringAfter('.', "").substringBefore('.').toIntOrNull() ?: 0
val iosWhistleEnabled = iosMajorVersion > 27 || (iosMajorVersion == 27 && iosMinorVersion >= 2)

fun registerRuntime(
    taskName: String,
    platform: String,
    archiveHash: String,
) = tasks.register<PrepareWhistleFiles>(taskName) {
    baseUrl.set("$runtimeUrl/$platform")
    hashes.putAll(mapOf("cactus.h" to headerHash, "libcactus.a" to archiveHash))
    destination.set(layout.buildDirectory.dir("whistle/$platform"))
    projectOffline.set(offline)
}

val prepareWhistleAndroid =
    registerRuntime(
        "prepareWhistleAndroid",
        "android-arm64",
        "4f75e975f6a859979a2715a89fdbc0fc32742cde001f8674598cf89017d39fdf",
    )
val prepareWhistleIosDevice =
    registerRuntime(
        "prepareWhistleIosDevice",
        "ios-arm64",
        "45e3832c6ed61fc00981e17a0ad7d1ffb795a8e98baf52cee9deb8b9d75c1991",
    )
val prepareWhistleIosSimulator =
    registerRuntime(
        "prepareWhistleIosSimulator",
        "ios-sim-arm64",
        "b228f1577dabec63a51b1d57b74a232ddf404e21168643dd083ebe22fb2b9b98",
    )
val prepareWhistleModel =
    tasks.register<PrepareWhistleFiles>("prepareWhistleModel") {
        baseUrl.set(modelUrl)
        hashes.put("whistle.cact", "b6e02f048568ac5d01a2042556c658061e699acbc0aa2a1439f52f3d461dffeb")
        hashes.put("LICENSE", "cfc7749b96f63bd31c3c42b5c471bf756814053e847c10f3eb003417bc523d30")
        destination.set(layout.buildDirectory.dir("whistleResources"))
        outputSubdirectory.set("files/whistle")
        projectOffline.set(offline)
    }

tasks.register("prepareWhistleArtifacts") {
    group = "build setup"
    description = "Downloads and verifies Whistle weights and the three supported mobile runtimes."
    dependsOn(prepareWhistleModel, prepareWhistleAndroid, prepareWhistleIosDevice, prepareWhistleIosSimulator)
}

kotlin {
    iosArm64()
    iosSimulatorArm64()
    if (iosWhistleEnabled) {
        targets.withType<KotlinNativeTarget>().configureEach {
            val runtime = if (name == "iosArm64") prepareWhistleIosDevice else prepareWhistleIosSimulator
            val runtimeDirectory = runtime.flatMap { it.destination }
            val interop =
                compilations.getByName("main").cinterops.create("whistle") {
                    definitionFile.set(project.file("src/nativeInterop/cinterop/whistle.def"))
                    val path = runtimeDirectory.get().asFile.absolutePath
                    compilerOpts("-I$path")
                    extraOpts("-libraryPath", path)
                }
            tasks.named(interop.interopProcessingTaskName) {
                dependsOn(runtime)
                inputs.dir(runtimeDirectory)
            }
        }
    }

    android {
        namespace = "com.abrarshakhi.mishti.core.speech"
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
        iosMain {
            kotlin.srcDir(if (iosWhistleEnabled) "src/iosWhistle/kotlin" else "src/iosWithoutWhistle/kotlin")
        }
        commonMain.dependencies {
            implementation(libs.compose.components.resources)
            implementation(libs.compose.runtime)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
        }
        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
            implementation(libs.compose.ui)
            implementation(libs.kotlinx.coroutines.android)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}

compose.resources {
    generateResClass = always
    packageOfResClass = "com.abrarshakhi.mishti.core.speech.resources"
    customDirectory("commonMain", prepareWhistleModel.flatMap { it.destination })
}
