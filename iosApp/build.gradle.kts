plugins { base }

tasks.named("assemble") {
    dependsOn(":composeApp:linkDebugFrameworkIosArm64", ":composeApp:linkDebugFrameworkIosSimulatorArm64")
}
tasks.named("check") { dependsOn(":composeApp:compileIosMainKotlinMetadata") }
