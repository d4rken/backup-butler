package eu.darken.bb.backups.file

import eu.darken.bb.backups.Backup
import eu.darken.bb.backups.BackupId
import eu.darken.bb.backups.BaseBackupBuilder

class FileBackupBuilder : BaseBackupBuilder<FileBackupConfig> {

    constructor(backup: Backup) : super(backup)

    constructor(config: FileBackupConfig, backupId: BackupId) : super(config, backupId)

    var backupName: String
        get() = backupConfig.label
        set(value) {
            backupConfig = backupConfig.copy(name = value)
        }

}

