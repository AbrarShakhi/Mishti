package com.abrarshakhi.mishti

import android.app.Application
import androidx.room.Room
import com.abrarshakhi.mishti.data.db.ModelDatabase
import com.abrarshakhi.mishti.data.repository.ModelRepository
import com.abrarshakhi.mishti.llm.LlamaEngine

/**
 * MishtiApp — the Application class.
 *
 * Android creates exactly ONE instance of this class for the whole app lifetime.
 * We use it to hold singletons (things that should exist only once):
 *   - The Room database
 *   - The ModelRepository
 *   - The LlamaEngine
 *
 * ViewModels receive these via the factory we'll build next.
 */
class MishtiApp : Application() {

    // Room database — built once, lives as long as the app does
    val database: ModelDatabase by lazy {
        Room.databaseBuilder(
            applicationContext,
            ModelDatabase::class.java,
            "mishti.db"
        ).build()
    }

    // Repository wraps the database DAO + download logic
    val modelRepository: ModelRepository by lazy {
        ModelRepository(
            context = applicationContext,
            dao     = database.modelDao()
        )
    }

    // The C++ inference engine — singleton so we don't load the model twice
    val llamaEngine: LlamaEngine by lazy { LlamaEngine() }
}