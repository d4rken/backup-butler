package eu.darken.bb.storage.core.local

import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.BackupSpec
import eu.darken.bb.common.files.core.local.LocalPath
import eu.darken.bb.storage.core.Storage

data class LocalStorageSpecInfo(
    override val path: LocalPath,
    override val storageId: Storage.Id,
    override val backupSpec: BackupSpec,
    override val backups: Collection<Backup.MetaData>
) : BackupSpec.Info