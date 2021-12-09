package eu.darken.bb.storage.ui.viewer.viewer

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.backup.core.BackupSpec
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.coroutine.DispatcherProvider
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.flow.DynamicStateFlow
import eu.darken.bb.common.flow.onError
import eu.darken.bb.common.navigation.navArgs
import eu.darken.bb.common.smart.Smart2VDC
import eu.darken.bb.processor.core.ProcessorControl
import eu.darken.bb.storage.core.Storage
import eu.darken.bb.storage.core.StorageManager
import eu.darken.bb.storage.ui.viewer.StorageViewerOptions
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.job
import javax.inject.Inject

@HiltViewModel
class StorageViewerFragmentVDC @Inject constructor(
    handle: SavedStateHandle,
    processorControl: ProcessorControl,
    storageManager: StorageManager,
    private val dispatcherProvider: DispatcherProvider,
) : Smart2VDC(dispatcherProvider) {

    private val navArgs by handle.navArgs<StorageViewerFragmentArgs>()
    private val viewerOptions: StorageViewerOptions = navArgs.viewerOptions

    private val storageFlow = flow { emit(storageManager.getStorage(viewerOptions.storageId)) }

    private val stater = DynamicStateFlow(TAG, vdcScope) { State() }
    val state = stater.asLiveData2()

    private val deletionStater = DynamicStateFlow(TAG, vdcScope) { DeletionState() }
    val deletionState = deletionStater.asLiveData2()

    val contentActionEvent = SingleLiveEvent<ContentActionEvent>()
    val processorEvent = SingleLiveEvent<Boolean>()

    init {
        processorControl.progressHost
            .onEach { processorEvent.postValue(it != null) }
            .launchInViewModel()

        storageFlow
            .flatMapConcat { it.info() }
            .filter { it.status != null }
            .map { it.status!! }
            .onEach { status ->
                stater.updateBlocking { copy(allowDeleteAll = !status.isReadOnly) }
            }
            .launchInViewModel()

        storageFlow
            .flatMapConcat { it.info() }
            .filter { it.config != null }
            .map { it.config!! }
            .take(1)
            .onEach { config ->
                stater.updateBlocking { copy(storageLabel = config.label, storageType = config.storageType) }
            }
            .launchInViewModel()

        // TODO use storage extension?
        storageFlow
            .flatMapConcat { it.specInfos() }
            .map { specInfos ->
                specInfos.filter {
                    viewerOptions.backupTypeFilter?.contains(it.backupSpec.backupType) ?: true
                }
            }
            .onEach { storageContents ->
                stater.updateBlocking {
                    copy(
                        specInfos = storageContents.toList(),
                        isLoading = false
                    )
                }
            }
            .onError { navEvents.postValue(null) }
            .launchInViewModel()
    }

    fun viewContent(info: BackupSpec.Info) {
        contentActionEvent.postValue(
            ContentActionEvent(
                storageId = info.storageId,
                backupSpecId = info.backupSpec.specId,
                allowView = true,
                allowDelete = true
            )
        )
    }

    fun deleteAll() = launch {
        stater.updateBlocking { copy(job = coroutineContext.job) }

        val storage = storageFlow.first()

        val specInfos = storage.specInfos().first()

        specInfos.forEach {
            deletionStater.updateBlocking { copy(backupSpec = it.backupSpec) }
            delay(300)
            storage.remove(it.backupSpec.specId)
        }

        stater.updateBlocking { copy(job = null) }
    }

    override fun onCleared() = launch {
        stater.value().job?.cancel()
        super.onCleared()
    }

    data class DeletionState(
        val backupSpec: BackupSpec? = null
    )

    data class State(
        val storageLabel: String? = null,
        val storageType: Storage.Type? = null,
        val specInfos: List<BackupSpec.Info> = emptyList(),
        val allowDeleteAll: Boolean = false,
        val isLoading: Boolean = true,
        val job: Job? = null
    ) {
        val isWorking: Boolean
            get() = isLoading || job != null
    }

    data class ContentActionEvent(
        val storageId: Storage.Id,
        val backupSpecId: BackupSpec.Id,
        val allowView: Boolean = false,
        val allowDelete: Boolean = false
    )

    companion object {
        private val TAG = logTag("Storage", "Item", "VDC")
    }
}