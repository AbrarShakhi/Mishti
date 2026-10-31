package com.abrarshakhi.mishti.features.models.data

import com.abrarshakhi.mishti.common.data.preferences.AppPreferences
import com.abrarshakhi.mishti.features.models.data.gguf.GgufReader
import com.abrarshakhi.mishti.features.models.data.local.InstalledModelDao
import com.abrarshakhi.mishti.features.models.data.local.InstalledModelEntity
import com.abrarshakhi.mishti.features.models.data.local.toDomain
import com.abrarshakhi.mishti.features.models.domain.model.CatalogModel
import com.abrarshakhi.mishti.features.models.domain.model.ModelOrigin
import com.abrarshakhi.mishti.features.models.domain.model.ShelfModel
import com.abrarshakhi.mishti.features.models.domain.model.Transfer
import com.abrarshakhi.mishti.features.models.domain.model.TransferError
import com.abrarshakhi.mishti.features.models.domain.model.TransferStatus
import com.abrarshakhi.mishti.features.models.domain.model.toOrigin
import com.abrarshakhi.mishti.features.models.domain.repository.ModelRepository
import com.abrarshakhi.mishti.features.models.domain.repository.StorageUsage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class DefaultModelRepository(
    private val dao: InstalledModelDao,
    private val storageManager: ModelStorage,
    private val downloader: ModelDownloader,
    private val importer: ModelImporter,
    private val preferences: AppPreferences,
    private val scope: CoroutineScope,
    private val knownModels: () -> List<CatalogModel>,
    private val notifier: DownloadNotifier = DownloadNotifier.Noop,
    private val clock: () -> Long = System::currentTimeMillis,
    private val defaultImportName: String = "model.gguf",
) : ModelRepository {
    override val selectedModelId: Flow<String?> = preferences.selectedModelId

    override val shelf: Flow<List<ShelfModel>> =
        dao
            .observeAll()
            .map { rows -> rows.filter { storageManager.isPresent(it.id) }.map { it.toDomain() } }
            .flowOn(Dispatchers.IO)

    private val _transfers = MutableStateFlow<List<Transfer>>(emptyList())
    override val transfers: Flow<List<Transfer>> = _transfers.asStateFlow()

    private val _storage = MutableStateFlow(StorageUsage())
    override val storage: Flow<StorageUsage> = _storage.asStateFlow()

    private val jobs = ConcurrentHashMap<String, Job>()

    init {
        scope.launch {
            reconcileWithDisk()
            refreshStorage()
        }
    }

    override suspend fun select(modelId: String?) {
        val valid =
            modelId == null ||
                (dao.byId(modelId) != null && storageManager.isPresent(modelId))
        if (valid) preferences.setSelectedModelId(modelId)
    }

    override fun download(model: CatalogModel) {
        val id = model.id
        if (jobs[id]?.isActive == true) return

        setTransfer(
            id,
            model.name,
            TransferStatus.Downloading(storageManager.partialBytes(id), model.sizeBytes),
        )
        notifier.onDownloadsActive()

        jobs[id] =
            scope.launch {
                try {
                    downloader.download(
                        DownloadRequest(
                            id,
                            model.name,
                            model.downloadUrl,
                            model.sizeBytes,
                            model.sha256,
                        ),
                    ) { progress ->
                        setTransfer(
                            id,
                            model.name,
                            when (progress) {
                                is DownloadProgress.Downloading -> {
                                    TransferStatus.Downloading(
                                        progress.downloadedBytes,
                                        progress.totalBytes,
                                    )
                                }

                                DownloadProgress.Verifying -> {
                                    TransferStatus.Verifying
                                }
                            },
                        )
                    }
                    dao.upsert(model.toEntity(storageManager.sizeOnDisk(id), clock()))
                    removeTransfer(id)
                } catch (e: CancellationException) {
                    removeTransfer(id)
                    throw e
                } catch (e: Exception) {
                    setTransfer(id, model.name, TransferStatus.Failed(e.toTransferError()))
                } finally {
                    jobs.remove(id)
                    refreshStorage()
                }
            }
    }

    override fun import(uri: String) {
        val id = "imported-" + UUID.randomUUID().toString().take(8)
        var name = defaultImportName
        setTransfer(id, name, TransferStatus.Importing(0L, -1L))
        notifier.onDownloadsActive()

        jobs[id] =
            scope.launch {
                try {
                    name = runCatching { importer.displayName(uri) }.getOrDefault(name)
                    val file =
                        importer.import(id, uri) { done, total ->
                            setTransfer(id, name, TransferStatus.Importing(done, total))
                        }
                    val info = file.info
                    dao.upsert(
                        InstalledModelEntity(
                            id = id,
                            name =
                            info.name?.takeIf { it.isNotBlank() }
                                ?: file.displayName.removeSuffix(".gguf"),
                            origin = ModelOrigin.Imported.name,
                            quantization = info.quantization,
                            parametersLabel = info.sizeLabel,
                            sizeBytes = file.sizeBytes,
                            hfRepo = null,
                            hfFile = file.displayName,
                            architecture = info.architecture,
                            contextLength = info.contextLength,
                            license = info.license,
                            installedAtMillis = clock(),
                        ),
                    )
                    removeTransfer(id)
                } catch (e: CancellationException) {
                    removeTransfer(id)
                    throw e
                } catch (e: Exception) {
                    setTransfer(id, name, TransferStatus.Failed(e.toTransferError()))
                } finally {
                    jobs.remove(id)
                    refreshStorage()
                }
            }
    }

    override fun cancel(transferId: String) {
        jobs[transferId]?.cancel() ?: removeTransfer(transferId)
    }

    override fun cancelAll() {
        jobs.keys.toList().forEach { cancel(it) }
    }

    override fun dismissTransfer(transferId: String) {
        if (jobs[transferId]?.isActive != true) removeTransfer(transferId)
    }

    override suspend fun delete(modelId: String) {
        cancel(modelId)
        storageManager.delete(modelId)
        dao.delete(modelId)
        if (preferences.selectedModelId.first() == modelId) preferences.setSelectedModelId(null)
        refreshStorage()
    }

    private suspend fun reconcileWithDisk() {
        val rows = dao.all().associateBy { it.id }
        rows.keys.filterNot { storageManager.isPresent(it) }.forEach { dao.delete(it) }

        val known = knownModels().associateBy { it.id }
        storageManager.presentIds().filterNot { it in rows }.forEach { id ->
            val entity =
                known[id]?.toEntity(storageManager.sizeOnDisk(id), clock())
                    ?: adoptUnknown(id)
            dao.upsert(entity)
        }
    }

    private fun adoptUnknown(id: String): InstalledModelEntity {
        val file = storageManager.modelFile(id)
        val info =
            runCatching { file.inputStream().buffered().use { GgufReader.read(it) } }.getOrNull()
        return InstalledModelEntity(
            id = id,
            name = info?.name ?: id,
            origin = ModelOrigin.Imported.name,
            quantization = info?.quantization,
            parametersLabel = info?.sizeLabel,
            sizeBytes = file.length(),
            hfRepo = null,
            hfFile = file.name,
            architecture = info?.architecture,
            contextLength = info?.contextLength,
            license = info?.license,
            installedAtMillis = file.lastModified(),
        )
    }

    private fun refreshStorage() {
        _storage.value =
            StorageUsage(
                usedBytes = storageManager.usedBytes(),
                availableBytes = storageManager.availableBytes(),
            )
    }

    private fun setTransfer(id: String, name: String, status: TransferStatus) {
        _transfers.update { current ->
            val transfer = Transfer(id, name, status)
            val index = current.indexOfFirst { it.id == id }
            if (index == -1) {
                current + transfer
            } else {
                current.toMutableList().apply { this[index] = transfer }
            }
        }
    }

    private fun removeTransfer(id: String) {
        _transfers.update { current ->
            current.filterNot { it.id == id }
        }
    }
}

private fun Exception.toTransferError(): TransferError = when (this) {
    is DownloadFailure -> error
    is IOException -> TransferError.Network
    else -> TransferError.Unknown
}

private fun CatalogModel.toEntity(sizeOnDisk: Long, now: Long) = InstalledModelEntity(
    id = id,
    name = name,
    origin = source.toOrigin().name,
    quantization = quantization.ifBlank { null },
    parametersLabel = parametersLabel,
    sizeBytes = sizeOnDisk.takeIf { it > 0 } ?: sizeBytes,
    hfRepo = hfRepo,
    hfFile = hfFile,
    architecture = null,
    contextLength = contextLength,
    license = license,
    installedAtMillis = now,
)
