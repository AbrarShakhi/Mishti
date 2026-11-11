import org.gradle.internal.os.OperatingSystem

plugins { base }

val isMac = OperatingSystem.current().isMacOsX

val buildLlama =
    tasks.register<Exec>("buildLlama") {
        group = "build"
        description = "Builds llama.cpp and mishti_llama_ios static libraries for iOS (macOS only)"

        onlyIf { isMac }

        val nativeDir = file("native")
        val llamaDir = rootProject.file("core/llm/native")
        val scriptFile = file("native/build-llama.sh")

        inputs.dir(llamaDir).withPropertyName("llamaSources").withPathSensitivity(PathSensitivity.RELATIVE)
        inputs.dir(nativeDir).withPropertyName("nativeSources").withPathSensitivity(PathSensitivity.RELATIVE)

        val derivedDir =
            providers
                .environmentVariable("DERIVED_FILE_DIR")
                .orElse(layout.buildDirectory.dir("native-build").map { it.asFile.absolutePath })

        outputs.dir(derivedDir.map { "$it/llama-install/lib" })

        commandLine("sh", scriptFile.absolutePath)
    }

tasks.named("assemble") {
    dependsOn(":composeApp:linkDebugFrameworkIosArm64", ":composeApp:linkDebugFrameworkIosSimulatorArm64")
    if (isMac) {
        dependsOn(buildLlama)
    }
}

tasks.named("check") {
    dependsOn(":composeApp:compileIosMainKotlinMetadata")
}
