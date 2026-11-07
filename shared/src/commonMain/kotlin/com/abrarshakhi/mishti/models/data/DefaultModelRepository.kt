package com.abrarshakhi.mishti.models.data

import com.abrarshakhi.mishti.core.storage.preferences.domain.AppPreferences
import com.abrarshakhi.mishti.core.utils.currentTimeMillis
import com.abrarshakhi.mishti.models.data.gguf.GgufReader
import com.abrarshakhi.mishti.models.data.local.InstalledModelDao
import com.abrarshakhi.mishti.models.data.local.InstalledModelEntity
import com.abrarshakhi.mishti.models.data.mapper.toDomain
import com.abrarshakhi.mishti.models.domain.model.CatalogModel
import com.abrarshakhi.mishti.models.domain.model.ModelOrigin
import com.abrarshakhi.mishti.models.domain.model.ShelfModel
import com.abrarshakhi.mishti.models.domain.model.Transfer
import com.abrarshakhi.mishti.models.domain.model.TransferError
import com.abrarshakhi.mishti.models.domain.model.TransferStatus
import com.abrarshakhi.mishti.models.domain.model.toOrigin
import com.abrarshakhi.mishti.models.domain.repository.ModelRepository
import com.abrarshakhi.mishti.models.domain.repository.StorageUsage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okio.IOException
import okio.buffer
import okio.use
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class DefaultModelRepository(
    private val dao: InstalledModelDao,
    private val storageManager: ModelStorage,
    private val downloader: ModelDownloader,
    private val importer: ModelImporter,
    private val preferences: AppPreferences,
    private val scope: CoroutineScope,
    private val knownModels: suspend () -> List<CatalogModel>,
    private val notifier: DownloadNotifier = DownloadNotifier.Noop,
    private val clock: () -> Long = ::currentTimeMillis,
    private val defaultImportName: String = "model.gguf",
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ModelRepository {
    override val selectedModelId: Flow<String?> = preferences.selectedModelId
    override val shelf: Flow<List<ShelfModel>> = dao.observeAll()
        .map { rows -> rows.filter { storageManager.isPresent(it.id) }.map { it.toDomain() } }
        .flowOn(ioDispatcher)

    private val mutableTransfers = MutableStateFlow<List<Transfer>>(emptyList())
    override val transfers: Flow<List<Transfer>> = mutableTransfers.asStateFlow()
    private val mutableStorage = MutableStateFlow(StorageUsage())
    override val storage: Flow<StorageUsage> = mutableStorage.asStateFlow()
    private val jobs = MutableStateFlow<Map<String, Job>>(emptyMap())
    private val modelLocks = MutableStateFlow<Map<String, Mutex>>(emptyMap())
    private val initialization = scope.async(ioDispatcher) {
        reconcileWithDisk()
        refreshStorage()
    }

    override suspend fun select(modelId: String?) = withContext(ioDispatcher) {
        initialization.await()
        if (modelId == null) {
            preferences.setSelectedModelId(null)
        } else {
            lockFor(modelId).withLock {
                check(dao.byId(modelId) != null && storageManager.isPresent(modelId)) { "Model is unavailable" }
                preferences.setSelectedModelId(modelId)
            }
        }
    }

    override fun download(model: CatalogModel) {
        startTransfer(model.id, model.name, TransferStatus.Downloading(0, model.sizeBytes)) {
            downloader.download(
                DownloadRequest(model.id, model.name, model.downloadUrl, model.sizeBytes, model.sha256),
            ) { progress ->
                setTransfer(
                    model.id,
                    model.name,
                    when (progress) {
                        is DownloadProgress.Downloading -> TransferStatus.Downloading(
                            progress.downloadedBytes,
                            progress.totalBytes,
                        )

                        DownloadProgress.Verifying -> TransferStatus.Verifying
                    },
                )
            }
            // Once the file has been committed, finish the matching database write even if cancelled.
            withContext(NonCancellable) {
                dao.upsert(model.toEntity(storageManager.sizeOnDisk(model.id), clock()))
            }
        }
    }

    override fun import(uri: String) {
        val id = "imported-${Uuid.random()}"
        startTransfer(id, defaultImportName, TransferStatus.Importing(0, -1)) {
            val name = importer.displayName(uri)
            val file = importer.import(id, uri) { done, total ->
                setTransfer(id, name, TransferStatus.Importing(done, total))
            }
            withContext(NonCancellable) {
                dao.upsert(
                    InstalledModelEntity(
                        id = id,
                        name = file.info.name?.takeIf { it.isNotBlank() } ?: file.displayName.removeSuffix(".gguf"),
                        origin = ModelOrigin.Imported.name,
                        quantization = file.info.quantization,
                        parametersLabel = file.info.sizeLabel,
                        sizeBytes = file.sizeBytes,
                        hfRepo = null,
                        hfFile = file.displayName,
                        architecture = file.info.architecture,
                        contextLength = file.info.contextLength,
                        license = file.info.license,
                        installedAtMillis = clock(),
                    ),
                )
            }
        }
    }

    private fun startTransfer(id: String, name: String, initial: TransferStatus, transfer: suspend () -> Unit) {
        val job = scope.launch(ioDispatcher, start = CoroutineStart.LAZY) {
            setTransfer(id, name, initial)
            try {
                initialization.await()
                // A denied foreground-service start must not prevent a foreground transfer.
                runCatching { notifier.onDownloadsActive() }
                lockFor(id).withLock { transfer() }
                removeTransfer(id)
            } catch (error: CancellationException) {
                removeTransfer(id)
                throw error
            } catch (error: Exception) {
                val problem = when (error) {
                    is DownloadFailure -> error.error
                    is IOException -> TransferError.Network
                    else -> TransferError.Unknown
                }
                setTransfer(id, name, TransferStatus.Failed(problem))
            } finally {
                refreshStorage()
            }
        }
        while (true) {
            val current = jobs.value
            if (id in current) {
                job.cancel()
                return
            }
            if (jobs.compareAndSet(current, current + (id to job))) break
        }
        job.invokeOnCompletion { cause ->
            if (cause is CancellationException) removeTransfer(id)
            jobs.update { current -> if (current[id] === job) current - id else current }
        }
        job.start()
    }

    override fun cancel(transferId: String) {
        jobs.value[transferId]?.cancel() ?: removeTransfer(transferId)
    }
    override fun cancelAll() {
        jobs.value.values.forEach { it.cancel() }
    }
    override fun dismissTransfer(transferId: String) {
        if (transferId !in jobs.value) removeTransfer(transferId)
    }
    override suspend fun delete(modelId: String) = withContext(ioDispatcher) {
        initialization.await()
        jobs.value[modelId]?.cancelAndJoin()
        lockFor(modelId).withLock {
            storageManager.delete(modelId)
            dao.delete(modelId)
            if (preferences.selectedModelId.first() == modelId) preferences.setSelectedModelId(null)
            refreshStorage()
        }
    }

    private fun lockFor(id: String): Mutex {
        while (true) {
            val current = modelLocks.value
            current[id]?.let { return it }
            val lock = Mutex()
            if (modelLocks.compareAndSet(current, current + (id to lock))) return lock
        }
    }

    private suspend fun reconcileWithDisk() {
        val rows = dao.all().associateBy { it.id }
        rows.keys.filterNot { storageManager.isPresent(it) }.forEach { dao.delete(it) }
        val known = knownModels().associateBy { it.id }
        storageManager.presentIds().filterNot { it in rows }.forEach { id ->
            dao.upsert(known[id]?.toEntity(storageManager.sizeOnDisk(id), clock()) ?: adoptUnknown(id))
        }
        val selected = preferences.selectedModelId.first()
        if (selected != null && dao.byId(selected) == null) preferences.setSelectedModelId(null)
    }

    private fun adoptUnknown(id: String): InstalledModelEntity {
        val file = storageManager.modelFile(id)
        val info = runCatching {
            storageManager.fileSystem.source(file).buffer().use { GgufReader.read(it) }
        }.getOrNull()
        return InstalledModelEntity(
            id = id, name = info?.name ?: id, origin = ModelOrigin.Imported.name,
            quantization = info?.quantization, parametersLabel = info?.sizeLabel,
            sizeBytes = storageManager.sizeOnDisk(id), hfRepo = null, hfFile = file.name,
            architecture = info?.architecture, contextLength = info?.contextLength, license = info?.license,
            installedAtMillis = storageManager.fileSystem.metadata(file).lastModifiedAtMillis ?: clock(),
        )
    }

    private fun refreshStorage() {
        mutableStorage.value = StorageUsage(storageManager.usedBytes(), storageManager.availableBytes())
    }
    private fun setTransfer(id: String, name: String, status: TransferStatus) {
        mutableTransfers.update { current -> current.filterNot { it.id == id } + Transfer(id, name, status) }
    }
    private fun removeTransfer(id: String) {
        mutableTransfers.update { current -> current.filterNot { it.id == id } }
    }
}

private fun CatalogModel.toEntity(sizeOnDisk: Long, now: Long) = InstalledModelEntity(
    id = id, name = name, origin = source.toOrigin().name,
    quantization = quantization.ifBlank { null }, parametersLabel = parametersLabel,
    sizeBytes = sizeOnDisk, hfRepo = hfRepo, hfFile = hfFile, architecture = null,
    contextLength = contextLength, license = license, installedAtMillis = now,
)
