package eu.darken.bb.storage.core.local

import eu.darken.bb.backup.core.BackupSpec
import eu.darken.bb.common.file.SFile
import eu.darken.bb.storage.core.SimpleVersioning
import eu.darken.bb.storage.core.Storage

data class LocalStorageContent(
        val path: SFile,
        override val storageId: Storage.Id,
        override val backupSpec: BackupSpec,
        override val versioning: SimpleVersioning
) : Storage.Content