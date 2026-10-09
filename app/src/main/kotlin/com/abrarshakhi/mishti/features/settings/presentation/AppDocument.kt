package com.abrarshakhi.mishti.features.settings.presentation

enum class AppDocument(
    val fileName: String,
) {
    About("about.md"),
    Credits("credits.md"),
    Privacy("privacy-policy.md"),
    Terms("terms-of-service.md"),
    Contributing("contributing.md"),
    ;

    val assetPath: String get() = "docs/$fileName"

    companion object {
        fun byFileName(name: String): AppDocument? =
            entries.find { it.fileName.equals(name, ignoreCase = true) }
    }
}

object ProjectLinks {
    const val REPOSITORY = "https://github.com/AbrarShakhi/Mishti"
    const val REPORT_ISSUE = "$REPOSITORY/issues/new?template=bug-report.yml"
    const val SUGGEST_MODEL = "$REPOSITORY/issues/new?template=model-suggestion.yml"
    const val SOURCE_FILES = "$REPOSITORY/blob/main/"
}

sealed interface DocumentLink {
    data class Internal(
        val document: AppDocument,
    ) : DocumentLink

    data class External(
        val url: String,
    ) : DocumentLink
}

fun resolveDocumentLink(link: String): DocumentLink {
    if (Regex("^[a-zA-Z][a-zA-Z0-9+.-]*:").containsMatchIn(link)) return DocumentLink.External(link)

    val path = link.substringBefore('#')
    AppDocument.byFileName(path.substringAfterLast('/'))?.let { return DocumentLink.Internal(it) }

    val segments = ArrayDeque<String>()
    "docs/$path".split('/').forEach { part ->
        when (part) {
            "", "." -> Unit
            ".." -> segments.removeLastOrNull()
            else -> segments.addLast(part)
        }
    }
    val anchor =
        link
            .substringAfter('#', "")
            .takeIf { it.isNotEmpty() }
            ?.let { "#$it" }
            .orEmpty()
    return DocumentLink.External(ProjectLinks.SOURCE_FILES + segments.joinToString("/") + anchor)
}

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

                inFence || previousBlank || trimmed == line || BlockStart.containsMatchIn(trimmed) -> {
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
