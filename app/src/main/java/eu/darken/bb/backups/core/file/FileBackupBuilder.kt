package eu.darken.bb.backups.core.file

import eu.darken.bb.backups.core.Backup
import eu.darken.bb.backups.core.BackupId
import eu.darken.bb.backups.core.BaseBackupBuilder

class FileBackupBuilder : BaseBackupBuilder<FileBackupSpec> {

    constructor(backup: Backup) : super(backup)

    constructor(config: FileBackupSpec, backupId: BackupId) : super(config, backupId)

    var backupName: String
        get() = backupConfig.label
        set(value) {
            backupConfig = backupConfig.copy(name = value)
        }

}

