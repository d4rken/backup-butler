package eu.darken.bb.backup.ui.generator.editor.types.app.preview

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.backup.core.Generator
import eu.darken.bb.backup.core.GeneratorBuilder
import eu.darken.bb.backup.core.app.AppSpecGeneratorEditor
import eu.darken.bb.common.Stater
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.navigation.navArgs
import eu.darken.bb.common.pkgs.AppPkg
import eu.darken.bb.common.pkgs.pkgops.PkgOps
import eu.darken.bb.common.rx.withScopeVDC
import eu.darken.bb.common.vdc.SmartVDC
import javax.inject.Inject

@HiltViewModel
class AppEditorPreviewFragmentVDC @Inject constructor(
    private val handle: SavedStateHandle,
    private val builder: GeneratorBuilder,
    private val pkgOps: PkgOps,
    private val previewFilter: PreviewFilter,
) : SmartVDC() {
    private val navArgs = handle.navArgs<AppEditorPreviewFragmentArgs>().value
    private val generatorId: Generator.Id = navArgs.generatorId
    private val previewMode: PreviewMode = navArgs.previewMode
    private val stater = Stater { State(previewMode = previewMode) }
    val state = stater.liveData

    private val editorObs = builder.generator(generatorId)
        .filter { it.editor != null }
        .map { it.editor as AppSpecGeneratorEditor }

    private val editorDataObs = editorObs.switchMap { it.editorData }

    private val editor: AppSpecGeneratorEditor by lazy { editorObs.blockingFirst() }

    init {
        editorDataObs
            .map { data -> previewFilter.filter(data, previewMode) }
            .subscribe { pkgs ->
                stater.update { state ->
                    state.copy(
                        pkgs = pkgs.sortedBy { it.pkg.getLabel(pkgOps) }.toList(),
                        selected = pkgs.filter { it.isSelected }.map { it.pkgName }.toSet(),
                        isLoading = false
                    )
                }
            }
            .withScopeVDC(this)
    }

    fun updateLabel(label: String) {
        editor.updateLabel(label)
    }

    fun onSelect(pkgWrap: PkgWrap) {
        if (previewMode == PreviewMode.PREVIEW) {
            return
        }

        val pkgName = pkgWrap.pkgName
        editor.update { data ->
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
        val pkgs: List<PkgWrap> = emptyList(),
        val isLoading: Boolean = true
    )

    data class PkgWrap(
        val pkg: AppPkg,
        val isSelected: Boolean = false,
        val mode: PreviewMode
    ) {
        val pkgName = pkg.packageName
    }

    companion object {
        val TAG = logTag("Generator", "App", "Editor", "Preview", "VDC")
    }
}