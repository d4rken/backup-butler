package eu.darken.bb.storage.ui.editor.types.local

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.App
import eu.darken.bb.common.HotData
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.common.rx.toLiveData
import eu.darken.bb.common.vdc.SmartVDC
import eu.darken.bb.common.vdc.VDCFactory
import eu.darken.bb.storage.core.Storage
import eu.darken.bb.storage.core.StorageBuilder
import eu.darken.bb.storage.core.local.LocalStorageEditor
import io.reactivex.rxkotlin.Observables
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class LocalEditorFragmentVDC @AssistedInject constructor(
        @Assisted private val handle: SavedStateHandle,
        @Assisted private val storageId: Storage.Id,
        @AppContext private val context: Context,
        private val builder: StorageBuilder
) : SmartVDC() {

    private val stateUpdater = HotData(State())

    private val editorObs = builder.storage(storageId)
            .filter { it.editor != null }
            .map { it.editor as LocalStorageEditor }

    private val configObs = editorObs
            .flatMap { it.config }

    val state = Observables.combineLatest(stateUpdater.data, configObs)
            .subscribeOn(Schedulers.io())
            .map { (state, config) ->
                state.copy(
                        label = config.label,
                        validPath = editor.isRefPathValid(state.path),
                        allowCreate = editor.isRefPathValid(state.path) && editor.isValidConfig(),
                        working = false,
                        existing = editor.isExistingStorage()
                )
            }
            .toLiveData()

    private val editor: LocalStorageEditor by lazy {
        editorObs.blockingFirst()
    }

    val finishActivity = SingleLiveEvent<Boolean>()

    init {
        editorObs.firstOrError()
                .subscribeOn(Schedulers.io())
                .filter { editor.refPath != null }
                .subscribe { editor ->
                    stateUpdater.update { it.copy(path = editor.refPath!!.path) }
                }
    }

    fun createStorage() {
        editor.updateRefPath(stateUpdater.snapshot.path)
        stateUpdater.data
                .firstOrError()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .doOnSubscribe { stateUpdater.update { it.copy(working = true) } }
                .doFinally { finishActivity.postValue(true) }
                .flatMap { builder.save(storageId) }
                .subscribe()
    }

    fun updateName(label: String) {
        Timber.tag(TAG).v("Updating label: %s", label)
        editor.updateLabel(label)
    }

    fun updatePath(path: String) {
        Timber.tag(TAG).v("Updating path: %s", path)
        stateUpdater.update { it.copy(path = path) }
        editor.updateRefPath(path)
    }

    fun onGoBack(): Boolean {
        if (editor.isExistingStorage()) {
            builder.remove(storageId)
                    .doOnSubscribe { stateUpdater.update { it.copy(working = true) } }
                    .subscribeOn(Schedulers.io())
                    .subscribe { _ ->
                        finishActivity.postValue(true)
                    }
            return true
        } else {
            builder
                    .update(storageId) { data ->
                        data!!.copy(
                                storageType = null,
                                editor = null
                        )
                    }
                    .subscribeOn(Schedulers.io())
                    .subscribe()
            return true
        }
    }

    data class State(
            val label: String = "",
            val path: String = "",
            val validPath: Boolean = false,
            val working: Boolean = true,
            val allowCreate: Boolean = false,
            val existing: Boolean = false
    )

    @AssistedInject.Factory
    interface Factory : VDCFactory<LocalEditorFragmentVDC> {
        fun create(handle: SavedStateHandle, storageId: Storage.Id): LocalEditorFragmentVDC
    }

    companion object {
        val TAG = App.logTag("Storage", "Local", "Editor", "VDC")
    }
}