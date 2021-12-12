package eu.darken.bb.backup.ui.generator.editor.types.app.preview

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.backup.core.Generator
import eu.darken.bb.backup.core.GeneratorBuilder
import eu.darken.bb.backup.core.app.AppSpecGeneratorEditor
import eu.darken.bb.common.coroutine.DispatcherProvider
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.flow.DynamicStateFlow
import eu.darken.bb.common.navigation.navArgs
import eu.darken.bb.common.pkgs.pkgops.PkgOps
import eu.darken.bb.common.smart.Smart2VDC
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class AppEditorPreviewFragmentVDC @Inject constructor(
    private val handle: SavedStateHandle,
    private val dispatcherProvider: DispatcherProvider,
    private val builder: GeneratorBuilder,
    private val pkgOps: PkgOps,
    private val previewFilter: PreviewFilter,
) : Smart2VDC(dispatcherProvider) {
    private val navArgs = handle.navArgs<AppEditorPreviewFragmentArgs>().value
    private val generatorId: Generator.Id = navArgs.generatorId
    private val previewMode: PreviewMode = navArgs.previewMode
    private val stater = DynamicStateFlow(TAG, vdcScope) { State(previewMode = previewMode) }
    val state = stater.asLiveData2()

    private val editorFlow = builder.generator(generatorId)
        .filter { it.editor != null }
        .map { it.editor as AppSpecGeneratorEditor }

    private val editorDataFlow = editorFlow.flatMapLatest { it.editorData }

    private suspend fun getEditor(): AppSpecGeneratorEditor = editorFlow.first()

    init {
        editorDataFlow
            .map { data -> previewFilter.filter(data, previewMode) }
            .onEach { pkgs ->
                stater.updateBlocking {
                    copy(
                        pkgs = pkgs.sortedBy { it.pkg.getLabel(pkgOps) }.toList(),
                        selected = pkgs.filter { it.isSelected }.map { it.pkgName }.toSet(),
                        isLoading = false
                    )
                }
            }
            .launchInViewModel()
    }

    fun updateLabel(label: String) = launch {
        getEditor().updateLabel(label)
    }

    fun onSelect(pkgWrap: PreviewFilter.PkgWrap) = launch {
        if (previewMode == PreviewMode.PREVIEW) {
            return@launch
        }

        val pkgName = pkgWrap.pkgName
        getEditor().update { data ->
            when (previewMode) {
                PreviewMode.INCLUDE -> {
                    val updated = data.packagesIncluded.let {
                        if (it.contains(pkgName)) it.minus(pkgName) else it.plus(pkgName)
                    }
                    data.copy(packagesIncluded = updated)
                }
                PreviewMode.EXCLUDE -> {
                    val updated = data.packagesExcluded.let {
                        if (it.contains(pkgName)) it.minus(pkgName) else it.plus(pkgName)
                    }
                    data.copy(packagesExcluded = updated)
                }
                PreviewMode.PREVIEW -> data
            }
        }
    }


    data class State(
        val previewMode: PreviewMode,
        val selected: Set<String> = emptySet(),
        val pkgs: List<PreviewFilter.PkgWrap> = emptyList(),
        val isLoading: Boolean = true
    )

    companion object {
        val TAG = logTag("Generator", "App", "Editor", "Preview", "VDC")
    }
}