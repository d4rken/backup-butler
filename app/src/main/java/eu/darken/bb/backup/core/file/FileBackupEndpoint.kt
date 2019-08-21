package eu.darken.bb.backup.core.file

import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.BackupSpec
import eu.darken.bb.common.progress.Progress
import java.io.File

class FileBackupEndpoint @AssistedInject constructor(
        @Assisted private val progressClient: Progress.Client?
) : Backup.Endpoint {
    val basePath = File("/storage/emulated/0")

    override fun backup(spec: BackupSpec): Backup.Unit {
        TODO("not implemented")
    }

    @AssistedInject.Factory
    interface Factory : Backup.Endpoint.Factory<FileBackupEndpoint>
}