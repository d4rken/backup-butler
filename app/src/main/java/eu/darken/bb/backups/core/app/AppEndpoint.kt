package eu.darken.bb.backups.core.app

import dagger.Reusable
import eu.darken.bb.App
import eu.darken.bb.backups.core.Backup
import eu.darken.bb.backups.core.BackupId
import eu.darken.bb.backups.core.BackupSpec
import eu.darken.bb.backups.core.Endpoint
import eu.darken.bb.common.file.asFile
import eu.darken.bb.processor.tmp.TmpDataRepo
import eu.darken.bb.processor.tmp.TmpRef
import javax.inject.Inject

class AppEndpoint(
        private val apkExporter: APKExporter,
        private val tmpDataRepo: TmpDataRepo
) : Endpoint {

    override fun backup(spec: BackupSpec): Backup {
        spec as AppBackupSpec
        val builder = AppBackupBuilder(spec, BackupId())

        val apkData = apkExporter.getAPKFile(spec.packageName)

        val baseApkRef: TmpRef = tmpDataRepo.create(backupId = builder.backupId)
        baseApkRef.originalPath = apkData.mainSource

        apkData.mainSource.asFile().copyTo(baseApkRef.file.asFile())
        builder.baseApk = baseApkRef

        val splitApkRefs = mutableListOf<TmpRef>()
        apkData.splitSources.forEach { splitApk ->
            val splitSourceRef = tmpDataRepo.create(builder.backupId)
            splitSourceRef.originalPath = splitApk
            splitApk.asFile().copyTo(splitSourceRef.file.asFile())
            splitApkRefs.add(splitSourceRef)
        }
        builder.splitApks = splitApkRefs

        return builder.toBackup()
    }

    override fun restore(backup: Backup): Boolean {

        TODO("not implemented")
    }

    override fun toString(): String = "AppEndpoint()"

    companion object {
        val TAG = App.logTag("AppBackup", "Endpoint")
    }

    @Reusable
    class Factory @Inject constructor(
            private val apkExporter: APKExporter,
            private val tmpDataRepo: TmpDataRepo
    ) : Endpoint.Factory {

        override fun create(spec: BackupSpec): AppEndpoint {
            return AppEndpoint(
                    apkExporter = apkExporter,
                    tmpDataRepo = tmpDataRepo
            )
        }

    }

}