package eu.darken.bb.backup.ui.generator.editor.types.files

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.App
import eu.darken.bb.backup.core.Generator
import eu.darken.bb.backup.core.GeneratorBuilder
import eu.darken.bb.backup.core.files.FilesSpecGeneratorEditor
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.Stater
import eu.darken.bb.common.WorkId
import eu.darken.bb.common.clearWorkId
import eu.darken.bb.common.file.core.APath
import eu.darken.bb.common.file.ui.picker.APathPicker
import eu.darken.bb.common.rx.withScopeVDC
import eu.darken.bb.common.vdc.SmartVDC
import eu.darken.bb.common.vdc.VDCFactory
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class FilesEditorConfigFragmentVDC @AssistedInject constructor(
        @Assisted private val handle: SavedStateHandle,
        @Assisted private val generatorId: Generator.Id,
        private val builder: GeneratorBuilder
) : SmartVDC() {

    private val stater = Stater(State())
    val state = stater.liveData

    private val dataObs = builder.generator(generatorId)
            .subscribeOn(Schedulers.io())

    private val editorObs = dataObs
            .filter { it.editor != null }
            .map { it.editor as FilesSpecGeneratorEditor }

    private val editorDataObs = editorObs.switchMap { it.editorData }

    private val editor by lazy { editorObs.blockingFirst() }

    val pickerEvent = SingleLiveEvent<APathPicker.Options>()
    val errorEvent = SingleLiveEvent<Throwable>()
    val finishEvent = SingleLiveEvent<Any>()

    init {
        editorDataObs
                .subscribe { editorData ->
                    stater.update { state ->
                        state.copy(
                                label = editorData.label,
                                path = editorData.path,
                                workIds = state.clearWorkId()
                        )
                    }
                }
                .withScopeVDC(this)

        editorObs
                .flatMap { it.isValid() }
                .subscribe { isValid -> stater.update { it.copy(isValid = isValid) } }
                .withScopeVDC(this)

        editorObs
                .flatMap { it.editorData }
                .subscribe { data ->
                    stater.update { it.copy(isExisting = data.isExistingGenerator) }
                }
                .withScopeVDC(this)
    }

    fun updateLabel(label: String) {
        editor.updateLabel(label)
    }

    fun updatePath(result: APathPicker.Result) {
        Timber.tag(TAG).d("updatePath(result=%s)", result)
        if (result.isFailed) {
            errorEvent.postValue(result.error)
            return
        }
        editor.updatePath(result.selection!!.first())
    }

    fun showPicker() {
        pickerEvent.postValue(APathPicker.Options(
//                startPath = configObs.blockingFirst().path, // TODO
                payload = Bundle().apply { putParcelable("generatorId", generatorId) }
        ))
    }

    fun saveConfig() {
        builder.save(generatorId)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .doFinally { finishEvent.postValue(Any()) }
                .subscribe()
    }

    data class State(
            val label: String = "",
            val path: APath? = null,
            val isValid: Boolean = false,
            val isExisting: Boolean = false,
            override val workIds: Set<WorkId> = setOf(WorkId.DEFAULT)
    ) : WorkId.State

    @AssistedInject.Factory
    interface Factory : VDCFactory<FilesEditorConfigFragmentVDC> {
        fun create(handle: SavedStateHandle, generatorId: Generator.Id): FilesEditorConfigFragmentVDC
    }

    companion object {
        val TAG = App.logTag("Generator", "Files", "Editor", "VDC")
    }
}