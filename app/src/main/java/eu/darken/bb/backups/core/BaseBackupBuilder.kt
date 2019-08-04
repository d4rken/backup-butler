package eu.darken.bb.backups.core

import eu.darken.bb.processor.tmp.TmpRef

open class BaseBackupBuilder<ConfigT : BackupSpec> {
    val data = mutableMapOf<String, MutableCollection<TmpRef>>()
    val backupId: BackupId
    var backupConfig: ConfigT

    constructor(backup: Backup) {
        this.backupId = backup.id
        this.backupConfig = backup.spec as ConfigT
    }

    constructor(config: ConfigT, backupId: BackupId) {
        this.backupId = backupId
        this.backupConfig = config
    }

    fun toBackup(): Backup = Backup(
            id = backupId,
            spec = backupConfig,
            backupType = Backup.Type.APP,
            data = data.toMap()
    )
}
