package eu.darken.bb.backup.ui.generator.editor.types.files

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.backup.core.Generator
import eu.darken.bb.backup.core.GeneratorBuilder
import eu.darken.bb.backup.core.files.FilesSpecGeneratorEditor
import eu.darken.bb.backup.ui.generator.editor.GeneratorEditorResult
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.WorkId
import eu.darken.bb.common.clearWorkId
import eu.darken.bb.common.coroutine.DispatcherProvider
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.files.core.APath
import eu.darken.bb.common.files.ui.picker.PathPickerOptions
import eu.darken.bb.common.files.ui.picker.PathPickerResult
import eu.darken.bb.common.flow.DynamicStateFlow
import eu.darken.bb.common.navigation.navArgs
import eu.darken.bb.common.smart.Smart2VDC
import kotlinx.coroutines.flow.*
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class FilesEditorConfigFragmentVDC @Inject constructor(
    handle: SavedStateHandle,
    private val builder: GeneratorBuilder,
    private val dispatcherProvider: DispatcherProvider,
) : Smart2VDC(dispatcherProvider) {

    private val generatorId: Generator.Id = handle.navArgs<FilesEditorConfigFragmentArgs>().value.generatorId
    private val stater = DynamicStateFlow(TAG, vdcScope) { State() }
    val state = stater.asLiveData2()

    private val editorFlow = builder.generator(generatorId)
        .filter { it.editor != null }
        .map { it.editor as FilesSpecGeneratorEditor }

    private val editorDataFlow = editorFlow.flatMapLatest { it.editorData }

    private suspend fun getEditor() = editorFlow.first()

    val pickerEvent = SingleLiveEvent<PathPickerOptions>()
    val finishEvent = SingleLiveEvent<GeneratorEditorResult>()

    init {
        editorDataFlow
            .onEach { editorData ->
                stater.updateBlocking {
                    copy(
                        label = editorData.label,
                        path = editorData.path,
                        workIds = clearWorkId()
                    )
                }
            }
            .launchInViewModel()

        editorFlow
            .flatMapConcat { it.isValid() }
            .onEach { isValid -> stater.updateBlocking { copy(isValid = isValid) } }
            .launchInViewModel()

        editorFlow
            .flatMapConcat { it.editorData }
            .onEach { data ->
                stater.updateBlocking { copy(isExisting = data.isExistingGenerator) }
            }
            .launchInViewModel()
    }

    fun updateLabel(label: String) = launch {
        getEditor().updateLabel(label)
    }

    fun updatePath(result: PathPickerResult) = launch {
        Timber.tag(TAG).d("updatePath(result=%s)", result)
        if (result.isFailed) {
            errorEvents.postValue(result.error!!)
            return@launch
        }
        getEditor().updatePath(result.selection!!.first())
    }

    fun showPicker() = launch {
        pickerEvent.postValue(PathPickerOptions(
            startPath = editorDataFlow.first().path,
            allowedTypes = setOf(APath.PathType.SAF, APath.PathType.LOCAL),
            selectionLimit = 1,
            onlyDirs = true,
            payload = Bundle().apply { putParcelable("generatorId", generatorId) }
        ))
    }

    fun saveConfig() = launch {
        val saved = builder.save(generatorId)
        GeneratorEditorResult(generatorId = saved.generatorId).run { finishEvent.postValue(this) }
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