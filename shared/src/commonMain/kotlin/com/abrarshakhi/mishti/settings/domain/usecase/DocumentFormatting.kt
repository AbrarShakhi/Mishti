package com.abrarshakhi.mishti.settings.domain.usecase

fun stripLeadingTitle(markdown: String): String {
    val lines = markdown.lines()
    val first = lines.indexOfFirst { it.isNotBlank() }
    if (first < 0 || !lines[first].startsWith("# ")) return markdown
    return lines.drop(first + 1).joinToString("\n").trimStart('\n')
}

private val BlockStart = Regex("^([-*+] |>|\\||#|\\d+[.)] )")

fun unindentWrappedLines(markdown: String): String {
    var inFence = false
    var previousBlank = true
    return markdown.lines().joinToString("\n") { line ->
        val trimmed = line.trimStart()
        val result =
            when {
                trimmed.startsWith("```") -> {
                    inFence = !inFence
                    line
                }

                inFence || previousBlank || trimmed == line || BlockStart.containsMatchIn(
                    trimmed,
                ) -> {
                    line
                }

                else -> {
                    trimmed
                }
            }
        previousBlank = line.isBlank()
        result
    }
}
