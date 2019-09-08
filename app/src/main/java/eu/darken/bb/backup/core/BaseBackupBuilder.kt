package eu.darken.bb.backup.core

import eu.darken.bb.processor.core.mm.MMRef

open class BaseBackupBuilder<ConfigT : BackupSpec> {
    val data = mutableMapOf<String, MutableCollection<MMRef>>()
    var backupConfig: ConfigT
    val backupId: Backup.Id

    constructor(backup: Backup.Unit) {
        this.backupConfig = backup.spec as ConfigT
        this.backupId = backup.id
        backup.data.forEach {
            data[it.key] = it.value.toMutableList()
        }
    }

    constructor(config: ConfigT, backupId: Backup.Id) {
        this.backupConfig = config
        this.backupId = backupId
    }

    fun toBackup(): Backup.Unit = Backup.Unit(
            spec = backupConfig,
            id = backupId,
            data = data.toMap()
    )
}
