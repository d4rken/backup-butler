package eu.darken.bb.backups.core.file

import dagger.Reusable
import eu.darken.bb.backups.core.Backup
import eu.darken.bb.backups.core.BackupSpec
import eu.darken.bb.backups.core.Endpoint
import java.io.File
import javax.inject.Inject

class FileEndpoint : Endpoint {
    val basePath = File("/storage/emulated/0")
    override fun restore(backup: Backup): Boolean {
        TODO("not implemented")
    }

    override fun backup(spec: BackupSpec): Backup {
        TODO("not implemented")
    }

    @Reusable
    class Factory @Inject constructor() : Endpoint.Factory {
        override fun create(spec: BackupSpec): Endpoint {
            return FileEndpoint()
        }

    }
}