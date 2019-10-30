package eu.darken.bb.backup.ui.generator.editor.types.files

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.App
import eu.darken.bb.backup.core.Generator
import eu.darken.bb.backup.core.GeneratorBuilder
import eu.darken.bb.backup.core.files.FilesSpecGeneratorEditor
import eu.darken.bb.common.*
import eu.darken.bb.common.file.APath
import eu.darken.bb.common.file.picker.APathPicker
import eu.darken.bb.common.rx.withScopeVDC
import eu.darken.bb.common.ui.BaseEditorFragment
import eu.darken.bb.common.vdc.SmartVDC
import eu.darken.bb.common.vdc.VDCFactory
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class FilesEditorFragmentVDC @AssistedInject constructor(
        @Assisted private val handle: SavedStateHandle,
        @Assisted private val generatorId: Generator.Id,
        private val builder: GeneratorBuilder
) : SmartVDC(), BaseEditorFragment.VDC {

    private val stater = Stater(State())
    override val state = stater.liveData

    private val dataObs = builder.config(generatorId)
            .subscribeOn(Schedulers.io())

    private val editorObs = dataObs
            .filter { it.editor != null }
            .map { it.editor as FilesSpecGeneratorEditor }

    private val editorDataObs = editorObs.switchMap { it.editorData }

    private val editor by lazy { editorObs.blockingFirst() }

    val pickerEvent = SingleLiveEvent<APathPicker.Options>()
    val errorEvent = SingleLiveEvent<Throwable>()

    init {
        editorDataObs
                .subscribe { editorData ->
                    stater.update { state ->
                        state.copy(
                                label = editorData.label,
                                path = editorData.path,
                                workIds = state.clearWorkId(),
                                isExisting = editorData.isExistingGenerator
                        )
                    }
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

    override fun onNavigateBack(): Boolean {
        if (stater.snapshot.isExisting) {
            builder.remove(generatorId)
                    .doOnSubscribe { stater.update { it.copy(workIds = it.addWorkId(WorkId.FOREVER)) } }
                    .subscribeOn(Schedulers.io())
                    .subscribe()

        } else {
            builder
                    .update(generatorId) { data ->
                        data!!.copy(generatorType = null, editor = null)
                    }
                    .subscribeOn(Schedulers.io())
                    .subscribe()
        }
        return true
    }

    fun showPicker() {
        pickerEvent.postValue(APathPicker.Options(
//                startPath = configObs.blockingFirst().path, // TODO
                payload = Bundle().apply { putParcelable("generatorId", generatorId) }
        ))
    }

    data class State(
            val label: String = "",
            val path: APath? = null,
            override val isExisting: Boolean = true,
            override val workIds: Set<WorkId> = setOf(WorkId.DEFAULT)
    ) : BaseEditorFragment.VDC.State, WorkId.State

    @AssistedInject.Factory
    interface Factory : VDCFactory<FilesEditorFragmentVDC> {
        fun create(handle: SavedStateHandle, generatorId: Generator.Id): FilesEditorFragmentVDC
    }

    companion object {
        val TAG = App.logTag("Generator", "Files", "Editor", "VDC")
    }
}