package eu.darken.bb.backup.core.files

import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.BaseBackupBuilder
import eu.darken.bb.processor.core.tmp.TmpRef

class FilesBackupBuilder : BaseBackupBuilder<FilesBackupSpec> {

    constructor(backup: Backup.Unit) : super(backup)

    constructor(config: FilesBackupSpec, backupId: Backup.Id)
            : super(config, backupId)

    init {
        data[""] = mutableListOf()
    }

    fun addBackupItem(item: TmpRef) {
        data[""]!!.add(item)
    }

}
