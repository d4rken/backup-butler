package eu.darken.bb.backup.backups.file

import dagger.Reusable
import eu.darken.bb.backup.backups.Backup
import eu.darken.bb.backup.backups.BackupConfig
import eu.darken.bb.backup.backups.BackupEndpoint
import java.io.File
import javax.inject.Inject

class FileBackupEndpoint : BackupEndpoint {
    val basePath = File("/storage/emulated/0")
    override fun restore(backup: Backup): Boolean {
        TODO("not implemented")
    }

    override fun backup(config: BackupConfig): Backup {
        TODO("not implemented")
    }

    @Reusable
    class Factory @Inject constructor() : BackupEndpoint.Factory {
        override fun isCompatible(config: BackupConfig): Boolean {
            return config.configType == Backup.Type.FILE
        }

        override fun create(config: BackupConfig): BackupEndpoint {
            return FileBackupEndpoint()
        }

    }
}