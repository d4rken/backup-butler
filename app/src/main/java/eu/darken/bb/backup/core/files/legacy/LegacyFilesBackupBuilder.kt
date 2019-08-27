package eu.darken.bb.backup.core.files.legacy

import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.BaseBackupBuilder
import eu.darken.bb.processor.core.tmp.TmpRef

class LegacyFilesBackupBuilder : BaseBackupBuilder<LegacyFilesBackupSpec> {

    constructor(backup: Backup.Unit) : super(backup)

    constructor(config: LegacyFilesBackupSpec, backupId: Backup.Id)
            : super(config, backupId)

    init {
        data[""] = mutableListOf()
    }

    fun addBackupItem(item: TmpRef) {
        data[""]!!.add(item)
    }

}
