package com.abrarshakhi.mishti.core.storage.preferences.data

import android.content.Context

fun produceDataStorePath(androidContext: Context) = createDataStore {
    androidContext.filesDir.resolve(DATASTORE_FILE_NAME).absolutePath
}
