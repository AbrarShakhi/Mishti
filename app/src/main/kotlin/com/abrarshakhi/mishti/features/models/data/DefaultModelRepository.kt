package com.abrarshakhi.mishti.features.models.data

import com.abrarshakhi.mishti.common.data.preferences.AppPreferences
import com.abrarshakhi.mishti.features.models.domain.model.LlmModel
import com.abrarshakhi.mishti.features.models.domain.model.ModelCatalog
import com.abrarshakhi.mishti.features.models.domain.model.ModelEntry
import com.abrarshakhi.mishti.features.models.domain.model.ModelStatus
import com.abrarshakhi.mishti.features.models.domain.repository.ModelRepository
import com.abrarshakhi.mishti.features.models.domain.repository.StorageUsage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

class DefaultModelRepository(
    private val storageManager: ModelStorage,
    private val downloader: ModelDownloader,
    private val preferences: AppPreferences,
    private val scope: CoroutineScope,
    private val notifier: DownloadNotifier = DownloadNotifier.Noop,
) : ModelRepository {

    override val selectedModelId: Flow<String?> = preferences.selectedModelId

    override suspend fun select(modelId: String?) {
        val valid = modelId == null || ModelCatalog.byId(modelId)
            ?.let { storageManager.isDownloaded(it) } == true
        if (valid) preferences.setSelectedModelId(modelId)
    }

    private val statuses = MutableStateFlow(initialStatuses())
    private val jobs = ConcurrentHashMap<String, Job>()

    override val entries: Flow<List<ModelEntry>> = statuses.asStateFlow().map { current ->
        ModelCatalog.models.map { model ->
            ModelEntry(model = model, status = current[model.id] ?: ModelStatus.NotDownloaded)
        }
    }

    private val _storage = MutableStateFlow(readStorage())
    override val storage: Flow<StorageUsage> = _storage.asStateFlow()

    private fun initialStatuses(): Map<String, ModelStatus> =
        ModelCatalog.models.associate { model ->
            model.id to if (storageManager.isDownloaded(model)) {
                ModelStatus.Downloaded(storageManager.sizeOnDisk(model))
            } else {
                ModelStatus.NotDownloaded
            }
        }

    private fun readStorage() = StorageUsage(
        usedBytes = storageManager.usedBytes(),
        availableBytes = storageManager.availableBytes(),
    )

    override fun download(modelId: String) {
        val model = ModelCatalog.byId(modelId) ?: return
        if (jobs[modelId]?.isActive == true) return

        setStatus(
            model,
            ModelStatus.Downloading(storageManager.partialBytes(model), model.sizeBytes)
        )
        notifier.onDownloadsActive()

        jobs[modelId] = scope.launch {
            try {
                downloader.download(model) { progress ->
                    setStatus(
                        model,
                        when (progress) {
                            is DownloadProgress.Downloading -> ModelStatus.Downloading(
                                downloadedBytes = progress.downloadedBytes,
                                totalBytes = progress.totalBytes,
                            )

                            DownloadProgress.Verifying -> ModelStatus.Verifying
                        },
                    )
                }
                setStatus(model, ModelStatus.Downloaded(storageManager.sizeOnDisk(model)))
            } catch (e: CancellationException) {
                setStatus(model, ModelStatus.NotDownloaded)
                throw e
            } catch (e: Exception) {
                setStatus(model, ModelStatus.Failed(e.message ?: "Download failed."))
            } finally {
                jobs.remove(modelId)
                _storage.value = readStorage()
            }
        }
    }

    override fun cancel(modelId: String) {
        jobs.remove(modelId)?.cancel()
    }

    override fun cancelAll() {
        jobs.keys.toList().forEach { cancel(it) }
    }

    override suspend fun delete(modelId: String) {
        val model = ModelCatalog.byId(modelId) ?: return
        cancel(modelId)
        storageManager.delete(model)
        setStatus(model, ModelStatus.NotDownloaded)
        if (preferences.selectedModelId.first() == modelId) preferences.setSelectedModelId(null)
        _storage.value = readStorage()
    }

    private fun setStatus(model: LlmModel, status: ModelStatus) {
        statuses.update { it + (model.id to status) }
    }
}
