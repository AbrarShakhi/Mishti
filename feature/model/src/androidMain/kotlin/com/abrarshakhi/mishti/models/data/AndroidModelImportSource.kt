package com.abrarshakhi.mishti.models.data

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import okio.Source
import okio.source
import okio.use

class AndroidModelImportSource(private val context: Context) : ModelImportSource {
    override fun metadata(uri: String): ImportMetadata {
        val document = Uri.parse(uri)
        var name = document.lastPathSegment ?: "model.gguf"
        var size = -1L
        context.contentResolver.query(
            document,
            arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE),
            null,
            null,
            null,
        )
            ?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameColumn = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val sizeColumn = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (nameColumn >= 0 && !cursor.isNull(nameColumn)) name = cursor.getString(nameColumn)
                    if (sizeColumn >= 0 && !cursor.isNull(sizeColumn)) size = cursor.getLong(sizeColumn)
                }
            }
        return ImportMetadata(name, size)
    }
    override fun open(uri: String): Source = context.contentResolver.openInputStream(Uri.parse(uri))?.source()
        ?: throw DownloadFailure(com.abrarshakhi.mishti.models.domain.model.TransferError.CannotOpen)
}
