package eu.darken.bb.backup.core

import eu.darken.bb.processor.core.mm.MMRef

abstract class BaseBackupWrapper<ConfigT : BackupSpec> {
    val backupId: Backup.Id
    val backupConfig: ConfigT
    val data = mutableMapOf<String, Collection<MMRef>>()

    constructor(backup: Backup.Unit) {
        this.backupId = backup.backupId
        @Suppress("UNCHECKED_CAST")
        this.backupConfig = backup.spec as ConfigT
        backup.data.forEach {
            data[it.key] = it.value.toMutableList()
        }
    }

    constructor(backupId: Backup.Id, config: ConfigT) {
        this.backupId = backupId
        this.backupConfig = config
    }

    abstract fun buildMeta(backupId: Backup.Id): Backup.MetaData

    fun createUnit(): Backup.Unit = Backup.Unit(
            spec = backupConfig,
            metaData = buildMeta(backupId),
            data = data.toMap()
    )
}
