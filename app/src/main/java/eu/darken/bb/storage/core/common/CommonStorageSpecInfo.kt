package eu.darken.bb.storage.core.common

import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.BackupSpec
import eu.darken.bb.common.files.core.APath
import eu.darken.bb.storage.core.Storage

data class CommonStorageSpecInfo(
    override val path: APath,
    override val storageId: Storage.Id,
    override val backupSpec: BackupSpec,
    override val backups: Collection<Backup.MetaData>
) : BackupSpec.Info