package eu.darken.bb.storage.ui.viewer.content

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import com.jakewharton.rx.replayingShare
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.backup.core.BackupSpec
import eu.darken.bb.common.*
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.common.rx.onErrorComplete
import eu.darken.bb.common.vdc.SmartVDC
import eu.darken.bb.common.vdc.VDCFactory
import eu.darken.bb.storage.core.Storage
import eu.darken.bb.storage.core.StorageManager
import io.reactivex.Single
import io.reactivex.disposables.Disposables
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class StorageContentFragmentVDC @AssistedInject constructor(
        @Assisted private val handle: SavedStateHandle,
        @Assisted private val storageId: Storage.Id,
        @AppContext private val context: Context,
        private val storageManager: StorageManager
) : SmartVDC() {

    private val storageObs = storageManager.getStorage(storageId)
            .subscribeOn(Schedulers.io()).replayingShare()

    private val stater: Stater<State> = Stater(State())
    val state = stater.liveData

    private val deletionStater = Stater(DeletionState())
    val deletionState = deletionStater.liveData

    val finishEvent = SingleLiveEvent<Boolean>()
    val contentActionEvent = SingleLiveEvent<ContentActionEvent>()

    init {
        storageObs.flatMap { it.info() }
                .filter { it.status != null }.map { it.status!! }
                .doOnNext { status ->
                    stater.update { it.copy(allowDeleteAll = !status.isReadOnly) }
                }
                .onErrorComplete()
                .withStater(stater)

        storageObs.flatMap { it.content() }
                .doOnNext { storageContents ->
                    stater.update {
                        it.copy(
                                contents = storageContents.toList(),
                                workIds = it.clearWorkId()
                        )
                    }
                }
                .doOnError { error ->
                    stater.update {
                        it.copy(
                                error = error
                        )
                    }
                    finishEvent.postValue(true)
                }
                .onErrorComplete()
                .withStater(stater)
    }

    fun viewContent(item: Storage.Content) {
        contentActionEvent.postValue(ContentActionEvent(
                storageId = item.storageId,
                backupSpecId = item.backupSpec.specId,
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
                    storage.content()
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
            val contents: List<Storage.Content> = emptyList(),
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
    interface Factory : VDCFactory<StorageContentFragmentVDC> {
        fun create(handle: SavedStateHandle, storageId: Storage.Id): StorageContentFragmentVDC
    }
}