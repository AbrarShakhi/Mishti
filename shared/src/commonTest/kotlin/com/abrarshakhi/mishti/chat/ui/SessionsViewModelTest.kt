package com.abrarshakhi.mishti.chat.ui

import com.abrarshakhi.mishti.chat.domain.model.ChatMessage
import com.abrarshakhi.mishti.chat.domain.model.MessageAuthor
import com.abrarshakhi.mishti.chat.domain.repository.ChatRepository
import com.abrarshakhi.mishti.chat.domain.usecase.CreateNewSessionUseCase
import com.abrarshakhi.mishti.chat.fake.FakeChatRepository
import com.abrarshakhi.mishti.core.ui.snackbar.SnackBarDispatcher
import com.abrarshakhi.mishti.core.ui.utils.uiText
import com.abrarshakhi.mishti.shared.resources.Res
import com.abrarshakhi.mishti.shared.resources.chat_error_delete
import com.abrarshakhi.mishti.shared.resources.chat_error_rename
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withTimeoutOrNull
import okio.IOException
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
class SessionsViewModelTest {
    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `the drawer lists sessions from the repository`() = runTest {
        val repository = FakeChatRepository()
        val vm = SessionsViewModel(repository, CreateNewSessionUseCase(repository), SnackBarDispatcher())
        advanceUntilIdle()

        repository.createSession("First")
        repository.createSession("Second")
        advanceUntilIdle()

        assertEquals(
            listOf("First", "Second"),
            vm.state.value.sessions
                .map { it.title },
        )
    }

    @Test
    fun `new chat creates a session and asks to open it`() = runTest {
        val repository = FakeChatRepository(existingSessionId = null)
        val vm = SessionsViewModel(repository, CreateNewSessionUseCase(repository), SnackBarDispatcher())
        advanceUntilIdle()

        vm.onIntent(SessionsIntent.NewChatClicked)
        val effect = vm.effects.first()

        assertEquals(1, repository.createdSessionCount)
        assertEquals(SessionsEffect.OpenSession("created-session-1"), effect)
    }

    @Test
    fun `new chat reuses an existing empty session instead of piling them up`() = runTest {
        val repository = FakeChatRepository(existingSessionId = "blank")
        val vm = SessionsViewModel(repository, CreateNewSessionUseCase(repository), SnackBarDispatcher())
        advanceUntilIdle()

        vm.onIntent(SessionsIntent.NewChatClicked)
        val effect = vm.effects.first()

        assertEquals(0, repository.createdSessionCount)
        assertEquals(SessionsEffect.OpenSession("blank"), effect)
    }

    @Test
    fun `new chat creates a session when the latest one has messages`() = runTest {
        val repository = FakeChatRepository(existingSessionId = "used")
        repository.appendMessage("used", ChatMessage("m1", MessageAuthor.User, "hi", 0L))
        val vm = SessionsViewModel(repository, CreateNewSessionUseCase(repository), SnackBarDispatcher())
        advanceUntilIdle()

        vm.onIntent(SessionsIntent.NewChatClicked)
        val effect = vm.effects.first()

        assertEquals(1, repository.createdSessionCount)
        assertEquals(SessionsEffect.OpenSession("created-session-1"), effect)
    }

    @Test
    fun `selecting a session asks to open it`() = runTest {
        val repository = FakeChatRepository()
        val vm = SessionsViewModel(repository, CreateNewSessionUseCase(repository), SnackBarDispatcher())
        advanceUntilIdle()

        vm.onIntent(SessionsIntent.SessionSelected("abc"))

        assertEquals(SessionsEffect.OpenSession("abc"), vm.effects.first())
    }

    private suspend fun withOneSession(): Pair<FakeChatRepository, SessionsViewModel> {
        val repository = FakeChatRepository()
        val id = repository.createSession("Original title")
        val vm = SessionsViewModel(repository, CreateNewSessionUseCase(repository), SnackBarDispatcher())
        check(id == "created-session-1")
        return repository to vm
    }

    @Test
    fun `long press opens the action menu for that session`() = runTest {
        val (_, vm) = withOneSession()
        advanceUntilIdle()

        vm.onIntent(SessionsIntent.SessionLongPressed("created-session-1"))

        assertEquals(
            "Original title",
            vm.state.value.actionsFor
                ?.title,
        )
    }

    @Test
    fun `rename starts prefilled with the current title`() = runTest {
        val (_, vm) = withOneSession()
        advanceUntilIdle()

        vm.onIntent(SessionsIntent.SessionLongPressed("created-session-1"))
        vm.onIntent(SessionsIntent.RenameRequested)

        assertNull(vm.state.value.actionsFor)
        assertEquals(
            "Original title",
            vm.state.value.renaming
                ?.title,
        )
    }

    @Test
    fun `renaming persists the new title`() = runTest {
        val (repository, vm) = withOneSession()
        advanceUntilIdle()

        vm.onIntent(SessionsIntent.SessionLongPressed("created-session-1"))
        vm.onIntent(SessionsIntent.RenameRequested)
        vm.onIntent(SessionsIntent.RenameTitleChanged("  Renamed  "))
        vm.onIntent(SessionsIntent.RenameConfirmed)
        advanceUntilIdle()

        assertNull(vm.state.value.renaming)
        assertEquals(
            "Renamed",
            repository
                .observeSessions()
                .first()
                .single()
                .title,
        )
    }

    @Test
    fun `a blank title cannot be confirmed`() = runTest {
        val (repository, vm) = withOneSession()
        advanceUntilIdle()

        vm.onIntent(SessionsIntent.SessionLongPressed("created-session-1"))
        vm.onIntent(SessionsIntent.RenameRequested)
        vm.onIntent(SessionsIntent.RenameTitleChanged("   "))

        assertFalse(
            vm.state.value.renaming!!
                .canConfirm,
        )

        vm.onIntent(SessionsIntent.RenameConfirmed)
        advanceUntilIdle()

        assertTrue(vm.state.value.renaming != null)
        assertEquals(
            "Original title",
            repository
                .observeSessions()
                .first()
                .single()
                .title,
        )
    }

    @Test
    fun `cancelling a rename leaves the title alone`() = runTest {
        val (repository, vm) = withOneSession()
        advanceUntilIdle()

        vm.onIntent(SessionsIntent.SessionLongPressed("created-session-1"))
        vm.onIntent(SessionsIntent.RenameRequested)
        vm.onIntent(SessionsIntent.RenameTitleChanged("Discarded"))
        vm.onIntent(SessionsIntent.RenameCancelled)
        advanceUntilIdle()

        assertNull(vm.state.value.renaming)
        assertEquals(
            "Original title",
            repository
                .observeSessions()
                .first()
                .single()
                .title,
        )
    }

    @Test
    fun `delete asks for confirmation before removing anything`() = runTest {
        val (repository, vm) = withOneSession()
        advanceUntilIdle()

        vm.onIntent(SessionsIntent.SessionLongPressed("created-session-1"))
        vm.onIntent(SessionsIntent.DeleteRequested)
        advanceUntilIdle()

        assertEquals(
            "Original title",
            vm.state.value.deleting
                ?.title,
        )
        assertEquals(1, repository.observeSessions().first().size)
    }

    @Test
    fun `cancelling the confirmation keeps the conversation`() = runTest {
        val (repository, vm) = withOneSession()
        advanceUntilIdle()

        vm.onIntent(SessionsIntent.SessionLongPressed("created-session-1"))
        vm.onIntent(SessionsIntent.DeleteRequested)
        vm.onIntent(SessionsIntent.DeleteCancelled)
        advanceUntilIdle()

        assertNull(vm.state.value.deleting)
        assertEquals(1, repository.observeSessions().first().size)
    }

    @Test
    fun `confirming delete removes the conversation`() = runTest {
        val (repository, vm) = withOneSession()
        advanceUntilIdle()

        vm.onIntent(SessionsIntent.SessionLongPressed("created-session-1"))
        vm.onIntent(SessionsIntent.DeleteRequested)
        vm.onIntent(SessionsIntent.DeleteConfirmed(visibleSessionId = null))
        advanceUntilIdle()

        assertTrue(repository.observeSessions().first().isEmpty())
        assertTrue(
            vm.state.value.sessions
                .isEmpty(),
        )
    }

    @Test
    fun `deleting a conversation the user is not looking at does not navigate`() = runTest {
        val repository = FakeChatRepository()
        repository.createSession("Other")
        val visible = repository.createSession("Visible")
        val vm = SessionsViewModel(repository, CreateNewSessionUseCase(repository), SnackBarDispatcher())
        advanceUntilIdle()

        vm.onIntent(SessionsIntent.SessionLongPressed("created-session-1"))
        vm.onIntent(SessionsIntent.DeleteRequested)
        vm.onIntent(SessionsIntent.DeleteConfirmed(visibleSessionId = visible))
        advanceUntilIdle()

        assertEquals(
            listOf("Visible"),
            vm.state.value.sessions
                .map { it.title },
        )
        assertNull(withTimeoutOrNull(1_000.milliseconds) { vm.effects.first() })
    }

    @Test
    fun `deleting the visible conversation opens a new empty chat, not an older one`() = runTest {
        val repository = FakeChatRepository()
        val older = repository.createSession("Older")
        repository.appendMessage(older, ChatMessage("m1", MessageAuthor.User, "hi", 0L))
        val doomed = repository.createSession("Doomed")
        val vm = SessionsViewModel(repository, CreateNewSessionUseCase(repository), SnackBarDispatcher())
        advanceUntilIdle()

        vm.onIntent(SessionsIntent.SessionLongPressed(doomed))
        vm.onIntent(SessionsIntent.DeleteRequested)
        vm.onIntent(SessionsIntent.DeleteConfirmed(visibleSessionId = doomed))

        val effect = vm.effects.first() as SessionsEffect.OpenSession
        assertTrue(effect.sessionId != older)
        assertTrue(repository.isSessionEmpty(effect.sessionId))
    }

    @Test
    fun `deleting the last conversation creates a fresh one to land on`() = runTest {
        val (repository, vm) = withOneSession()
        advanceUntilIdle()

        vm.onIntent(SessionsIntent.SessionLongPressed("created-session-1"))
        vm.onIntent(SessionsIntent.DeleteRequested)
        vm.onIntent(SessionsIntent.DeleteConfirmed(visibleSessionId = "created-session-1"))

        assertEquals(SessionsEffect.OpenSession("created-session-2"), vm.effects.first())
        advanceUntilIdle()
        assertEquals(2, repository.createdSessionCount)
    }

    @Test
    fun `closing the drawer dismisses a rename without saving its draft`() = runTest {
        val (repository, vm) = withOneSession()
        advanceUntilIdle()
        vm.onIntent(SessionsIntent.SessionLongPressed("created-session-1"))
        vm.onIntent(SessionsIntent.RenameRequested)
        vm.onIntent(SessionsIntent.RenameTitleChanged("Unsaved draft"))
        vm.onIntent(SessionsIntent.DrawerClosed)
        assertNull(vm.state.value.renaming)
        assertEquals("Original title", repository.observeSessions().first().single().title)
    }

    @Test
    fun `a rename is dismissed if another action removes its session`() = runTest {
        val (repository, vm) = withOneSession()
        advanceUntilIdle()
        vm.onIntent(SessionsIntent.SessionLongPressed("created-session-1"))
        vm.onIntent(SessionsIntent.RenameRequested)
        repository.deleteSession("created-session-1")
        advanceUntilIdle()
        assertNull(vm.state.value.renaming)
    }

    @Test
    fun `closing the drawer cancels a pending delete confirmation`() = runTest {
        val (repository, vm) = withOneSession()
        advanceUntilIdle()
        vm.onIntent(SessionsIntent.SessionLongPressed("created-session-1"))
        vm.onIntent(SessionsIntent.DeleteRequested)
        vm.onIntent(SessionsIntent.DrawerClosed)
        assertNull(vm.state.value.deleting)
        assertEquals(1, repository.observeSessions().first().size)
    }

    @Test
    fun `a cancelled rename is not reported as a storage failure`() = runTest {
        val backing = FakeChatRepository()
        val id = backing.createSession("Original title")
        val repository = object : ChatRepository by backing {
            override suspend fun renameSession(sessionId: String, title: String): Unit =
                throw CancellationException("cancelled")
        }
        val snackBar = SnackBarDispatcher()
        val vm = SessionsViewModel(repository, CreateNewSessionUseCase(repository), snackBar)
        advanceUntilIdle()
        vm.onIntent(SessionsIntent.SessionLongPressed(id))
        vm.onIntent(SessionsIntent.RenameRequested)
        vm.onIntent(SessionsIntent.RenameTitleChanged("Renamed"))
        vm.onIntent(SessionsIntent.RenameConfirmed)
        advanceUntilIdle()
        assertEquals("Original title", backing.observeSessions().first().single().title)
        assertNull(withTimeoutOrNull(1_000.milliseconds) { snackBar.messages.first() })
    }

    @Test
    fun `a failed rename keeps the conversation and reports the failure`() = runTest {
        val backing = FakeChatRepository()
        val id = backing.createSession("Original title")
        val repository = object : ChatRepository by backing {
            override suspend fun renameSession(sessionId: String, title: String): Unit =
                throw IOException("write failed")
        }
        val snackBar = SnackBarDispatcher()
        val vm = SessionsViewModel(repository, CreateNewSessionUseCase(repository), snackBar)
        advanceUntilIdle()
        vm.onIntent(SessionsIntent.SessionLongPressed(id))
        vm.onIntent(SessionsIntent.RenameRequested)
        vm.onIntent(SessionsIntent.RenameConfirmed)
        advanceUntilIdle()
        assertEquals("Original title", backing.observeSessions().first().single().title)
        assertEquals(uiText(Res.string.chat_error_rename), snackBar.messages.first().text)
    }

    @Test
    fun `a failed delete keeps the visible conversation and does not navigate`() = runTest {
        val backing = FakeChatRepository()
        val id = backing.createSession("Keep me")
        val repository = object : ChatRepository by backing {
            override suspend fun deleteSession(sessionId: String): Unit = throw IOException("write failed")
        }
        val snackBar = SnackBarDispatcher()
        val vm = SessionsViewModel(repository, CreateNewSessionUseCase(repository), snackBar)
        advanceUntilIdle()
        vm.onIntent(SessionsIntent.SessionLongPressed(id))
        vm.onIntent(SessionsIntent.DeleteRequested)
        vm.onIntent(SessionsIntent.DeleteConfirmed(id))
        advanceUntilIdle()
        assertEquals(id, backing.observeSessions().first().single().id)
        assertEquals(uiText(Res.string.chat_error_delete), snackBar.messages.first().text)
        assertNull(withTimeoutOrNull(1_000.milliseconds) { vm.effects.first() })
    }
}
