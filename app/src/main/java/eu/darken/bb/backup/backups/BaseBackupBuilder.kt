package eu.darken.bb.backup.backups

import eu.darken.bb.backup.processor.tmp.TmpRef

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
            backupType = Backup.Type.APP_BACKUP,
            data = data.toMap()
    )
}
