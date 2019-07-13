package eu.darken.bb.backup.backups.file

import dagger.Reusable
import eu.darken.bb.backup.backups.Backup
import java.io.File
import javax.inject.Inject

class FileBackupEndpoint : Backup.Endpoint {
    val basePath = File("/storage/emulated/0")
    override fun restore(backup: Backup): Boolean {
        TODO("not implemented")
    }

    override fun backup(config: Backup.Config): FileBackup {
        TODO("not implemented")
    }

    @Reusable
    class Factory @Inject constructor() : Backup.Endpoint.Factory {
        override fun isCompatible(config: Backup.Config): Boolean {
            return config.configType == Backup.Type.FILE
        }

        override fun create(config: Backup.Config): Backup.Endpoint {
            return FileBackupEndpoint()
        }

    }
}