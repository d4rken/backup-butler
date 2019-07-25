package eu.darken.bb.backups.file

import dagger.Reusable
import eu.darken.bb.backups.Backup
import eu.darken.bb.backups.BackupConfig
import eu.darken.bb.backups.Endpoint
import java.io.File
import javax.inject.Inject

class FileEndpoint : Endpoint {
    val basePath = File("/storage/emulated/0")
    override fun restore(backup: Backup): Boolean {
        TODO("not implemented")
    }

    override fun backup(config: BackupConfig): Backup {
        TODO("not implemented")
    }

    @Reusable
    class Factory @Inject constructor() : Endpoint.Factory {
        override fun isCompatible(config: BackupConfig): Boolean {
            return config.configType == Backup.Type.FILE
        }

        override fun create(config: BackupConfig): Endpoint {
            return FileEndpoint()
        }

    }
}