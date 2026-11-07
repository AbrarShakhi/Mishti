package com.abrarshakhi.mishti.models.data

import kotlinx.cinterop.ExperimentalForeignApi
import okio.FileSystem
import okio.ForwardingSource
import okio.Path.Companion.toPath
import okio.Source
import platform.Foundation.NSURL

@OptIn(ExperimentalForeignApi::class)
class IosModelImportSource : ModelImportSource {
    private fun url(uri: String): NSURL = requireNotNull(NSURL.URLWithString(uri))
    override fun metadata(uri: String): ImportMetadata {
        val url = url(uri)
        val scoped = url.startAccessingSecurityScopedResource()
        return try {
            val path = requireNotNull(url.path).toPath()
            ImportMetadata(path.name, FileSystem.SYSTEM.metadataOrNull(path)?.size ?: -1L)
        } finally {
            if (scoped) url.stopAccessingSecurityScopedResource()
        }
    }
    override fun open(uri: String): Source {
        val url = url(uri)
        val scoped = url.startAccessingSecurityScopedResource()
        try {
            return object : ForwardingSource(FileSystem.SYSTEM.source(requireNotNull(url.path).toPath())) {
                override fun close() {
                    try {
                        super.close()
                    } finally {
                        if (scoped) url.stopAccessingSecurityScopedResource()
                    }
                }
            }
        } catch (error: Throwable) {
            if (scoped) url.stopAccessingSecurityScopedResource()
            throw error
        }
    }
}
