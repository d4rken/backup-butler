package eu.darken.bb.backup.core.file

import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.BaseBackupBuilder

class FileBackupBuilder : BaseBackupBuilder<FileBackupSpec> {

    constructor(backup: Backup) : super(backup)

    constructor(config: FileBackupSpec, backupId: Backup.Id) : super(config, backupId)

    var backupName: String
        get() = backupConfig.label
        set(value) {
            backupConfig = backupConfig.copy(name = value)
        }

}

