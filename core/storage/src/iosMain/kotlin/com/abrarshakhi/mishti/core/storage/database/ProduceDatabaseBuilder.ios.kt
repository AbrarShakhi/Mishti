package com.abrarshakhi.mishti.core.storage.database

import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

@OptIn(ExperimentalForeignApi::class)
fun produceDatabaseBuilder(): RoomDatabase.Builder<MishtiDatabase> {
    val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = true,
        error = null,
    )?.path ?: error("Document directory not found")
    val dbFilePath = "$documentDirectory/$DATABASE_NAME"
    return Room.databaseBuilder<MishtiDatabase>(
        name = dbFilePath,
    )
}
