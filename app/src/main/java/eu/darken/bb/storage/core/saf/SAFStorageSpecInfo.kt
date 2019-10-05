package eu.darken.bb.storage.core.saf

import eu.darken.bb.backup.core.BackupSpec
import eu.darken.bb.common.file.SAFPath
import eu.darken.bb.storage.core.Storage
import eu.darken.bb.storage.core.Versioning

data class SAFStorageSpecInfo(
        val path: SAFPath,
        override val storageId: Storage.Id,
        override val backupSpec: BackupSpec,
        override val versioning: Versioning
) : BackupSpec.Info