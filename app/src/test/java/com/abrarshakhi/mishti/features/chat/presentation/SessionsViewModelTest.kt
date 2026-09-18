package com.abrarshakhi.mishti.features.chat.presentation

import com.abrarshakhi.mishti.common.MainDispatcherRule
import com.abrarshakhi.mishti.common.ui.snackbar.SnackbarDispatcher
import com.abrarshakhi.mishti.features.chat.domain.model.ChatMessage
import com.abrarshakhi.mishti.features.chat.domain.model.MessageAuthor
import com.abrarshakhi.mishti.features.chat.fake.FakeChatRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SessionsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `the drawer lists sessions from the repository`() = runTest {
        val repository = FakeChatRepository()
        val vm = SessionsViewModel(repository, SnackbarDispatcher())
        advanceUntilIdle()

        repository.createSession("First")
        repository.createSession("Second")
        advanceUntilIdle()

        assertEquals(listOf("First", "Second"), vm.state.value.sessions.map { it.title })
    }

    @Test
    fun `new chat creates a session and asks to open it`() = runTest {
        val repository = FakeChatRepository(existingSessionId = null)
        val vm = SessionsViewModel(repository, SnackbarDispatcher())
        advanceUntilIdle()

        vm.onIntent(SessionsIntent.NewChatClicked)
        val effect = vm.effects.first()

        assertEquals(1, repository.createdSessionCount)
        assertEquals(SessionsEffect.OpenSession("created-session-1"), effect)
    }

    @Test
    fun `new chat reuses an existing empty session instead of piling them up`() = runTest {
        val repository = FakeChatRepository(existingSessionId = "blank")
        val vm = SessionsViewModel(repository, SnackbarDispatcher())
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
        val vm = SessionsViewModel(repository, SnackbarDispatcher())
        advanceUntilIdle()

        vm.onIntent(SessionsIntent.NewChatClicked)
        val effect = vm.effects.first()

        assertEquals(1, repository.createdSessionCount)
        assertEquals(SessionsEffect.OpenSession("created-session-1"), effect)
    }

    @Test
    fun `selecting a session asks to open it`() = runTest {
        val vm = SessionsViewModel(FakeChatRepository(), SnackbarDispatcher())
        advanceUntilIdle()

        vm.onIntent(SessionsIntent.SessionSelected("abc"))

        assertEquals(SessionsEffect.OpenSession("abc"), vm.effects.first())
    }

    // --- long-press actions -------------------------------------------------------------

    private suspend fun withOneSession(): Pair<FakeChatRepository, SessionsViewModel> {
        val repository = FakeChatRepository()
        val id = repository.createSession("Original title")
        val vm = SessionsViewModel(repository, SnackbarDispatcher())
        check(id == "created-session-1")
        return repository to vm
    }

    @Test
    fun `long press opens the action menu for that session`() = runTest {
        val (_, vm) = withOneSession()
        advanceUntilIdle()

        vm.onIntent(SessionsIntent.SessionLongPressed("created-session-1"))

        assertEquals("Original title", vm.state.value.actionsFor?.title)
    }

    @Test
    fun `rename starts prefilled with the current title`() = runTest {
        val (_, vm) = withOneSession()
        advanceUntilIdle()

        vm.onIntent(SessionsIntent.SessionLongPressed("created-session-1"))
        vm.onIntent(SessionsIntent.RenameRequested)

        // The menu closes as the dialog opens, so both are never on screen at once.
        assertNull(vm.state.value.actionsFor)
        assertEquals("Original title", vm.state.value.renaming?.title)
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
        assertEquals("Renamed", repository.observeSessions().first().single().title)
    }

    @Test
    fun `a blank title cannot be confirmed`() = runTest {
        val (repository, vm) = withOneSession()
        advanceUntilIdle()

        vm.onIntent(SessionsIntent.SessionLongPressed("created-session-1"))
        vm.onIntent(SessionsIntent.RenameRequested)
        vm.onIntent(SessionsIntent.RenameTitleChanged("   "))

        assertFalse(vm.state.value.renaming!!.canConfirm)

        vm.onIntent(SessionsIntent.RenameConfirmed)
        advanceUntilIdle()

        // Still open, and the stored title is untouched.
        assertTrue(vm.state.value.renaming != null)
        assertEquals("Original title", repository.observeSessions().first().single().title)
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
        assertEquals("Original title", repository.observeSessions().first().single().title)
    }

    @Test
    fun `delete asks for confirmation before removing anything`() = runTest {
        val (repository, vm) = withOneSession()
        advanceUntilIdle()

        vm.onIntent(SessionsIntent.SessionLongPressed("created-session-1"))
        vm.onIntent(SessionsIntent.DeleteRequested)
        advanceUntilIdle()

        assertEquals("Original title", vm.state.value.deleting?.title)
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
        assertTrue(vm.state.value.sessions.isEmpty())
    }

    @Test
    fun `deleting a conversation the user is not looking at does not navigate`() = runTest {
        val repository = FakeChatRepository()
        repository.createSession("Other")
        val visible = repository.createSession("Visible")
        val vm = SessionsViewModel(repository, SnackbarDispatcher())
        advanceUntilIdle()

        vm.onIntent(SessionsIntent.SessionLongPressed("created-session-1"))
        vm.onIntent(SessionsIntent.DeleteRequested)
        vm.onIntent(SessionsIntent.DeleteConfirmed(visibleSessionId = visible))
        advanceUntilIdle()

        assertEquals(listOf("Visible"), vm.state.value.sessions.map { it.title })
        // `first()` on the effect channel would suspend forever when nothing is emitted, so
        // absence has to be asserted against a (virtual) timeout.
        assertNull(withTimeoutOrNull(1_000) { vm.effects.first() })
    }

    @Test
    fun `deleting the visible conversation opens a new empty chat, not an older one`() = runTest {
        val repository = FakeChatRepository()
        val older = repository.createSession("Older")
        repository.appendMessage(older, ChatMessage("m1", MessageAuthor.User, "hi", 0L))
        val doomed = repository.createSession("Doomed")
        val vm = SessionsViewModel(repository, SnackbarDispatcher())
        advanceUntilIdle()

        vm.onIntent(SessionsIntent.SessionLongPressed(doomed))
        vm.onIntent(SessionsIntent.DeleteRequested)
        vm.onIntent(SessionsIntent.DeleteConfirmed(visibleSessionId = doomed))

        // Not `older`, and not null: being dropped into a previous conversation is
        // disorienting, and an equal Chat(null) route would reuse the dead ViewModel.
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
}
