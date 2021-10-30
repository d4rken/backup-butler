package eu.darken.bb.backup.ui.generator.editor.types.files

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.backup.core.Generator
import eu.darken.bb.backup.core.GeneratorBuilder
import eu.darken.bb.backup.core.files.FilesSpecGeneratorEditor
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.Stater
import eu.darken.bb.common.WorkId
import eu.darken.bb.common.clearWorkId
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.files.core.APath
import eu.darken.bb.common.files.ui.picker.PathPicker
import eu.darken.bb.common.navigation.navArgs
import eu.darken.bb.common.rx.withScopeVDC
import eu.darken.bb.common.vdc.SmartVDC
import io.reactivex.rxjava3.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class FilesEditorConfigFragmentVDC @Inject constructor(
    handle: SavedStateHandle,
    private val builder: GeneratorBuilder
) : SmartVDC() {

    private val generatorId: Generator.Id = handle.navArgs<FilesEditorConfigFragmentArgs>().value.generatorId
    private val stater = Stater { State() }
    val state = stater.liveData

    private val dataObs = builder.generator(generatorId).observeOn(Schedulers.computation())

    private val editorObs = dataObs
        .filter { it.editor != null }
        .map { it.editor as FilesSpecGeneratorEditor }

    private val editorDataObs = editorObs.switchMap { it.editorData }

    private val editor by lazy { editorObs.blockingFirst() }

    val pickerEvent = SingleLiveEvent<PathPicker.Options>()
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
            .observeOn(Schedulers.computation())
            .subscribe(
                { },
                { err -> errorEvent.postValue(err) }
            )
    }

    fun updatePath(result: PathPicker.Result) {
        Timber.tag(TAG).d("updatePath(result=%s)", result)
        if (result.isFailed) {
            errorEvent.postValue(result.error!!)
            return
        }
        editor.updatePath(result.selection!!.first())
            .observeOn(Schedulers.computation())
            .subscribe(
                { },
                { err -> errorEvent.postValue(err) }
            )
    }

    fun showPicker() {
        pickerEvent.postValue(PathPicker.Options(
            startPath = editorDataObs.blockingFirst().path,
            allowedTypes = setOf(APath.PathType.SAF, APath.PathType.LOCAL),
            selectionLimit = 1,
            onlyDirs = true,
            payload = Bundle().apply { putParcelable("generatorId", generatorId) }
        ))
    }

    fun saveConfig() {
        builder.save(generatorId)
            .observeOn(Schedulers.computation())
            .doFinally { finishEvent.postValue(Any()) }
            .subscribe(
                { },
                { err -> errorEvent.postValue(err) }
            )
    }

    data class State(
        val label: String = "",
        val path: APath? = null,
        val isValid: Boolean = false,
        val isExisting: Boolean = false,
        override val workIds: Set<WorkId> = setOf(WorkId.DEFAULT)
    ) : WorkId.State

    companion object {
        val TAG = logTag("Generator", "Files", "Editor", "VDC")
    }
}