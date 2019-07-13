package eu.darken.bb.backup.backups.app

import dagger.Reusable
import eu.darken.bb.backup.backups.Backup
import eu.darken.bb.backup.backups.BackupId
import eu.darken.bb.backup.processor.cache.CacheRef
import eu.darken.bb.backup.processor.cache.CacheRepo
import eu.darken.bb.common.file.asFile
import eu.darken.bb.common.file.copyTo
import javax.inject.Inject

class AppBackupEndpoint(
        private val apkExporter: APKExporter,
        private val cacheRepo: CacheRepo
) : Backup.Endpoint {
    override fun backup(config: Backup.Config): AppBackup {
        config as AppBackup.Config
        val backup = AppBackup(
                id = BackupId(),
                config = config
        )
        val apkData = apkExporter.getAPKFile(config.packageName)

        val baseApkRef: CacheRef = cacheRepo.create(backupId = backup.id)
        apkData.mainSource.asFile().copyTo(baseApkRef.file.asFile())
        backup.baseApk = baseApkRef

        val splitApkRefs = mutableListOf<CacheRef>()
        apkData.splitSources.forEach { splitApk ->
            val splitSourceRef = cacheRepo.create(backup.id)
            splitApk.asFile().copyTo(splitSourceRef.file.asFile())
        }
        backup.splitApks = splitApkRefs

        return backup
    }

    override fun restore(backup: Backup): Boolean {
        TODO("not implemented")
    }

    @Reusable
    class Factory @Inject constructor(
            private val apkExporter: APKExporter,
            private val cacheRepo: CacheRepo
    ) : Backup.Endpoint.Factory {
        override fun isCompatible(config: Backup.Config): Boolean {
            return config.configType == Backup.Type.APP_BACKUP
        }

        override fun create(config: Backup.Config): AppBackupEndpoint {
            return AppBackupEndpoint(
                    apkExporter = apkExporter,
                    cacheRepo = cacheRepo
            )
        }

    }

}