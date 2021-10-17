package eu.darken.bb.storage.ui.editor.types.local

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.Stater
import eu.darken.bb.common.debug.logging.log
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.errors.getRootCause
import eu.darken.bb.common.files.core.APath
import eu.darken.bb.common.files.core.local.LocalGateway
import eu.darken.bb.common.files.core.local.LocalPath
import eu.darken.bb.common.files.ui.picker.APathPicker
import eu.darken.bb.common.files.ui.picker.local.LocalPickerFragmentVDC
import eu.darken.bb.common.navigation.navArgs
import eu.darken.bb.common.permission.Permission
import eu.darken.bb.common.rx.latest
import eu.darken.bb.common.rx.withScopeVDC
import eu.darken.bb.common.vdc.SmartVDC
import eu.darken.bb.storage.core.Storage
import eu.darken.bb.storage.core.StorageBuilder
import eu.darken.bb.storage.core.local.LocalStorageEditor
import eu.darken.bb.storage.ui.editor.StorageEditorResult
import io.reactivex.rxjava3.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class LocalEditorFragmentVDC @Inject constructor(
    handle: SavedStateHandle,
    private val builder: StorageBuilder
) : SmartVDC() {
    private val navArgs by handle.navArgs<LocalEditorFragmentArgs>()
    private val storageId: Storage.Id = navArgs.storageId
    private val stater = Stater { State() }
    val state = stater.liveData

    private val editorObs = builder.storage(storageId)
        .observeOn(Schedulers.computation())
        .filter { it.editor != null }
        .map { it.editor as LocalStorageEditor }

    private val editorDataObs = editorObs.switchMap { it.editorData }

    private val editor: LocalStorageEditor by lazy { editorObs.blockingFirst() }

    val requestPermissionEvent = SingleLiveEvent<Permission>()

    val errorEvent = SingleLiveEvent<Throwable>()
    val pickerEvent = SingleLiveEvent<APathPicker.Options>()
    val finishEvent = SingleLiveEvent<StorageEditorResult>()

    init {
        editorDataObs
            .subscribe { editorData ->
                stater.update { state ->
                    state.copy(
                        label = editorData.label,
                        path = editorData.refPath?.path ?: "",
                        isWorking = false,
                        isExisting = editorData.existingStorage,
                        missingPermissions = editor.getMissingPermissions()
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
        log { "updatePath=$result" }
        if (result.isCanceled) return
        if (result.isFailed) {
            errorEvent.postValue(result.error!!)
            return
        }

        val path: LocalPath = result.selection!!.first() as LocalPath
        log { "Updating path: $path " }
        editor.updatePath(path, false)
            .observeOn(Schedulers.computation())
            .subscribe { _, error: Throwable? ->
                if (error != null) errorEvent.postValue(error.getRootCause())
            }
    }

    fun importStorage(path: APath) {
        log { "importStorage=$path" }
        path as LocalPath
        editor.updatePath(path, true)
            .observeOn(Schedulers.computation())
            .subscribe { _, error: Throwable? ->
                if (error != null) errorEvent.postValue(error.getRootCause())
            }
    }

    fun selectPath() {
        editorDataObs.latest().subscribe { data ->
            pickerEvent.postValue(APathPicker.Options(
                startPath = data.refPath,
                allowedTypes = setOf(APath.PathType.LOCAL),
                payload = Bundle().apply {
                    putString(LocalPickerFragmentVDC.ARG_MODE, LocalGateway.Mode.NORMAL.name)
                }
            ))
        }
    }

    fun requestPermission(permission: Permission) {
        log { "onGrantPermission=$permission" }
        requestPermissionEvent.postValue(permission)
    }

    fun onUpdatePermission(permission: Permission) {
        log { "onUpdatePermission=$permission" }
        stater.update { it.copy(missingPermissions = editor.getMissingPermissions()) }
    }

    fun saveConfig() {
        builder.save(storageId)
            .observeOn(Schedulers.computation())
            .doOnSubscribe { stater.update { it.copy(isWorking = true) } }
            .subscribe { ref, error: Throwable? ->
                if (error != null) {
                    errorEvent.postValue(error.getRootCause())
                } else {
                    finishEvent.postValue(StorageEditorResult(storageId = ref.storageId))
                }
            }
    }

    data class State(
        val label: String = "",
        val path: String = "",
        val isWorking: Boolean = false,
        val missingPermissions: Set<Permission> = emptySet(),
        val isExisting: Boolean = false,
        val isValid: Boolean = false
    )

    companion object {
        val TAG = logTag("Storage", "Local", "Editor", "VDC")
    }
}