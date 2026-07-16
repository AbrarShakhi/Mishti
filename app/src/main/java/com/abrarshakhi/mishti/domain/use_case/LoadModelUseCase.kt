package com.abrarshakhi.mishti.domain.use_case

import com.abrarshakhi.mishti.domain.repository.ModelRepository
import com.abrarshakhi.mishti.llm.LlamaEngine

/**
 * Loads a downloaded model into the inference engine.
 *
 * Resolves the file path from [ModelRepository] so the ViewModel
 * never has to know about internal storage paths.
 *
 * Returns a [Result] so the caller can surface the error cleanly.
 */
class LoadModelUseCase(
    private val modelRepository: ModelRepository,
    private val engine: LlamaEngine,
) {
    suspend operator fun invoke(modelId: String): Result<Unit> {
        val path = modelRepository.getFilePath(modelId)
            ?: return Result.failure(IllegalStateException("Model $modelId is not downloaded."))

        val ok = engine.loadModel(path)
        return if (ok) Result.success(Unit)
        else Result.failure(RuntimeException("LlamaEngine failed to load model at $path"))
    }
}
