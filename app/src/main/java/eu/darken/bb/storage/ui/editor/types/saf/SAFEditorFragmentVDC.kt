package eu.darken.bb.storage.ui.editor.types.saf

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.App
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.Stater
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.common.file.APath
import eu.darken.bb.common.file.SAFGateway
import eu.darken.bb.common.file.SAFPath
import eu.darken.bb.common.getRootCause
import eu.darken.bb.common.rx.withScopeVDC
import eu.darken.bb.common.ui.BaseEditorFragment
import eu.darken.bb.common.vdc.SmartVDC
import eu.darken.bb.common.vdc.VDCFactory
import eu.darken.bb.storage.core.Storage
import eu.darken.bb.storage.core.StorageBuilder
import eu.darken.bb.storage.core.saf.SAFStorageEditor
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class SAFEditorFragmentVDC @AssistedInject constructor(
        @Assisted private val handle: SavedStateHandle,
        @Assisted private val storageId: Storage.Id,
        @AppContext private val context: Context,
        private val builder: StorageBuilder,
        private val safGateway: SAFGateway
) : SmartVDC(), BaseEditorFragment.VDC {

    private val stater = Stater(State())
    override val state = stater.liveData

    private val editorObs = builder.storage(storageId)
            .subscribeOn(Schedulers.io())
            .filter { it.editor != null }
            .map { it.editor as SAFStorageEditor }

    private val editorDataObs = editorObs.switchMap { it.editorData }

    private val editor: SAFStorageEditor by lazy { editorObs.blockingFirst() }
    val openPickerEvent = SingleLiveEvent<Intent>()
    val errorEvent = SingleLiveEvent<Throwable>()

    init {
        editorDataObs.take(1)
                .subscribe { editor ->
                    stater.update { it.copy(path = editor.refPath?.path ?: "") }
                }
                .withScopeVDC(this)

        editorDataObs
                .subscribe { data ->
                    stater.update { state ->
                        state.copy(
                                label = data.label,
                                path = data.refPath?.path ?: "",
                                isWorking = false,
                                isExisting = data.existingStorage
                        )
                    }
                }
                .withScopeVDC(this)
    }

    fun updateName(label: String) {
        Timber.tag(TAG).v("Updating label: %s", label)
        editor.updateLabel(label)
    }

    fun selectPath() {
        openPickerEvent.postValue(safGateway.createPickerIntent())
    }

    override fun onNavigateBack(): Boolean = if (editorDataObs.map { it.existingStorage }.blockingFirst()) {
        builder.remove(storageId)
                .doOnSubscribe { stater.update { it.copy(isWorking = true) } }
                .subscribeOn(Schedulers.io())
                .subscribe()
        true
    } else {
        builder
                .update(storageId) { data ->
                    data!!.copy(storageType = null, editor = null)
                }
                .subscribeOn(Schedulers.io())
                .subscribe()
        true
    }

    fun onPermissionResult(uri: Uri) {
        editor.updatePath(uri, false)
                .subscribeOn(Schedulers.io())
                .subscribe { path, error ->
                    if (error != null) {
                        errorEvent.postValue(error)
                    }
                }
    }

    fun importStorage(path: APath) {
        path as SAFPath
        editor.updatePath(path, true)
                .subscribeOn(Schedulers.io())
                .subscribe { path, error ->
                    if (error != null) {
                        errorEvent.postValue(error.getRootCause())
                    }
                }
    }

    data class State(
            val label: String = "",
            val path: String = "",
            val validPath: Boolean = false,
            val isWorking: Boolean = false,
            override val isExisting: Boolean = false
    ) : BaseEditorFragment.VDC.State

    @AssistedInject.Factory
    interface Factory : VDCFactory<SAFEditorFragmentVDC> {
        fun create(handle: SavedStateHandle, storageId: Storage.Id): SAFEditorFragmentVDC
    }

    companion object {
        val TAG = App.logTag("Storage", "SAF", "Editor", "VDC")
    }
}