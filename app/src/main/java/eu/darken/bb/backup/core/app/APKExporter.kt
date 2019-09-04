package eu.darken.bb.backup.core.app

import android.content.pm.PackageManager
import dagger.Reusable
import java.io.File
import javax.inject.Inject

@Reusable
class APKExporter @Inject constructor(
        private val packageManager: PackageManager
) {
    fun getAPKFile(packageName: String): APKData {
        val appInfo = packageManager.getApplicationInfo(packageName, 0)
        val apk = File(appInfo.publicSourceDir)
        val splitFiles = mutableSetOf<File>()
        appInfo.splitPublicSourceDirs?.forEach { splitFiles.add(File(it)) }
        return APKData(apk, splitFiles)
    }

    data class APKData(
            val mainSource: File,
            val splitSources: Collection<File>
    )

}