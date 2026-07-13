package com.abrarshakhi.mishti.llm

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext

/**
 * LlamaEngine
 *
 * This is the Kotlin side of the C++ bridge.
 *
 * The `external` keyword tells Kotlin "this function's body lives in C++".
 * The `System.loadLibrary("mishti-llm")` call loads the compiled .so file
 * (libmishti-llm.so) that CMake built for us.
 *
 * Threading:
 *   - All native calls are blocking — they don't return until done.
 *   - We always dispatch them to Dispatchers.IO so we never block the UI thread.
 */
class LlamaEngine {

    // ── Interface used by C++ to send tokens back to Kotlin ──────────────────
    // C++ calls tokenCallback.onToken(piece) for every generated token.
    // This must be a plain interface (not SAM / lambda) so JNI can find the method.
    interface TokenCallback {
        fun onToken(piece: String)
    }

    // ── JNI declarations ──────────────────────────────────────────────────────
    // These match the function signatures in llm_bridge.cpp exactly.
    private external fun nativeLoadModel(modelPath: String): Boolean
    private external fun nativeCompletion(prompt: String, callback: TokenCallback)
    private external fun nativeReset()
    private external fun nativeIsModelLoaded(): Boolean

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Load a model from a .gguf file on disk.
     * @return true if the model loaded successfully.
     */
    suspend fun loadModel(modelPath: String): Boolean = withContext(Dispatchers.IO) {
        Log.i(TAG, "Loading model from $modelPath")
        nativeLoadModel(modelPath)
    }

    /**
     * Run a completion. Returns a [Flow] that emits tokens as they are generated.
     * Collect this flow from a coroutine on the UI side to stream text to the screen.
     *
     * Example usage in a ViewModel:
     *   engine.complete(prompt).collect { token -> uiState = uiState + token }
     */
    fun complete(prompt: String): Flow<String> = callbackFlow {
        // callbackFlow lets us convert a callback-based API into a Flow.
        // 'trySend' pushes each token into the flow's buffer.
        val callback = object : TokenCallback {
            override fun onToken(piece: String) {
                trySend(piece) // non-blocking send to the flow collector
            }
        }

        // Run on IO thread so we don't block the main thread
        withContext(Dispatchers.IO) {
            nativeCompletion(prompt, callback)
        }

        // Tell the flow there are no more tokens
        close()

        // awaitClose is required by callbackFlow — it runs when the collector
        // cancels. We don't need cleanup here because nativeCompletion already
        // returned, but it's good practice.
        awaitClose { }
    }

    /**
     * Free the native model and context from memory.
     * Call this when switching models or in onDestroy.
     */
    suspend fun reset() = withContext(Dispatchers.IO) {
        nativeReset()
    }

    /** Returns true if a model is currently loaded and ready. */
    fun isModelLoaded(): Boolean = nativeIsModelLoaded()

    companion object {
        private const val TAG = "LlamaEngine"

        init {
            // This runs once when the class is first used.
            // It tells Android to load libmishti-llm.so from the APK.
            System.loadLibrary("mishti-llm")
        }
    }
}