package eu.darken.bb.storage.ui.editor.types.saf

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.App
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.Stater
import eu.darken.bb.common.files.core.APath
import eu.darken.bb.common.files.core.saf.SAFGateway
import eu.darken.bb.common.files.core.saf.SAFPath
import eu.darken.bb.common.files.ui.picker.APathPicker
import eu.darken.bb.common.getRootCause
import eu.darken.bb.common.navigation.navArgs
import eu.darken.bb.common.rx.withScopeVDC
import eu.darken.bb.common.vdc.SmartVDC
import eu.darken.bb.storage.core.Storage
import eu.darken.bb.storage.core.StorageBuilder
import eu.darken.bb.storage.core.saf.SAFStorageEditor
import io.reactivex.rxjava3.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SAFEditorFragmentVDC @Inject constructor(
    handle: SavedStateHandle,
    private val builder: StorageBuilder,
    private val safGateway: SAFGateway
) : SmartVDC() {

    private val navArgs by handle.navArgs<SAFEditorFragmentArgs>()
    private val storageId: Storage.Id = navArgs.storageId
    private val stater = Stater(State())
    val state = stater.liveData

    private val editorObs = builder.storage(storageId)
        .subscribeOn(Schedulers.io())
        .filter { it.editor != null }
        .map { it.editor as SAFStorageEditor }

    private val editorDataObs = editorObs.switchMap { it.editorData }

    private val editor: SAFStorageEditor by lazy { editorObs.blockingFirst() }

    val openPickerEvent = SingleLiveEvent<APathPicker.Options>()
    val errorEvent = SingleLiveEvent<Throwable>()
    val finishEvent = SingleLiveEvent<Any>()

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

        editorObs
            .switchMap { it.isValid() }
            .subscribe { isValid: Boolean -> stater.update { it.copy(isValid = isValid) } }
            .withScopeVDC(this)
    }

    fun updateName(label: String) {
        Timber.tag(TAG).v("Updating label: %s", label)
        editor.updateLabel(label)
    }

    fun selectPath() {
        openPickerEvent.postValue(
            APathPicker.Options(
                startPath = editorDataObs.blockingFirst().refPath,
                allowedTypes = setOf(APath.PathType.SAF)
            )
        )
    }

    fun onUpdatePath(result: APathPicker.Result) {
        val p = result.selection!!.first() as SAFPath
        val newlyPersistedPermission = result.persistedPermissions?.isNotEmpty() ?: false
        editor.updatePath(p, false, newlyPersistedPermission)
            .subscribeOn(Schedulers.io())
            .subscribe { _, error ->
                if (error != null) errorEvent.postValue(error)
            }
    }

    fun importStorage(path: APath) {
        path as SAFPath
        editor.updatePath(path, true)
            .subscribeOn(Schedulers.io())
            .subscribe { _, error ->
                if (error != null) errorEvent.postValue(error.getRootCause())
            }
    }

    fun saveConfig() {
        builder.save(storageId)
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .doOnSubscribe { stater.update { it.copy(isWorking = true) } }
            .doFinally { finishEvent.postValue(Any()) }
            .subscribe { _, error ->
                if (error != null) errorEvent.postValue(error.getRootCause())
            }
    }

    data class State(
        val label: String = "",
        val path: String = "",
        val isWorking: Boolean = false,
        val isExisting: Boolean = false,
        val isValid: Boolean = false
    )

    companion object {
        val TAG = App.logTag("Storage", "SAF", "Editor", "VDC")
    }
}