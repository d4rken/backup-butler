package eu.darken.bb.backup.backups.app

import dagger.Reusable
import eu.darken.bb.App
import eu.darken.bb.backup.backups.Backup
import eu.darken.bb.backup.backups.BackupId
import eu.darken.bb.backup.processor.tmp.TmpDataRepo
import eu.darken.bb.backup.processor.tmp.TmpRef
import eu.darken.bb.common.file.asFile
import eu.darken.bb.common.file.copyTo
import javax.inject.Inject

class AppBackupEndpoint(
        private val apkExporter: APKExporter,
        private val tmpDataRepo: TmpDataRepo
) : Backup.Endpoint {

    override fun backup(config: Backup.Config): AppBackup {

        config as AppBackup.Config
        val backup = AppBackup(
                packageName = config.packageName,
                id = BackupId(),
                config = config
        )
        val apkData = apkExporter.getAPKFile(config.packageName)

        val baseApkRef: TmpRef = tmpDataRepo.create(backupId = backup.id)
        baseApkRef.originalPath = apkData.mainSource

        apkData.mainSource.asFile().copyTo(baseApkRef.file.asFile())
        backup.baseApk = baseApkRef

        val splitApkRefs = mutableListOf<TmpRef>()
        apkData.splitSources.forEach { splitApk ->
            val splitSourceRef = tmpDataRepo.create(backup.id)
            splitSourceRef.originalPath = splitApk
            splitApk.asFile().copyTo(splitSourceRef.file.asFile())
            splitApkRefs.add(splitSourceRef)
        }
        backup.splitApks = splitApkRefs

        return backup
    }

    override fun restore(backup: Backup): Boolean {
        backup as AppBackup

        TODO("not implemented")
    }

    override fun toString(): String = "AppBackupEndpoint()"

    companion object {
        val TAG = App.logTag("AppBackup", "Endpoint")
    }

    @Reusable
    class Factory @Inject constructor(
            private val apkExporter: APKExporter,
            private val tmpDataRepo: TmpDataRepo
    ) : Backup.Endpoint.Factory {
        override fun isCompatible(config: Backup.Config): Boolean {
            return config.configType == Backup.Type.APP_BACKUP
        }

        override fun create(config: Backup.Config): AppBackupEndpoint {
            return AppBackupEndpoint(
                    apkExporter = apkExporter,
                    tmpDataRepo = tmpDataRepo
            )
        }

    }

}