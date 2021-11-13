package eu.darken.bb.storage.ui.viewer.content

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.BackupSpec
import eu.darken.bb.common.coroutine.DispatcherProvider
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.flow.DynamicStateFlow
import eu.darken.bb.common.navigation.navArgs
import eu.darken.bb.common.smart.Smart2VDC
import eu.darken.bb.storage.core.Storage
import eu.darken.bb.storage.core.StorageManager
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class ItemContentsFragmentVDC @Inject constructor(
    handle: SavedStateHandle,
    private val storageManager: StorageManager,
    private val dispatcherProvider: DispatcherProvider,
) : Smart2VDC(dispatcherProvider) {

    private val navArgs by handle.navArgs<ItemContentsFragmentArgs>()
    private val storageId: Storage.Id = navArgs.storageId
    private val backupSpecId: BackupSpec.Id = navArgs.specId

    private val stater = DynamicStateFlow(TAG, vdcScope) { State() }
    val state = stater.asLiveData2()

    init {

        flow { emit(storageManager.getStorage(storageId)) }
            .flatMapConcat { it.specInfos() }
            .map { contents -> contents.find { it.backupSpec.specId == backupSpecId }!! }
            .onEach { item ->
                stater.updateBlocking {
                    copy(
                        backupSpec = item.backupSpec,
                        versions = item.backups.sortedBy { it.createdAt }.reversed(),
                        loading = false
                    )
                }
            }
            .onCompletion {
                navEvents.postValue(null)
            }
            .launchInViewModel()
    }

    data class State(
        val backupSpec: BackupSpec? = null,
        val versions: List<Backup.MetaData>? = null,
        val loading: Boolean = true,
        val error: Throwable? = null
    )

    companion object {
        private val TAG = logTag("Storage", "Item", "Contents", "VDC")
    }
}