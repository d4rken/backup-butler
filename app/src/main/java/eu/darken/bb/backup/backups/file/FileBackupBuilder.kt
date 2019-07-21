package eu.darken.bb.backup.backups.file

import eu.darken.bb.backup.backups.Backup
import eu.darken.bb.backup.backups.BackupId
import eu.darken.bb.backup.backups.BaseBackupBuilder

class FileBackupBuilder : BaseBackupBuilder<FileBackupConfig> {

    constructor(backup: Backup) : super(backup)

    constructor(config: FileBackupConfig, backupId: BackupId) : super(config, backupId)

    var backupName: String
        get() = backupConfig.backupName
        set(value) {
            backupConfig = backupConfig.copy(name = value)
        }

}

