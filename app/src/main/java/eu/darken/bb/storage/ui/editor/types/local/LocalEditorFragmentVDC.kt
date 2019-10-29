package eu.darken.bb.storage.ui.editor.types.local

import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.App
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.Stater
import eu.darken.bb.common.file.APath
import eu.darken.bb.common.file.JavaPath
import eu.darken.bb.common.file.picker.APathPicker
import eu.darken.bb.common.getRootCause
import eu.darken.bb.common.rx.withScopeVDC
import eu.darken.bb.common.ui.BaseEditorFragment
import eu.darken.bb.common.vdc.SmartVDC
import eu.darken.bb.common.vdc.VDCFactory
import eu.darken.bb.storage.core.Storage
import eu.darken.bb.storage.core.StorageBuilder
import eu.darken.bb.storage.core.local.LocalStorageEditor
import io.reactivex.schedulers.Schedulers
import kotlinx.android.parcel.Parcelize
import timber.log.Timber

class LocalEditorFragmentVDC @AssistedInject constructor(
        @Assisted private val handle: SavedStateHandle,
        @Assisted private val storageId: Storage.Id,
        private val builder: StorageBuilder
) : SmartVDC(), BaseEditorFragment.VDC {

    private val stater = Stater(State(isPermissionGranted = true))
    override val state = stater.liveData

    private val editorObs = builder.storage(storageId)
            .subscribeOn(Schedulers.io())
            .filter { it.editor != null }
            .map { it.editor as LocalStorageEditor }

    private val editorDataObs = editorObs.switchMap { it.editorData }

    private val editor: LocalStorageEditor by lazy { editorObs.blockingFirst() }
    val requestPermissionEvent = SingleLiveEvent<Any>()
    val errorEvent = SingleLiveEvent<Throwable>()
    val pickerEvent = SingleLiveEvent<APathPicker.Options>()

    init {
        editorDataObs
                .subscribe { editorData ->
                    stater.update { state ->
                        state.copy(
                                label = editorData.label,
                                path = editorData.refPath?.path ?: "",
                                isWorking = false,
                                isExisting = editorData.existingStorage,
                                isPermissionGranted = editor.isPermissionGranted()
                        )
                    }
                }
                .withScopeVDC(this)
    }

    fun updateName(label: String) {
        Timber.tag(TAG).v("Updating label: %s", label)
        editor.updateLabel(label)
    }

    fun updatePath(result: APathPicker.Result) {
        if (result.isCanceled) return
        if (result.isFailed) {
            errorEvent.postValue(result.error)
            return
        }

        val path: JavaPath = result.selection!!.first() as JavaPath
        Timber.tag(TAG).v("Updating path: %s", path)
        editor.updatePath(path, false)
                .subscribeOn(Schedulers.io())
                .subscribe { path, error ->
                    if (error != null) {
                        errorEvent.postValue(error)
                    }
                }
    }

    fun importStorage(path: APath) {
        path as JavaPath
        editor.updatePath(path, true)
                .subscribeOn(Schedulers.io())
                .subscribe { path, error ->
                    if (error != null) {
                        errorEvent.postValue(error.getRootCause())
                    }
                }
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

    fun selectPath() {
        pickerEvent.postValue(APathPicker.Options(
        ))
    }

    fun selectRoot() {
        // TODO
    }

    fun onGrantPermission() {
        requestPermissionEvent.postValue(Any())
    }

    fun onPermissionResult() {
        stater.update { it.copy(isPermissionGranted = editor.isPermissionGranted()) }
    }

    data class State(
            val label: String = "",
            val path: String = "",
            val isWorking: Boolean = false,
            val isPermissionGranted: Boolean = true,
            override val isExisting: Boolean = false
    ) : BaseEditorFragment.VDC.State

    @AssistedInject.Factory
    interface Factory : VDCFactory<LocalEditorFragmentVDC> {
        fun create(handle: SavedStateHandle, storageId: Storage.Id): LocalEditorFragmentVDC
    }

    @Parcelize
    data class PermissionRequest(
            val followUpWithDefaults: Boolean = true
    ) : Parcelable

    companion object {
        val TAG = App.logTag("Storage", "Local", "Editor", "VDC")
    }
}