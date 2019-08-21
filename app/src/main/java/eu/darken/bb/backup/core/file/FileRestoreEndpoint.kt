package eu.darken.bb.backup.core.file

import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.Restore
import eu.darken.bb.common.progress.Progress
import java.io.File

class FileRestoreEndpoint @AssistedInject constructor(
        @Assisted private val progressClient: Progress.Client?
) : Restore.Endpoint {
    val basePath = File("/storage/emulated/0")

    override fun restore(config: Restore.Config, backup: Backup.Unit): Boolean {
        TODO("not implemented")
    }

    @AssistedInject.Factory
    interface Factory : Restore.Endpoint.Factory<FileRestoreEndpoint>
}