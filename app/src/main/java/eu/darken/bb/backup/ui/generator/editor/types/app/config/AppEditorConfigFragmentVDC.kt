package eu.darken.bb.backup.ui.generator.editor.types.app.config

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.backup.core.GeneratorBuilder
import eu.darken.bb.backup.core.app.AppSpecGeneratorEditor
import eu.darken.bb.backup.ui.generator.editor.GeneratorEditorResult
import eu.darken.bb.backup.ui.generator.editor.types.app.preview.PreviewFilter
import eu.darken.bb.backup.ui.generator.editor.types.app.preview.PreviewMode
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.Stater
import eu.darken.bb.common.coroutine.DispatcherProvider
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.files.core.APath
import eu.darken.bb.common.navigation.navArgs
import eu.darken.bb.common.smart.Smart2VDC
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class AppEditorConfigFragmentVDC @Inject constructor(
    handle: SavedStateHandle,
    private val builder: GeneratorBuilder,
    private val previewFilter: PreviewFilter,
    private val dispatcherProvider: DispatcherProvider,
) : Smart2VDC(dispatcherProvider) {

    private val navArgs = handle.navArgs<AppEditorConfigFragmentArgs>()
    private val generatorId = navArgs.value.generatorId

    private val stater = Stater { State() }
    val state = stater.liveData

    private val editorFlow = builder.generator(generatorId)
        .filter { it.editor != null }
        .map { it.editor as AppSpecGeneratorEditor }

    private val editorDataFlow = editorFlow.flatMapLatest { it.editorData }

    val matchedPkgsCount = editorDataFlow
        .map { previewFilter.filter(it, PreviewMode.PREVIEW).size }
        .asLiveData2()

    private suspend fun getEditor(): AppSpecGeneratorEditor = editorFlow.first()

    val finishEvent = SingleLiveEvent<GeneratorEditorResult>()

    init {
        editorDataFlow
            .onEach { editorData ->
                stater.update { state ->
                    state.copy(
                        label = editorData.label,
                        isWorking = false,
                        isExisting = editorData.isExistingGenerator,
                        autoInclude = editorData.autoInclude,
                        includeUserApps = editorData.includeUserApps,
                        includeSystemApps = editorData.includeSystemApps,
                        packagesIncluded = editorData.packagesIncluded,
                        packagesExcluded = editorData.packagesExcluded,
                        backupApk = editorData.backupApk,
                        backupData = editorData.backupData,
                        backupCache = editorData.backupCache,
                        extraPaths = editorData.extraPaths
                    )
                }
            }
            .launchInViewModel()

        editorFlow
            .flatMapConcat { it.isValid() }
            .onEach { isValid -> stater.update { it.copy(isValid = isValid) } }
            .launchInViewModel()

        editorFlow
            .flatMapConcat { it.editorData }
            .onEach { data ->
                stater.update { it.copy(isExisting = data.isExistingGenerator) }
            }
            .launchInViewModel()
    }

    fun updateLabel(label: String) = launch {
        getEditor().updateLabel(label)
    }

    fun onUpdateAutoInclude(enabled: Boolean) = launch {
        getEditor().update { it.copy(autoInclude = enabled) }
    }

    fun onUpdateIncludeUser(enabled: Boolean) = launch {
        // TODO this hangs the UI?
        getEditor().update { it.copy(includeUserApps = enabled) }
    }

    fun onUpdateIncludeSystem(enabled: Boolean) = launch {
        getEditor().update { it.copy(includeSystemApps = enabled) }
    }

    fun onUpdateBackupApk(enabled: Boolean) = launch {
        getEditor().update { it.copy(backupApk = enabled) }
    }

    fun onUpdateBackupData(enabled: Boolean) = launch {
        getEditor().update { it.copy(backupData = enabled) }
    }

    fun onUpdateBackupCache(enabled: Boolean) = launch {
        getEditor().update { it.copy(backupCache = enabled) }
    }

    fun saveConfig() = launch {
        val config = builder.save(generatorId)
        GeneratorEditorResult(
            generatorId = config.generatorId
        ).run { finishEvent.postValue(this) }
    }

    data class State(
        val label: String = "",
        val includedPackages: List<String> = emptyList(),
        val isWorking: Boolean = false,
        val isValid: Boolean = false,
        val isExisting: Boolean = false,
        val autoInclude: Boolean = false,
        val includeUserApps: Boolean = false,
        val includeSystemApps: Boolean = false,
        val packagesIncluded: Collection<String> = listOf(),
        val packagesExcluded: Collection<String> = listOf(),
        val backupApk: Boolean = false,
        val backupData: Boolean = false,
        val backupCache: Boolean = false,
        val extraPaths: Map<String, Collection<APath>> = emptyMap()
    )

    companion object {
        val TAG = logTag("Generator", "App", "Editor", "Config", "VDC")
    }
}