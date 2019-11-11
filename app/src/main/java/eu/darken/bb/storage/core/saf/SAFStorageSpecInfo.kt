package eu.darken.bb.storage.core.saf

import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.BackupSpec
import eu.darken.bb.common.file.core.saf.SAFPath
import eu.darken.bb.storage.core.Storage

data class SAFStorageSpecInfo(
        val path: SAFPath,
        override val storageId: Storage.Id,
        override val backupSpec: BackupSpec,
        override val backups: Collection<Backup.MetaData>
) : BackupSpec.Info