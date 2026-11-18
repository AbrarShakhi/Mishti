package com.abrarshakhi.mishti.core.storage.preferences.data

import android.content.Context
import java.io.File

fun produceDataStorePath(androidContext: Context) = createDataStore {
    resolvePreferencesFile(androidContext.filesDir).absolutePath
}

internal fun resolvePreferencesFile(filesDirectory: File): File {
    val earlyCmpFile = filesDirectory.resolve(DATASTORE_FILE_NAME)
    return if (earlyCmpFile.isFile) earlyCmpFile else filesDirectory.resolve("datastore/$DATASTORE_FILE_NAME")
}
