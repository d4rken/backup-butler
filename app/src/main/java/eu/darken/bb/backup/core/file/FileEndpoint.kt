package eu.darken.bb.backup.core.file

import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.BackupSpec
import eu.darken.bb.backup.core.Endpoint
import eu.darken.bb.common.progress.Progress
import java.io.File

class FileEndpoint @AssistedInject constructor(
        @Assisted private val progressClient: Progress.Client?
) : Endpoint {
    val basePath = File("/storage/emulated/0")
    override fun restore(backup: Backup): Boolean {
        TODO("not implemented")
    }

    override fun backup(spec: BackupSpec): Backup {
        TODO("not implemented")
    }

    @AssistedInject.Factory
    interface Factory : Endpoint.Factory<FileEndpoint>
}