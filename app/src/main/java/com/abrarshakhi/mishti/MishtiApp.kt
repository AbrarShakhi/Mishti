package com.abrarshakhi.mishti

import android.app.Application
import androidx.room.Room
import com.abrarshakhi.mishti.data.db.ModelDatabase
import com.abrarshakhi.mishti.data.repository.ModelRepositoryImpl
import com.abrarshakhi.mishti.domain.repository.ModelRepository
import com.abrarshakhi.mishti.llm.LlamaEngine

class MishtiApp : Application() {

    val database: ModelDatabase by lazy {
        Room.databaseBuilder(
            applicationContext,
            ModelDatabase::class.java,
            "mishti.db"
        ).build()
    }

    val modelRepository: ModelRepository by lazy {
        ModelRepositoryImpl(
            context = applicationContext,
            dao = database.modelDao()
        )
    }

    val llamaEngine: LlamaEngine by lazy { LlamaEngine() }
}