package eu.darken.bb.backup.backups.app

import dagger.Reusable
import eu.darken.bb.App
import eu.darken.bb.backup.backups.Backup
import eu.darken.bb.backup.backups.BackupConfig
import eu.darken.bb.backup.backups.BackupEndpoint
import eu.darken.bb.backup.backups.BackupId
import eu.darken.bb.backup.processor.tmp.TmpDataRepo
import eu.darken.bb.backup.processor.tmp.TmpRef
import eu.darken.bb.common.file.asFile
import eu.darken.bb.common.file.copyTo
import javax.inject.Inject

class AppBackupEndpoint(
        private val apkExporter: APKExporter,
        private val tmpDataRepo: TmpDataRepo
) : BackupEndpoint {

    override fun backup(config: BackupConfig): Backup {
        config as AppBackupConfig
        val builder = AppBackupBuilder(config, BackupId())

        val apkData = apkExporter.getAPKFile(config.packageName)

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

    override fun toString(): String = "AppBackupEndpoint()"

    companion object {
        val TAG = App.logTag("AppBackup", "Endpoint")
    }

    @Reusable
    class Factory @Inject constructor(
            private val apkExporter: APKExporter,
            private val tmpDataRepo: TmpDataRepo
    ) : BackupEndpoint.Factory {
        override fun isCompatible(config: BackupConfig): Boolean {
            return config.configType == Backup.Type.APP_BACKUP
        }

        override fun create(config: BackupConfig): AppBackupEndpoint {
            return AppBackupEndpoint(
                    apkExporter = apkExporter,
                    tmpDataRepo = tmpDataRepo
            )
        }

    }

}