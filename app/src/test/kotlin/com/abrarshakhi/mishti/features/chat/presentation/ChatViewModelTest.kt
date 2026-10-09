package com.abrarshakhi.mishti.features.chat.presentation

import com.abrarshakhi.mishti.common.MainDispatcherRule
import com.abrarshakhi.mishti.common.fake.FakeAppPreferences
import com.abrarshakhi.mishti.common.llm.EngineOptions
import com.abrarshakhi.mishti.common.llm.EngineState
import com.abrarshakhi.mishti.common.llm.GenerationEvent
import com.abrarshakhi.mishti.common.llm.GenerationParams
import com.abrarshakhi.mishti.common.llm.InferenceSettings
import com.abrarshakhi.mishti.common.llm.LlmEngine
import com.abrarshakhi.mishti.common.llm.LlmMessage
import com.abrarshakhi.mishti.common.llm.LlmRole
import com.abrarshakhi.mishti.common.llm.ModelHandle
import com.abrarshakhi.mishti.common.llm.ScriptedLlmEngine
import com.abrarshakhi.mishti.features.chat.domain.model.MessageAuthor
import com.abrarshakhi.mishti.features.chat.fake.FakeChatRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds

private class RecordingEngine(
    private val tokenCount: Int = 3,
    private val durationMillis: Long = 1_000,
    private val tokens: List<String> = listOf("ok"),
    private val supportsThinking: Boolean = false,
) : LlmEngine {
    private val _state = kotlinx.coroutines.flow.MutableStateFlow<EngineState>(EngineState.Idle)
    override val state = _state

    var lastMessages: List<LlmMessage> = emptyList()
        private set
    var lastParams: GenerationParams? = null
        private set

    override suspend fun load(
        model: ModelHandle,
        options: EngineOptions,
    ) {
        _state.value = EngineState.Ready(model, supportsThinking)
    }

    override suspend fun unload() {
        _state.value = EngineState.Idle
    }

    override fun generate(
        messages: List<LlmMessage>,
        params: GenerationParams,
    ) = kotlinx.coroutines.flow.flow {
        lastMessages = messages
        lastParams = params
        tokens.forEach { emit(GenerationEvent.Token(it)) }
        emit(GenerationEvent.Completed(tokenCount, durationMillis))
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun engine() =
        ScriptedLlmEngine(
            script = listOf("Scripted reply."),
            loadDelayMillis = 0,
            tokenDelayMillis = 0,
        )

    private var nextId = 0

    private fun viewModel(
        repository: FakeChatRepository = FakeChatRepository(existingSessionId = "s1"),
        requestedSessionId: String? = null,
        engine: LlmEngine = engine(),
        preferences: FakeAppPreferences = FakeAppPreferences(),
    ) = ChatViewModel(
        requestedSessionId = requestedSessionId,
        repository = repository,
        engine = engine,
        preferences = preferences,
        clock = { 1_000L },
        newId = { "id-${nextId++}" },
    )

    private suspend fun LlmEngine.ready() =
        load(ModelHandle(id = "test-model", name = "Test", path = "/tmp/test.gguf"))

    @Test
    fun `resumes the most recent session instead of creating one`() =
        runTest {
            val repository = FakeChatRepository(existingSessionId = "s1")
            val vm = viewModel(repository)
            advanceUntilIdle()

            assertEquals("s1", vm.state.value.sessionId)
            assertEquals(0, repository.createdSessionCount)
        }

    @Test
    fun `creates a session only when none exists`() =
        runTest {
            val repository = FakeChatRepository(existingSessionId = null)
            val vm = viewModel(repository)
            advanceUntilIdle()

            assertEquals("created-session-1", vm.state.value.sessionId)
            assertEquals(1, repository.createdSessionCount)
        }

    @Test
    fun `an explicit session id wins over the most recent one`() =
        runTest {
            val repository = FakeChatRepository(existingSessionId = "s1")
            val vm = viewModel(repository, requestedSessionId = "requested")
            advanceUntilIdle()

            assertEquals("requested", vm.state.value.sessionId)
        }

    @Test
    fun `the title follows the session, renames included`() =
        runTest {
            val repository = FakeChatRepository(existingSessionId = null)
            val vm = viewModel(repository)
            advanceUntilIdle()
            assertEquals("New chat", vm.state.value.title)

            repository.renameSession("created-session-1", "Rainbows")
            advanceUntilIdle()
            assertEquals("Rainbows", vm.state.value.title)
        }

    @Test
    fun `loading clears once the first emission arrives`() =
        runTest {
            val vm = viewModel()
            assertTrue(vm.state.value.isLoading)

            advanceUntilIdle()
            assertFalse(vm.state.value.isLoading)
        }

    @Test
    fun `send is disabled until the draft has content`() =
        runTest {
            val engine = engine()
            val vm = viewModel(engine = engine)
            engine.ready()
            advanceUntilIdle()
            assertFalse(vm.state.value.canSend)

            vm.onIntent(ChatIntent.DraftChanged("Hello"))
            assertTrue(vm.state.value.canSend)
        }

    @Test
    fun `send stays disabled while no model is loaded`() =
        runTest {
            val vm = viewModel()
            advanceUntilIdle()

            vm.onIntent(ChatIntent.DraftChanged("Hello"))
            assertFalse(vm.state.value.canSend)
        }

    @Test
    fun `a draft typed before the model loads becomes sendable once it is ready`() =
        runTest {
            val engine = engine()
            val vm = viewModel(engine = engine)
            advanceUntilIdle()

            vm.onIntent(ChatIntent.DraftChanged("Hello"))
            assertFalse(vm.state.value.canSend)

            engine.ready()
            advanceUntilIdle()
            assertTrue(vm.state.value.canSend)
        }

    @Test
    fun `a blank draft does not enable send`() =
        runTest {
            val engine = engine()
            val vm = viewModel(engine = engine)
            engine.ready()
            advanceUntilIdle()

            vm.onIntent(ChatIntent.DraftChanged("   "))
            assertFalse(vm.state.value.canSend)
        }

    @Test
    fun `sending persists the user turn and clears the draft`() =
        runTest {
            val engine = engine()
            val vm = viewModel(engine = engine)
            engine.ready()
            advanceUntilIdle()

            vm.onIntent(ChatIntent.DraftChanged("  Hello  "))
            vm.onIntent(ChatIntent.SendClicked)
            advanceUntilIdle()

            val messages = vm.state.value.messages
            assertEquals("Hello", messages.first().content)
            assertEquals(MessageAuthor.User, messages.first().author)
            assertEquals(1_000L, messages.first().createdAtMillis)
            assertEquals("", vm.state.value.draft)
        }

    @Test
    fun `a reply is generated and persisted as an assistant turn`() =
        runTest {
            val engine = engine()
            val vm = viewModel(engine = engine)
            engine.ready()
            advanceUntilIdle()

            vm.onIntent(ChatIntent.DraftChanged("Hello"))
            vm.onIntent(ChatIntent.SendClicked)
            advanceUntilIdle()

            val messages = vm.state.value.messages
            assertEquals(2, messages.size)
            assertEquals(MessageAuthor.Assistant, messages[1].author)
            assertEquals("Scripted reply.", messages[1].content)
        }

    @Test
    fun `streaming state is cleared once the reply is stored`() =
        runTest {
            val engine = engine()
            val vm = viewModel(engine = engine)
            engine.ready()
            advanceUntilIdle()

            vm.onIntent(ChatIntent.DraftChanged("Hello"))
            vm.onIntent(ChatIntent.SendClicked)
            advanceUntilIdle()

            val state = vm.state.value
            assertEquals("", state.streamingResponse)
            assertFalse(state.isGenerating)
            assertFalse(state.canStop)
        }

    @Test
    fun `stopping keeps the partial reply instead of discarding it`() =
        runTest {
            val engine =
                ScriptedLlmEngine(
                    script = listOf("abcdefghijklmnop"),
                    loadDelayMillis = 0,
                    tokenDelayMillis = 10,
                )
            val vm = viewModel(engine = engine)
            engine.ready()
            advanceUntilIdle()

            vm.onIntent(ChatIntent.DraftChanged("Hello"))
            vm.onIntent(ChatIntent.SendClicked)
            advanceTimeBy(35.milliseconds)
            assertTrue(vm.state.value.isGenerating)
            val partial = vm.state.value.streamingResponse
            assertTrue("expected some tokens by now", partial.isNotEmpty())

            vm.onIntent(ChatIntent.StopClicked)
            advanceUntilIdle()

            val messages = vm.state.value.messages
            assertEquals(2, messages.size)
            assertEquals(partial, messages[1].content)
            assertFalse(vm.state.value.isGenerating)
        }

    @Test
    fun `sending a blank draft is ignored even if the intent arrives`() =
        runTest {
            val engine = engine()
            val vm = viewModel(engine = engine)
            engine.ready()
            advanceUntilIdle()

            vm.onIntent(ChatIntent.SendClicked)
            advanceUntilIdle()

            assertTrue(
                vm.state.value.messages
                    .isEmpty(),
            )
        }

    @Test
    fun `a pre-instruction is sent ahead of the conversation`() =
        runTest {
            val engine = RecordingEngine()
            val vm =
                viewModel(
                    engine = engine,
                    preferences =
                        FakeAppPreferences(
                            InferenceSettings(systemPrompt = "  Answer in one word.  "),
                        ),
                )
            engine.ready()
            advanceUntilIdle()

            vm.onIntent(ChatIntent.DraftChanged("Hello"))
            vm.onIntent(ChatIntent.SendClicked)
            advanceUntilIdle()

            val sent = engine.lastMessages
            assertEquals(LlmRole.System, sent.first().role)
            assertEquals("Answer in one word.", sent.first().content)
            assertEquals(LlmRole.User, sent[1].role)
        }

    @Test
    fun `no pre-instruction means no system turn`() =
        runTest {
            val engine = RecordingEngine()
            val vm = viewModel(engine = engine)
            engine.ready()
            advanceUntilIdle()

            vm.onIntent(ChatIntent.DraftChanged("Hello"))
            vm.onIntent(ChatIntent.SendClicked)
            advanceUntilIdle()

            assertTrue(engine.lastMessages.none { it.role == LlmRole.System })
        }

    @Test
    fun `sampling settings reach the engine`() =
        runTest {
            val engine = RecordingEngine()
            val custom =
                InferenceSettings(temperature = 0.25f, topK = 7, topP = 0.5f, maxTokens = 99)
            val vm = viewModel(engine = engine, preferences = FakeAppPreferences(custom))
            engine.ready()
            advanceUntilIdle()

            vm.onIntent(ChatIntent.DraftChanged("Hello"))
            vm.onIntent(ChatIntent.SendClicked)
            advanceUntilIdle()

            assertEquals(custom.generation, engine.lastParams)
        }

    @Test
    fun `throughput is recorded on the assistant turn`() =
        runTest {
            val engine = RecordingEngine(tokenCount = 20, durationMillis = 1_000)
            val vm = viewModel(engine = engine)
            engine.ready()
            advanceUntilIdle()

            vm.onIntent(ChatIntent.DraftChanged("Hello"))
            vm.onIntent(ChatIntent.SendClicked)
            advanceUntilIdle()

            val reply =
                vm.state.value.messages
                    .last()
            assertEquals(MessageAuthor.Assistant, reply.author)
            assertEquals(20.0, reply.tokensPerSecond!!, 0.01)
        }

    @Test
    fun `thinking is offered only for models that support it`() =
        runTest {
            val plain = RecordingEngine()
            val plainVm = viewModel(engine = plain)
            plain.ready()
            advanceUntilIdle()
            plainVm.onIntent(ChatIntent.ThinkingToggled)
            advanceUntilIdle()

            assertFalse(plainVm.state.value.thinkingSupported)
            assertFalse(plainVm.state.value.thinkingEnabled)

            val thinker = RecordingEngine(supportsThinking = true)
            val thinkerVm = viewModel(engine = thinker)
            thinker.ready()
            advanceUntilIdle()

            assertTrue(thinkerVm.state.value.thinkingSupported)
            assertFalse(thinkerVm.state.value.thinkingEnabled)
        }

    @Test
    fun `the thinking choice is remembered for the model`() =
        runTest {
            val preferences = FakeAppPreferences()
            val engine = RecordingEngine(supportsThinking = true)
            val vm = viewModel(engine = engine, preferences = preferences)
            engine.ready()
            advanceUntilIdle()

            vm.onIntent(ChatIntent.ThinkingToggled)
            advanceUntilIdle()

            assertTrue(vm.state.value.thinkingEnabled)
            assertEquals(setOf("test-model"), preferences.thinkingModelIds.first())

            vm.onIntent(ChatIntent.ThinkingToggled)
            advanceUntilIdle()

            assertFalse(vm.state.value.thinkingEnabled)
            assertTrue(preferences.thinkingModelIds.first().isEmpty())
        }

    @Test
    fun `thinking asks the engine to reason and doubles the token budget`() =
        runTest {
            val engine = RecordingEngine(supportsThinking = true)
            val custom = InferenceSettings(maxTokens = 300)
            val vm = viewModel(engine = engine, preferences = FakeAppPreferences(custom))
            engine.ready()
            advanceUntilIdle()

            vm.onIntent(ChatIntent.DraftChanged("Hello"))
            vm.onIntent(ChatIntent.SendClicked)
            advanceUntilIdle()
            assertEquals(custom.generation, engine.lastParams)

            vm.onIntent(ChatIntent.ThinkingToggled)
            vm.onIntent(ChatIntent.DraftChanged("Again"))
            advanceUntilIdle()
            vm.onIntent(ChatIntent.SendClicked)
            advanceUntilIdle()
            assertEquals(
                custom.generation.copy(thinking = true, maxTokens = 600),
                engine.lastParams
            )
        }

    @Test
    fun `reasoning is kept apart from the answer and saved with the reply`() =
        runTest {
            val engine =
                RecordingEngine(
                    tokens = listOf("<think>", "\nWeigh it up.", "</think>", "\n\nAnswer."),
                    supportsThinking = true,
                )
            val vm = viewModel(engine = engine)
            engine.ready()
            advanceUntilIdle()

            vm.onIntent(ChatIntent.DraftChanged("Hello"))
            vm.onIntent(ChatIntent.SendClicked)
            advanceUntilIdle()

            val reply =
                vm.state.value.messages
                    .last()
            assertEquals("Answer.", reply.content)
            assertEquals("Weigh it up.", reply.reasoning)
            assertEquals(0L, reply.reasoningMillis)
            assertNull(vm.state.value.streamingReasoning)
        }

    @Test
    fun `earlier reasoning is not sent back to the model`() =
        runTest {
            val engine =
                RecordingEngine(
                    tokens = listOf("<think>Private.</think>", "Answer."),
                    supportsThinking = true,
                )
            val vm = viewModel(engine = engine)
            engine.ready()
            advanceUntilIdle()

            vm.onIntent(ChatIntent.DraftChanged("Hello"))
            vm.onIntent(ChatIntent.SendClicked)
            advanceUntilIdle()
            vm.onIntent(ChatIntent.DraftChanged("And then?"))
            vm.onIntent(ChatIntent.SendClicked)
            advanceUntilIdle()

            val assistantTurn = engine.lastMessages.single { it.role == LlmRole.Assistant }
            assertEquals("Answer.", assistantTurn.content)
        }

    @Test
    fun `think tags stay in the reply when the model does not support thinking`() =
        runTest {
            val engine = RecordingEngine(tokens = listOf("<think>x</think>y"))
            val vm = viewModel(engine = engine)
            engine.ready()
            advanceUntilIdle()

            vm.onIntent(ChatIntent.DraftChanged("Hello"))
            vm.onIntent(ChatIntent.SendClicked)
            advanceUntilIdle()

            val reply =
                vm.state.value.messages
                    .last()
            assertEquals("<think>x</think>y", reply.content)
            assertNull(reply.reasoning)
        }

    @Test
    fun `stopping while the model is thinking keeps its thoughts`() =
        runTest {
            val engine =
                ScriptedLlmEngine(
                    script = listOf("<think>abcdefghijklmnop</think>Answer"),
                    loadDelayMillis = 0,
                    tokenDelayMillis = 10,
                    supportsThinking = true,
                )
            val vm = viewModel(engine = engine)
            engine.ready()
            advanceUntilIdle()

            vm.onIntent(ChatIntent.DraftChanged("Hello"))
            vm.onIntent(ChatIntent.SendClicked)
            advanceTimeBy(35.milliseconds)
            assertTrue(vm.state.value.isReasoning)
            val thoughts = vm.state.value.streamingReasoning
            assertTrue("expected some reasoning by now", !thoughts.isNullOrEmpty())
            assertEquals("", vm.state.value.streamingResponse)

            vm.onIntent(ChatIntent.StopClicked)
            advanceUntilIdle()

            val reply =
                vm.state.value.messages
                    .last()
            assertEquals(MessageAuthor.Assistant, reply.author)
            assertEquals("", reply.content)
            assertEquals(thoughts, reply.reasoning)
        }

    @Test
    fun `a failed write restores the draft so the user does not lose it`() =
        runTest {
            val repository =
                FakeChatRepository(existingSessionId = "s1").apply { failOnAppend = true }
            val engine = engine()
            val vm = viewModel(repository, engine = engine)
            engine.ready()
            advanceUntilIdle()

            vm.onIntent(ChatIntent.DraftChanged("Hello"))
            vm.onIntent(ChatIntent.SendClicked)
            advanceUntilIdle()

            val state = vm.state.value
            assertTrue(state.messages.isEmpty())
            assertEquals("Hello", state.draft)
            assertTrue(state.canSend)
        }
}
