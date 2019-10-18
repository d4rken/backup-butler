package eu.darken.bb.storage.ui.viewer.item

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import com.jakewharton.rx.replayingShare
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.backup.core.BackupSpec
import eu.darken.bb.common.*
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.common.rx.onErrorComplete
import eu.darken.bb.common.rx.withScopeVDC
import eu.darken.bb.common.vdc.SmartVDC
import eu.darken.bb.common.vdc.VDCFactory
import eu.darken.bb.storage.core.Storage
import eu.darken.bb.storage.core.StorageManager
import io.reactivex.Single
import io.reactivex.disposables.Disposables
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class StorageItemFragmentVDC @AssistedInject constructor(
        @Assisted private val handle: SavedStateHandle,
        @Assisted private val storageId: Storage.Id,
        @AppContext private val context: Context,
        private val storageManager: StorageManager
) : SmartVDC() {

    private val storageObs = storageManager.getStorage(storageId)
            .subscribeOn(Schedulers.io()).observeOn(Schedulers.io())
            .replayingShare()

    private val stater: Stater<State> = Stater(State())
    val state = stater.liveData

    private val deletionStater = Stater(DeletionState())
    val deletionState = deletionStater.liveData

    val finishEvent = SingleLiveEvent<Boolean>()
    val contentActionEvent = SingleLiveEvent<ContentActionEvent>()

    init {
        storageObs.flatMap { it.info() }
                .filter { it.status != null }.map { it.status!! }
                .onErrorComplete()
                .subscribe { status ->
                    stater.update { it.copy(allowDeleteAll = !status.isReadOnly) }
                }
                .withScopeVDC(this)

        storageObs.flatMap { it.specInfos() }
                .subscribe({ storageContents ->
                    stater.update {
                        it.copy(
                                specInfos = storageContents.toList(),
                                workIds = it.clearWorkId()
                        )
                    }
                }, { error ->
                    stater.update {
                        it.copy(
                                error = error
                        )
                    }
                    finishEvent.postValue(true)
                })
                .withScopeVDC(this)
        storageObs.flatMap { it.info() }
                .filter { it.config != null }
                .map { it.config!! }
                .take(1)
                .subscribe { config ->
                    stater.update { it.copy(storageLabel = config.label, storageType = config.storageType) }
                }
                .withScopeVDC(this)
    }

    fun viewContent(info: BackupSpec.Info) {
        contentActionEvent.postValue(ContentActionEvent(
                storageId = info.storageId,
                backupSpecId = info.backupSpec.specId,
                allowView = true,
                allowDelete = true
        ))
    }

    private var activeDeletion = Disposables.disposed()
    fun deleteAll() {
        activeDeletion.dispose()

        val workId = WorkId()
        activeDeletion = storageObs
                .switchMap { storage ->
                    storage.specInfos()
                            .take(1)
                            .flatMapIterable { it }
                            .concatMapSingle { content ->
                                deletionStater.update { it.copy(backupSpec = content.backupSpec) }
                                Single.timer(100, TimeUnit.MILLISECONDS).flatMap { storage.remove(content.backupSpec.specId) }
                            }
                }
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { stater.update { it.copy(workIds = it.addWorkId(workId)) } }
                .doFinally {
                    stater.update {
                        it.copy(workIds = it.clearWorkId(workId))
                    }
                }
                .subscribe()
    }

    override fun onCleared() {
        activeDeletion.dispose()
        super.onCleared()
    }

    data class DeletionState(
            val backupSpec: BackupSpec? = null
    )

    data class State(
            val storageLabel: String? = null,
            val storageType: Storage.Type? = null,
            val specInfos: List<BackupSpec.Info> = emptyList(),
            val error: Throwable? = null,
            val allowDeleteAll: Boolean = false,
            override val workIds: Set<WorkId> = setOf(WorkId.DEFAULT)
    ) : WorkId.State

    data class ContentActionEvent(
            val storageId: Storage.Id,
            val backupSpecId: BackupSpec.Id,
            val allowView: Boolean = false,
            val allowDelete: Boolean = false
    )

    @AssistedInject.Factory
    interface Factory : VDCFactory<StorageItemFragmentVDC> {
        fun create(handle: SavedStateHandle, storageId: Storage.Id): StorageItemFragmentVDC
    }
}