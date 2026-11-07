package com.abrarshakhi.mishti.settings.domain.model

import com.abrarshakhi.mishti.core.domain.documents.AppDocument

object ProjectLinks {
    const val REPOSITORY = "https://github.com/AbrarShakhi/Mishti"
    const val REPORT_ISSUE = "$REPOSITORY/issues/new?template=bug-report.yml"
    const val SUGGEST_MODEL = "$REPOSITORY/issues/new?template=model-suggestion.yml"
    const val SOURCE_FILES = "$REPOSITORY/blob/main/"
}

sealed interface DocumentLink {
    data class Internal(val document: AppDocument) : DocumentLink

    data class External(val url: String) : DocumentLink
}

fun resolveDocumentLink(link: String, currentDocument: AppDocument = AppDocument.About): DocumentLink {
    if (link.startsWith("//")) return DocumentLink.External("https:$link")
    if (link.startsWith('#')) return DocumentLink.Internal(currentDocument)
    if (Regex("^[a-zA-Z][a-zA-Z0-9+.-]*:").containsMatchIn(link)) return DocumentLink.External(link)

    val path = link.substringBefore('#')
    AppDocument.byFileName(path.substringAfterLast('/'))?.let { return DocumentLink.Internal(it) }

    val segments = ArrayDeque<String>()
    val baseDirectory = if (currentDocument == AppDocument.Contributing) "" else "docs/"
    "$baseDirectory$path".split('/').forEach { part ->
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
