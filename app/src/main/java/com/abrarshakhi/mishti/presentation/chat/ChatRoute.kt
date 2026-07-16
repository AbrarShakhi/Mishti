package com.abrarshakhi.mishti.presentation.chat

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.Room
import com.abrarshakhi.mishti.data.db.AppDatabase
import com.abrarshakhi.mishti.data.repository.ConversationRepositoryImpl
import com.abrarshakhi.mishti.data.repository.ModelRepositoryImpl
import com.abrarshakhi.mishti.domain.use_case.CreateConversationUseCase
import com.abrarshakhi.mishti.domain.use_case.GetConversationGroupsUseCase
import com.abrarshakhi.mishti.domain.use_case.LoadModelUseCase
import com.abrarshakhi.mishti.domain.use_case.SendMessageUseCase
import com.abrarshakhi.mishti.llm.LlamaEngine


@Composable
fun ChatRoute(
    onOpenModels: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val context = LocalContext.current.applicationContext as Application

    val db = remember {
        Room.databaseBuilder(context, AppDatabase::class.java, "mishti.db").build()
    }

    val conversationRepo = remember {
        ConversationRepositoryImpl(db.conversationDao())
    }

    val modelRepo = remember {
        ModelRepositoryImpl(context, db.modelDao())
    }

    val engine = remember { LlamaEngine() }

    val factory = remember {
        ChatViewModel.Factory(
            getConversationGroups = GetConversationGroupsUseCase(conversationRepo),
            createConversation = CreateConversationUseCase(conversationRepo),
            sendMessage = SendMessageUseCase(conversationRepo, engine),
            loadModel = LoadModelUseCase(modelRepo, engine),
        )
    }

    // ── Get / reuse the ViewModel ─────────────────────────────────────────────
    val viewModel: ChatViewModel = viewModel(factory = factory)
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    ChatScreen(
        state = state,
        effect = viewModel.effect,
        onIntent = viewModel::onIntent,
        onOpenModels = onOpenModels,
        onOpenSettings = onOpenSettings,
    )
}
