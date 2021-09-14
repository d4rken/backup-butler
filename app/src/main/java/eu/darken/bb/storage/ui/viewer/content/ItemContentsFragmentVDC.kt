package eu.darken.bb.storage.ui.viewer.content

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.BackupSpec
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.Stater
import eu.darken.bb.common.navigation.navArgs
import eu.darken.bb.common.rx.withScopeVDC
import eu.darken.bb.common.vdc.SmartVDC
import eu.darken.bb.storage.core.Storage
import eu.darken.bb.storage.core.StorageManager
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject

@HiltViewModel
class ItemContentsFragmentVDC @Inject constructor(
    handle: SavedStateHandle,
    private val storageManager: StorageManager
) : SmartVDC() {

    private val navArgs by handle.navArgs<ItemContentsFragmentArgs>()
    private val storageId: Storage.Id = navArgs.storageId
    private val backupSpecId: BackupSpec.Id = navArgs.specId

    private val storageObs = storageManager.getStorage(storageId).subscribeOn(Schedulers.io())

    private val stater: Stater<State> = Stater(State())
    val state = stater.liveData
    val errorEvent = SingleLiveEvent<Throwable>()
    val finishEvent = SingleLiveEvent<Any>()

    init {
        storageObs.flatMap { it.specInfos() }
            .map { contents -> contents.find { it.backupSpec.specId == backupSpecId }!! }
            .subscribe({ item ->
                stater.update { state ->
                    state.copy(
                        backupSpec = item.backupSpec,
                        versions = item.backups.sortedBy { it.createdAt }.reversed(),
                        loading = false
                    )
                }
            }, { error ->
                errorEvent.postValue(error)
                finishEvent.postValue(Any())
            })
            .withScopeVDC(this)
    }

    data class State(
        val backupSpec: BackupSpec? = null,
        val versions: List<Backup.MetaData>? = null,
        val loading: Boolean = true,
        val error: Throwable? = null
    )
}