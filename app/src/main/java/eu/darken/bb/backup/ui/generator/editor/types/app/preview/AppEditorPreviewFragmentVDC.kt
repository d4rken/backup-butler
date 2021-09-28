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
import eu.darken.bb.common.pkgs.Pkg
import eu.darken.bb.common.pkgs.pkgops.PkgOps
import eu.darken.bb.common.rx.withScopeVDC
import eu.darken.bb.common.vdc.SmartVDC
import javax.inject.Inject

@HiltViewModel
class AppEditorPreviewFragmentVDC @Inject constructor(
    private val handle: SavedStateHandle,
    private val builder: GeneratorBuilder,
    private val pkgOps: PkgOps
) : SmartVDC() {
    private val navArgs = handle.navArgs<AppEditorPreviewFragmentArgs>().value
    private val generatorId: Generator.Id = navArgs.generatorId
    private val previewMode: PreviewMode = navArgs.previewMode
    private val stater = Stater(State(previewMode = previewMode))
    val state = stater.liveData

    private val editorObs = builder.generator(generatorId)
        .filter { it.editor != null }
        .map { it.editor as AppSpecGeneratorEditor }

    private val editorDataObs = editorObs.switchMap { it.editorData }

    private val editor: AppSpecGeneratorEditor by lazy { editorObs.blockingFirst() }

    init {
        editorDataObs
            .map { data ->
                val allPkgs = pkgOps.listPkgs()
                    .filter { it.packageType == Pkg.Type.NORMAL }
                    .map { it as AppPkg }

                val previewPkgs = mutableSetOf<PkgWrap>()
                if (data.includeUserApps) {
                    val userPkgs = allPkgs.filterNot { it.isSystemApp }.map { PkgWrap(it, mode = previewMode) }
                    previewPkgs.addAll(userPkgs)
                }
                if (data.includeSystemApps) {
                    val systemApps = allPkgs.filter { it.isSystemApp }.map { PkgWrap(it, mode = previewMode) }
                    previewPkgs.addAll(systemApps)
                }

                val displayPkgs = mutableSetOf<PkgWrap>()
                when (previewMode) {
                    PreviewMode.PREVIEW -> {
                        data.packagesIncluded.forEach { pkg ->
                            val pw = PkgWrap(allPkgs.first { it.packageName == pkg }, mode = previewMode)
                            previewPkgs.add(pw)
                        }
                        previewPkgs.removeAll { data.packagesExcluded.contains(it.pkg.packageName) }
                        displayPkgs.addAll(previewPkgs)
                    }
                    PreviewMode.INCLUDE -> {
                        val possibly = allPkgs
                            .filter { p ->
                                // Those in the preview
                                previewPkgs.find { it.pkg == p } == null
                            }
                            .map {
                                PkgWrap(
                                    pkg = it,
                                    isSelected = data.packagesIncluded.contains(it.packageName),
                                    mode = previewMode
                                )
                            }
                        val already = data.packagesIncluded.map { inPkg ->
                            val appPkg = allPkgs.single { it.packageName == inPkg }
                            PkgWrap(appPkg, true, previewMode)
                        }
                        displayPkgs.addAll(possibly)
                        displayPkgs.addAll(already)
                    }
                    PreviewMode.EXCLUDE -> {
                        val possibly = previewPkgs.map {
                            it.copy(isSelected = data.packagesExcluded.contains(it.pkg.packageName))
                        }
                        val already = data.packagesExcluded.map { exPkg ->
                            val appPkg = allPkgs.single { it.packageName == exPkg }
                            PkgWrap(appPkg, true, previewMode)
                        }
                        displayPkgs.addAll(possibly)
                        displayPkgs.addAll(already)
                    }
                }
                displayPkgs
            }
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