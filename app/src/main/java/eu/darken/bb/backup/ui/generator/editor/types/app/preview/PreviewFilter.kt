package eu.darken.bb.backup.ui.generator.editor.types.app.preview

import dagger.Reusable
import eu.darken.bb.backup.core.app.AppSpecGeneratorEditor
import eu.darken.bb.common.pkgs.AppPkg
import eu.darken.bb.common.pkgs.Pkg
import eu.darken.bb.common.pkgs.pkgops.PkgOps
import javax.inject.Inject

@Reusable
class PreviewFilter @Inject constructor(
    private val pkgOps: PkgOps
) {

    fun filter(
        data: AppSpecGeneratorEditor.Data,
        previewMode: PreviewMode
    ): Set<PkgWrap> {
        val allPkgs = pkgOps.listPkgs()
            .filter { it.packageType == Pkg.Type.NORMAL }
            .map { it as AppPkg }

        val previewPkgs = mutableSetOf<PkgWrap>()
        if (data.includeUserApps) {
            val userPkgs = allPkgs.filterNot { it.isSystemApp }.map {
                PkgWrap(
                    it,
                    mode = previewMode
                )
            }
            previewPkgs.addAll(userPkgs)
        }
        if (data.includeSystemApps) {
            val systemApps = allPkgs.filter { it.isSystemApp }.map {
                PkgWrap(
                    it,
                    mode = previewMode
                )
            }
            previewPkgs.addAll(systemApps)
        }

        val displayPkgs = mutableSetOf<PkgWrap>()
        when (previewMode) {
            PreviewMode.PREVIEW -> {
                data.packagesIncluded.forEach { pkg ->
                    val pw =
                        PkgWrap(allPkgs.first { it.packageName == pkg }, mode = previewMode)
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
        return displayPkgs
    }

    data class PkgWrap(
        val pkg: AppPkg,
        val isSelected: Boolean = false,
        val mode: PreviewMode
    ) {
        val pkgName = pkg.packageName
    }

}