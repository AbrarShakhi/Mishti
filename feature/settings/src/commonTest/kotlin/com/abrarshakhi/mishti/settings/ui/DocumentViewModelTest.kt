package com.abrarshakhi.mishti.settings.ui

import com.abrarshakhi.mishti.core.domain.documents.AppDocument
import com.abrarshakhi.mishti.settings.domain.repository.DocumentRepository
import com.abrarshakhi.mishti.settings.domain.usecase.ReadDocumentUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okio.IOException
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class DocumentViewModelTest {
    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `documents are loaded through the repository and formatted for the reader`() = runTest {
        val viewModel = DocumentViewModel(
            AppDocument.Credits,
            ReadDocumentUseCase(
                DocumentRepository {
                    assertEquals(AppDocument.Credits, it)
                    "# Credits\n\n- A library\n  and its author."
                },
            ),
        )
        assertTrue(viewModel.state.value.isLoading)
        advanceUntilIdle()
        assertEquals("- A library\nand its author.", viewModel.state.value.markdown)
        assertFalse(viewModel.state.value.isLoading)
        assertFalse(viewModel.state.value.hasFailed)
    }

    @Test
    fun `a failed document can be retried without reopening the screen`() = runTest {
        var fails = true
        val viewModel = DocumentViewModel(
            AppDocument.About,
            ReadDocumentUseCase(
                DocumentRepository {
                    if (fails) throw IOException("missing")
                    "# About\n\nAvailable offline."
                },
            ),
        )
        advanceUntilIdle()
        assertTrue(viewModel.state.value.hasFailed)
        fails = false
        viewModel.onIntent(DocumentIntent.Retry)
        advanceUntilIdle()
        assertFalse(viewModel.state.value.hasFailed)
        assertEquals("Available offline.", viewModel.state.value.markdown)
    }
}
