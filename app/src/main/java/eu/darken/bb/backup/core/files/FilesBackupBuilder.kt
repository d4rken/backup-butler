package eu.darken.bb.backup.core.files

import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.BaseBackupBuilder
import eu.darken.bb.processor.core.mm.MMRef

class FilesBackupBuilder : BaseBackupBuilder<FilesBackupSpec> {

    constructor(backup: Backup.Unit) : super(backup)

    constructor(config: FilesBackupSpec, backupId: Backup.Id)
            : super(config, backupId)

    val files: MutableCollection<MMRef> = data.getOrElse("", { mutableSetOf() })

}
