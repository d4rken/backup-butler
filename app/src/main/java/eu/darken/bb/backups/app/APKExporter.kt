package eu.darken.bb.backups.app

import android.content.pm.PackageManager
import dagger.Reusable
import eu.darken.bb.common.file.JavaFile
import eu.darken.bb.common.file.SFile
import javax.inject.Inject

@Reusable
class APKExporter @Inject constructor(
        private val packageManager: PackageManager
) {
    fun getAPKFile(packageName: String): APKData {
        val appInfo = packageManager.getApplicationInfo(packageName, 0)
        val apk = JavaFile.build(appInfo.publicSourceDir)
        val splitFiles = mutableSetOf<SFile>()
        appInfo.splitPublicSourceDirs?.forEach { splitFiles.add(JavaFile.build(it)) }
        return APKData(apk, splitFiles)
    }

    data class APKData(
            val mainSource: SFile,
            val splitSources: Collection<SFile>
    )

}