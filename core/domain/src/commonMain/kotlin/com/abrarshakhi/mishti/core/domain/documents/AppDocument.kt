package com.abrarshakhi.mishti.core.domain.documents

enum class AppDocument(val fileName: String) {
    About("about.md"),
    Credits("credits.md"),
    Privacy("privacy-policy.md"),
    Terms("terms-of-service.md"),
    Contributing("contributing.md"),
    ;

    companion object {
        fun byFileName(name: String): AppDocument? = entries.find { it.fileName.equals(name, ignoreCase = true) }
    }
}
