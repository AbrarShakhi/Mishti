package com.abrarshakhi.mishti.core.storage.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

fun produceDatabaseBuilder(context: Context): RoomDatabase.Builder<MishtiDatabase> {
    val appContext = context.applicationContext
    val dbFile = appContext.getDatabasePath(DATABASE_NAME)
    return Room.databaseBuilder<MishtiDatabase>(
        context = appContext,
        name = dbFile.absolutePath,
    )
}
