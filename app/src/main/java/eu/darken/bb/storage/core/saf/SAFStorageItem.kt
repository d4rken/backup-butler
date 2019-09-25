package eu.darken.bb.storage.core.saf

import eu.darken.bb.backup.core.BackupSpec
import eu.darken.bb.common.file.SAFFile
import eu.darken.bb.storage.core.Storage
import eu.darken.bb.storage.core.Versioning

data class SAFStorageItem(
        val path: SAFFile,
        override val storageId: Storage.Id,
        override val backupSpec: BackupSpec,
        override val versioning: Versioning
) : Storage.Item