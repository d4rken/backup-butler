package eu.darken.bb.storage.core.local

import eu.darken.bb.backup.core.BackupSpec
import eu.darken.bb.common.file.AFile
import eu.darken.bb.storage.core.Storage
import eu.darken.bb.storage.core.Versioning

data class LocalStorageItem(
        val path: AFile,
        override val storageId: Storage.Id,
        override val backupSpec: BackupSpec,
        override val versioning: Versioning
) : Storage.Item