package eu.darken.bb.storage.core.local

import eu.darken.bb.backup.core.BackupSpec
import eu.darken.bb.common.file.APath
import eu.darken.bb.storage.core.Storage
import eu.darken.bb.storage.core.Versioning

data class LocalStorageItem(
        val path: APath,
        override val storageId: Storage.Id,
        override val backupSpec: BackupSpec,
        override val versioning: Versioning
) : Storage.Item