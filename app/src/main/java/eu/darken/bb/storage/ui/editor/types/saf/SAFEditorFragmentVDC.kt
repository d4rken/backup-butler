package eu.darken.bb.storage.ui.editor.types.saf

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.coroutine.DispatcherProvider
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.files.core.APath
import eu.darken.bb.common.files.core.saf.SAFGateway
import eu.darken.bb.common.files.core.saf.SAFPath
import eu.darken.bb.common.files.ui.picker.PathPickerOptions
import eu.darken.bb.common.files.ui.picker.PathPickerResult
import eu.darken.bb.common.flow.DynamicStateFlow
import eu.darken.bb.common.navigation.navArgs
import eu.darken.bb.common.smart.Smart2VDC
import eu.darken.bb.storage.core.Storage
import eu.darken.bb.storage.core.StorageBuilder
import eu.darken.bb.storage.core.saf.SAFStorageEditor
import eu.darken.bb.storage.ui.editor.StorageEditorResult
import kotlinx.coroutines.flow.*
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SAFEditorFragmentVDC @Inject constructor(
    handle: SavedStateHandle,
    private val builder: StorageBuilder,
    private val safGateway: SAFGateway,
    private val dispatcherProvider: DispatcherProvider,
) : Smart2VDC(dispatcherProvider) {

    private val navArgs by handle.navArgs<SAFEditorFragmentArgs>()
    private val storageId: Storage.Id = navArgs.storageId
    private val stater = DynamicStateFlow(TAG, vdcScope) { State() }
    val state = stater.asLiveData2()

    private val editorObs = builder.storage(storageId)
        .filter { it.editor != null }
        .map { it.editor as SAFStorageEditor }

    private val editorDataObs = editorObs.flatMapConcat { it.editorData }

    private suspend fun getEditor(): SAFStorageEditor = editorObs.first()

    val openPickerEvent = SingleLiveEvent<PathPickerOptions>()
    val errorEvent = SingleLiveEvent<Throwable>()
    val finishEvent = SingleLiveEvent<StorageEditorResult>()

    init {
        editorDataObs.take(1)
            .onEach { editor ->
                stater.updateBlocking { copy(path = editor.refPath?.path ?: "") }
            }
            .launchInViewModel()

        editorDataObs
            .onEach { data ->
                stater.updateBlocking {
                    copy(
                        label = data.label,
                        path = data.refPath?.path ?: "",
                        isWorking = false,
                        isExisting = data.existingStorage
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

    fun selectPath() = launch {
        openPickerEvent.postValue(
            PathPickerOptions(
                startPath = editorDataObs.first().refPath,
                allowedTypes = setOf(APath.PathType.SAF)
            )
        )
    }

    fun onUpdatePath(result: PathPickerResult) = launch {
        val p = result.selection!!.first() as SAFPath
        val newlyPersistedPermission = result.persistedPermissions?.isNotEmpty() ?: false
        getEditor().updatePath(p, false, newlyPersistedPermission)
    }

    fun importStorage(path: APath) = launch {
        path as SAFPath
        getEditor().updatePath(path, true)
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
        val isExisting: Boolean = false,
        val isValid: Boolean = false
    )

    companion object {
        val TAG = logTag("Storage", "SAF", "Editor", "VDC")
    }
}