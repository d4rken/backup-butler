package eu.darken.bb.backup.core

import eu.darken.bb.processor.tmp.TmpRef

open class BaseBackupBuilder<ConfigT : BackupSpec> {
    val data = mutableMapOf<String, MutableCollection<TmpRef>>()
    var backupConfig: ConfigT
    val backupId: Backup.Id

    constructor(backup: Backup) {
        this.backupConfig = backup.spec as ConfigT
        this.backupId = backup.id
    }

    constructor(config: ConfigT, backupId: Backup.Id) {
        this.backupConfig = config
        this.backupId = backupId
    }

    fun toBackup(): Backup = Backup(
            spec = backupConfig,
            id = backupId,
            backupType = Backup.Type.APP,
            data = data.toMap()
    )
}
