package eu.darken.bb.backup.core

import eu.darken.bb.processor.core.mm.MMRef

abstract class BaseBackupBuilder<ConfigT : BackupSpec> {
    val data = mutableMapOf<String, MutableCollection<MMRef>>()
    val backupConfig: ConfigT
    lateinit var metaData: Backup.MetaData
    val backupId: Backup.Id

    constructor(backup: Backup.Unit) {
        this.backupConfig = backup.spec as ConfigT
        this.backupId = backup.id
        this.metaData = backup.metaData
        backup.data.forEach {
            data[it.key] = it.value.toMutableList()
        }
    }

    constructor(backupId: Backup.Id, config: ConfigT) {
        this.backupConfig = config
        this.backupId = backupId
    }

    fun toBackup(): Backup.Unit = Backup.Unit(
            spec = backupConfig,
            id = backupId,
            metaData = metaData,
            data = data.toMap()
    )
}
