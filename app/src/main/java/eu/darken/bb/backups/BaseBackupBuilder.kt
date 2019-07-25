package eu.darken.bb.backups

import eu.darken.bb.processor.tmp.TmpRef

open class BaseBackupBuilder<ConfigT : BackupConfig> {
    val data = mutableMapOf<String, MutableCollection<TmpRef>>()
    val backupId: BackupId
    var backupConfig: ConfigT

    constructor(backup: Backup) {
        this.backupId = backup.id
        this.backupConfig = backup.config as ConfigT
    }

    constructor(config: ConfigT, backupId: BackupId) {
        this.backupId = backupId
        this.backupConfig = config
    }

    fun toBackup(): Backup = Backup(
            id = backupId,
            config = backupConfig,
            backupType = Backup.Type.APP,
            data = data.toMap()
    )
}
