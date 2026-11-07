package com.abrarshakhi.mishti.architecture

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ModuleArchitectureTest {
    private val root = File("..").canonicalFile
    private val projectReference = Regex("""project\("(:[^"]+)"\)""")
    private val includes = Regex("""include\("(:[^"]+)"\)""")
    private val modules = includes.findAll(root.resolve("settings.gradle.kts").readText())
        .map { it.groupValues[1] }
        .associateWith { path -> root.resolve("${path.drop(1).replace(':', '/')}/build.gradle.kts").readText() }
    private val productionDependencies = modules.mapValues { (_, script) ->
        projectReference.findAll(script.substringBefore("commonTest.dependencies"))
            .map { it.groupValues[1] }.toSet()
    }

    @Test
    fun `production dependencies form an acyclic graph`() {
        val completed = mutableSetOf<String>()
        val visiting = mutableListOf<String>()
        fun visit(module: String) {
            if (module in completed) return
            assertFalse(module in visiting, "Dependency cycle: ${(visiting + module).joinToString(" -> ")}")
            visiting.add(module)
            for (dependency in productionDependencies.getValue(module)) {
                assertTrue(dependency in modules, "Unknown module $dependency used by $module")
                visit(dependency)
            }
            visiting.removeAt(visiting.lastIndex)
            completed.add(module)
        }
        modules.keys.forEach(::visit)
    }

    @Test
    fun `core and features respect dependency direction`() {
        for ((module, dependencies) in productionDependencies) {
            assertFalse(":core:testing" in dependencies, "$module uses test helpers in production")
            if (module.startsWith(":core:")) {
                assertTrue(dependencies.all { it.startsWith(":core:") }, "$module depends outside core")
            }
            if (module.startsWith(":feature:")) {
                assertTrue(dependencies.all { it.startsWith(":core:") }, "$module depends on another feature or host")
            }
        }
        assertTrue(productionDependencies.getValue(":core:domain").isEmpty())
        assertFalse("composeMultiplatform" in modules.getValue(":core:domain"))
        assertFalse(":core:storage" in productionDependencies.getValue(":feature:chat"))
        assertFalse(":core:llm" in productionDependencies.getValue(":feature:chat"))
    }

    @Test
    fun `fonts animations and documents have separate resource owners`() {
        val resources = modules.keys.associateWith { module ->
            root.resolve("${module.drop(1).replace(':', '/')}/src/commonMain/composeResources")
                .walkTopDown().filter { it.isFile }.toList()
        }
        val fonts = resources.filterValues { files -> files.any { it.extension in setOf("ttf", "otf") } }.keys
        val animations = resources.filterValues { files -> files.any { it.extension == "json" } }.keys
        assertEquals(setOf(":core:designsystem"), fonts)
        assertEquals(setOf(":feature:onboarding"), animations)
        assertTrue(resources.getValue(":feature:onboarding").count { it.extension == "json" } > 0)
        assertTrue(resources.values.flatten().none { it.name == "catalog.v1.json" })
        assertTrue(resources.values.flatten().none { it.extension == "md" })
        assertTrue(root.resolve("docs/about.md").isFile)
        assertTrue(root.resolve("CONTRIBUTING.md").isFile)
        assertTrue("bundleDocs" in modules.getValue(":feature:settings"))
    }
}
