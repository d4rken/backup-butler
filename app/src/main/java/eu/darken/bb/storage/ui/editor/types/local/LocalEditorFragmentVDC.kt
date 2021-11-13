package eu.darken.bb.storage.ui.editor.types.local

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.coroutine.DispatcherProvider
import eu.darken.bb.common.debug.logging.log
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.files.core.APath
import eu.darken.bb.common.files.core.local.LocalGateway
import eu.darken.bb.common.files.core.local.LocalPath
import eu.darken.bb.common.files.ui.picker.PathPickerOptions
import eu.darken.bb.common.files.ui.picker.PathPickerResult
import eu.darken.bb.common.files.ui.picker.local.LocalPickerFragmentVDC
import eu.darken.bb.common.flow.DynamicStateFlow
import eu.darken.bb.common.navigation.navArgs
import eu.darken.bb.common.permission.Permission
import eu.darken.bb.common.smart.Smart2VDC
import eu.darken.bb.storage.core.Storage
import eu.darken.bb.storage.core.StorageBuilder
import eu.darken.bb.storage.core.local.LocalStorageEditor
import eu.darken.bb.storage.ui.editor.StorageEditorResult
import kotlinx.coroutines.flow.*
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class LocalEditorFragmentVDC @Inject constructor(
    handle: SavedStateHandle,
    private val builder: StorageBuilder,
    private val dispatcherProvider: DispatcherProvider,
) : Smart2VDC(dispatcherProvider) {
    private val navArgs by handle.navArgs<LocalEditorFragmentArgs>()
    private val storageId: Storage.Id = navArgs.storageId
    private val stater = DynamicStateFlow(TAG, vdcScope) { State() }
    val state = stater.asLiveData2()

    private val editorObs = builder.storage(storageId)
        .filter { it.editor != null }
        .map { it.editor as LocalStorageEditor }

    private val editorDataObs = editorObs.flatMapConcat { it.editorData }

    private suspend fun getEditor(): LocalStorageEditor = editorObs.first()

    val requestPermissionEvent = SingleLiveEvent<Permission>()

    val pickerEvent = SingleLiveEvent<PathPickerOptions>()
    val finishEvent = SingleLiveEvent<StorageEditorResult>()

    init {
        editorDataObs
            .onEach { editorData ->
                stater.updateBlocking {
                    copy(
                        label = editorData.label,
                        path = editorData.refPath?.path ?: "",
                        isWorking = false,
                        isExisting = editorData.existingStorage,
                        missingPermissions = getEditor().getMissingPermissions()
                    )
                }
            }
            .launchInViewModel()

        editorObs
            .flatMapConcat { it.isValid() }
            .onEach { isValid: Boolean -> stater.updateBlocking { copy(isValid = isValid) } }
            .launchInViewModel()
    }

    fun updateName(label: String) = launch {
        Timber.tag(TAG).v("Updating label: %s", label)
        getEditor().updateLabel(label)
    }

    fun updatePath(result: PathPickerResult) = launch {
        log { "updatePath=$result" }
        if (result.isCanceled) return@launch
        if (result.isFailed) {
            errorEvents.postValue(result.error!!)
            return@launch
        }

        val path: LocalPath = result.selection!!.first() as LocalPath
        log { "Updating path: $path " }
        getEditor().updatePath(path, false)
    }

    fun importStorage(path: APath) = launch {
        log { "importStorage=$path" }
        path as LocalPath
        getEditor().updatePath(path, true)
    }

    fun selectPath() = launch {
        val data = editorDataObs.first()
        pickerEvent.postValue(PathPickerOptions(
            startPath = data.refPath,
            allowedTypes = setOf(APath.PathType.LOCAL),
            payload = Bundle().apply {
                putString(LocalPickerFragmentVDC.ARG_MODE, LocalGateway.Mode.NORMAL.name)
            }
        ))
    }

    fun requestPermission(permission: Permission) {
        log { "onGrantPermission=$permission" }
        requestPermissionEvent.postValue(permission)
    }

    fun onUpdatePermission(permission: Permission) = launch {
        log { "onUpdatePermission=$permission" }
        stater.updateBlocking { copy(missingPermissions = getEditor().getMissingPermissions()) }
    }

    fun saveConfig() = launch {
        stater.updateBlocking { copy(isWorking = true) }

        val ref = builder.save(storageId)

        finishEvent.postValue(StorageEditorResult(storageId = ref.storageId))
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