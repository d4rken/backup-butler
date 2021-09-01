package eu.darken.bb.backup.core.app

import android.content.pm.PackageManager
import dagger.Reusable
import eu.darken.bb.common.files.core.local.LocalPath
import javax.inject.Inject

@Reusable
class APKExporter @Inject constructor(
    private val packageManager: PackageManager
) {
    fun getAPKFile(packageName: String): APKData {
        val appInfo = packageManager.getApplicationInfo(packageName, 0)
        val apk = LocalPath.build(appInfo.publicSourceDir)
        val splitFiles = mutableSetOf<LocalPath>()
        appInfo.splitPublicSourceDirs?.forEach { splitFiles.add(LocalPath.build(it)) }
        return APKData(apk, splitFiles)
    }

    data class APKData(
        val mainSource: LocalPath,
        val splitSources: Collection<LocalPath>
    )

}