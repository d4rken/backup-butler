package eu.darken.bb.storage.ui.editor.types.local

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.App
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.Stater
import eu.darken.bb.common.files.core.APath
import eu.darken.bb.common.files.core.local.LocalGateway
import eu.darken.bb.common.files.core.local.LocalPath
import eu.darken.bb.common.files.ui.picker.APathPicker
import eu.darken.bb.common.files.ui.picker.local.LocalPickerFragmentVDC
import eu.darken.bb.common.getRootCause
import eu.darken.bb.common.rx.withScopeVDC
import eu.darken.bb.common.vdc.SmartVDC
import eu.darken.bb.common.vdc.VDCFactory
import eu.darken.bb.storage.core.Storage
import eu.darken.bb.storage.core.StorageBuilder
import eu.darken.bb.storage.core.local.LocalStorageEditor
import io.reactivex.rxjava3.schedulers.Schedulers
import timber.log.Timber

class LocalEditorFragmentVDC @AssistedInject constructor(
        @Assisted private val handle: SavedStateHandle,
        @Assisted private val storageId: Storage.Id,
        private val builder: StorageBuilder
) : SmartVDC() {

    private val stater = Stater(State(isPermissionGranted = true))
    val state = stater.liveData

    private val editorObs = builder.storage(storageId)
            .subscribeOn(Schedulers.io())
            .filter { it.editor != null }
            .map { it.editor as LocalStorageEditor }

    private val editorDataObs = editorObs.switchMap { it.editorData }

    private val editor: LocalStorageEditor by lazy { editorObs.blockingFirst() }

    val requestPermissionEvent = SingleLiveEvent<Any>()

    val errorEvent = SingleLiveEvent<Throwable>()
    val pickerEvent = SingleLiveEvent<APathPicker.Options>()
    val finishEvent = SingleLiveEvent<Any>()


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

        editorObs
                .switchMap { it.isValid() }
                .subscribe { isValid: Boolean -> stater.update { it.copy(isValid = isValid) } }
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

        val path: LocalPath = result.selection!!.first() as LocalPath
        Timber.tag(TAG).v("Updating path: %s", path)
        editor.updatePath(path, false)
                .subscribeOn(Schedulers.io())
                .subscribe { _, error ->
                    if (error != null) errorEvent.postValue(error.getRootCause())
                }
    }

    fun importStorage(path: APath) {
        path as LocalPath
        editor.updatePath(path, true)
                .subscribeOn(Schedulers.io())
                .subscribe { _, error ->
                    if (error != null) errorEvent.postValue(error.getRootCause())
                }
    }

    fun selectPath() {
        editorDataObs.firstOrError().subscribe { data ->
            pickerEvent.postValue(APathPicker.Options(
                    startPath = data.refPath,
                    allowedTypes = setOf(APath.PathType.LOCAL),
                    payload = Bundle().apply {
                        putString(LocalPickerFragmentVDC.ARG_MODE, LocalGateway.Mode.NORMAL.name)
                    }
            ))
        }
    }

    fun onGrantPermission() {
        requestPermissionEvent.postValue(Any())
    }

    fun onPermissionResult() {
        stater.update { it.copy(isPermissionGranted = editor.isPermissionGranted()) }
    }

    fun saveConfig() {
        builder.save(storageId)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .doOnSubscribe { stater.update { it.copy(isWorking = true) } }
                .doFinally { finishEvent.postValue(true) }
                .subscribe { _, error ->
                    if (error != null) errorEvent.postValue(error.getRootCause())
                }
    }

    data class State(
            val label: String = "",
            val path: String = "",
            val isWorking: Boolean = false,
            val isPermissionGranted: Boolean = true,
            val isExisting: Boolean = false,
            val isValid: Boolean = false
    )

    @AssistedInject.Factory
    interface Factory : VDCFactory<LocalEditorFragmentVDC> {
        fun create(handle: SavedStateHandle, storageId: Storage.Id): LocalEditorFragmentVDC
    }

    companion object {
        val TAG = App.logTag("Storage", "Local", "Editor", "VDC")
    }
}